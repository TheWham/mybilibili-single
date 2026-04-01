package com.easylive.web.task;

import com.easylive.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VideoLikeSyncTask {

    @Resource
    private UserActionSyncSupport userActionSyncSupport;

    @Scheduled(fixedDelay = 10000)
    public void syncVideoLikeQueue() {
        userActionSyncSupport.syncVideoActionQueue(Constants.REDIS_WEB_ACTION_VIDEO_LIKE_QUEUE_KEY);
    }
}
