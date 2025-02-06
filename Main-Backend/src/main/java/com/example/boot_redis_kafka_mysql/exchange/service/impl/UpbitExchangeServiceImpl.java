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
            
            if (!node.has("type") || !"ticker".equals(node.get("type").asText())) {
                return null;
            }

            String marketCode = node.get("code").asText();  // "KRW-BTC"
            String[] parts = marketCode.split("-");  // ["KRW", "BTC"]
            String currency = parts[0];  // "KRW"
            String symbol = parts[1];    // "BTC"

            return MarketPriceDTO.builder()
                .exchange(Exchange.UPBIT)
                .symbol(symbol)
                .currency(currency)
                .price(new BigDecimal(node.get("trade_price").asText()))
                .volume(new BigDecimal(node.get("acc_trade_volume_24h").asText()))
                .timestamp(Instant.now())
                .highPrice(new BigDecimal(node.get("high_price").asText()))
                .lowPrice(new BigDecimal(node.get("low_price").asText()))
                .openPrice(new BigDecimal(node.get("opening_price").asText()))
                .changeRate(new BigDecimal(node.get("signed_change_rate").asText()))
                .build();
        } catch (Exception e) {
            log.debug("Skipping Upbit message: {}", message);
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