package com.example.boot_redis_kafka_mysql.exchange.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpbitMarketService {
    private static final Logger log = LoggerFactory.getLogger(UpbitMarketService.class);
    private final RestTemplate restTemplate;

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