package com.easylive.web.controller;


import cn.hutool.core.bean.BeanUtil;
import com.easylive.annotaion.GlobalInterceptor;
import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.UserInfoDTO;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.UserVideoAction;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.*;
import com.easylive.entity.vo.*;
import com.easylive.enums.PageSize;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/uhome")
@GlobalInterceptor(checkLogin = true)
public class UHomeController extends ABaseController{

    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private UserFocusService userFocusService;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserVideoActionService userVideoActionService;
    @Resource
    private VideoCommentService videoCommentService;
    @Resource
    private VideoDanmuService videoDanmuService;

    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(@NotEmpty String userId, Integer type, Integer pageNo, String videoName, Integer orderType)
    {

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setVideoName(videoName);
        if (type != null)
            videoInfoQuery.setPageSize(PageSize.SIZE10.getSize());

        com.easylive.enums.VideoOrderTypeEnum typeEnum = com.easylive.enums.VideoOrderTypeEnum.getEnum(orderType);
        if (typeEnum == null)
            typeEnum =  com.easylive.enums.VideoOrderTypeEnum.ORDER_POST_TIME;
        String orderDesc = typeEnum.getField() + " desc";
        videoInfoQuery.setOrderBy(orderDesc);

        PaginationResultVO<VideoInfo> listVideo = videoInfoService.findListByPage(videoInfoQuery);
        PaginationResultVO<VideoInfoUHomeVO> videoListVO = new PaginationResultVO<>();

        videoListVO.setPageNo(listVideo.getPageNo());
        videoListVO.setPageSize(listVideo.getPageSize());
        videoListVO.setPageTotal(listVideo.getPageTotal());
        videoListVO.setTotalCount(listVideo.getTotalCount());
        List<VideoInfoUHomeVO> videoInfoUHomeVO = BeanUtil.copyToList(listVideo.getList(), VideoInfoUHomeVO.class);
        videoListVO.setList(videoInfoUHomeVO);

        return getSuccessResponseVO(videoListVO);
    }

    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(@NotEmpty String userId) {
        // 获取基本信息
        UserInfo userInfoDb = userInfoService.getUserInfoByUserId(userId);
        if (userInfoDb == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserInfoVO userInfoVO = BeanUtil.toBean(userInfoDb, UserInfoVO.class);

        TokenUserInfoDTO currentUser = getTokenUserInfo();
        if (currentUser != null && !userId.equals(currentUser.getUserId())) {
            Integer haveFocus = userFocusService.selectHaveFocus(currentUser.getUserId(), userId);
            userInfoVO.setHaveFocus(haveFocus);
        }

        String formattedDate =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 尝试从 Redis 获取统计数据
        Map<String, Integer> statsMap = redisComponent.getUserStatsInfo(userId, formattedDate);

        if (statsMap != null && !statsMap.isEmpty()) {
            // 刷新过期时间
            redisComponent.flashUserStatsExpire(userId, formattedDate);
            fillStatsToVO(userInfoVO, statsMap);
        } else {
            // Redis 失效，从数据库聚合数据
            userInfoService.setUserInHome(userInfoVO);
            // 重新同步回 Redis
            syncStatsToRedis(userId, userInfoVO);
        }
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 封装：从 Map 填充到 VO
     */
    private void fillStatsToVO(UserInfoVO vo, Map<String, Integer> map) {
        vo.setFocusCount(map.getOrDefault(UserStatsRedisEnum.USER_FOCUS.getField(), 0));
        vo.setFansCount(map.getOrDefault(UserStatsRedisEnum.USER_FANS.getField(), 0));
        vo.setLikeCount(map.getOrDefault(UserStatsRedisEnum.VIDEO_LIKE.getField(), 0));
        vo.setCurrentCoinCount(map.getOrDefault(UserStatsRedisEnum.USER_COIN.getField(), 0));
        vo.setPlayCount(map.getOrDefault(UserStatsRedisEnum.VIDEO_PLAY.getField(), 0));
    }

    /**
     * 封装：同步到 Redis
     */
    private void syncStatsToRedis(String userId, UserInfoVO vo) {
        Map<String, Integer> map = new HashMap<>();
        map.put(UserStatsRedisEnum.VIDEO_PLAY.getField(), Optional.ofNullable(vo.getPlayCount()).orElse(0));
        map.put(UserStatsRedisEnum.USER_FANS.getField(), Optional.ofNullable(vo.getFansCount()).orElse(0));
        map.put(UserStatsRedisEnum.USER_FOCUS.getField(), Optional.ofNullable(vo.getFocusCount()).orElse(0));
        map.put(UserStatsRedisEnum.USER_COIN.getField(), Optional.ofNullable(vo.getCurrentCoinCount()).orElse(0));
        map.put(UserStatsRedisEnum.VIDEO_LIKE.getField(), Optional.ofNullable(vo.getLikeCount()).orElse(0));

        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoUserId(userId);
        //添加评论数量
        Integer commentCount = videoCommentService.findCountByParam(videoCommentQuery);
        map.put(UserStatsRedisEnum.USER_COMMENT_COUNT.getField(), Optional.ofNullable(commentCount).orElse(0));
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setVideoUserId(userId);
        //添加弹幕数量
        Integer danmuCount = videoDanmuService.findCountByParam(videoDanmuQuery);
        map.put(UserStatsRedisEnum.VIDEO_DANMU.getField(), Optional.ofNullable(danmuCount).orElse(0));
        //添加用户投币数量
        Integer coinCount = userVideoActionService.sumCoinCount(userId);
        map.put(UserStatsRedisEnum.VIDEO_COIN.getField(), Optional.ofNullable(coinCount).orElse(0));
        //查询收藏
        map.put(UserStatsRedisEnum.USER_COLLECT_COUNT.getField(), Optional.ofNullable(vo.getLikeCount()).orElse(0));
        String formattedDate =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        redisComponent.saveUserStatsInfo(userId, map, formattedDate);
    }

    @RequestMapping("/updateUserInfo")
    public ResponseVO updateUserInfo(@Validated UserInfoDTO userInfoDTO)
    {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();

        if (tokenUserInfo == null || !tokenUserInfo.getUserId().equals(userInfoDTO.getUserId()))
            throw new BusinessException(ResponseCodeEnum.CODE_404);

        UserInfo userInfo = BeanUtil.toBean(userInfoDTO, UserInfo.class);
        userInfoService.updateUserInfoUHome(tokenUserInfo, userInfo);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/saveTheme")
    public ResponseVO saveTheme(@Max(10) @Min(1) @NotNull Integer theme)
    {
        UserInfo userInfo = new UserInfo();
        userInfo.setTheme(theme);
        userInfoService.updateUserInfoByUserId(userInfo, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/focus")
    public ResponseVO focus(@NotEmpty String focusUserId)
    {
        String userId = getTokenUserInfo().getUserId();
        if (focusUserId.equals(userId))
            throw new BusinessException("无法关注自己");
        UserInfo focusUserInfo = userInfoService.getUserInfoByUserId(focusUserId);

        if (focusUserInfo == null)
            throw new BusinessException(ResponseCodeEnum.CODE_600);

        userFocusService.focus(focusUserId, userId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/cancelFocus")
    public ResponseVO cancelFocus(@NotEmpty String focusUserId)
    {
        String userId = getTokenUserInfo().getUserId();
        if (focusUserId.equals(userId))
            throw new BusinessException("无法取关自己");

        UserInfo focusUserInfo = userInfoService.getUserInfoByUserId(focusUserId);

        if (focusUserInfo == null)
            throw new BusinessException(ResponseCodeEnum.CODE_600);

        userFocusService.cancelFocus(focusUserId, userId);
        return getSuccessResponseVO(null);

    }

    @RequestMapping("/loadFocusList")
    public ResponseVO loadFocusList(Integer pageNo, Integer pageSize) {
        UserFocusQuery focusQuery = new UserFocusQuery();
        focusQuery.setPageNo(pageNo);
        focusQuery.setPageSize(pageSize);
        focusQuery.setOrderBy("v.focus_time desc");
        focusQuery.setUserId(getTokenUserInfo().getUserId());
        focusQuery.setQueryFocusDetailInfo(true);
        return getSuccessResponseVO(userFocusService.findListByPage(focusQuery));
    }

    @RequestMapping("/loadFansList")
    public ResponseVO loadFansList(Integer pageNo, Integer pageSize) {
        UserFocusQuery focusQuery = new UserFocusQuery();
        focusQuery.setPageNo(pageNo);
        focusQuery.setPageSize(pageSize);
        focusQuery.setOrderBy("v.focus_time desc");
        focusQuery.setUserFocusId(getTokenUserInfo().getUserId());
        focusQuery.setQueryFansDetailInfo(true);
        return getSuccessResponseVO(userFocusService.findListByPage(focusQuery));
    }

    @RequestMapping("/loadUserCollection")
    public ResponseVO loadUserCollection(Integer pageNo, @NotEmpty String userId)
    {

        UserActionQuery actionQuery = new UserActionQuery();
        actionQuery.setUserId(userId);
        actionQuery.setPageNo(pageNo);
        actionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        actionQuery.setOrderBy("v.action_time desc");
        PaginationResultVO<UserVideoAction> userCollectionVideoPage = userVideoActionService.findListByPage(actionQuery);

        if (userCollectionVideoPage == null || userCollectionVideoPage.getList() == null || userCollectionVideoPage.getList().isEmpty())
            return getSuccessResponseVO(Collections.emptyList());

        List<UserVideoAction> userCollectionVideoList = userCollectionVideoPage.getList();

        Map<String, Date> videoIdTimeMap = userCollectionVideoList.stream().collect(Collectors.toMap(UserVideoAction::getVideoId, UserVideoAction::getActionTime, (e, r)->e));
        List<String> userCollectionIds = userCollectionVideoList.stream().map(UserVideoAction::getVideoId).collect(Collectors.toList());
        // selectByIds ids在in中 查询出的结果是乱序的 需要修正
        List<VideoInfo> unOrderVideoInfos = videoInfoService.selectByIds(userCollectionIds);
        Map<String, VideoInfo> videoCollectMap = unOrderVideoInfos.stream().collect(Collectors.toMap(VideoInfo::getVideoId, v -> v));
        List<VideoInfo> finalOrderVideoList = userCollectionIds.stream()
                .map(videoCollectMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        //设置video对应的actionTime
        List<UserCollectionVO> userCollectionVOList = BeanUtil.copyToList(finalOrderVideoList, UserCollectionVO.class);
        userCollectionVOList.forEach(userCollectionVO -> userCollectionVO.setActionTime(videoIdTimeMap.get(userCollectionVO.getVideoId())));

        PaginationResultVO<UserCollectionVO> userCollectionPage = new PaginationResultVO<>();

        userCollectionPage.setTotalCount(userCollectionVideoPage.getTotalCount());
        userCollectionPage.setPageTotal(userCollectionVideoPage.getPageTotal());
        userCollectionPage.setPageSize(userCollectionVideoPage.getPageSize());
        userCollectionPage.setPageNo(userCollectionVideoPage.getPageNo());
        userCollectionPage.setList(userCollectionVOList);
        return getSuccessResponseVO(userCollectionPage);
    }

}
