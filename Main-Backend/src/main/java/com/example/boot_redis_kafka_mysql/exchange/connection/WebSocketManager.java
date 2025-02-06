package com.example.boot_redis_kafka_mysql.exchange.connection;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
@RequiredArgsConstructor
public class WebSocketManager {
    private static final Logger log = LoggerFactory.getLogger(WebSocketManager.class);
    
    private final Map<ExchangeConfig.Exchange, WebSocketClient> clients = new ConcurrentHashMap<>();
    private final Map<ExchangeConfig.Exchange, Sinks.Many<String>> sinks = new ConcurrentHashMap<>();
    private final WebSocketClient webSocketClient;
    private final Map<ExchangeConfig.Exchange, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        Arrays.stream(ExchangeConfig.Exchange.values())
            .forEach(exchange -> sinks.put(exchange, Sinks.many().multicast().onBackpressureBuffer()));
    }

    public Mono<Void> createConnection(Exchange exchange, WebSocketHandler handler) {
        String url = getWebSocketUrl(exchange);
        log.info("{} 웹소켓 연결 시도: {}", exchange, url);
        
        return webSocketClient.execute(URI.create(url), session -> {
                sessions.put(exchange, session);
                log.info("{} 웹소켓 연결 성공", exchange);
                return handler.handle(session);
            })
            .doOnSubscribe(s -> log.debug("{} 웹소켓 연결 구독 시작", exchange))
            .doOnError(e -> {
                sessions.remove(exchange);
                log.error("{} 웹소켓 연결 실패: {}", exchange, e.getMessage());
            })
            .then();
    }

    public Flux<String> subscribe(ExchangeConfig.Exchange exchange) {
        return sinks.get(exchange).asFlux();
    }

    public Mono<Void> sendMessage(ExchangeConfig.Exchange exchange, String message) {
        WebSocketSession session = sessions.get(exchange);
        if (session == null) {
            return Mono.error(new IllegalStateException(exchange + " WebSocket session not found"));
        }
        return session.send(Mono.just(session.textMessage(message)));
    }

    // public void connectAll() {
    //     log.info("모든 거래소 웹소켓 연결 시작");
    //     Arrays.stream(ExchangeConfig.Exchange.values())
    //         .forEach(exchange -> createConnection(exchange, null).subscribe());
    // }

    public void disconnectAll() {
        clients.clear();
        sinks.values().forEach(sink -> sink.tryEmitComplete());
        log.info("모든 거래소 웹소켓 연결 종료");
    }

    private String getWebSocketUrl(Exchange exchange) {
        return switch (exchange) {
            case BINANCE -> "wss://stream.binance.com:9443/ws";
            case UPBIT -> "wss://api.upbit.com/websocket/v1";
            case BITHUMB -> "wss://pubwss.bithumb.com/pub/ws";
            default -> throw new IllegalArgumentException("Unsupported exchange: " + exchange);
        };
    }
}