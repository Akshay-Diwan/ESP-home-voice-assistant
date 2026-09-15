package com.akshay.assistant.tools.messaging.model;

public record ChatResponse(
        String status,
        String error,
        String message
) {
    public static ChatResponse success() {
        return new ChatResponse("success", null, null);
    }

    public static ChatResponse success(String message) {
        return new ChatResponse("success", null, message);
    }

    public static ChatResponse error(String error, String message) {
        return new ChatResponse("error", error, message);
    }
}
