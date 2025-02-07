package com.example.boot_redis_kafka_mysql.exchange.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;
import com.example.boot_redis_kafka_mysql.exchange.handler.BithumbWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.model.dto.MarketPriceDTO;
import com.example.boot_redis_kafka_mysql.exchange.service.BithumbExchangeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BithumbExchangeServiceImpl implements BithumbExchangeService {
    private static final Logger log = LoggerFactory.getLogger(BithumbExchangeServiceImpl.class);
    
    private final BithumbWebSocketHandler handler;
    private final ObjectMapper objectMapper;
    private Flux<String> sharedMarketDataStream;

    @Override
    public Mono<Void> subscribeToSymbols(List<String> symbols, List<String> currencies) {
        return handler.subscribeToTicker(symbols, currencies)
            .doOnSuccess(unused -> {
                this.sharedMarketDataStream = handler.getMarketDataStream().share();
                log.info("Bithumb market data stream initialized for symbols: {}", symbols);
            })
            .doOnError(e -> log.error("Bithumb subscription error: ", e));
    }

    @Override
    public Flux<MarketPriceDTO> getMarketDataStream() {
        if (sharedMarketDataStream == null) {
            return Flux.error(new IllegalStateException("Bithumb market data stream not initialized"));
        }
        return sharedMarketDataStream
            .map(this::parseMarketData)
            .filter(Objects::nonNull)
            .doOnNext(data -> log.info("[Bithumb] {}-{}: {} (시간: {})", 
                data.getSymbol(), 
                data.getCurrency(), 
                data.getPrice(),
                data.getTimestamp()));
    }

    @Override
    public MarketPriceDTO parseMarketData(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            
            if (node.has("status") || !node.has("type") || !"ticker".equals(node.get("type").asText())) {
                return null;
            }

            JsonNode content = node.get("content");
            String marketSymbol = content.get("symbol").asText();  // "BTC_KRW"
            String[] parts = marketSymbol.split("_");  // ["BTC", "KRW"]
            String symbol = parts[0];  // "BTC"
            String currency = parts[1];  // "KRW"

            return MarketPriceDTO.builder()
                .exchange(Exchange.BITHUMB)
                .symbol(symbol)
                .currency(currency)
                .price(new BigDecimal(content.get("closePrice").asText()))
                .volume(new BigDecimal(content.get("volume").asText()))
                .timestamp(Instant.now())
                .highPrice(new BigDecimal(content.get("highPrice").asText()))
                .lowPrice(new BigDecimal(content.get("lowPrice").asText()))
                .openPrice(new BigDecimal(content.get("openPrice").asText()))
                .changeRate(new BigDecimal(content.get("chgRate").asText()).divide(BigDecimal.valueOf(100)))
                .build();
        } catch (Exception e) {
            log.debug("Skipping Bithumb message: {}", message);
            return null;
        }
    }
} 