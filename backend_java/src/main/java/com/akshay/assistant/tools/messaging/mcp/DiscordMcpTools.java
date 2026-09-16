package com.akshay.assistant.tools.messaging.mcp;

import com.akshay.assistant.tools.messaging.DiscordChatTool;
import com.akshay.assistant.tools.messaging.model.ActiveConversation;
import com.akshay.assistant.tools.messaging.model.ChatResponse;
import com.akshay.assistant.tools.messaging.model.ConversationHistoryResponse;
import com.akshay.assistant.tools.messaging.model.NewMessagesResponse;
import com.akshay.assistant.tools.messaging.model.UnreadChatsResponse;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DiscordMcpTools {

    private final DiscordChatTool discordChatTool;

    public DiscordMcpTools(DiscordChatTool discordChatTool) {
        this.discordChatTool = discordChatTool;
    }

    // ============================================================
    // SEND MESSAGE
    // ============================================================

    @Tool(
        name = "send_discord_message",
        description = """
            Send a text message to a Discord conversation.
            If chatName is not provided, the currently active
            conversation will be used.
            """
    )
    public ChatResponse sendMessage(
            @ToolParam(
                description = "The text message to send"
            )
            String message,

            @ToolParam(
                description = """
                    The Discord contact or chat name.
                    Leave empty to use the currently active conversation.
                    """,
                required = false
            )
            String chatName
    ) {

        return discordChatTool.sendMessage(
                message,
                chatName
        );
    }

    // ============================================================
    // LIST UNREAD CHATS
    // ============================================================

    @Tool(
        name = "list_unread_discord_chats",
        description = """
            List Discord conversations that have unread messages.
            """
    )
    public UnreadChatsResponse listUnreadChats() {

        return discordChatTool.listUnreadChats();
    }

    // ============================================================
    // GET NEW MESSAGES
    // ============================================================

    @Tool(
        name = "get_new_discord_messages",
        description = """
            Get and consume unread messages from a Discord
            conversation.
            """
    )
    public NewMessagesResponse getNewMessages(
            @ToolParam(
                description = "The Discord contact or chat name"
            )
            String chatName
    ) {

        return discordChatTool.getNewMessages(chatName);
    }

    // ============================================================
    // OPEN CONVERSATION
    // ============================================================

    @Tool(
        name = "open_discord_conversation",
        description = """
            Open a Discord conversation and make it the
            currently active conversation.
            """
    )
    public ChatResponse openConversation(
            @ToolParam(
                description = "The Discord contact or chat name"
            )
            String chatName
    ) {

        return discordChatTool.openConversation(chatName);
    }

    // ============================================================
    // CLOSE CONVERSATION
    // ============================================================

    @Tool(
        name = "close_discord_conversation",
        description = """
            Close the currently active Discord conversation.
            """
    )
    public ChatResponse closeConversation() {

        return discordChatTool.closeConversation();
    }

    // ============================================================
    // GET ACTIVE CONVERSATION
    // ============================================================

    @Tool(
        name = "get_active_discord_conversation",
        description = """
            Get the currently active Discord conversation.
            """
    )
    public ActiveConversation getActiveConversation() {

        return discordChatTool.getActiveConversation();
    }

    // ============================================================
    // GET CONVERSATION HISTORY
    // ============================================================

    @Tool(
        name = "get_discord_conversation_history",
        description = """
            Retrieve the conversation history from a Discord chat.
            You can optionally specify a message limit and time range.
            """
    )
    public ConversationHistoryResponse getConversationHistory(

            @ToolParam(
                description = "The Discord contact or chat name"
            )
            String chatName,

            @ToolParam(
                description = "Maximum number of messages to retrieve",
                required = false
            )
            Integer limit,

            @ToolParam(
                description = """
                    Start time in ISO-8601 format.
                    Example: 2026-09-16T10:00:00Z
                    """,
                required = false
            )
            String startTime,

            @ToolParam(
                description = """
                    End time in ISO-8601 format.
                    Example: 2026-09-16T12:00:00Z
                    """,
                required = false
            )
            String endTime
    ) {

        Instant start = startTime == null
                ? null
                : Instant.parse(startTime);

        Instant end = endTime == null
                ? null
                : Instant.parse(endTime);

        return discordChatTool.getConversationHistory(
                chatName,
                limit,
                start,
                end
        );
    }
}

