package com.bilibili.rec_system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源（前端地址）
        config.addAllowedOrigin("http://localhost:3000");  // React
        config.addAllowedOrigin("http://localhost:8081");  // Vue
        config.addAllowedOrigin("http://localhost:5173");  // Vite
        config.addAllowedOrigin("http://localhost:4200");   // Angular
        config.addAllowedOrigin("http://localhost:8080");   // 添加Vue CLI默认端口
        
        // 允许携带凭证（如cookies）
        config.setAllowCredentials(true);
        
        // 允许的HTTP方法
        config.addAllowedMethod("*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);
        
        // 配置映射源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有API路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        
        // 返回CORS过滤器
        return new CorsFilter(source);
    }
}