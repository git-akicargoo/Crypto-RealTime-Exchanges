package com.example.boot_redis_kafka_mysql.exchange.service;

import java.util.List;

public interface UpbitExchangeService extends BaseExchangeService {
    List<String> getAllMarkets();  // Upbit만의 특화 기능
} 