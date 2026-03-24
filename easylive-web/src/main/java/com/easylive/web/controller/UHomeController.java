package com.easylive.web.controller;


import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.UserInfoDTO;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.UserVideoAction;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserActionQuery;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.*;
import com.easylive.enums.PageSize;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.enums.UserActionTypeEnum;
import com.easylive.enums.UserStatsRedisEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.UserFocusService;
import com.easylive.service.UserInfoService;
import com.easylive.service.UserVideoActionService;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/uhome")
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

    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(@NotEmpty String userId, Integer type, Integer pageNo, String videoName, Integer orderType)
    {

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setVideoName(videoName);
        if (type != null)
            videoInfoQuery.setPageSize(PageSize.SIZE10.getSize());

        VideoOrderTypeEnum typeEnum = VideoOrderTypeEnum.getEnum(orderType);
        if (typeEnum == null)
            typeEnum =  VideoOrderTypeEnum.ORDER_POST_TIME;
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
        UserInfo userInfoDb = userInfoService.getUserInfoByUserId(userId);

        if (userInfoDb == null)
            throw new BusinessException(ResponseCodeEnum.CODE_600);

        //先查询redis
        HashMap<String, Integer> statsMap = redisComponent.getUserStatsInfo(userId);
        UserInfoVO userInfoVO = BeanUtil.toBean(userInfoDb, UserInfoVO.class);

        if (!userId.equals(getTokenUserInfo().getUserId())) {
            Integer haveFocus = userFocusService.selectHaveFocus(getTokenUserInfo().getUserId(), userId);
            userInfoVO.setHaveFocus(haveFocus);
        }

        if (statsMap != null && !statsMap.isEmpty()) {
            //刷新时间
            redisComponent.flashUserStatsExpire(userId);
            userInfoVO.setFocusCount(statsMap.get(UserStatsRedisEnum.USER_FOCUS.getField()));
            userInfoVO.setFansCount(statsMap.get(UserStatsRedisEnum.USER_FANS.getField()));
            userInfoVO.setLikeCount(statsMap.get(UserStatsRedisEnum.VIDEO_LIKE.getField()));
            userInfoVO.setCurrentCoinCount(statsMap.get(UserStatsRedisEnum.USER_COIN.getField()));
            userInfoVO.setPlayCount(statsMap.get(UserStatsRedisEnum.VIDEO_PLAY.getField()));
            return getSuccessResponseVO(userInfoVO);
        }

        userInfoService.setUserInHome(userInfoVO);
        //刷新redis
        Map<String, Integer> userStatsMap = new HashMap<>(UserStatsRedisEnum.values().length);
        userStatsMap.put(UserStatsRedisEnum.VIDEO_PLAY.getField(), userInfoVO.getCurrentCoinCount() == null ? 0 : userInfoVO.getPlayCount());
        userStatsMap.put(UserStatsRedisEnum.USER_FANS.getField(), userInfoVO.getFansCount() == null ? 0 : userInfoVO.getFansCount());
        userStatsMap.put(UserStatsRedisEnum.USER_FOCUS.getField(), userInfoVO.getFocusCount() == null ? 0 : userInfoVO.getFocusCount());
        userStatsMap.put(UserStatsRedisEnum.USER_COIN.getField(), userInfoVO.getCurrentCoinCount() == null ? 0 : userInfoVO.getCurrentCoinCount());
        userStatsMap.put(UserStatsRedisEnum.VIDEO_LIKE.getField(), userInfoVO.getLikeCount() == null ? 0 : userInfoVO.getLikeCount());
        redisComponent.saveUserStatsInfo(userId, userStatsMap);
        return getSuccessResponseVO(userInfoVO);

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
        List<VideoInfo> finalOrderVideoList = userCollectionIds.stream().map(videoCollectMap::get).filter(Objects::nonNull).toList();

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
