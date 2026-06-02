package com.brevery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt broker in-memory để gửi tin nhắn đến client có tiền tố /topic hoặc /queue
        config.enableSimpleBroker("/topic", "/queue");
        // Tiền tố cho các tin nhắn từ client gửi lên server
        config.setApplicationDestinationPrefixes("/app");
        // Tiền tố cho các tin nhắn gửi riêng cho từng user
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint dùng để client kết nối WebSocket (hỗ trợ SockJS làm fallback)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
