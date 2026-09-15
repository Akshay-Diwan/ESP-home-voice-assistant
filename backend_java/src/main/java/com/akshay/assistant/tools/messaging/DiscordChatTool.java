package com.akshay.assistant.tools.messaging;
import com.akshay.assistant.tools.messaging.model.*;

import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class DiscordChatTool {

    private final DiscordChatResolver resolver;
    private final long autoCloseMinutes;
    private final int historyPageSize;
    private final DiscordChatState state;

    private final Map<String, Chat> knownChats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "discord-chat-auto-close");
                thread.setDaemon(true);
                return thread;
            });

    public DiscordChatTool(
  
            DiscordChatResolver resolver,
            @Value ("${discord.bot.auto-close-minutes}")
            long autoCloseMinutes,
            @Value ("${discord.bot.history-page-size}")
            int historyPageSize,
            DiscordChatState state
    ) {

        this.resolver = resolver;
        this.autoCloseMinutes = autoCloseMinutes;
        this.historyPageSize = historyPageSize;
        this.state = state;
    }

    @PostConstruct
    void startAutoClose() {
        scheduler.scheduleAtFixedRate(this::autoCloseIfIdle, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Sends a text message to the supplied Discord chat.
     * If chatName is omitted, the current open conversation is used.
     */
    // public ChatResponse sendMessageById(String message, String chatId){
    //     if(message == null)
    // }
    public ChatResponse sendMessage(String message, String chatName) {
        if (message == null || message.isBlank()) {
            return ChatResponse.error("OperationError", "Message cannot be empty.");
        }

        try {
            MessageChannel channel = resolveTarget(chatName);
            Chat chat = remember(channel);

            channel.sendMessage(message).complete();
            touchIfActive(chat.chatId());

            return ChatResponse.success();
        } catch (ChatToolException e) {
            return ChatResponse.error(e.errorCode(), e.getMessage());
        } catch (RuntimeException e) {
            return ChatResponse.error("OperationError", rootMessage(e));
        }
    }

    public UnreadChatsResponse listUnreadChats() {
        try {
            return UnreadChatsResponse.success(state.unreadChatNames(knownChats));
        } catch (RuntimeException e) {
            return UnreadChatsResponse.error("OperationError", rootMessage(e));
        }
    }

    public NewMessagesResponse getNewMessages(String chatName) {
        try {
            MessageChannel channel = resolver.resolve(chatName);
            Chat chat = remember(channel);
            List<Message> messages = state.consumeUnread(chat.chatId());

            return NewMessagesResponse.success(chat.type(), messages);
        } catch (ChatToolException e) {
            return NewMessagesResponse.error(e.errorCode(), e.getMessage());
        } catch (RuntimeException e) {
            return NewMessagesResponse.error("OperationError", rootMessage(e));
        }
    }

    public ChatResponse openConversation(String chatName) {
        try {
            MessageChannel channel = resolver.resolve(chatName);
            Chat chat = remember(channel);
            Instant now = Instant.now();

            state.open(chat, now);
            state.consumeUnread(chat.chatId());

            return ChatResponse.success("Conversation Opened with " + chat.chatName());
        } catch (ChatToolException e) {
            return ChatResponse.error(e.errorCode(), e.getMessage());
        } catch (RuntimeException e) {
            return ChatResponse.error("OperationError", rootMessage(e));
        }
    }

    public ChatResponse closeConversation() {
        try {
            ActiveConversation active = state.activeConversation();
            if (active == null) {
                return ChatResponse.success("No conversation was open");
            }

            String name = active.chat().chatName();
            state.close();
            return ChatResponse.success("Conversation Closed with " + name);
        } catch (RuntimeException e) {
            return ChatResponse.error(
                    "ConversationCouldNotBeClosed",
                    "Failed to close conversation."
            );
        }
    }

    public ConversationHistoryResponse getConversationHistory(
            String chatName,
            Integer limit,
            Instant startTime,
            Instant endTime
    ) {
        try {
            if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
                return ConversationHistoryResponse.error(
                        "InvalidTimeRange",
                        "start_time must be earlier than end_time."
                );
            }

            if (limit != null && limit <= 0) {
                return ConversationHistoryResponse.error(
                        "InvalidLimit",
                        "limit must be greater than 0."
                );
            }

            MessageChannel channel = resolver.resolve(chatName);
            Chat chat = remember(channel);

            List<net.dv8tion.jda.api.entities.Message> discordMessages;

            if (startTime != null || endTime != null) {
                int requested = limit == null ? historyPageSize
                        : Math.min(limit, historyPageSize);

                if (startTime != null) {
                    String pivot = "" + TimeUtil.getDiscordTimestamp(startTime.toEpochMilli());
                    MessageHistory history = MessageHistory.getHistoryAfter(channel, pivot)
                            .limit(requested)
                            .complete();

                    discordMessages = history.getRetrievedHistory().stream()
                            .filter(m -> endTime == null
                                    || !m.getTimeCreated().toInstant().isAfter(endTime))
                            .toList();
                } else {
                    String pivot = "" + TimeUtil.getDiscordTimestamp(endTime.toEpochMilli());
                    MessageHistory history = MessageHistory.getHistoryBefore(channel, pivot)
                            .limit(requested)
                            .complete();

                    discordMessages = history.getRetrievedHistory();
                }
            } else {
                int requested = limit == null ? 20 : Math.min(limit, historyPageSize);
                discordMessages = channel.getHistory().retrievePast(requested).complete();
            }

            List<Message> result = discordMessages.stream()
                    .map(m -> toModel(m, chat))
                    .sorted(Comparator.comparing(Message::timestamp))
                    .toList();

            return ConversationHistoryResponse.success(result);
        } catch (ChatToolException e) {
            return ConversationHistoryResponse.error(e.errorCode(), e.getMessage());
        } catch (RuntimeException e) {
            return ConversationHistoryResponse.error(
                    "OperationError",
                    "Failed to retrieve conversation history: " + rootMessage(e)
            );
        }
    }

    public ActiveConversation getActiveConversation() {
        return state.activeConversation();
    }

    private MessageChannel resolveTarget(String chatName) {
        if (chatName != null && !chatName.isBlank()) {
            return resolver.resolve(chatName);
        }

        ActiveConversation active = state.activeConversation();
        if (active == null) {
            throw new ChatToolException(
                    "NoOpenConversation",
                    "No active conversation. Please specify a contact."
            );
        }

        state.touch();
        return resolver.resolveById(active.chat().chatId());
    }

    private Chat remember(MessageChannel channel) {
        Chat chat = toChat(channel);
        knownChats.put(chat.chatId(), chat);
        return chat;
    }

    private Chat toChat(MessageChannel channel) {
        if (channel instanceof net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel privateChannel) {
            User user = privateChannel.getUser();
            String displayName = user.getGlobalName() != null
                    ? user.getGlobalName()
                    : user.getName();

            return new Chat(
                    channel.getId(),
                    displayName,
                    ChatType.INDIVIDUAL,
                    List.of(new Person(user.getId(), true, displayName, user.getName()))
            );
        }

        return new Chat(
                channel.getId(),
                channel.getName(),
                ChatType.GROUP,
                List.of()
        );
    }

    private Message toModel(
            net.dv8tion.jda.api.entities.Message message,
            Chat chat
    ) {
        User author = message.getAuthor();
        boolean bot = author.isBot();

        Person sender = new Person(
                author.getId(),
                !bot,
                author.getGlobalName() != null ? author.getGlobalName() : author.getName(),
                author.getName()
        );

        return new Message(
                message.getId(),
                sender,
                message.getContentDisplay(),
                message.getTimeCreated().toInstant(),
                bot ? Direction.OUTGOING : Direction.INCOMING
        );
    }

    private void touchIfActive(String chatId) {
        ActiveConversation active = state.activeConversation();
        if (active != null && active.chat().chatId().equals(chatId)) {
            state.touch();
        }
    }

    private void autoCloseIfIdle() {
        try {
            ActiveConversation active = state.activeConversation();
            if (active == null) return;

            Duration idle = Duration.between(active.lastActivityAt(), Instant.now());
            if (idle.toMinutes() >= autoCloseMinutes) {
                state.close();
            }
        } catch (RuntimeException ignored) {
            // Never let the scheduler thread die because of a state error.
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    @PreDestroy
    void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
