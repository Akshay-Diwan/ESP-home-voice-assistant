package com.akshay.assistant.tools.messaging.model;

import java.util.List;

public record NewMessagesResponse(
        String status,
        ChatType type,
        int count,
        List<Message> messageList,
        String error,
        String message
) {
    public static NewMessagesResponse success(ChatType type, List<Message> messages) {
        return new NewMessagesResponse("success", type, messages.size(), List.copyOf(messages), null, null);
    }

    public static NewMessagesResponse empty(ChatType type) {
        return success(type, List.of());
    }

    public static NewMessagesResponse error(String error, String message) {
        return new NewMessagesResponse("error", null, 0, List.of(), error, message);
    }
}
