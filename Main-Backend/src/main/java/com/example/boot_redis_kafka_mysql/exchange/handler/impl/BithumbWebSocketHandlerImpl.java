package com.example.boot_redis_kafka_mysql.exchange.handler.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.handler.BithumbWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.connection.WebSocketManager;
import com.example.boot_redis_kafka_mysql.exchange.config.ExchangeConfig.Exchange;
import com.example.boot_redis_kafka_mysql.exchange.model.vo.MarketSubscribeVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BithumbWebSocketHandlerImpl extends AbstractWebSocketHandler implements BithumbWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(BithumbWebSocketHandlerImpl.class);
    
    private final WebSocketManager webSocketManager;
    private WebSocketSession session;

    @PostConstruct
    public void init() {
        this.session = webSocketManager.createSession(Exchange.BITHUMB, this);
    }

    @Override
    public void subscribeToTicker(List<String> symbols, List<String> currencies) throws Exception {
        if (session != null && session.isOpen()) {
            MarketSubscribeVO subscribeVO = MarketSubscribeVO.builder()
                .exchange(Exchange.BITHUMB)
                .symbols(symbols)
                .currencies(currencies)
                .build();
                
            String message = subscribeVO.createSubscribeMessage();
            session.sendMessage(new TextMessage(message));
            log.info("Bithumb 구독 메시지 전송: {}", message);
        }
    }

    @Override
    public void handleMessage(String payload) {
        log.debug("Bithumb 메시지 수신: {}", payload);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        handleMessage(message.getPayload());
    }
} 