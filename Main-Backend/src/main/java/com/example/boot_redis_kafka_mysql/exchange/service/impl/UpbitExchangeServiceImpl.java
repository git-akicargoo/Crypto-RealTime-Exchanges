package com.example.boot_redis_kafka_mysql.exchange.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.example.boot_redis_kafka_mysql.exchange.service.UpbitExchangeService;
import com.example.boot_redis_kafka_mysql.exchange.handler.UpbitWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UpbitExchangeServiceImpl implements UpbitExchangeService {
    private static final Logger log = LoggerFactory.getLogger(UpbitExchangeServiceImpl.class);
    
    private final UpbitWebSocketHandler upbitHandler;
    private final RestTemplate restTemplate;

    @Override
    public void subscribeToSymbols(List<String> symbols, List<String> currencies) {
        try {
            upbitHandler.subscribeToTicker(symbols, currencies);
        } catch (Exception e) {
            log.error("Upbit 구독 중 에러: ", e);
        }
    }

    @Override
    public void processMarketData(String payload) {
        try {
            JSONObject data = new JSONObject(payload);
            log.debug("Upbit 데이터 처리: {}", data);
        } catch (Exception e) {
            log.error("Upbit 데이터 처리 중 에러: ", e);
        }
    }

    @Override
    public List<String> getAllMarkets() {
        String url = "https://api.upbit.com/v1/market/all?isDetails=false";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        List<String> markets = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(response.getBody());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject market = jsonArray.getJSONObject(i);
                String marketId = market.getString("market");
                if (marketId.startsWith("KRW-")) {
                    markets.add(marketId);
                }
            }
            log.info("Retrieved {} KRW markets from Upbit", markets.size());
        } catch (Exception e) {
            log.error("Failed to parse Upbit markets", e);
        }
        return markets;
    }
} 