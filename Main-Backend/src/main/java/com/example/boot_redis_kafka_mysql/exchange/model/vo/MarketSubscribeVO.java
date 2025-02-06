package com.example.boot_redis_kafka_mysql.exchange.model.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.stream.Collectors;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;

@Getter
@Builder
@EqualsAndHashCode
public class MarketSubscribeVO {
    private final Exchange exchange;        // 거래소 (BINANCE, UPBIT, BITHUMB)
    private final List<String> symbols;     // 구독할 심볼 목록 (BTC, ETH 등)
    private final List<String> currencies;  // 기준 통화 목록 (KRW, USDT, BTC 등)

    public String createSubscribeMessage() {
        switch (exchange) {
            case BINANCE:
                return createBinanceMessage();
            case UPBIT:
                return createUpbitMessage();
            case BITHUMB:
                return createBithumbMessage();
            default:
                throw new IllegalArgumentException("Unsupported exchange: " + exchange);
        }
    }

    private String createBinanceMessage() {
        String symbolsString = symbols.stream()
            .flatMap(symbol -> currencies.stream()
                .map(currency -> "\"" + symbol.toLowerCase() + currency.toLowerCase() + "@ticker\""))
            .collect(Collectors.joining(","));
        return String.format("{\"method\":\"SUBSCRIBE\",\"params\":[%s],\"id\":1}", symbolsString);
    }

    private String createUpbitMessage() {
        String symbolsString = symbols.stream()
            .flatMap(symbol -> currencies.stream()
                .map(currency -> "\"" + currency + "-" + symbol.toUpperCase() + "\""))
            .collect(Collectors.joining(","));
        return String.format("[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"ticker\",\"codes\":[%s]}]", symbolsString);
    }

    private String createBithumbMessage() {
        String symbolsString = symbols.stream()
            .flatMap(symbol -> currencies.stream()
                .map(currency -> symbol.toUpperCase() + "_" + currency))
            .collect(Collectors.joining("\",\""));
        return String.format("{\"type\":\"ticker\",\"symbols\":[\"%s\"],\"tickTypes\":[\"24H\"]}", symbolsString);
    }
} 