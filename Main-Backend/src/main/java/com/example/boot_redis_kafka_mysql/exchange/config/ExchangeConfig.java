package com.example.boot_redis_kafka_mysql.exchange.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "exchange")
@Getter
@Setter
public class ExchangeConfig {
    private List<String> targetSymbols = new ArrayList<>();
} 