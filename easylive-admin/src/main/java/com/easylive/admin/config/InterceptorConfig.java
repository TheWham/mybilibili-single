package com.easylive.admin.config;

import com.easylive.admin.interceptor.WebAppInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
