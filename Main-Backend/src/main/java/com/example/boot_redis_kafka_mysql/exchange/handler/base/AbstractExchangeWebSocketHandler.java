package com.example.boot_redis_kafka_mysql.exchange.handler.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public abstract class AbstractExchangeWebSocketHandler extends TextWebSocketHandler {
    protected static final Logger log = LoggerFactory.getLogger(AbstractExchangeWebSocketHandler.class);
    private static final String FORMAT = "| %-8s | %-10s | %-15s | %-15s | %-10s |%n";
    private static final String HEADER = String.format(FORMAT, "거래소", "심볼", "현재가", "변동률", "거래량");
    private static final String LINE = "-".repeat(70);

    protected AbstractExchangeWebSocketHandler() {
        if ("Upbit".equals(getExchangeName())) {
            log.info("\n" + LINE);
            log.info(HEADER);
            log.info(LINE);
        }
    }

    protected void printMarketData(String symbol, String price, String changeRate, String volume) {
        log.info(String.format(FORMAT, 
            getExchangeName(),
            symbol,
            price,
            changeRate + "%",
            volume
        ));
    }

    /**
     * 각 거래소별로 구독 메시지를 생성하는 메서드.
     * 예) JSON 형식의 구독 요청 메시지.
     */
    protected abstract String createSubscriptionMessage();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 연결이 성공하면 구독 메시지 전송
        String subscriptionMessage = createSubscriptionMessage();
        session.sendMessage(new TextMessage(subscriptionMessage));
        log.info("{} - 구독 메시지 전송: {}", getExchangeName(), subscriptionMessage);
    }
    
    /**
     * 거래소 이름을 리턴 (로그용)
     */
    protected abstract String getExchangeName();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 각 거래소의 메시지 처리 로직 구현 (필요에 따라 오버라이드)
        String payload = message.getPayload();
        log.debug("{} - 수신 메시지: {}", getExchangeName(), payload);
        // 메시지 파싱 및 필요한 후속 처리 로직 추가 가능
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("{} - 전송 에러: {}", getExchangeName(), exception.getMessage(), exception);
    }
}
