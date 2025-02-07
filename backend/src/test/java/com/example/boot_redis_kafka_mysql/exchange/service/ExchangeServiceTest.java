package com.example.boot_redis_kafka_mysql.exchange.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.boot_redis_kafka_mysql.exchange.handler.BinanceWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.handler.BithumbWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.handler.UpbitWebSocketHandler;
import com.example.boot_redis_kafka_mysql.exchange.model.dto.MarketPriceDTO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExchangeServiceTest {
    private static final Logger log = LoggerFactory.getLogger(ExchangeServiceTest.class);

    @Autowired
    private BinanceExchangeService binanceService;

    @Autowired
    private UpbitExchangeService upbitService;

    @Autowired
    private BithumbExchangeService bithumbService;

    @Autowired
    private BinanceWebSocketHandler binanceHandler;

    @Autowired
    private UpbitWebSocketHandler upbitHandler;

    @Autowired
    private BithumbWebSocketHandler bithumbHandler;

    @BeforeAll
    static void setup() {
        // 테스트 시작 전 로그 레벨 설정
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // 루트 로거 설정
        loggerContext.getLogger("root").setLevel(Level.INFO);
        
        // 애플리케이션 패키지 로거 설정
        loggerContext.getLogger("com.example.boot_redis_kafka_mysql").setLevel(Level.INFO);
        
        // reactor.netty 로거 설정
        loggerContext.getLogger("reactor.netty").setLevel(Level.INFO);
        
        // 스프링 웹 로거 설정
        loggerContext.getLogger("org.springframework.web").setLevel(Level.INFO);
    }

    @Test
    void testAllExchangesRealTimeData() throws InterruptedException {
        // Given
        List<String> symbols = Arrays.asList("BTC");
        List<String> currencies = Arrays.asList("USDT");
        List<String> krwCurrencies = Arrays.asList("KRW");
        
        log.info("\n\n============ WebSocket 연결 시작 ============");
        
        // WebSocket 연결
        binanceHandler.connect().subscribe();
        upbitHandler.connect().subscribe();
        bithumbHandler.connect().subscribe();
        
        // 연결이 완료될 때까지 대기
        Thread.sleep(2000);
        
        log.info("============ 실시간 데이터 구독 시작 ============");
        
        // 먼저 구독 요청을 보내고
        binanceService.subscribeToSymbols(symbols, currencies).subscribe();
        upbitService.subscribeToSymbols(symbols, krwCurrencies).subscribe();
        bithumbService.subscribeToSymbols(symbols, krwCurrencies).subscribe();
        
        // 잠시 대기하여 스트림이 초기화되도록 함
        Thread.sleep(1000);
        
        // 그 다음 데이터 스트림을 구독
        binanceService.getMarketDataStream().subscribe();
        upbitService.getMarketDataStream().subscribe();
        bithumbService.getMarketDataStream().subscribe();
        
        log.info("============ 구독 완료, 실시간 데이터 수신 시작 ============");
        
        // 데이터 수신 대기
        Thread.sleep(30000);
    }

    @SuppressWarnings("unused")
    private void validateAndLogMarketData(MarketPriceDTO data) {
        assertNotNull(data.getExchange(), "거래소 정보가 없습니다");
        assertNotNull(data.getSymbol(), "심볼 정보가 없습니다");
        assertNotNull(data.getPrice(), "가격 정보가 없습니다");
        assertNotNull(data.getTimestamp(), "타임스탬프가 없습니다");
        
        log.info("[{}] {}-{}: {} (시간: {})", 
            data.getExchange(),
            data.getSymbol(),
            data.getCurrency(),
            data.getPrice(),
            data.getTimestamp());
    }
} 