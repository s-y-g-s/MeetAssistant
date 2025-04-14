package com.treemoon.MeetAssist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 指定需要跨域的接口路径，"/**"表示全部路径
                .allowedOrigins("*")    // 允许的来源（*表示所有域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的HTTP方法
                .allowedHeaders("*");    // 允许的请求头
//                .allowCredentials(true); // 是否允许携带凭证（如Cookie）
    }
}
