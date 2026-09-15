package com.akshay.assistant.tools.messaging;
import com.akshay.assistant.tools.messaging.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscordChatStateTest {

    @Test
    void consumesUnreadMessages() {
        DiscordChatState state = new DiscordChatState();

        Chat chat = new Chat("1", "Rahul", ChatType.INDIVIDUAL, List.of());
        Message message = new Message(
                "m1",
                new Person("u1", true, "Rahul", "rahul"),
                "Hi",
                Instant.now(),
                Direction.INCOMING
        );

        state.addIncoming(chat.chatId(), message);

        assertEquals(List.of(message), state.consumeUnread(chat.chatId()));
        assertTrue(state.consumeUnread(chat.chatId()).isEmpty());
    }

    @Test
    void opensAndClosesConversation() {
        DiscordChatState state = new DiscordChatState();
        Chat chat = new Chat("1", "general", ChatType.GROUP, List.of());

        state.open(chat, Instant.now());
        assertNotNull(state.activeConversation());

        state.close();
        assertNull(state.activeConversation());
    }
}
