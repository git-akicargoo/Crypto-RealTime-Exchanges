package com.example.boot_redis_kafka_mysql.exchange.handler.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.handler.UpbitWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.connection.WebSocketManager;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;
import com.example.boot_redis_kafka_mysql.exchange.model.vo.MarketSubscribeVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class UpbitWebSocketHandlerImpl extends AbstractWebSocketHandler implements UpbitWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(UpbitWebSocketHandlerImpl.class);
    
    private final WebSocketManager webSocketManager;
    private WebSocketSession session;

    @PostConstruct
    public void init() {
        this.session = webSocketManager.createSession(Exchange.UPBIT, this);
    }

    @Override
    public void subscribeToTicker(List<String> symbols, List<String> currencies) throws Exception {
        if (session != null && session.isOpen()) {
            MarketSubscribeVO subscribeVO = MarketSubscribeVO.builder()
                .exchange(Exchange.UPBIT)
                .symbols(symbols)
                .currencies(currencies)
                .build();
                
            String message = subscribeVO.createSubscribeMessage();
            session.sendMessage(new BinaryMessage(message.getBytes(StandardCharsets.UTF_8)));
            log.info("Upbit 구독 메시지 전송: {}", message);
        }
    }

    @Override
    public void handleMessage(String payload) {
        log.debug("Upbit 메시지 수신: {}", payload);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);
        handleMessage(payload);
    }
}