// 패키지: com.example.websocket.manager
package com.example.boot_redis_kafka_mysql.exchange.connection;

import com.example.boot_redis_kafka_mysql.exchange.handler.exchange.BinanceWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.handler.exchange.BithumbWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.handler.exchange.UpbitWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketManager {
    private static final Logger log = LoggerFactory.getLogger(WebSocketManager.class);

    private final StandardWebSocketClient client;
    private final Map<String, WebSocketSession> sessionMap = new HashMap<>();
    
    // 각 거래소별 핸들러 주입
    private final UpbitWebSocketHandler upbitHandler;
    private final BinanceWebSocketHandler binanceHandler;
    private final BithumbWebSocketHandler bithumbHandler;

    private static final Map<String, String> EXCHANGE_URLS = Map.of(
        "Upbit", "wss://api.upbit.com/websocket/v1",
        "Binance", "wss://stream.binance.com:9443/ws",
        "Bithumb", "wss://pubwss.bithumb.com/pub/ws"
    );

    private void connectExchange(String exchange) {
        try {
            WebSocketHandler handler = switch (exchange) {
                case "Upbit" -> upbitHandler;
                case "Binance" -> binanceHandler;
                case "Bithumb" -> bithumbHandler;
                default -> throw new IllegalArgumentException("Unknown exchange: " + exchange);
            };

            WebSocketSession session = client.execute(handler, EXCHANGE_URLS.get(exchange)).get();
            sessionMap.put(exchange, session);
            log.info("{} 웹소켓 연결 성공: {}", exchange, session.getId());

        } catch (Exception e) {
            log.error("{} 웹소켓 연결 실패: {}", exchange, e.getMessage(), e);
            // 5초 후 재시도
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    connectExchange(exchange);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private void initExchangeUrls() {
        EXCHANGE_URLS.keySet().forEach(this::connectExchange);
    }

    /**
     * 각 거래소의 웹소켓 연결을 시작합니다.
     */
    public void connectAll() {
        initExchangeUrls();
    }

    /**
     * 모든 연결을 종료합니다.
     */
    public void disconnectAll() {
        sessionMap.forEach((exchange, session) -> {
            try {
                session.close();
                log.info("{} 연결 종료", exchange);
            } catch (Exception e) {
                log.error("{} 연결 종료 실패: {}", exchange, e.getMessage(), e);
            }
        });
        sessionMap.clear();
    }
}
