package com.easylive.web.controller;


import cn.hutool.core.bean.BeanUtil;
import com.easylive.annotation.LoginInterceptor;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.entity.dto.UserInfoDTO;
import com.easylive.entity.po.UserInfo;
import com.easylive.entity.query.UserFocusQuery;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.service.UserFocusService;
import com.easylive.service.UserInfoService;
import com.easylive.service.UserVideoActionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/uhome")
@LoginInterceptor(checkLogin = true)
public class UHomeController extends ABaseController{

    @Resource
    private UserInfoService userInfoService;
    @Resource
    private UserFocusService userFocusService;
    @Resource
    private UserVideoActionService userVideoActionService;

    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(@NotEmpty String userId, Integer type, Integer pageNo, String videoName, Integer orderType)
    {
        return getSuccessResponseVO(userInfoService.loadUHomeVideoList(userId, type, pageNo, videoName, orderType));
    }

    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(@NotEmpty String userId) {
        return getSuccessResponseVO(userInfoService.getUHomeUserInfo(userId, getTokenUserInfo()));
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
        userInfoService.saveTheme(getTokenUserInfo().getUserId(), theme);
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
        return getSuccessResponseVO(userVideoActionService.loadUserCollection(pageNo, userId));
    }

}
