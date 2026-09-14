package com.akshay.assistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NotionConfig {

    @Bean
    RestClient notionRestClient(
            @Value("${notion.version}") String apiVersion,
            @Value("${notion.token}") String token
    ) {
        return RestClient.builder()
                .baseUrl("https://api.notion.com/v1")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Notion-Version", apiVersion)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}