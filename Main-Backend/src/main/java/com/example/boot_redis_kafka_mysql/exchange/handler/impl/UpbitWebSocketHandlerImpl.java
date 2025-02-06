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
import com.example.boot_redis_kafka_mysql.exchange.handler.UpbitWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.model.vo.MarketSubscribeVO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class UpbitWebSocketHandlerImpl implements WebSocketHandler, UpbitWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(UpbitWebSocketHandlerImpl.class);
    
    private final WebSocketManager webSocketManager;
    private final Sinks.Many<String> marketDataSink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Mono<Void> connect() {
        return webSocketManager.createConnection(Exchange.UPBIT, this)
            .doOnSuccess(v -> log.info("Upbit WebSocket 연결 성공"))
            .doOnError(e -> log.error("Upbit WebSocket 연결 실패", e));
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
            .exchange(Exchange.UPBIT)
            .symbols(symbols)
            .currencies(currencies)
            .build();
            
        String message = subscribeVO.createSubscribeMessage();
        return webSocketManager.sendMessage(Exchange.UPBIT, message)
            .doOnSuccess(v -> log.info("Upbit 구독 성공: symbols={}, currencies={}", symbols, currencies))
            .doOnError(e -> log.error("Upbit 구독 실패", e));
    }

    @Override
    public Flux<String> getMarketDataStream() {
        return marketDataSink.asFlux();
    }

    private void handleMessage(String message) {
        marketDataSink.tryEmitNext(message);
    }
}