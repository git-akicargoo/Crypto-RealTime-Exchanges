package com.example.boot_redis_kafka_mysql.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Starting to register STOMP endpoints...");
        log.info("Allowed origins: {}", allowedOrigins);
        try {
            registry.addEndpoint("/ws")
                    .setAllowedOrigins("http://localhost:5173")
                    .withSockJS();  // 단순화
            
            log.info("STOMP endpoints registered successfully");
        } catch (Exception e) {
            log.error("Failed to register STOMP endpoints", e);
        }
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        try {
            log.info("Starting to configure message broker...");
            registry.setApplicationDestinationPrefixes("/publish")
                   .enableSimpleBroker("/subscribe")
                   .setHeartbeatValue(new long[]{10000, 10000})
                   .setTaskScheduler(taskScheduler());
            
            log.info("Message broker configured successfully");
        } catch (Exception e) {
            log.error("Failed to configure message broker", e);
        }
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        log.info("Configuring WebSocket transport...");
        registration.setMessageSizeLimit(8192)
                   .setSendBufferSizeLimit(512 * 1024)
                   .setSendTimeLimit(20000);
        registration.setTimeToFirstMessage(30000);
        log.info("WebSocket transport configured");
    }

    @Bean
    public WebSocketClient webSocketClient() {
        return new ReactorNettyWebSocketClient();  // WebSocketClient 빈 추가
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("websocket-heartbeat-thread-");
        scheduler.initialize();
        return scheduler;
    }
} 