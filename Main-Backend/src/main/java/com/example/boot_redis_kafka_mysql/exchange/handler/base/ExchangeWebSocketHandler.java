package com.example.boot_redis_kafka_mysql.exchange.handler.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public abstract class ExchangeWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ExchangeWebSocketHandler.class);
    private final String exchangeName;
    private static final String FORMAT = "| %-8s | %-10s | %-15s | %-15s | %-10s |%n";
    private static final String HEADER = String.format(FORMAT, "거래소", "심볼", "현재가", "변동률", "거래량");
    private static final String LINE = "-".repeat(70);

    public ExchangeWebSocketHandler(String exchangeName) {
        this.exchangeName = exchangeName;
        if ("Upbit".equals(exchangeName)) {
            log.info("\n" + LINE);
            log.info(HEADER);
            log.info(LINE);
        }
    }

    public String getExchangeName() {
        return exchangeName;
    }

    protected abstract String createSubscriptionMessage();

    protected void printMarketData(String symbol, String price, String changeRate, String volume) {
        log.info(String.format(FORMAT, 
            exchangeName,
            symbol,
            price,
            changeRate + "%",
            volume
        ));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("{} 연결 설정됨", exchangeName);
        String subscriptionMessage = createSubscriptionMessage();
        session.sendMessage(new TextMessage(subscriptionMessage));
        log.info("{} - 구독 메시지 전송: {}", exchangeName, subscriptionMessage);
    }

    @Override
    public abstract void handleTextMessage(WebSocketSession session, TextMessage message);

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("{} 연결 종료됨", exchangeName);
    }
} 