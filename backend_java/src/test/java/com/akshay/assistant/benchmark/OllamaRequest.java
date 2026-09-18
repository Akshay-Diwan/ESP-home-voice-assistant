package com.akshay.assistant.benchmark;

import java.util.List;
import java.util.Map;

public record OllamaRequest(
        String model,
        List<Message> messages,
        List<Tool> tools,
        boolean stream
) {
    public record Message(String role, String content) {
        public static Message user(String content) {
            return new Message("user", content);
        }
    }

    public record Tool(
            String type,
            Function function
    ) {
        public record Function(
                String name,
                String description,
                Map<String, Object> parameters
        ) {}

        public static Tool function(String name, String description, Map<String, Object> parameters) {
            return new Tool("function", new Function(name, description, parameters));
        }
    }
}