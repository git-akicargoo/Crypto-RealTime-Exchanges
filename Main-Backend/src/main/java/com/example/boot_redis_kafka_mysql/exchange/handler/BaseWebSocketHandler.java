package com.example.boot_redis_kafka_mysql.exchange.handler;

import java.util.List;

public interface BaseWebSocketHandler {
    void subscribeToTicker(List<String> symbols, List<String> currencies) throws Exception;
    void handleMessage(String payload);
} 