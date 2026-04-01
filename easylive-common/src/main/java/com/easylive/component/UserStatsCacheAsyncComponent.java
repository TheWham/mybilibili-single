package com.easylive.component;

import com.easylive.entity.dto.VideoCountDTO;
import com.easylive.entity.po.*;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.UserCountVO;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.mappers.*;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class UserStatsCacheAsyncComponent {

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;
    @Resource
    private UserVideoActionMapper<UserVideoAction, UserActionQuery> userVideoActionMapper;
    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;
    @Resource
    private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;
    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Async("userStatsCacheExecutor")
    public void refreshRealtimeUserStatsCache(String userId) {
        if (userId == null) {
            return;
        }
        HashMap<String, Integer> cacheMap = redisComponent.getRealtimeUserStatsInfo(userId);
        if (cacheMap != null && !cacheMap.isEmpty()) {
            redisComponent.refreshRealtimeUserStatsExpire(userId);
            return;
        }
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        if (userInfo == null) {
            return;
        }
        redisComponent.saveRealtimeUserStatsInfo(userId, buildRealtimeUserStatsMap(userId, buildUserCountVO(userId, userInfo)));
    }

    private HashMap<String, Integer> buildRealtimeUserStatsMap(String userId, UserCountVO userCountVO) {
        HashMap<String, Integer> statsMap = new HashMap<>();
        statsMap.put(UserStatsRedisEnum.USER_FOCUS.getField(), defaultValue(userCountVO.getFocusCount()));
        statsMap.put(UserStatsRedisEnum.USER_FANS.getField(), defaultValue(userCountVO.getFansCount()));
        statsMap.put(UserStatsRedisEnum.USER_COIN.getField(), defaultValue(userCountVO.getCurrentCoinCount()));
        statsMap.put(UserStatsRedisEnum.VIDEO_LIKE.getField(), defaultValue(userCountVO.getLikeCount()));
        statsMap.put(UserStatsRedisEnum.VIDEO_PLAY.getField(), defaultValue(userCountVO.getPlayCount()));
        statsMap.put(UserStatsRedisEnum.USER_COMMENT_COUNT.getField(), defaultValue(countVideoComment(userId)));
        statsMap.put(UserStatsRedisEnum.VIDEO_DANMU.getField(), defaultValue(countVideoDanmu(userId)));
        statsMap.put(UserStatsRedisEnum.VIDEO_COIN.getField(), defaultValue(userVideoActionMapper.sumCoinCount(userId)));
        statsMap.put(UserStatsRedisEnum.USER_COLLECT_COUNT.getField(), defaultValue(countVideoCollect(userId)));
        return statsMap;
    }

    private UserCountVO buildUserCountVO(String userId, UserInfo userInfo) {
        UserCountVO userCountVO = new UserCountVO();
        userCountVO.setCurrentCoinCount(defaultValue(userInfo.getCurrentCoinCount()));

        UserFocusQuery focusQuery = new UserFocusQuery();
        focusQuery.setUserId(userId);
        userCountVO.setFocusCount(defaultValue(userFocusMapper.selectCount(focusQuery)));

        UserFocusQuery fansQuery = new UserFocusQuery();
        fansQuery.setUserFocusId(userId);
        userCountVO.setFansCount(defaultValue(userFocusMapper.selectCount(fansQuery)));

        VideoCountDTO videoCountDTO = videoInfoMapper.sumVideoCountByUserId(userId);
        if (videoCountDTO != null) {
            userCountVO.setLikeCount(defaultValue(videoCountDTO.getTotalLikeCount()));
            userCountVO.setPlayCount(defaultValue(videoCountDTO.getTotalPlayCount()));
        } else {
            userCountVO.setLikeCount(0);
            userCountVO.setPlayCount(0);
        }
        return userCountVO;
    }

    private Integer countVideoComment(String userId) {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoUserId(userId);
        return videoCommentMapper.selectCount(videoCommentQuery);
    }

    private Integer countVideoDanmu(String userId) {
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setVideoUserId(userId);
        return videoDanmuMapper.selectCount(videoDanmuQuery);
    }

    private Integer countVideoCollect(String userId) {
        UserActionQuery userActionQuery = new UserActionQuery();
        userActionQuery.setVideoUserId(userId);
        userActionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        return userVideoActionMapper.selectCount(userActionQuery);
    }

    private Integer defaultValue(Integer value) {
        return value == null ? 0 : value;
    }
}
