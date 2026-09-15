package com.akshay.assistant.tools.messaging.model;

import java.util.List;

public record ConversationHistoryResponse(
        String status,
        int count,
        List<Message> messageList,
        String error,
        String message
) {
    public static ConversationHistoryResponse success(List<Message> messages) {
        return new ConversationHistoryResponse("success", messages.size(), List.copyOf(messages), null, null);
    }

    public static ConversationHistoryResponse error(String error, String message) {
        return new ConversationHistoryResponse("error", 0, List.of(), error, message);
    }
}
