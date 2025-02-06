package com.example.boot_redis_kafka_mysql.exchange.service;

import java.util.List;

public interface BaseExchangeService {
    void processMarketData(String payload);  // 모든 거래소가 market data를 처리
    void subscribeToSymbols(List<String> symbols, List<String> currencies);  // currencies 파라미터 추가
} 