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
public class BinanceWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(BinanceWebSocketHandler.class);
    private static final String SUBSCRIPTION_MESSAGE_FORMAT = 
        "{\"method\":\"SUBSCRIBE\",\"params\":[%s],\"id\":1}";
    
    private final MarketDataObserver dataObserver;
    private final ExchangeConfig config;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Binance WebSocket 연결 시도...");
        
        String symbolsString = config.getTargetSymbols().stream()
            .map(symbol -> "\"" + symbol.toLowerCase() + "usdt@ticker\"")
            .collect(Collectors.joining(","));
            
        String subscriptionMessage = String.format(SUBSCRIPTION_MESSAGE_FORMAT, symbolsString);
        
        session.sendMessage(new TextMessage(subscriptionMessage));
        log.info("Binance 구독 메시지 전송: {}", subscriptionMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("Binance 메시지 수신: {}", payload);
            
            if (payload.contains("error")) {
                log.warn("Binance error response: {}", payload);
                return;
            }
            
            dataObserver.onBinanceMessage(payload);
        } catch (Exception e) {
            log.error("Binance 메시지 처리 중 에러: ", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Binance 전송 에러: ", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.error("Binance 연결 종료: {}", status);
    }
}
