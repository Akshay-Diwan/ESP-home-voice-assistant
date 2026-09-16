package com.akshay.assistant.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(
             ChatClient.Builder builder,
             ToolCallbackProvider toolCallbackProvider
    ) {
         return builder
                 .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                 .build();
     }
}