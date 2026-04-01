package com.easylive.web.task;

import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VideoCoinSyncTask {

    @Resource
    private UserActionSyncSupport userActionSyncSupport;

    @Scheduled(fixedDelay = 10000)
    public void syncVideoCoinQueue() {
        userActionSyncSupport.syncCoinQueue();
    }
}
