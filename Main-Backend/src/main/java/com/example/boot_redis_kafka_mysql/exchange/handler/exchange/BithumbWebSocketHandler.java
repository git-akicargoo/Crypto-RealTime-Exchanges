package com.example.boot_redis_kafka_mysql.exchange.handler.exchange;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.example.boot_redis_kafka_mysql.exchange.handler.base.AbstractExchangeWebSocketHandler;

@Component
public class BithumbWebSocketHandler extends AbstractExchangeWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(BithumbWebSocketHandler.class);
    
    @Override
    protected String getExchangeName() {
        return "Bithumb";
    }

    @Override
    protected String createSubscriptionMessage() {
        return "{\"type\":\"ticker\",\"symbols\":[\"ALL\"],\"tickTypes\":[\"24H\"]}";
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JSONObject json = new JSONObject(message.getPayload());
            if (json.has("content")) {
                JSONObject content = json.getJSONObject("content");
                String symbol = content.getString("symbol").split("_")[0];
                String price = String.format("%,d", content.getLong("closePrice"));
                String changeRate = String.format("%.2f", content.getDouble("chgRate"));
                String volume = String.format("%.2f", content.getDouble("volume"));
                
                printMarketData(symbol, price, changeRate, volume);
            }
        } catch (Exception e) {
            logger.error("Bithumb 메시지 파싱 실패: {}", e.getMessage(), e);
        }
    }
}

