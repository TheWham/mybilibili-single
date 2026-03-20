package com.easylive.web.controller;


import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.UserInfoDTO;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.po.UserVideoSeries;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.query.UserVideoSeriesQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.UserInfoVO;
import com.easylive.entity.vo.VideoInfoUHomeVO;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.UserFocusService;
import com.easylive.service.UserInfoService;
import com.easylive.service.UserVideoSeriesService;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("uhome")
public class UHomeController extends ABaseController{

    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private UserVideoSeriesService userVideoSeriesService;
    @Resource
    private UserFocusService userFocusService;
    @Resource
    private RedisComponent redisComponent;


    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(@NotEmpty String userId, Integer type, Integer pageNo, String videoName, Integer orderType)
    {

        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setVideoName(videoName);
        String orderDesc = null;
        VideoOrderTypeEnum typeEnum = VideoOrderTypeEnum.getEnum(orderType);
        if (orderType != null && typeEnum != null)
        {
            if (typeEnum.equals(VideoOrderTypeEnum.ORDER_POST_TIME))
                orderDesc = "v.create_time desc";
            if (typeEnum.equals(VideoOrderTypeEnum.ORDER_PLAY_COUNT))
                orderDesc = "v.play_count desc";
            if (typeEnum.equals(VideoOrderTypeEnum.ORDER_COLLECT_COUNT))
                orderDesc = "v.collect_count desc";
        }
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
    public ResponseVO getUserInfo(@NotEmpty String userId)
    {
        UserInfo userInfoDb = userInfoService.getUserInfoByUserId(userId);

        if (userInfoDb == null)
            throw new BusinessException(ResponseCodeEnum.CODE_600);

        //先查询redis
        UserInfoVO userInfoVOInRedis = redisComponent.getUserInfoVOInRedis(userId);
        if (userInfoVOInRedis != null)
        {
            //重置时间
            redisComponent.saveUserInfoVOInRedis(userInfoVOInRedis);
            return getSuccessResponseVO(userInfoVOInRedis);
        }
        UserInfoVO userInfoVO = BeanUtil.toBean(userInfoDb, UserInfoVO.class);
        userInfoService.setUserInHome(userInfoVO);
        //刷新redis
        redisComponent.saveUserInfoVOInRedis(userInfoVO);
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

    //TODO loadVideoSeriesWithVideo
    @RequestMapping("/series/loadVideoSeriesWithVideo")
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId)
    {
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/series/loadVideoSeries")
    public ResponseVO loadVideoSeries(@NotEmpty String userId)
    {
        UserVideoSeriesQuery videoSeriesQuery= new UserVideoSeriesQuery();
        videoSeriesQuery.setUserId(userId);
        PaginationResultVO<UserVideoSeries> videoSeriesList = userVideoSeriesService.findListByPage(videoSeriesQuery);
        return getSuccessResponseVO(videoSeriesList);
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

}
