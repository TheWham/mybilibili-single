package com.easylive.web.task;

import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VideoPlaySyncTask {
    @Resource
    private VideoPlayCountAndHistorySyncTask videoPlayCountAndHistorySyncTask;

    @Scheduled(fixedDelay = 20000)
    public void syncVideoPlayQueue() {
        videoPlayCountAndHistorySyncTask.syncStatsVideoPlayCount();
        videoPlayCountAndHistorySyncTask.syncStatsVideoHistory();
        videoPlayCountAndHistorySyncTask.syncDeletedVideoHistory();
    }
}
