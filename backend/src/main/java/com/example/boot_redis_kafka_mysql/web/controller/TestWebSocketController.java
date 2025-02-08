package com.example.boot_redis_kafka_mysql.web.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class TestWebSocketController {
    
    @MessageMapping("/test/connect")
    @SendTo("/subscribe/test")
    public String connect(String message) {
        log.info("Client connected with message: {}", message);
        return "Server received connect message: " + message;
    }

    @MessageMapping("/test/message")
    @SendTo("/subscribe/test")
    public String handleMessage(String message) {
        log.info("Received message: {}", message);
        return "Server echo: " + message;
    }
} 