package com.akshay.assistant.tools.messaging;

import com.akshay.assistant.tools.messaging.model.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/discord")
public class DiscordChatController {

    private final DiscordChatTool discordChatTool;

    public DiscordChatController(DiscordChatTool discordChatTool) {
        this.discordChatTool = discordChatTool;
    }

    // ============================================================
    // SEND MESSAGE
    // ============================================================

    @PostMapping("/messages")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestParam String message,
            @RequestParam(required = false) String chatName
    ) {
        ChatResponse response =
                discordChatTool.sendMessage(message, chatName);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // LIST UNREAD CHATS
    // ============================================================

    @GetMapping("/chats/unread")
    public ResponseEntity<UnreadChatsResponse> listUnreadChats() {

        UnreadChatsResponse response =
                discordChatTool.listUnreadChats();

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // GET NEW / UNREAD MESSAGES
    // ============================================================

    @GetMapping("/chats/{chatName}/messages/new")
    public ResponseEntity<NewMessagesResponse> getNewMessages(
            @PathVariable String chatName
    ) {

        NewMessagesResponse response =
                discordChatTool.getNewMessages(chatName);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // OPEN CONVERSATION
    // ============================================================

    @PostMapping("/conversations/open")
    public ResponseEntity<ChatResponse> openConversation(
            @RequestParam String chatName
    ) {

        ChatResponse response =
                discordChatTool.openConversation(chatName);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // CLOSE CONVERSATION
    // ============================================================

    @PostMapping("/conversations/close")
    public ResponseEntity<ChatResponse> closeConversation() {

        ChatResponse response =
                discordChatTool.closeConversation();

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // GET CONVERSATION HISTORY
    // ============================================================

    @GetMapping("/chats/{chatName}/history")
    public ResponseEntity<ConversationHistoryResponse> getConversationHistory(
            @PathVariable String chatName,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime
    ) {

        ConversationHistoryResponse response =
                discordChatTool.getConversationHistory(
                        chatName,
                        limit,
                        startTime,
                        endTime
                );

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // GET ACTIVE CONVERSATION
    // ============================================================

    @GetMapping("/conversations/active")
    public ResponseEntity<ActiveConversation> getActiveConversation() {

        ActiveConversation active =
                discordChatTool.getActiveConversation();

        if (active == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(active);
    }
}

