// 패키지: com.example.websocket.manager
package com.example.boot_redis_kafka_mysql.exchange.connection;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketManager {
    private static final Logger log = LoggerFactory.getLogger(WebSocketManager.class);
    
    private final Map<ExchangeConfig.Exchange, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public WebSocketSession createSession(ExchangeConfig.Exchange exchange, AbstractWebSocketHandler handler) {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketSession session = client.execute(handler, exchange.getWebsocketUrl()).get();
            sessions.put(exchange, session);
            log.info("{} 웹소켓 연결 성공", exchange);
            return session;
        } catch (Exception e) {
            log.error("{} 웹소켓 연결 실패", exchange, e);
            return null;
        }
    }

    public void connectAll() {
        log.info("모든 거래소 웹소켓 연결 시작");
    }

    public void disconnectAll() {
        sessions.forEach((exchange, session) -> {
            try {
                if (session.isOpen()) {
                    session.close();
                    log.info("{} 웹소켓 연결 종료", exchange);
                }
            } catch (Exception e) {
                log.error("{} 웹소켓 연결 종료 중 에러", exchange, e);
            }
        });
        sessions.clear();
    }
}