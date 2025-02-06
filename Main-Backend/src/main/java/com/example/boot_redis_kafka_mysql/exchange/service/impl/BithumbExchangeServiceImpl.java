package com.example.boot_redis_kafka_mysql.exchange.service.impl;

import org.springframework.stereotype.Service;
import com.example.boot_redis_kafka_mysql.exchange.service.BithumbExchangeService;
import com.example.boot_redis_kafka_mysql.exchange.handler.BithumbWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BithumbExchangeServiceImpl implements BithumbExchangeService {
    private static final Logger log = LoggerFactory.getLogger(BithumbExchangeServiceImpl.class);
    
    private final BithumbWebSocketHandler bithumbHandler;

    @Override
    public void subscribeToSymbols(List<String> symbols, List<String> currencies) {
        try {
            bithumbHandler.subscribeToTicker(symbols, currencies);
        } catch (Exception e) {
            log.error("Bithumb 구독 중 에러: ", e);
        }
    }

    @Override
    public void processMarketData(String payload) {
        try {
            JSONObject data = new JSONObject(payload);
            log.debug("Bithumb 데이터 처리: {}", data);
        } catch (Exception e) {
            log.error("Bithumb 데이터 처리 중 에러: ", e);
        }
    }
} 