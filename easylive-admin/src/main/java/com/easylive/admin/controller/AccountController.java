package com.easylive.admin.controller;


import com.easylive.admin.service.AdminAccountService;
import com.easylive.component.RedisComponent;
import com.easylive.entity.dto.AdminLoginDTO;
import com.easylive.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController extends ABaseController{

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AdminAccountService adminAccountService;

    @RequestMapping("/checkCode")
    public ResponseVO getCheckCode(){
        return getSuccessResponseVO(adminAccountService.getCheckCode());
    }

    @RequestMapping("/login")
    public ResponseVO login(
            HttpServletResponse response,
            AdminLoginDTO adminLoginDTO)
    {
        try {
            String tokenId = adminAccountService.login(adminLoginDTO);
            saveToken2Session(response, tokenId);
            return getSuccessResponseVO(adminLoginDTO.getAccount());
        }finally {
            redisComponent.cleanCheckCode(adminLoginDTO.getCheckCodeKey());
        }
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletResponse response){
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }

}
