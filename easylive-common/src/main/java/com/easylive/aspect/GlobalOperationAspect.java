package com.easylive.aspect;

import com.easylive.annotaion.GlobalInterceptor;
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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;


@Aspect
@Component
public class GlobalOperationAspect {

    @Resource
    private RedisComponent redisComponent;

    @Before("@annotation(com.easylive.annotaion.GlobalInterceptor) || @within(com.easylive.annotaion.GlobalInterceptor)")
    public void interceptorToDo(JoinPoint point)
    {
        GlobalInterceptor annotation = getInterceptor(point);
        if (annotation == null)
            return;

        if (annotation.checkLogin())
        {
            checkLogin();
        }
    }

    /**
     * 规则：
     * 1. 先取方法上的注解，便于对单个接口做覆盖
     * 2. 方法上没有时，再退回到类上的注解
     */
    private GlobalInterceptor getInterceptor(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method interfaceMethod = signature.getMethod();
        Class<?> targetClass = point.getTarget().getClass();

        Method targetMethod;
        try {
            targetMethod = targetClass.getMethod(signature.getName(), signature.getParameterTypes());
        } catch (NoSuchMethodException e) {
            targetMethod = interfaceMethod;
        }

        GlobalInterceptor methodInterceptor = AnnotationUtils.findAnnotation(targetMethod, GlobalInterceptor.class);
        if (methodInterceptor != null) {
            return methodInterceptor;
        }

        return AnnotationUtils.findAnnotation(targetClass, GlobalInterceptor.class);
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
