package com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
    scanBasePackages = {
        "com.stocks", 
        "com.externalServices" // if you have another root
    }
)
@EnableJpaRepositories(basePackages = {"com.stocks.repo", "com.externalServices.repo"})
@EntityScan(basePackages = {"com.stocks.data", "com.externalServices.data"})
public class StockApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockApplication.class, args);
	}

}
