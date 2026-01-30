package com.easylive.admin.interceptor;

import com.easylive.component.RedisComponent;
import com.easylive.constants.Constants;
import com.easylive.enums.ResponseCodeEnum;
import com.easylive.exception.BusinessException;
import com.easylive.utils.StringTools;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Component
public class WebAppInterceptor implements HandlerInterceptor {

    private static final String URL_FILE = "/file/";
    private static final String URL_ACCOUNT = "/admin/account/";

    @Resource
    private RedisComponent redisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (Objects.isNull(handler))
            return false;

        if (!(handler instanceof HandlerMethod))
            return true;

        // 登陆注册信息直接放行
        if (request.getRequestURI().startsWith(URL_ACCOUNT))
            return true;

        String tokenId = request.getHeader(Constants.ADMIN_TOKEN_KEY);

        // 比如如果想查看网站中的图片其实是点击的超链接不是发起的请求,所以请求头中不会带上token, 只能从cookie中取出
        if (request.getRequestURI().contains(URL_FILE))
        {
            tokenId = getTokenFromCookie(request);
        }

        if (StringTools.isEmpty(tokenId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        Object tokenInfo4Admin = redisComponent.getTokenInfo4Admin(tokenId);

        if (Objects.isNull(tokenInfo4Admin))
            return false;

        return true;
    }

    private String getTokenFromCookie(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) return null;

        for (Cookie cookie : cookies)
        {
            if (cookie.getName().equals(Constants.ADMIN_TOKEN_KEY))
            {
               return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
