package com.treemoon.MeetAssist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 注册STOMP端点（前端连接地址）
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")        // 连接端点
                .setAllowedOrigins("*")    // 允许跨域
                .withSockJS();             // 支持SockJS回退
    }

    // 配置消息代理
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // 消息广播前缀
        registry.setApplicationDestinationPrefixes("/app"); // 应用请求前缀
    }
}