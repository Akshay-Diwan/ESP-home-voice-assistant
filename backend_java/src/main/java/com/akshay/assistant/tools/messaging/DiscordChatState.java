package com.akshay.assistant.tools.messaging;

import com.akshay.assistant.tools.messaging.model.ActiveConversation;
import com.akshay.assistant.tools.messaging.model.Chat;
import com.akshay.assistant.tools.messaging.model.Message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class DiscordChatState {

    private volatile ActiveConversation activeConversation;

    private final Map<String, List<Message>> unreadMessages = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastSeen = new ConcurrentHashMap<>();

    public ActiveConversation activeConversation() {
        return activeConversation;
    }

    public synchronized void open(Chat chat, Instant now) {
        activeConversation = new ActiveConversation(chat, now, now);
        lastSeen.put(chat.chatId(), now);
    }

    public synchronized void touch() {
        if (activeConversation == null) return;
        activeConversation = new ActiveConversation(
                activeConversation.chat(),
                activeConversation.openedAt(),
                Instant.now()
        );
    }

    public synchronized void close() {
        activeConversation = null;
    }

    public synchronized void addIncoming(String chatId, Message message) {
        unreadMessages.computeIfAbsent(chatId, ignored -> new ArrayList<>()).add(message);
    }

    public synchronized List<Message> consumeUnread(String chatId) {
        List<Message> messages = unreadMessages.remove(chatId);
        return messages == null ? List.of() : List.copyOf(messages);
    }

    public synchronized List<String> unreadChatNames(Map<String, Chat> knownChats) {
        return unreadMessages.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> knownChats.getOrDefault(e.getKey(),
                        new Chat(e.getKey(), e.getKey(), null, List.of())).chatName())
                .toList();
    }

    public synchronized Instant lastSeen(String chatId) {
        return lastSeen.get(chatId);
    }

    public synchronized void markSeen(String chatId, Instant timestamp) {
        lastSeen.put(chatId, timestamp);
    }
}
