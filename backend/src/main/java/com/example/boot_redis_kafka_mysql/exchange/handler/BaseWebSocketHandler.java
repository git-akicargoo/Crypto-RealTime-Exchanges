package com.example.boot_redis_kafka_mysql.exchange.handler;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.List;

public interface BaseWebSocketHandler {
    Mono<Void> subscribeToTicker(List<String> symbols, List<String> currencies);
    Flux<String> getMarketDataStream();
} 