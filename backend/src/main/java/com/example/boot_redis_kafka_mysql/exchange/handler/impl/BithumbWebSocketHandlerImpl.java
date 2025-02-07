package com.example.boot_redis_kafka_mysql.exchange.handler.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;
import com.example.boot_redis_kafka_mysql.exchange.connection.WebSocketManager;
import com.example.boot_redis_kafka_mysql.exchange.handler.BithumbWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.model.vo.MarketSubscribeVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class BithumbWebSocketHandlerImpl implements WebSocketHandler, BithumbWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(BithumbWebSocketHandlerImpl.class);
    
    private final WebSocketManager webSocketManager;
    private final ObjectMapper objectMapper;
    private final Sinks.Many<String> marketDataSink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Mono<Void> connect() {
        return webSocketManager.createConnection(Exchange.BITHUMB, this)
            .doOnSuccess(v -> log.info("Bithumb WebSocket 연결 성공"))
            .doOnError(e -> log.error("Bithumb WebSocket 연결 실패", e));
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(this::handleMessage)
            .then();
    }

    @Override
    public Mono<Void> subscribeToTicker(List<String> symbols, List<String> currencies) {
        MarketSubscribeVO subscribeVO = MarketSubscribeVO.builder()
            .exchange(Exchange.BITHUMB)
            .symbols(symbols)
            .currencies(currencies)
            .build();
            
        String message = subscribeVO.createSubscribeMessage();
        return webSocketManager.sendMessage(Exchange.BITHUMB, message)
            .doOnSuccess(v -> log.info("Bithumb 구독 성공: symbols={}, currencies={}", symbols, currencies))
            .doOnError(e -> log.error("Bithumb 구독 실패", e));
    }

    @Override
    public Flux<String> getMarketDataStream() {
        return marketDataSink.asFlux();
    }

    private void handleMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            
            // 연결 성공 메시지는 무시
            if (node.has("status")) {
                return;
            }
            
            // 실제 마켓 데이터만 전달
            if (node.has("type") && "ticker".equals(node.get("type").asText())) {
                marketDataSink.tryEmitNext(message);
            }
        } catch (Exception e) {
            log.error("Bithumb 메시지 처리 실패: {}", message, e);
        }
    }
} 