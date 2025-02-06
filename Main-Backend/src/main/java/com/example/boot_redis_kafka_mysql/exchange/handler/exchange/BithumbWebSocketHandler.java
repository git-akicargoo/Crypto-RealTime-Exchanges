package com.example.boot_redis_kafka_mysql.exchange.handler.exchange;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.observer.MarketDataObserver;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BithumbWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(BithumbWebSocketHandler.class);
    private static final String SUBSCRIPTION_MESSAGE_FORMAT = 
        "{\"type\":\"ticker\",\"symbols\":[%s],\"tickTypes\":[\"24H\"]}";
    
    private final MarketDataObserver dataObserver;
    private final ExchangeConfig config;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String symbolsString = config.getTargetSymbols().stream()
            .map(symbol -> "\"" + symbol + "_KRW\"")
            .collect(Collectors.joining(","));
            
        String subscriptionMessage = String.format(SUBSCRIPTION_MESSAGE_FORMAT, symbolsString);
        
        session.sendMessage(new TextMessage(subscriptionMessage));
        log.info("Bithumb subscription sent: {}", subscriptionMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("Bithumb raw message: {}", payload);
            dataObserver.onBithumbMessage(payload);
        } catch (Exception e) {
            log.error("Bithumb message handling error", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Bithumb transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.error("Bithumb connection closed: {}", status);
    }
}

