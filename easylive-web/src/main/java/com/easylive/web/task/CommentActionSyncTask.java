package com.easylive.web.task;

import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CommentActionSyncTask {

    @Resource
    private UserActionSyncSupport userActionSyncSupport;

    @Scheduled(fixedDelay = 10000)
    public void syncCommentActionQueue() {
        userActionSyncSupport.syncCommentQueue();
    }
}
