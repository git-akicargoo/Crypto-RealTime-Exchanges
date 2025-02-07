package com.example.boot_redis_kafka_mysql.exchange.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.Instant;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;

@Getter
@Builder
@ToString
public class MarketPriceDTO {
    private final Exchange exchange;     // 거래소
    private final String symbol;         // 심볼 (BTC, ETH 등)
    private final String currency;       // 기준 통화 (KRW, USDT 등)
    private final BigDecimal price;      // 현재가
    private final BigDecimal volume;     // 거래량
    private final Instant timestamp;     // 타임스탬프
    private final BigDecimal highPrice;  // 고가
    private final BigDecimal lowPrice;   // 저가
    private final BigDecimal openPrice;  // 시가
    private final BigDecimal changeRate; // 변동률
} 