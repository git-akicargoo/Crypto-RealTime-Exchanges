package com.example.boot_redis_kafka_mysql.exchange.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "exchange.log")
@Getter
@Setter
public class ExchangeLogConfig {
    private boolean enabled = true;  // 기본값을 true로 설정
    private boolean debugEnabled = false;  // 상세 로그 여부
    private String[] targetSymbols = {"BTC", "ETH"};  // 기본 타겟 심볼 설정
} 