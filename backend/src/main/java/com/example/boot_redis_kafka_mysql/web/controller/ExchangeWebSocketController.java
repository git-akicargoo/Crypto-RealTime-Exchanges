package com.example.boot_redis_kafka_mysql.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.example.boot_redis_kafka_mysql.exchange.service.BinanceExchangeService;
import com.example.boot_redis_kafka_mysql.exchange.service.BithumbExchangeService;
import com.example.boot_redis_kafka_mysql.exchange.service.UpbitExchangeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ExchangeWebSocketController implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ExchangeWebSocketController.class);
    
    private final BinanceExchangeService binanceService;
    private final UpbitExchangeService upbitService;
    private final BithumbExchangeService bithumbService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("New WebSocket connection: {}", session.getId());
        
        // 테스트용 메시지 전송
        return session.send(
            Mono.just(session.textMessage("Connected to crypto price feed"))
        ).then();
    }
} 