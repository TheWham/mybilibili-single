package com.easylive.web.task;

import com.easylive.component.RedisComponent;
import com.easylive.entity.po.UserMessage;
import com.easylive.service.UserMessageService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMessageSyncTask {

    private static final Logger log = LoggerFactory.getLogger(UserMessageSyncTask.class);
    private static final int MAX_SYNC_COUNT = 200;

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserMessageService userMessageService;

    @Scheduled(fixedDelay = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void syncUserMessageQueue() {
        List<UserMessage> messageList = new ArrayList<>();
        for (int i = 0; i < MAX_SYNC_COUNT; i++) {
            UserMessage userMessage = redisComponent.getNextUserMessageQueue();
            if (userMessage == null) {
                break;
            }
            messageList.add(userMessage);
        }

        if (messageList.isEmpty()) {
            return;
        }

        // 通知是旁路数据，批量落库就够了，不用占住主请求。
        userMessageService.addBatch(messageList);
        log.info("syncUserMessageQueue finished, count={}", messageList.size());
    }
}
