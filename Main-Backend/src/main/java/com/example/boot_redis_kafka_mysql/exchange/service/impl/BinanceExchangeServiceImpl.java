package com.example.boot_redis_kafka_mysql.exchange.service.impl;

import org.springframework.stereotype.Service;
import com.example.boot_redis_kafka_mysql.exchange.service.BinanceExchangeService;
import com.example.boot_redis_kafka_mysql.exchange.handler.BinanceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BinanceExchangeServiceImpl implements BinanceExchangeService {
    private static final Logger log = LoggerFactory.getLogger(BinanceExchangeServiceImpl.class);
    
    private final BinanceWebSocketHandler binanceHandler;

    @Override
    public void subscribeToSymbols(List<String> symbols, List<String> currencies) {
        try {
            binanceHandler.subscribeToTicker(symbols, currencies);
        } catch (Exception e) {
            log.error("Binance 구독 중 에러: ", e);
        }
    }

    @Override
    public void processMarketData(String payload) {
        try {
            JSONObject data = new JSONObject(payload);
            log.debug("Binance 데이터 처리: {}", data);
        } catch (Exception e) {
            log.error("Binance 데이터 처리 중 에러: ", e);
        }
    }
} 