package com.easylive.web.task;


import com.easylive.constants.Constants;
import com.easylive.entity.event.UserStatsChangeEvent;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 监听硬币,关注,粉丝数变化
 */
@Component
public class UserStatsCacheListener {

    @Resource
    private RedisUtils redisUtils;
    /**
     * @Async: 开启新线程后台执行
     * @TransactionalEventListener(phase = AFTER_COMMIT): 只有前面 MySQL 的事务成功提交了，才会走到这里
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStatsChange(UserStatsChangeEvent event) {
        String myId = event.getFromUserId();
        String upId = event.getToUserId();
        UserStatsRedisEnum typeEnum = event.getTypeEnum();
        int count = event.getCount();
        String myKey = Constants.REDIS_WEB_USER_STATS_KEY + myId;
        String upKey = Constants.REDIS_WEB_USER_STATS_KEY + upId;
        //看看他们有没有缓存
        boolean isMyKeyExist = redisUtils.keyExists(myKey);
        boolean isUpKeyExist = redisUtils.keyExists(upKey);
        // 如果两边都没缓存无需修改
        if (!isMyKeyExist && !isUpKeyExist) {
            return;
        }

        // 使用 Pipeline 流水线，将两条更新命令打包发送
        redisUtils.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {

                switch (typeEnum) {
                    case USER_FOCUS -> handleFocusCache(isMyKeyExist, isUpKeyExist,myKey, upKey, typeEnum.getField(), count, operations);

                    case USER_COIN  -> handleCoinCache(isMyKeyExist, isUpKeyExist,myKey, upKey, typeEnum.getField(), count, operations);

                    case VIDEO_LIKE -> handleVideoLikeCache(isMyKeyExist, myKey, typeEnum.getField(), count, operations);

                    default -> throw new IllegalStateException("未知的操作类型: " + typeEnum);
                }
                // 必须返回 null，底层的 Spring Data Redis 才会把暂存的命令一并打包发给服务器
                return null;
            }
        });
        }

    private void handleFocusCache(boolean isMyKeyExist, boolean isUpKeyExist, String myKey, String upKey, String field, Integer count, RedisOperations operations)
    {
        //操作关注数
        if (isMyKeyExist) {
            operations.opsForHash().increment(myKey, field, count);
        }
        if (isUpKeyExist) {
            operations.opsForHash().increment(upKey, UserStatsRedisEnum.USER_FANS.getField(), count);
        }
    }

    private void handleCoinCache(boolean isMyKeyExist, boolean isUpKeyExist, String myKey, String upKey, String field, Integer count, RedisOperations operations)
    {
        //操作硬币
        if (isMyKeyExist)
            operations.opsForHash().increment(myKey, field, -count);
        if (isUpKeyExist)
            operations.opsForHash().increment(upKey, field, count);
    }

    private void handleVideoLikeCache(boolean isMyKeyExist, String myKey,String field, Integer count, RedisOperations operations)
    {
        //操作视频点赞
        if (isMyKeyExist)
            operations.opsForHash().increment(myKey, field, count);
    }
}