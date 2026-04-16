package com.easylive.web.task;

import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.VideoCountUpdateDTO;
import com.easylive.entity.dto.VideoHistoryDeleteDTO;
import com.easylive.entity.dto.VideoPlayDTO;
import com.easylive.entity.po.VideoPlayHistory;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.service.VideoInfoService;
import com.easylive.service.VideoPlayHistoryService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class VideoPlayCountAndHistorySyncTask {
    private static final int MAX_DELETE_COUNT = 200;

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    @Transactional(rollbackFor = Exception.class)
    public void syncStatsVideoPlayCount() {
        Map<String, Integer> deltaMap = redisComponent.getAllVideoPlayCountDelta();
        if (deltaMap == null || deltaMap.isEmpty()) {
            return;
        }

        List<VideoCountUpdateDTO> list = new ArrayList<>(deltaMap.size());
        for (Map.Entry<String, Integer> entry : deltaMap.entrySet()) {
            String videoId = entry.getKey();
            Integer delta = entry.getValue();
            if (delta == null || delta <= 0) {
                continue;
            }
            list.add(new VideoCountUpdateDTO(videoId, delta));
        }
        if (!list.isEmpty()) {
            videoInfoService.updateCountBatch(UserActionTypeEnum.VIDEO_PLAY.getField(), list);
            List<String> syncedVideoIds = list.stream().map(VideoCountUpdateDTO::getVideoId).collect(Collectors.toList());
            redisComponent.clearVideoPlayCountDelta(syncedVideoIds);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncStatsVideoHistory() {
        Set<String> userIds = redisComponent.getDirtyHistoryUsers();
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        for (String userId : userIds) {
            List<VideoPlayHistory> videoPlayHistories = buildUserHistoryList(userId);
            // 主同步链路只负责新增和更新，不在这里做旧数据删除。
            if (!videoPlayHistories.isEmpty()) {
                videoPlayHistoryService.addOrUpdateBatch(videoPlayHistories);
            }
            redisComponent.clearDirtyHistoryUser(userId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncDeletedVideoHistory() {
        List<VideoHistoryDeleteDTO> deleteDTOList = new ArrayList<>(MAX_DELETE_COUNT);
        for (int i = 0; i < MAX_DELETE_COUNT; i++) {
            VideoHistoryDeleteDTO deleteDTO = redisComponent.getNextVideoHistoryDeleteQueue();
            if (deleteDTO == null) {
                break;
            }
            deleteDTOList.add(deleteDTO);
        }
        if (deleteDTOList.isEmpty()) {
            return;
        }

        // 删除队列里放的都是 Redis 已经裁剪掉的视频，按 userId + videoId 精确删库。
        for (VideoHistoryDeleteDTO deleteDTO : deleteDTOList) {
            videoPlayHistoryService.deleteVideoPlayHistoryByUserIdAndVideoId(deleteDTO.getUserId(), deleteDTO.getVideoId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredVideoHistory() {
        // 1000 条上限走删除队列，90 天过期走日清理。
        Date expireTime = new Date(System.currentTimeMillis() - (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_90);
        videoPlayHistoryService.deleteVideoPlayHistoryBefore(expireTime);
    }

    private List<VideoPlayHistory> buildUserHistoryList(String userId) {
        Set<ZSetOperations.TypedTuple<String>> historyTupleSet = redisComponent.getVideoHistoryWithScores(userId);
        if (historyTupleSet == null || historyTupleSet.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Integer> fileIndexMap = redisComponent.getVideoHistoryFileIndexMap(userId);

        List<VideoPlayDTO> videoPlayDTOList = new ArrayList<>(historyTupleSet.size());
        for (ZSetOperations.TypedTuple<String> historyTuple : historyTupleSet) {
            if (historyTuple == null || historyTuple.getValue() == null) {
                continue;
            }
            // 现在 Redis 的历史结构已经固定成：
            // 1. ZSet 只存 videoId 和最后观看时间
            // 2. Hash 单独存 videoId 对应的 fileIndex
            // 旧结构不再兼容，发现异常数据时直接清理 Redis 更合适。
            String videoId = historyTuple.getValue();
            VideoPlayDTO videoPlayDTO = new VideoPlayDTO();
            videoPlayDTO.setUserId(userId);
            videoPlayDTO.setVideoId(videoId);
            videoPlayDTO.setFileIndex(fileIndexMap.getOrDefault(videoId, Constants.ONE));
            // ZSet 的 score 存的就是最后一次观看时间戳，这里直接还原成数据库字段。
            if (historyTuple.getScore() != null) {
                videoPlayDTO.setLastUpdateTime(new Date(historyTuple.getScore().longValue()));
            }
            videoPlayDTOList.add(videoPlayDTO);
        }
        return BeanUtil.copyToList(videoPlayDTOList, VideoPlayHistory.class);
    }
}
