package com.example.boot_redis_kafka_mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.boot_redis_kafka_mysql.exchange.connection.WebSocketManager;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
@EnableConfigurationProperties
public class BootRedisKafkaMysqlApplication {
	private static final Logger log = LoggerFactory.getLogger(BootRedisKafkaMysqlApplication.class);

	@Autowired
	private WebSocketManager webSocketManager;

	public static void main(String[] args) {
		SpringApplication.run(BootRedisKafkaMysqlApplication.class, args);
	}

	// @Bean
	// public CommandLineRunner run() {
	// 	return args -> {
	// 		// 애플리케이션 시작 시 모든 거래소에 연결을 시작
	// 		webSocketManager.connectAll();
			
	// 		// 종료 hook 등록
	// 		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	// 			log.info("애플리케이션 종료 중... 웹소켓 연결을 종료합니다.");
	// 			webSocketManager.disconnectAll();
	// 		}));
	// 	};
	// }

	@PreDestroy
	public void onShutdown() {
		log.info("Spring 컨텍스트 종료 중... 웹소켓 연결을 종료합니다.");
		webSocketManager.disconnectAll();
	}
}
