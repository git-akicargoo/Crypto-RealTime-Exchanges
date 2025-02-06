package com.example.boot_redis_kafka_mysql.exchange.service;

import reactor.core.publisher.Flux;

public interface UpbitExchangeService extends BaseExchangeService {
    Flux<String> getAllMarkets();  // List<String>에서 Flux<String>으로 변경
} 