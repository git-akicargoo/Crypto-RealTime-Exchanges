package com.example.boot_redis_kafka_mysql.exchange.handler.exchange;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.nio.charset.StandardCharsets;

@Component
public class UpbitWebSocketHandler extends AbstractWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpbitWebSocketHandler.class);
    private static final String FORMAT = "| %-8s | %-10s | %-15s | %-15s | %-10s |%n";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 모든 마켓의 실시간 데이터 구독
        String subscriptionMessage = "[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"ticker\",\"codes\":[\"ALL\"]}]";
        session.sendMessage(new BinaryMessage(subscriptionMessage.getBytes(StandardCharsets.UTF_8)));
        logger.info("Upbit - 구독 시작");
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);
        try {
            JSONObject json = new JSONObject(payload);
            if (json.has("type") && "ticker".equals(json.getString("type"))) {
                String symbol = json.getString("code").replace("KRW-", "");
                String price = String.format("%,d", json.getLong("trade_price"));
                String changeRate = String.format("%.2f", json.getDouble("signed_change_rate") * 100);
                String volume = String.format("%.2f", json.getDouble("acc_trade_volume_24h"));
                
                logger.info(String.format(FORMAT, 
                    "Upbit",
                    symbol,
                    price,
                    changeRate + "%",
                    volume
                ));
            }
        } catch (Exception e) {
            logger.error("Upbit 메시지 파싱 실패: {}", e.getMessage(), e);
        }
    }
}
