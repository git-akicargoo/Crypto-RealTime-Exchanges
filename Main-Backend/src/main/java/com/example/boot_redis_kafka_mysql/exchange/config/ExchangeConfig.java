package com.example.boot_redis_kafka_mysql.exchange.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import java.util.List;
import java.util.ArrayList;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "exchange")
@Getter
public class ExchangeConfig {
    private List<String> targetSymbols = new ArrayList<>();

    public enum Exchange {
        BINANCE("wss://stream.binance.com:9443/ws"),
        UPBIT("wss://api.upbit.com/websocket/v1"),
        BITHUMB("wss://pubwss.bithumb.com/pub/ws");

        private final String websocketUrl;

        Exchange(String websocketUrl) {
            this.websocketUrl = websocketUrl;
        }

        public String getWebsocketUrl() {
            return websocketUrl;
        }
    }
} 