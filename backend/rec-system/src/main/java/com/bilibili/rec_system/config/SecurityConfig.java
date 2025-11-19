package com.bilibili.rec_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())      // 关闭 CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // 允许所有请求，不需要登录
                )
                .formLogin(form -> form.disable()) // 禁用登录页面
                .httpBasic(httpBasic -> httpBasic.disable()); // 禁用 HTTP Basic

        return http.build();
    }
}
