package com.example.boot_redis_kafka_mysql.web.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.boot_redis_kafka_mysql.exchange.handler.BinanceWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.handler.BithumbWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.handler.UpbitWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.service.BinanceExchangeService;
import com.example.boot_redis_kafka_mysql.exchange.service.BithumbExchangeService;
import com.example.boot_redis_kafka_mysql.exchange.service.UpbitExchangeService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CryptoWebSocketController {
    private final BinanceExchangeService binanceService;
    private final UpbitExchangeService upbitService;
    private final BithumbExchangeService bithumbService;
    private final SimpMessagingTemplate messagingTemplate;
    private final BinanceWebSocketHandler binanceHandler;
    private final UpbitWebSocketHandler upbitHandler;
    private final BithumbWebSocketHandler bithumbHandler;

    @PostConstruct
    public void init() {
        log.info("Starting WebSocket connections...");
        
        // 1. 거래소와 WebSocket 연결만 수립
        Mono.when(
            binanceHandler.connect(),
            upbitHandler.connect(),
            bithumbHandler.connect()
        )
        .doOnSuccess(v -> {
            log.info("All WebSocket connections established successfully");
        })
        .doOnError(e -> {
            log.error("Failed to establish WebSocket connections", e);
        })
        .subscribe();
    }

    @MessageMapping("/crypto/subscribe")
    public void handleSubscription(SubscriptionRequest request) {
        log.info("Client subscribed to crypto prices with request: {}", request);
        
        // 2. 클라이언트 요청에 따라 거래소 구독 시작
        subscribeToExchanges(request.getSymbols());
    }

    private void subscribeToExchanges(List<String> symbols) {
        log.info("Starting to subscribe to exchanges for symbols: {}", symbols);

        // Binance 구독 (USDT만)
        binanceService.subscribeToSymbols(symbols, Arrays.asList("USDT"))
            .doOnSuccess(v -> {
                log.info("Binance subscription successful");
                binanceService.getMarketDataStream()
                    .doOnNext(data -> {
                        log.debug("Sending Binance data: {}", data);
                        messagingTemplate.convertAndSend("/subscribe/crypto/prices", data);
                    })
                    .subscribe();
            })
            .subscribe();

        // Upbit 구독 (KRW만)
        upbitService.subscribeToSymbols(symbols, Arrays.asList("KRW"))
            .doOnSuccess(v -> {
                log.info("Upbit subscription successful");
                upbitService.getMarketDataStream()
                    .doOnNext(data -> {
                        log.debug("Sending Upbit data: {}", data);
                        messagingTemplate.convertAndSend("/subscribe/crypto/prices", data);
                    })
                    .subscribe();
            })
            .subscribe();

        // Bithumb 구독 (KRW만)
        bithumbService.subscribeToSymbols(symbols, Arrays.asList("KRW"))
            .doOnSuccess(v -> {
                log.info("Bithumb subscription successful");
                bithumbService.getMarketDataStream()
                    .doOnNext(data -> {
                        log.debug("Sending Bithumb data: {}", data);
                        messagingTemplate.convertAndSend("/subscribe/crypto/prices", data);
                    })
                    .subscribe();
            })
            .subscribe();
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class SubscriptionRequest {
    private List<String> symbols;
} 