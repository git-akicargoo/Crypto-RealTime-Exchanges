package com.example.boot_redis_kafka_mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.boot_redis_kafka_mysql.exchange.connection.WebSocketManager;

@SpringBootApplication
public class BootRedisKafkaMysqlApplication {

	@Autowired
	private WebSocketManager webSocketManager;

	public static void main(String[] args) {
		SpringApplication.run(BootRedisKafkaMysqlApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
			return args -> {
					// 애플리케이션 시작 시 모든 거래소에 연결을 시작
					webSocketManager.connectAll();

					// 필요시 shutdown hook이나 별도의 스케줄러로 disconnectAll() 호출
			};
	}
}
