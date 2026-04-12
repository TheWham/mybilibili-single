package com.easylive.aspect;

import com.easylive.annotation.LoginInterceptor;
import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.entity.dto.TokenUserInfoDTO;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
public class UserLoginAspect extends GlobalOperationAspect{

    @Resource
    private RedisComponent redisComponent;

    @Before("@annotation(com.easylive.annotation.LoginInterceptor) || @within(com.easylive.annotation.LoginInterceptor)")
    public void validLogin(JoinPoint point)
    {
        LoginInterceptor annotation = getAnnotation(point, LoginInterceptor.class);
        if (annotation == null || !annotation.checkLogin())
            return;

        checkLogin();
    }


    private void checkLogin()
    {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        String tokenId = request.getHeader(Constants.WEB_TOKEN_KEY);
        if (tokenId == null)
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        TokenUserInfoDTO tokenInfo = redisComponent.getTokenInfo(tokenId);
        if (tokenInfo == null)
            throw new BusinessException(ResponseCodeEnum.CODE_901);
    }

}
