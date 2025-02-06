package com.example.boot_redis_kafka_mysql.exchange.observer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class MarketDataObserver {
    private static final Logger log = LoggerFactory.getLogger(MarketDataObserver.class);
    private final ExchangeConfig config;
    private final MarketDataLogger logger;

    @PostConstruct
    public void init() {
        log.info("MarketDataObserver initialized with symbols: {}", config.getTargetSymbols());
    }

    public void onUpbitMessage(String payload) {
        try {
            log.debug("Processing Upbit message: {}", payload);
            JSONObject json = new JSONObject(payload);
            
            // 타입이 trade인 경우만 처리
            if (!"ticker".equals(json.getString("type"))) {
                return;
            }

            String code = json.getString("code");
            String symbol = code.replace("KRW-", "");
            
            if (isTargetSymbol(symbol)) {
                logger.onMarketData(
                    "Upbit",
                    symbol,
                    String.format("%,d", json.getLong("trade_price")),
                    String.format("%.2f", json.getDouble("signed_change_rate") * 100),
                    String.format("%.2f", json.getDouble("acc_trade_volume_24h"))
                );
            }
        } catch (Exception e) {
            log.error("Upbit message parsing error: {}", e.getMessage());
            log.debug("Error payload: {}", payload);
        }
    }

    public void onBinanceMessage(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            // 구독 응답 메시지 무시
            if (json.has("result")) {
                return;
            }
            
            String symbol = json.getString("s").replace("USDT", "");  // BTCUSDT -> BTC
            if (isTargetSymbol(symbol)) {
                logger.onMarketData(
                    "Binance",
                    symbol,
                    String.format("%,d", Math.round(json.getDouble("c"))),  // 현재가
                    String.format("%.2f", json.getDouble("P")),  // 변동률
                    String.format("%.2f", json.getDouble("v"))   // 거래량
                );
            }
        } catch (Exception e) {
            log.error("Binance parsing error", e);
        }
    }

    public void onBithumbMessage(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            if (!json.has("content")) return;  // 구독 응답 메시지 무시
            
            JSONObject content = json.getJSONObject("content");
            String symbol = content.getString("symbol").split("_")[0];
            
            if (isTargetSymbol(symbol)) {
                logger.onMarketData(
                    "Bithumb",
                    symbol,
                    String.format("%,d", Math.round(content.getDouble("closePrice"))),
                    String.format("%.2f", content.getDouble("chgRate")),
                    String.format("%.2f", content.getDouble("volume"))
                );
            }
        } catch (Exception e) {
            log.error("Bithumb parsing error", e);
        }
    }

    private boolean isTargetSymbol(String symbol) {
        return config.getTargetSymbols().contains(symbol);
    }
} 