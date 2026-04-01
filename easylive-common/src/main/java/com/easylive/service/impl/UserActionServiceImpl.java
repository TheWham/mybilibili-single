package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.UserActionSyncDTO;
import com.easylive.entity.po.*;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.UserCommentActionMapper;
import com.easylive.mappers.UserInfoMapper;
import com.easylive.mappers.VideoInfoMapper;
import com.easylive.service.UserActionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service("UserActionService")
public class UserActionServiceImpl implements UserActionService {

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private UserCommentActionMapper<UserCommentAction, UserActionQuery> userCommentActionMapper;
    @Resource
    private RedisComponent redisComponent;

    /**
     * 用户点击点赞
     *    ↓
     * 请求到服务
     *    ↓
     * Redis记录点赞状态
     *    ↓
     * 立即返回成功
     *    ↓
     * MQ发送消息
     *    ↓
     * 异步写MySQL
     * 更新之后解决高并发版本 没有加入redis mq 只是单纯用mysql抗压
     * 从之前先select在更新变成先insert判断解决数据不一致问题
     *
     */

    @Override
    public void doAction(UserAction userAction)
    {

        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(userAction.getVideoId());
        UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getEnum(userAction.getActionType());

        if (videoInfo == null || actionTypeEnum == null)
            throw new BusinessException(ResponseCodeEnum.CODE_600);

        userAction.setVideoUserId(videoInfo.getUserId());
        userAction.setActionTime(new Date());
        //TODO 引入redis存储点赞数量异步放到MQ处理消息

        switch (actionTypeEnum) {
            case VIDEO_LIKE:
            case VIDEO_COLLECT:
                handleToggleAction(userAction, actionTypeEnum);
                break;

            case VIDEO_COIN:
                handleCoinAction(userAction, actionTypeEnum);
                break;

            case COMMENT_LIKE:
            case COMMENT_HATE:
                handleComment(userAction, actionTypeEnum);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + actionTypeEnum);
        }

    }

    private void handleToggleAction(UserAction userAction, UserActionTypeEnum userActionTypeEnum)
    {
        int actionCount = normalizeActionCount(userAction.getActionCount());
        boolean isActive = !redisComponent.hasVideoActionStatus(userAction.getUserId(), userAction.getVideoId(), userAction.getActionType());

        if (isActive) {
            redisComponent.saveVideoActionStatus(userAction.getUserId(), userAction.getVideoId(), userAction.getActionType(), actionCount);
        } else {
            redisComponent.removeVideoActionStatus(userAction.getUserId(), userAction.getVideoId(), userAction.getActionType());
        }

        int delta = isActive ? actionCount : -actionCount;
        String statsField = userActionTypeEnum.equals(UserActionTypeEnum.VIDEO_LIKE)
                ? UserStatsRedisEnum.VIDEO_LIKE.getField()
                : UserStatsRedisEnum.USER_COLLECT_COUNT.getField();
        redisComponent.incrementUserStats(userAction.getVideoUserId(), statsField, delta);
        redisComponent.incrementUserStatsByDay(
                userAction.getVideoUserId(),
                buildStatsDay(),
                statsField,
                delta
        );

        UserActionSyncDTO actionSyncDTO = buildBaseSyncDTO(userAction);
        actionSyncDTO.setActive(isActive);
        actionSyncDTO.setActionCount(delta);
        redisComponent.addUserActionQueue(getQueueKey(userActionTypeEnum), actionSyncDTO);
    }

    /**
     * 尽量避免先查询操作, 涉及到扣减问题一定要用原子更新 或者 加锁避免并发问题
     */
    private void handleCoinAction(UserAction userAction, UserActionTypeEnum userActionTypeEnum)
    {
        UserVideoAction userVideoAction = BeanUtil.toBean(userAction, UserVideoAction.class);
        String userId = userVideoAction.getUserId();
        String videoUserId = userVideoAction.getVideoUserId();

        if (userId.equals(videoUserId))
            throw new BusinessException("不能给自己投币");

        if (redisComponent.hasVideoActionStatus(userId, userVideoAction.getVideoId(), userVideoAction.getActionType())) {
            throw new BusinessException("已经投过币了");
        }

        int actionCount = normalizeActionCount(userVideoAction.getActionCount());
        ensureCurrentCoinLoaded(userId);
        ensureCurrentCoinLoaded(videoUserId);
        Long remainCoin = redisComponent.incrementUserStats(userId, UserStatsRedisEnum.USER_COIN.getField(), -actionCount);
        if (remainCoin < 0) {
            redisComponent.incrementUserStats(userId, UserStatsRedisEnum.USER_COIN.getField(), actionCount);
            throw new BusinessException("投币失败,硬币不足");
        }

        redisComponent.incrementUserStats(videoUserId, UserStatsRedisEnum.USER_COIN.getField(), actionCount);
        redisComponent.incrementUserStats(videoUserId, UserStatsRedisEnum.VIDEO_COIN.getField(), actionCount);
        redisComponent.incrementUserStatsByDay(userId, buildStatsDay(), UserStatsRedisEnum.USER_COIN.getField(), -actionCount);
        redisComponent.incrementUserStatsByDay(videoUserId, buildStatsDay(), UserStatsRedisEnum.USER_COIN.getField(), actionCount);
        redisComponent.incrementUserStatsByDay(videoUserId, buildStatsDay(), UserStatsRedisEnum.VIDEO_COIN.getField(), actionCount);
        redisComponent.saveVideoActionStatus(userId, userVideoAction.getVideoId(), userVideoAction.getActionType(), actionCount);

        UserActionSyncDTO actionSyncDTO = buildBaseSyncDTO(userAction);
        actionSyncDTO.setActive(true);
        actionSyncDTO.setActionCount(actionCount);
        redisComponent.addUserActionQueue(getQueueKey(userActionTypeEnum), actionSyncDTO);
    }

    /**
     * 需要引入redis不能每次操作全盘扫描, 从redis 用lua脚本计算好之后 用定时任务更新到mysql中 一天只需更新一次剩下的全部从redis中去
     * 还要注意不能把每个视频都存入redis中, 不然会让内存爆炸, 一般只需存储热门视频的操作数, 冷门的视频从mysql中去然后同步到redis中
     */
    private void handleComment(UserAction userAction, UserActionTypeEnum typeEnum)
    {
        Integer commentId = userAction.getCommentId();

        if (commentId == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String userId = userAction.getUserId();
        Integer actionCount = normalizeActionCount(userAction.getActionCount());
        Integer oldActionType = redisComponent.getCommentActionStatus(userId, commentId);
        if (oldActionType == null) {
            UserCommentAction oldAction = userCommentActionMapper.selectByCommentIdAndUserId(commentId, userId);
            oldActionType = oldAction == null ? null : oldAction.getActionType();
            if (oldActionType != null) {
                redisComponent.saveCommentActionStatus(userId, commentId, oldActionType);
            }
        }

        Integer likeDiff = 0, hateDiff = 0;
        boolean isCancel = false;
        if (oldActionType != null && oldActionType.equals(typeEnum.getType()))
        {
            isCancel = true;
            if (typeEnum.equals(UserActionTypeEnum.COMMENT_LIKE)) {
                likeDiff = -actionCount;
            } else {
                hateDiff = -actionCount;
            }
        }else if (oldActionType != null)
        {
            //oldType != newType
            if (typeEnum.equals(UserActionTypeEnum.COMMENT_LIKE))
            {
                likeDiff = actionCount;
                hateDiff = -actionCount;
            }else {
                likeDiff = -actionCount;
                hateDiff = actionCount;
            }
        }else {
            //oldActionType == null
            if (typeEnum.equals(UserActionTypeEnum.COMMENT_LIKE)) {
                likeDiff = actionCount;
            } else {
                hateDiff = actionCount;
            }
        }

        if (isCancel){
            redisComponent.removeCommentActionStatus(userId, commentId);
        }else {
            redisComponent.saveCommentActionStatus(userId, commentId, typeEnum.getType());
        }

        UserActionSyncDTO actionSyncDTO = buildBaseSyncDTO(userAction);
        actionSyncDTO.setActive(!isCancel);
        actionSyncDTO.setActionCount(actionCount);
        actionSyncDTO.setLikeDiff(likeDiff);
        actionSyncDTO.setHateDiff(hateDiff);
        redisComponent.addUserActionQueue(Constants.REDIS_WEB_ACTION_VIDEO_COMMENT_QUEUE_KEY, actionSyncDTO);
    }

    private int normalizeActionCount(Integer actionCount) {
        if (actionCount == null || actionCount <= 0) {
            return Constants.ONE;
        }
        return Math.min(actionCount, Constants.TWO);
    }

    private void ensureCurrentCoinLoaded(String userId) {
        Integer currentCoin = redisComponent.getUserStatsValue(userId, UserStatsRedisEnum.USER_COIN.getField());
        if (currentCoin != null) {
            return;
        }
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        redisComponent.setUserStatsValue(userId, UserStatsRedisEnum.USER_COIN.getField(), userInfo.getCurrentCoinCount());
    }

    private UserActionSyncDTO buildBaseSyncDTO(UserAction userAction) {
        UserActionSyncDTO actionSyncDTO = new UserActionSyncDTO();
        actionSyncDTO.setUserId(userAction.getUserId());
        actionSyncDTO.setVideoId(userAction.getVideoId());
        actionSyncDTO.setVideoUserId(userAction.getVideoUserId());
        actionSyncDTO.setCommentId(userAction.getCommentId());
        actionSyncDTO.setActionType(userAction.getActionType());
        actionSyncDTO.setActionTime(userAction.getActionTime());
        actionSyncDTO.setStatsDay(buildStatsDay());
        return actionSyncDTO;
    }

    private String buildStatsDay() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String getQueueKey(UserActionTypeEnum actionTypeEnum) {
        return switch (actionTypeEnum) {
            case VIDEO_LIKE -> Constants.REDIS_WEB_ACTION_VIDEO_LIKE_QUEUE_KEY;
            case VIDEO_COLLECT -> Constants.REDIS_WEB_ACTION_VIDEO_COLLECT_QUEUE_KEY;
            case VIDEO_COIN -> Constants.REDIS_WEB_ACTION_VIDEO_COIN_QUEUE_KEY;
            case COMMENT_LIKE, COMMENT_HATE -> Constants.REDIS_WEB_ACTION_VIDEO_COMMENT_QUEUE_KEY;
            default -> throw new IllegalStateException("Unexpected value: " + actionTypeEnum);
        };
    }

}
