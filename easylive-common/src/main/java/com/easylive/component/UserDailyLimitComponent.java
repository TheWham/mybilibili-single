package com.easylive.component;

import com.easylive.constants.Constants;
import com.easylive.entity.dto.SysSettingDTO;
import com.easylive.enums.UserDailyLimitTypeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * 统一维护用户每日行为限制。
 */
@Component("userDailyLimitComponent")
public class UserDailyLimitComponent {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private RedisUtils redisUtils;

    public void checkDailyLimit(String userId, UserDailyLimitTypeEnum limitType) {
        Integer limitCount = getLimitCount(limitType);
        if (limitCount == null || limitCount <= 0) {
            return;
        }

        String redisKey = buildDailyLimitKey(userId, limitType);
        Object currentValue = redisUtils.get(redisKey);
        long currentCount = currentValue == null ? 0L : Long.parseLong(currentValue.toString());
        if (currentCount >= limitCount) {
            throw new BusinessException(String.format("%s已达到今日上限", limitType.getDesc()));
        }
    }

    public void recordDailyAction(String userId, UserDailyLimitTypeEnum limitType) {
        Integer limitCount = getLimitCount(limitType);
        if (limitCount == null || limitCount <= 0) {
            return;
        }

        String redisKey = buildDailyLimitKey(userId, limitType);
        long expireMilliseconds = getRemainMillisecondsToday();
        redisUtils.executeLongScript(
                Constants.REDIS_LUA_USER_DAILY_LIMIT,
                Collections.singletonList(redisKey),
                limitCount,
                expireMilliseconds
        );
    }

    private Integer getLimitCount(UserDailyLimitTypeEnum limitType) {
        SysSettingDTO sysSettingDTO = redisComponent.getSysSetting();
        return limitType.resolveLimit(sysSettingDTO);
    }

    private String buildDailyLimitKey(String userId, UserDailyLimitTypeEnum limitType) {
        String today = LocalDate.now().format(DAY_FORMATTER);
        return Constants.REDIS_KEY_USER_DAILY_LIMIT + limitType.getKeySuffix() + ":" + userId + ":" + today;
    }

    private long getRemainMillisecondsToday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        return Duration.between(now, tomorrowStart).toMillis() + Constants.REDIS_EXPIRE_TIME_ONE_SECOND;
    }
}
