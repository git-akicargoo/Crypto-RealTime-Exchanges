package com.example.boot_redis_kafka_mysql.exchange.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;
import com.example.boot_redis_kafka_mysql.exchange.handler.BinanceWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.model.dto.MarketPriceDTO;
import com.example.boot_redis_kafka_mysql.exchange.service.BinanceExchangeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BinanceExchangeServiceImpl implements BinanceExchangeService {
    private static final Logger log = LoggerFactory.getLogger(BinanceExchangeServiceImpl.class);
    
    private final BinanceWebSocketHandler handler;
    private final ObjectMapper objectMapper;
    private Flux<String> sharedMarketDataStream;

    @Override
    public Mono<Void> subscribeToSymbols(List<String> symbols, List<String> currencies) {
        try {
            // 바이낸스는 소문자로 된 스트림 이름을 사용
            List<String> streams = symbols.stream()
                .flatMap(symbol -> currencies.stream()
                    .map(currency -> String.format("%s%s@miniTicker", 
                        symbol.toLowerCase(), 
                        currency.toLowerCase())))  // miniTicker로 변경
                .collect(Collectors.toList());
            
            String subscribeMessage = String.format("""
                {
                    "method": "SUBSCRIBE",
                    "params": %s,
                    "id": 1
                }
                """, objectMapper.writeValueAsString(streams));
            
            log.info("Binance subscribe message: {}", subscribeMessage);
            
            return handler.subscribeToTicker(symbols, currencies)
                .doOnSuccess(unused -> {
                    this.sharedMarketDataStream = handler.getMarketDataStream().share();
                    log.info("Binance market data stream initialized for symbols: {}", symbols);
                })
                .doOnError(e -> log.error("Binance subscription error: ", e));
        } catch (JsonProcessingException e) {
            log.error("Failed to create Binance subscribe message", e);
            return Mono.error(e);
        }
    }

    @Override
    public Flux<MarketPriceDTO> getMarketDataStream() {
        if (sharedMarketDataStream == null) {
            return Flux.error(new IllegalStateException("Binance market data stream not initialized"));
        }
        return sharedMarketDataStream
            .mapNotNull(this::parseMarketData)  // Raw 메시지 로그 제거
            .doOnNext(data -> log.info("[Binance] {}-{}: {} (시간: {})", 
                data.getSymbol(), 
                data.getCurrency(), 
                data.getPrice(),
                data.getTimestamp()));
    }

    @Override
    public MarketPriceDTO parseMarketData(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            
            // 구독 응답은 무시
            if (node.has("result")) {
                return null;
            }

            // bookTicker 메시지 파싱
            return MarketPriceDTO.builder()
                .exchange(Exchange.BINANCE)
                .symbol(node.get("s").asText().replace("USDT", ""))
                .currency("USDT")
                .price(new BigDecimal(node.get("a").asText()))  // 매도 호가 사용
                .timestamp(Instant.now())
                .build();
        } catch (Exception e) {
            log.error("Failed to parse Binance message: {}", message, e);
            return null;
        }
    }
} 