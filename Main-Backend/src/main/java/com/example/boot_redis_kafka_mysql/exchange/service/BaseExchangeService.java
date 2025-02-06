package com.example.boot_redis_kafka_mysql.exchange.service;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.List;
import com.example.boot_redis_kafka_mysql.exchange.model.dto.MarketPriceDTO;

public interface BaseExchangeService {
    Mono<Void> subscribeToSymbols(List<String> symbols, List<String> currencies);
    Flux<MarketPriceDTO> getMarketDataStream();
    MarketPriceDTO parseMarketData(String rawData);
} 