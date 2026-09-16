package com.akshay.assistant.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private final ChatClient chatClient;

    public AssistantService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String message) {

        return chatClient
                .prompt()
                .system("""
                        You are a personal assistant.

                        You have access to tools for:
                        - reminders
                        - Discord
                        - calendar
                        - notes
                        - other connected services

                        Use tools when necessary.

                        Never claim that an action was completed unless
                        the corresponding tool successfully completed.
                        """)
                .user(message)
                .call()
                .content();
    }
}