package com.example.boot_redis_kafka_mysql.exchange.handler.exchange;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.example.boot_redis_kafka_mysql.exchange.handler.base.AbstractExchangeWebSocketHandler;

@Component
public class BinanceWebSocketHandler extends AbstractExchangeWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(BinanceWebSocketHandler.class);
    
    @Override
    protected String getExchangeName() {
        return "Binance";
    }

    @Override
    protected String createSubscriptionMessage() {
        // 모든 USDT 마켓의 티커 정보 구독
        return "{\"method\":\"SUBSCRIBE\",\"params\":[\"!ticker@arr\"],\"id\":1}";
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JSONObject json = new JSONObject(message.getPayload());
            if (json.has("e") && "24hrTicker".equals(json.getString("e"))) {
                String symbol = json.getString("s").replace("USDT", "");
                String price = String.format("%,d", Math.round(json.getDouble("c")));
                String changeRate = String.format("%.2f", json.getDouble("P"));
                String volume = String.format("%.2f", json.getDouble("v"));
                
                printMarketData(symbol, price, changeRate, volume);
            }
        } catch (Exception e) {
            logger.error("Binance 메시지 파싱 실패: {}", e.getMessage(), e);
        }
    }
}
