package com.akshay.assistant.tools.messaging.model;

import java.util.List;

public record UnreadChatsResponse(
        String status,
        int count,
        List<String> chatList,
        String error,
        String message
) {
    public static UnreadChatsResponse success(List<String> chats) {
        return new UnreadChatsResponse("success", chats.size(), List.copyOf(chats), null, null);
    }

    public static UnreadChatsResponse error(String error, String message) {
        return new UnreadChatsResponse("error", 0, List.of(), error, message);
    }
}
