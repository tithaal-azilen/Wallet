package com.Tithaal.Wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
@EnableRetry
@EnableJpaRepositories(basePackages = "com.Tithaal.Wallet.repository")
@EnableRedisRepositories(basePackages = "com.Tithaal.Wallet.redis")
public class WalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletApplication.class, args);
	}

}
