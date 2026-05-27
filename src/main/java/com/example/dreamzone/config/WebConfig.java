package com.example.dreamzone.config;

import com.example.dreamzone.interceptor.SessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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
                        "/uploads/**",   // Compatibilidad con imágenes locales antiguas
                        "/imagenes/**",  // Imágenes servidas desde MongoDB GridFS
                        "/favicon.ico",
                        "/auth/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Compatibilidad con imágenes locales antiguas (si aún existen en disco)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}