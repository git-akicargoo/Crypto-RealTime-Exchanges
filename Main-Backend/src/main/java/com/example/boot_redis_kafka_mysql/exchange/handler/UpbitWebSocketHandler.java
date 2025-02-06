package com.example.boot_redis_kafka_mysql.exchange.handler;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UpbitWebSocketHandler {
    Mono<Void> connect();
    Mono<Void> subscribeToTicker(List<String> symbols, List<String> currencies);
    Flux<String> getMarketDataStream();
} 