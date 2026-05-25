package com.example.dreamzone.config;

import com.example.dreamzone.interceptor.SessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionInterceptor())
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        "/auth/**"
                );
    }
}