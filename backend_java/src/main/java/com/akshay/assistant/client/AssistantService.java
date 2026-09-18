package com.akshay.assistant.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import com.akshay.assistant.tools.messaging.mcp.DiscordTools;
import com.akshay.assistant.tools.reminder.mcp.ReminderTools;

@Service
public class AssistantService {

    private final ChatClient chatClient;
    private final ReminderTools reminderTools;
    private final DiscordTools discordTools;

    public AssistantService(
            ChatClient chatClient,
            ReminderTools reminderTools,
            DiscordTools discordTools
    ) {
        this.chatClient = chatClient;
        this.reminderTools = reminderTools;
        this.discordTools = discordTools;
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
                .options(
                        OllamaChatOptions.builder()
                                .model("qwen2.5:3b")
                                .build()
                )
                .tools(reminderTools, discordTools)
                .call()
                .content();
    }
}