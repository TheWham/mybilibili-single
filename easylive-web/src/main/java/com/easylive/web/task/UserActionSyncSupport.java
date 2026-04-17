package com.easylive.web.task;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.CommentCountUpdateDTO;
import com.easylive.entity.dto.UserActionSyncDTO;
import com.easylive.entity.dto.UserCoinCountUpdateDTO;
import com.easylive.entity.dto.VideoCountUpdateDTO;
import com.easylive.entity.po.*;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.query.VideoCommentQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.mappers.*;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserActionSyncSupport {

    private static final int MAX_SYNC_COUNT = 200;
    private static final Logger log = LoggerFactory.getLogger(UserActionSyncSupport.class);

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserVideoActionMapper<UserVideoAction, UserActionQuery> userVideoActionMapper;
    @Resource
    private UserCommentActionMapper<UserCommentAction, UserActionQuery> userCommentActionMapper;
    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Transactional(rollbackFor = Exception.class)
    public void syncVideoActionQueue(String queueKey) {
        List<UserActionSyncDTO> queueList = drainQueue(queueKey);
        if (queueList.isEmpty()) {
            return;
        }

        List<UserVideoAction> saveList = new ArrayList<>(queueList.size());
        Map<String, Integer> videoCountDeltaMap = new HashMap<>();
        int deleteCount = 0;
        for (UserActionSyncDTO actionSyncDTO : queueList) {
            UserVideoAction userVideoAction = BeanUtil.toBean(actionSyncDTO, UserVideoAction.class);
            userVideoAction.setActionCount(Math.abs(actionSyncDTO.getActionCount()));
            if (Boolean.TRUE.equals(actionSyncDTO.getActive())) {
                saveList.add(userVideoAction);
            } else {
                userVideoActionMapper.deleteByVideoIdAndActionTypeAndUserId(
                        userVideoAction.getVideoId(),
                        userVideoAction.getActionType(),
                        userVideoAction.getUserId()
                );
                deleteCount++;
            }
            mergeVideoCount(videoCountDeltaMap, userVideoAction.getVideoId(), actionSyncDTO.getActionCount());
        }

        if (!saveList.isEmpty()) {
            userVideoActionMapper.insertOrUpdateBatch(saveList);
        }
        String field = resolveVideoCountField(queueKey);
        flushVideoInfoCount(videoCountDeltaMap, field);
        int syncCount = saveList.size() + deleteCount;
        if (syncCount > 0) {
            log.info("syncVideoActionQueue finished, queueKey={}, count={}, saveCount={}, deleteCount={}", queueKey, syncCount, saveList.size(), deleteCount);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncCoinQueue() {
        List<UserActionSyncDTO> queueList = drainQueue(Constants.REDIS_WEB_ACTION_VIDEO_COIN_QUEUE_KEY);
        if (queueList.isEmpty()) {
            return;
        }

        List<UserVideoAction> saveList = new ArrayList<>(queueList.size());
        Map<String, Integer> videoCountDeltaMap = new HashMap<>();
        Map<String, Integer> userCurrentCoinDeltaMap = new HashMap<>();
        Map<String, Integer> userTotalCoinDeltaMap = new HashMap<>();
        for (UserActionSyncDTO actionSyncDTO : queueList) {
            UserVideoAction userVideoAction = BeanUtil.toBean(actionSyncDTO, UserVideoAction.class);
            saveList.add(userVideoAction);
            mergeVideoCount(videoCountDeltaMap, userVideoAction.getVideoId(), actionSyncDTO.getActionCount());
            mergeUserCoinCount(userCurrentCoinDeltaMap, userVideoAction.getUserId(), -actionSyncDTO.getActionCount());
            mergeUserCoinCount(userCurrentCoinDeltaMap, userVideoAction.getVideoUserId(), actionSyncDTO.getActionCount());
            mergeUserCoinCount(userTotalCoinDeltaMap, userVideoAction.getVideoUserId(), actionSyncDTO.getActionCount());
        }

        if (!saveList.isEmpty()) {
            userVideoActionMapper.insertOrUpdateBatch(saveList);
            flushVideoInfoCount(videoCountDeltaMap, UserActionTypeEnum.VIDEO_COIN.getField());
            flushUserCoinCount(userCurrentCoinDeltaMap, userTotalCoinDeltaMap);
            log.info("syncCoinQueue finished, count={}", saveList.size());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncCommentQueue() {
        List<UserActionSyncDTO> queueList = drainQueue(Constants.REDIS_WEB_ACTION_VIDEO_COMMENT_QUEUE_KEY);
        if (queueList.isEmpty()) {
            return;
        }

        List<UserCommentAction> saveList = new ArrayList<>(queueList.size());
        Map<Integer, Integer> likeDiffMap = new HashMap<>();
        Map<Integer, Integer> hateDiffMap = new HashMap<>();
        int deleteCount = 0;
        for (UserActionSyncDTO actionSyncDTO : queueList) {
            if (Boolean.TRUE.equals(actionSyncDTO.getActive())) {
                UserCommentAction userCommentAction = BeanUtil.toBean(actionSyncDTO, UserCommentAction.class);
                saveList.add(userCommentAction);
            } else {
                userCommentActionMapper.deleteByCommentIdAndUserId(actionSyncDTO.getCommentId(), actionSyncDTO.getUserId());
                deleteCount++;
            }
            mergeCommentCount(likeDiffMap, actionSyncDTO.getCommentId(), actionSyncDTO.getLikeDiff());
            mergeCommentCount(hateDiffMap, actionSyncDTO.getCommentId(), actionSyncDTO.getHateDiff());
        }
        if (!saveList.isEmpty()) {
            userCommentActionMapper.insertOrUpdateBatch(saveList);
        }
        flushCommentCount(likeDiffMap, hateDiffMap);
        int syncCount = saveList.size() + deleteCount;
        if (syncCount > 0) {
            log.info("syncCommentQueue finished, count={}, saveCount={}, deleteCount={}", syncCount, saveList.size(), deleteCount);
        }
    }

    private List<UserActionSyncDTO> drainQueue(String queueKey) {
        List<UserActionSyncDTO> queueList = new ArrayList<>();
        for (int i = 0; i < MAX_SYNC_COUNT; i++) {
            UserActionSyncDTO actionSyncDTO = redisComponent.getNextUserActionQueue(queueKey);
            if (actionSyncDTO == null) {
                break;
            }
            queueList.add(actionSyncDTO);
        }
        return queueList;
    }

    private void mergeVideoCount(Map<String, Integer> countMap, String videoId, Integer delta) {
        if (videoId == null || delta == null || delta == 0) {
            return;
        }
        countMap.merge(videoId, delta, Integer::sum);
    }

    private void mergeCommentCount(Map<Integer, Integer> countMap, Integer commentId, Integer delta) {
        if (commentId == null || delta == null || delta == 0) {
            return;
        }
        countMap.merge(commentId, delta, Integer::sum);
    }

    private void mergeUserCoinCount(Map<String, Integer> countMap, String userId, Integer delta) {
        if (userId == null || delta == null || delta == 0) {
            return;
        }
        countMap.merge(userId, delta, Integer::sum);
    }

    private void flushVideoInfoCount(Map<String, Integer> countMap, String field) {
        List<VideoCountUpdateDTO> updateList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }
            updateList.add(new VideoCountUpdateDTO(entry.getKey(), entry.getValue()));
        }
        if (!updateList.isEmpty()) {
            videoInfoMapper.updateCountBatch(field, updateList);
            // 这份 delta 只表示“还没来得及刷进 MySQL 的增量”。
            // 批量更新数据库成功后，把对应增量从 Redis 里扣掉，后续读取详情页时就不会重复叠加。
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() == 0) {
                    continue;
                }
                redisComponent.addVideoActionCountDelta(entry.getKey(), field, -entry.getValue());
            }
        }
    }

    private void flushCommentCount(Map<Integer, Integer> likeDiffMap, Map<Integer, Integer> hateDiffMap) {
        List<CommentCountUpdateDTO> updateList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : likeDiffMap.entrySet()) {
            Integer commentId = entry.getKey();
            Integer likeDiff = entry.getValue();
            Integer hateDiff = hateDiffMap.getOrDefault(commentId, 0);
            updateList.add(new CommentCountUpdateDTO(commentId, likeDiff, hateDiff));
        }
        for (Map.Entry<Integer, Integer> entry : hateDiffMap.entrySet()) {
            Integer commentId = entry.getKey();
            if (likeDiffMap.containsKey(commentId)) {
                continue;
            }
            updateList.add(new CommentCountUpdateDTO(commentId, 0, entry.getValue()));
        }
        if (!updateList.isEmpty()) {
            videoCommentMapper.updateCountBatch(updateList);
        }
    }

    private void flushUserCoinCount(Map<String, Integer> currentCoinMap, Map<String, Integer> totalCoinMap) {
        List<UserCoinCountUpdateDTO> updateList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : currentCoinMap.entrySet()) {
            String userId = entry.getKey();
            Integer currentCoinCount = entry.getValue();
            Integer totalCoinCount = totalCoinMap.getOrDefault(userId, 0);
            if (currentCoinCount == 0 && totalCoinCount == 0) {
                continue;
            }
            updateList.add(new UserCoinCountUpdateDTO(userId, totalCoinCount, currentCoinCount));
        }
        for (Map.Entry<String, Integer> entry : totalCoinMap.entrySet()) {
            String userId = entry.getKey();
            if (currentCoinMap.containsKey(userId) && currentCoinMap.get(userId) != 0) {
                continue;
            }
            if (entry.getValue() == 0) {
                continue;
            }
            updateList.add(new UserCoinCountUpdateDTO(userId, entry.getValue(), 0));
        }
        if (!updateList.isEmpty()) {
            userInfoMapper.updateCountBatch(updateList);
        }
    }


    private String resolveVideoCountField(String queueKey) {
        if (Constants.REDIS_WEB_ACTION_VIDEO_LIKE_QUEUE_KEY.equals(queueKey)) {
            return UserActionTypeEnum.VIDEO_LIKE.getField();
        }
        if (Constants.REDIS_WEB_ACTION_VIDEO_COLLECT_QUEUE_KEY.equals(queueKey)) {
            return UserActionTypeEnum.VIDEO_COLLECT.getField();
        }
        throw new IllegalStateException("Unknown queueKey: " + queueKey);
    }
}
