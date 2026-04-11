package com.easylive.web.task;

import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.VideoCountUpdateDTO;
import com.easylive.entity.dto.VideoPlayDTO;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VideoPlayCountAndHistorySyncTask {

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private VideoInfoService videoInfoService;

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
        List<VideoPlayDTO> videoHistory = getVideoHistory();
        if (videoHistory == null || videoHistory.isEmpty()) {
            return;
        }
        Map<String, List<VideoPlayDTO>> map = videoHistory.stream().collect(Collectors.groupingBy(VideoPlayDTO::getUserId));
        //TODO 根据每个用户批量落库
    }

    private List<VideoPlayDTO> getVideoHistory() {
        Set<String> userIds = redisComponent.getDirtyHistoryUsers();
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }
        List<VideoPlayDTO> videoPlayDTOS = new ArrayList<>();
        for (String userId : userIds) {
            List<String> videoInfoList = redisComponent.getVideoHistoryList(userId);
            if (videoInfoList == null || videoInfoList.isEmpty()) {
                continue;
            }
            for (String videoInfo : videoInfoList) {
                String[] ans = videoInfo.split(":");
                if (ans.length < 2) {
                    continue;
                }
                String videoId = ans[0];
                Integer fileIndex = Integer.parseInt(ans[1]);
                VideoPlayDTO videoPlayDTO = new VideoPlayDTO();
                videoPlayDTO.setFileIndex(fileIndex);
                videoPlayDTO.setVideoId(videoId);
                videoPlayDTO.setUserId(userId);
                videoPlayDTOS.add(videoPlayDTO);
            }
        }
        return videoPlayDTOS;
    }
}
