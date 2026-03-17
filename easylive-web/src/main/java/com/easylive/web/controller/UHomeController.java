package com.easylive.web.controller;


import cn.hutool.core.bean.BeanUtil;
import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.VideoCountDTO;
import com.easylive.entity.po.VideoInfo;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.query.VideoInfoQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.entity.vo.UserInfoVO;
import com.easylive.service.UserFocusService;
import com.easylive.service.UserInfoService;
import com.easylive.service.UserVideoSeriesService;
import com.easylive.service.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    public ResponseVO loadVideoList(@NotEmpty String userId, @NotNull Integer type)
    {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();
        UserInfoVO userInfoVO = BeanUtil.toBean(tokenUserInfo, UserInfoVO.class);
        // 查询视频likeCount, playCount
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        List<VideoInfo> videoList = videoInfoService.findListByParam(videoInfoQuery);

        VideoCountDTO videoCountDTO = videoInfoService.sumVideoCountByUserId(userId);
        userInfoVO.setPlayCount(videoCountDTO.getTotalPlayCount());
        userInfoVO.setLikeCount(videoCountDTO.getTotalLikeCount());
        //查询用户粉丝和关注量
        //查询fans focus数量
        UserFocusQuery userFocusQuery = new UserFocusQuery();
        return null;
    }

    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(@NotEmpty String userId)
    {
        userInfoService.getUserInfoByUserId(userId);
        return null;
    }

    @RequestMapping("/series/loadVideoSeriesWithVideo")
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId)
    {
        return null;
    }

    @RequestMapping("/loadFocusList")
    public ResponseVO loadFocusList(Integer pageNo, Integer pageSize)
    {
        return null;
    }

}
