package com.easylive.admin.config;

import com.easylive.admin.interceptor.WebAppInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author amani
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource
    private WebAppInterceptor webAppInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webAppInterceptor).addPathPatterns("/**");
    }
}
