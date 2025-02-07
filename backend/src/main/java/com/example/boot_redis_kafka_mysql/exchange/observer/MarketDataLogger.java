package com.example.boot_redis_kafka_mysql.exchange.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeLogConfig;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.Data;
import lombok.AllArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketDataLogger {
    private static final Logger logger = LoggerFactory.getLogger(MarketDataLogger.class);
    private static final String FORMAT = "[%s] | %-8s | %-10s | %-15s | %-15s | %-10s |%n";
    private static final String HEADER = String.format("%-10s | %-10s | %-15s | %-15s | %-10s |", "거래소", "심볼", "현재가", "변동률", "거래량");
    private static final String LINE = "-".repeat(80);
    
    private final ExchangeLogConfig config;
    private final Map<String, Map<String, MarketData>> lastData = new ConcurrentHashMap<>();
    private boolean headerPrinted = false;

    @Scheduled(fixedRate = 5000)  // 5초마다 출력
    public void printMarketData() {
        if (lastData.isEmpty()) {
            return;
        }

        if (!headerPrinted) {
            System.out.println(LINE);
            System.out.println(HEADER);
            System.out.println(LINE);
            headerPrinted = true;
        }

        // 모든 거래소의 모든 코인 데이터를 한번에 출력
        lastData.forEach((exchange, symbolMap) -> {
            symbolMap.forEach((symbol, data) -> {
                System.out.printf(FORMAT,
                    data.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    exchange,
                    symbol,
                    data.getPrice(),
                    data.getChangeRate() + "%",
                    data.getVolume()
                );
            });
        });
        System.out.println(LINE);
    }

    public void onMarketData(String exchange, String symbol, String price, String changeRate, String volume) {
        if (!config.isEnabled()) return;

        // 헤더가 아직 출력되지 않았다면 출력
        if (!headerPrinted) {
            System.out.println(LINE);
            System.out.println(HEADER);
            System.out.println(LINE);
            headerPrinted = true;
        }

        // 데이터를 저장하고 즉시 출력
        lastData.computeIfAbsent(exchange, k -> new ConcurrentHashMap<>())
            .put(symbol, new MarketData(
                LocalDateTime.now(),
                price,
                changeRate,
                volume
            ));

        // 새로운 데이터 즉시 출력
        MarketData data = lastData.get(exchange).get(symbol);
        System.out.printf(FORMAT,
            data.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            exchange,
            symbol,
            data.getPrice(),
            data.getChangeRate() + "%",
            data.getVolume()
        );
    }

    public void logError(String exchange, String message, Exception e) {
        logger.error("{} - {}", exchange, message, e);
    }

    @Data
    @AllArgsConstructor
    private static class MarketData {
        private LocalDateTime timestamp;
        private String price;
        private String changeRate;
        private String volume;
    }
} 