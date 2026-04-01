package com.easylive.web.task;

import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.po.UserStats;
import com.easylive.entity.query.UserStatsQuery;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.mappers.UserStatsMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DailyUserStatsSyncTask {
    private static final Logger log = LoggerFactory.getLogger(DailyUserStatsSyncTask.class);

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserStatsMapper<UserStats, UserStatsQuery> userStatsMapper;

    @Scheduled(cron = "0 5 0 * * ?")
    public void syncYesterdayUserStats() {
        String statsDay = LocalDate.now().minusDays(Constants.ONE).toString();
        Set<String> keySet = redisComponent.getUserStatsKeysByDay(statsDay);
        if (keySet == null || keySet.isEmpty()) {
            return;
        }

        List<UserStats> saveList = new ArrayList<>(keySet.size());
        for (String key : keySet) {
            String userId = key.substring(key.lastIndexOf(":") + 1);
            Map<String, Integer> statsMap = redisComponent.getUserStatsInfo(userId, statsDay);
            if (statsMap == null || statsMap.isEmpty()) {
                continue;
            }
            UserStats userStats = buildUserStats(userId, statsDay, statsMap);
            saveList.add(userStats);
        }
        if (saveList.isEmpty()) {
            return;
        }
        userStatsMapper.insertOrUpdateBatch(saveList);
        int syncCount = saveList.size();
        if (syncCount > 0) {
            log.info("syncYesterdayUserStats finished, statsDay={}, count={}", statsDay, syncCount);
        }
    }

    private UserStats buildUserStats(String userId, String statsDay, Map<String, Integer> statsMap) {
        UserStats userStats = new UserStats();
        userStats.setUserId(userId);
        userStats.setStatsDay(Date.valueOf(statsDay));
        userStats.setPlayCount(statsMap.get(UserStatsRedisEnum.VIDEO_PLAY.getField()));
        userStats.setLikeCount(statsMap.get(UserStatsRedisEnum.VIDEO_LIKE.getField()));
        userStats.setCurrentCoinCount(statsMap.get(UserStatsRedisEnum.USER_COIN.getField()));
        userStats.setFocusCount(statsMap.get(UserStatsRedisEnum.USER_FOCUS.getField()));
        userStats.setFansCount(statsMap.get(UserStatsRedisEnum.USER_FANS.getField()));
        userStats.setCollectCount(statsMap.get(UserStatsRedisEnum.USER_COLLECT_COUNT.getField()));
        userStats.setCommentCount(statsMap.get(UserStatsRedisEnum.USER_COMMENT_COUNT.getField()));
        userStats.setDanmuCount(statsMap.get(UserStatsRedisEnum.VIDEO_DANMU.getField()));
        userStats.setCoinCount(statsMap.get(UserStatsRedisEnum.VIDEO_COIN.getField()));
        return userStats;
    }
}
