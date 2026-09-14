package com.akshay.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@IntegrationComponentScan(basePackages = "com.akshay.assistant.gateway")
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
