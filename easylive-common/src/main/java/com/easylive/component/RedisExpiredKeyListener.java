package com.easylive.component;


import com.easylive.constants.Constants;
import com.easylive.redis.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisExpiredKeyListener extends KeyExpirationEventMessageListener {

    @Resource
    private RedisUtils redisUtils;

    public RedisExpiredKeyListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        String prefix = Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREIFX + Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX;
        if (!key.startsWith(prefix))
        {
            return;
        }
        String fileId = key.substring(prefix.length(), prefix.length() + Constants.LENGTH_20);
        log.error("fileId:{}", fileId);
        redisUtils.decrement(String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE, fileId));
    }
}
