package com.example.boot_redis_kafka_mysql.exchange.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;
import com.example.boot_redis_kafka_mysql.exchange.handler.UpbitWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.model.dto.MarketPriceDTO;
import com.example.boot_redis_kafka_mysql.exchange.service.UpbitExchangeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UpbitExchangeServiceImpl implements UpbitExchangeService {
    private static final Logger log = LoggerFactory.getLogger(UpbitExchangeServiceImpl.class);
    
    private final UpbitWebSocketHandler handler;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private Flux<String> sharedMarketDataStream;

    @Override
    public Mono<Void> subscribeToSymbols(List<String> symbols, List<String> currencies) {
        return handler.subscribeToTicker(symbols, currencies)
            .doOnSuccess(unused -> {
                this.sharedMarketDataStream = handler.getMarketDataStream().share();
                log.info("Upbit market data stream initialized for symbols: {}", symbols);
            })
            .doOnError(e -> log.error("Upbit subscription error: ", e));
    }

    @Override
    public Flux<MarketPriceDTO> getMarketDataStream() {
        if (sharedMarketDataStream == null) {
            return Flux.error(new IllegalStateException("Upbit market data stream not initialized"));
        }
        return sharedMarketDataStream
            .map(this::parseMarketData)
            .filter(Objects::nonNull)
            .doOnNext(data -> log.info("[Upbit] {}-{}: {} (시간: {})", 
                data.getSymbol(), 
                data.getCurrency(), 
                data.getPrice(),
                data.getTimestamp()));
    }

    @Override
    public MarketPriceDTO parseMarketData(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            
            // 구독 응답이나 에러 응답은 조용히 null 반환
            if (!node.has("type") || !"ticker".equals(node.get("type").asText())) {
                return null;
            }

            String symbol = node.get("code").asText().replace("KRW-", "");
            String price = node.get("trade_price").asText();
            String volume = node.get("acc_trade_volume_24h").asText();
            String highPrice = node.get("high_price").asText();
            String lowPrice = node.get("low_price").asText();
            String openPrice = node.get("opening_price").asText();
            String changeRate = node.get("signed_change_rate").asText();

            return MarketPriceDTO.builder()
                .exchange(Exchange.UPBIT)
                .symbol(symbol)
                .currency("KRW")
                .price(new BigDecimal(price))
                .volume(new BigDecimal(volume))
                .timestamp(Instant.now())
                .highPrice(new BigDecimal(highPrice))
                .lowPrice(new BigDecimal(lowPrice))
                .openPrice(new BigDecimal(openPrice))
                .changeRate(new BigDecimal(changeRate))
                .build();
        } catch (Exception e) {
            log.debug("Skipping Upbit message: {}", message);  // error -> debug로 변경
            return null;
        }
    }

    @Override
    public Flux<String> getAllMarkets() {
        return webClient.get()
            .uri("https://api.upbit.com/v1/market/all?isDetails=false")
            .retrieve()
            .bodyToFlux(String.class)
            .filter(market -> market.startsWith("KRW-"));
    }
} 