package com.easylive.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.event.UserStatsChangeEvent;
import com.easylive.entity.po.*;
import com.easylive.entity.query.*;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.exception.BusinessException;
import com.easylive.mappers.*;
import com.easylive.service.UserActionService;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("UserActionService")
public class UserActionServiceImpl implements UserActionService {

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;
    @Resource
    private UserVideoActionMapper<UserVideoAction, UserActionQuery> userVideoActionMapper;
    @Resource
    private UserCommentActionMapper<UserCommentAction, UserActionQuery> userCommentActionMapper;
    @Resource
    private UserStatsMapper<UserStats, UserStatsQuery> userStatsMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private ApplicationEventPublisher eventPublisher;

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
    @Transactional(rollbackFor = Exception.class)
    public void doAction(UserAction userAction)
    {
        VideoInfo videoInfo = videoInfoMapper.selectByVideoId(userAction.getVideoId());
        UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getEnum(userAction.getActionType());

        if (videoInfo == null || actionTypeEnum == null)
            throw new BusinessException(ResponseCodeEnum.CODE_600);

        userAction.setVideoUserId(videoInfo.getUserId());
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
        UserVideoAction userVideoAction = BeanUtil.toBean(userAction, UserVideoAction.class);
        // 因为已经加入了unique字段索引 所以插入成功表示原先不存在
        Integer insertRows = userVideoActionMapper.insertIgnore(userVideoAction);
        boolean liked = insertRows > 0;

        if (!liked) {
            //插入失败需要进行删除
            this.userVideoActionMapper.deleteByVideoIdAndActionTypeAndUserId(
                    userVideoAction.getVideoId(),
                    userVideoAction.getActionType(),
                    userVideoAction.getUserId()
            );
        }

        int count = liked ? userAction.getActionCount(): -userAction.getActionCount();
        videoInfoMapper.updateCount(userAction.getVideoId(), userActionTypeEnum.getField(), count);

        if (userActionTypeEnum.equals(UserActionTypeEnum.VIDEO_LIKE) || userActionTypeEnum.equals(UserActionTypeEnum.VIDEO_PLAY)){
            /**
             * 可以优化进入定时任务
             */
            userStatsMapper.insertOrUpdateCount(userAction.getUserId(), userActionTypeEnum.getField(), count);
            eventPublisher.publishEvent(new UserStatsChangeEvent(this, userAction.getUserId(), null,count, UserStatsRedisEnum.VIDEO_LIKE));
        }


        if (userActionTypeEnum.equals(UserActionTypeEnum.VIDEO_COLLECT))
        {
            //TODO 将收藏数量更新到es
        }
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

        // 因为已经加入了unique字段索引 所以插入成功表示原先不存在
        Integer insertRows = userVideoActionMapper.insertIgnore(userVideoAction);
        boolean hasNoCoinEver = insertRows > 0;

        if (!hasNoCoinEver)
            throw new BusinessException("已经投过币了");;

        //扣除投币用户硬币
        Integer updateRows = userInfoMapper.updateUserCoin(userId, -userVideoAction.getActionCount());
        boolean isDecreaseSuccess = updateRows > 0;

        if (!isDecreaseSuccess)
        {
            throw new BusinessException("投币失败,硬币不足");
        }

        /**
         * 后续要进行优化成高并发模式
         */
        //增加发布视频用户硬币
        userInfoMapper.updateUserCoin(videoUserId, userVideoAction.getActionCount());
        //增加视频硬币数量
        videoInfoMapper.updateCount(userVideoAction.getVideoId(), userActionTypeEnum.getField(), userVideoAction.getActionCount());
        //更新统计表中用户数量 可以优化异步处理
        userStatsMapper.insertOrUpdateCount(videoUserId, UserActionTypeEnum.USER_COIN.getField(), userVideoAction.getActionCount());
        userStatsMapper.insertOrUpdateCount(userId, UserActionTypeEnum.USER_COIN.getField(), -userVideoAction.getActionCount());
        //更新redis
        eventPublisher.publishEvent(new UserStatsChangeEvent(this, userId, videoUserId, userVideoAction.getActionCount(), UserStatsRedisEnum.USER_COIN));
    }

    /**
     * 需要引入redis不能每次操作全盘扫描, 从redis 用lua脚本计算好之后 用定时任务更新到mysql中 一天只需更新一次剩下的全部从redis中去
     * 还要注意不能把每个视频都存入redis中, 不然会让内存爆炸, 一般只需存储热门视频的操作数, 冷门的视频从mysql中去然后同步到redis中
     */
    private void handleComment(UserAction userAction, UserActionTypeEnum typeEnum)
    {
        UserCommentAction userCommentAction = BeanUtil.toBean(userAction, UserCommentAction.class);
        //检查评论是否存在
        Integer commentId = userAction.getCommentId();

        if (commentId == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String userId = userAction.getUserId();
        Integer actionCount = userAction.getActionCount();

        actionCount = actionCount > Constants.ONE ? Constants.ONE : actionCount;

        //先用悲观锁 解决并发问题
        Integer oldActionType = userCommentActionMapper.selectActionTypeForUpdate(commentId, userId);

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
            userCommentActionMapper.deleteByCommentIdAndUserId(commentId, userId);
        }else {
            userCommentActionMapper.insertOrUpdate(userCommentAction);
        }

        Integer rows = videoCommentMapper.updateCount(commentId, likeDiff, hateDiff);
        if (rows == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

}
