package com.bilibili.rec_system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // 所有接口
                        .allowedOrigins(    // 允许的前端地址
                                "http://localhost:3000",  // React
                                "http://localhost:8081",  // Vue
                                "http://localhost:5173",  // Vite
                                "http://localhost:4200",   // Angular
                                "http://localhost:8080"   // 添加Vue CLI默认端口
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600); // 1小时
            }
        };
    }
}