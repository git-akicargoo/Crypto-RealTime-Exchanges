package com.example.boot_redis_kafka_mysql.exchange.handler.exchange;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.observer.MarketDataObserver;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import java.util.stream.Collectors;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpbitWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(UpbitWebSocketHandler.class);
    private static final String SUBSCRIPTION_MESSAGE_FORMAT = 
        "[{\"ticket\":\"%s\"},{\"type\":\"ticker\",\"codes\":[%s]}]";
    
    private final MarketDataObserver dataObserver;
    private final ExchangeConfig config;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Upbit WebSocket 연결 시도...");
        
        String symbolsString = config.getTargetSymbols().stream()
            .map(symbol -> "\"KRW-" + symbol + "\"")
            .collect(Collectors.joining(","));
            
        String subscriptionMessage = String.format(SUBSCRIPTION_MESSAGE_FORMAT,
            UUID.randomUUID().toString(),
            symbolsString);
            
        session.sendMessage(new BinaryMessage(subscriptionMessage.getBytes()));
        log.info("Upbit 구독 메시지 전송: {}", subscriptionMessage);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            String payload = new String(message.getPayload().array());
            log.debug("Upbit 메시지 수신: {}", payload);
            dataObserver.onUpbitMessage(payload);
        } catch (Exception e) {
            log.error("Upbit 메시지 처리 중 에러: ", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Upbit WebSocket 전송 에러 발생", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.error("Upbit 연결 종료: {}", status);
    }
}
