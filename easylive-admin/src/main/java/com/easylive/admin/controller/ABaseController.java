package com.easylive.admin.controller;

import com.easylive.component.RedisComponent;
import com.easylive.config.AdminConfig;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.AdminLoginDTO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ABaseController {

    protected static final String STATUS_SUCCESS = "success";
    protected static final String STATUS_ERROR = "error";

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AdminConfig adminConfig;



    protected <T> ResponseVO getSuccessResponseVO(T t){
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t){
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_ERROR);
        if (e.getCode() == null){
            responseVO.setCode(ResponseCodeEnum.CODE_600.getCode());
        }else{
            responseVO.setCode(e.getCode());
        }
        responseVO.setInfo(e.getMessage());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t){
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_500.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected void saveToken2Session(HttpServletResponse response, String tokenId){
        Cookie cookie = new Cookie(Constants.ADMIN_TOKEN_KEY, tokenId);
        //admin cookie周期为会话, 关闭浏览器则失效
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


    protected void cleanCookie(HttpServletResponse response)
    {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.ADMIN_TOKEN_KEY)){
                String tokenId = cookie.getValue();
                cookie.setPath("/");
                cookie.setMaxAge(0);
                redisComponent.cleanAdminToken(tokenId);
                response.addCookie(cookie);
                break;
            }
        }
    }

    protected void validAdminInfo(HttpServletResponse response, AdminLoginDTO adminLoginDTO)
    {
        // 从 redis中获取存储的验证码
        String redisCode = redisComponent.getCode(adminLoginDTO.getCheckCodeKey());
        if (!adminLoginDTO.getCheckCode().equalsIgnoreCase(redisCode)) {
            throw new BusinessException("图形验证码不正确");
        }

        if (!adminLoginDTO.getAccount().equals(adminConfig.getAccount()) || !adminLoginDTO.getPassword().equals(StringTools.md5Password(adminConfig.getPassword())))
        {
            throw new BusinessException("账号或密码错误");
        }
        String tokenId = redisComponent.saveToken4Admin(adminLoginDTO.getAccount());
        //为了方便直接将token信息到session中, 正常来说只需要提供tokenID给前端即可, 然后每次请求带着tokenID从redis中取出数据
        saveToken2Session(response, tokenId);
    }
}
