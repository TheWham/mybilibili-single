package com.easylive.admin.controller;


import com.easylive.entity.po.UserInfo;
import com.easylive.entity.query.UserInfoQuery;
import com.easylive.entity.vo.PaginationResultVO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.service.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author amani
 * @since 2026.4.22
 */
@RestController
@Validated
@RequestMapping("user")
public class UserController extends ABaseController{

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/loadUser")
    public ResponseVO loadUser(Integer pageNo, Integer pageSize, String nickNameFuzzy, Integer status)
    {
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setPageNo(pageNo);
        userInfoQuery.setPageSize(pageSize);
        userInfoQuery.setNickNameFuzzy(nickNameFuzzy);
        userInfoQuery.setStatus(status);
        userInfoQuery.setOrderBy("join_time desc");
        PaginationResultVO<UserInfo> listByPage = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(listByPage);
    }

    @RequestMapping("changeStatus")
    public ResponseVO changeStatus(@NotBlank String userId, @NotNull Integer status)
    {
        userInfoService.changeStatus(userId, status);
        return getSuccessResponseVO(null);
    }

}
