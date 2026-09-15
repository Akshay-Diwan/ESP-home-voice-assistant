package com.akshay.assistant.tools.messaging;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiscordChatResolver {

    private final JDA jda;

    public DiscordChatResolver(JDA jda) {
        this.jda = jda;
    }

    public MessageChannel resolve(String chatName) {
        if (chatName == null || chatName.isBlank()) {
            throw new ChatToolException("ChatNotFound", "Chat name cannot be empty.");
        }

        String wanted = chatName.trim();

        List<MessageChannel> matches = new ArrayList<>();
        System.out.println("Channel Chats: ");
        for (PrivateChannel channel : jda.getPrivateChannels()) {
            User user = channel.getUser();
            System.out.println(user.getName() + " " + user.getGlobalName());
            if (matchesName(user.getName(), user.getGlobalName(), wanted)) {
                matches.add(channel);
            }
        }
        System.out.println("TextChannel List: ");
        for (TextChannel channel : jda.getTextChannels()) {
            System.out.println(channel.getName());
            if (channel.getName().equalsIgnoreCase(wanted)
                    || channel.getAsMention().equalsIgnoreCase(wanted)) {
                matches.add(channel);
            }
        }
        
        if (matches.isEmpty()) {
            // Discord does not provide a global user search by display name.
            // JDA can resolve users that are currently visible in its user cache.
            for (User user : jda.getUsersByName(wanted, true)) {
                try {
                    matches.add(user.openPrivateChannel().complete());
                } catch (RuntimeException ignored) {
                    // Continue searching other matching users.
                }
            }
        }

        if (matches.isEmpty()) {
            throw new ChatToolException(
                    "ChatNotFound",
                    "Contact or Group '" + chatName + "' not found."
            );
        }

        if (matches.size() > 1) {
            throw new ChatToolException(
                    "ChatNotFound",
                    "Multiple Discord chats match '" + chatName + "'. Use a unique channel/contact name."
            );
        }

        return matches.getFirst();
    }

    public MessageChannel resolveById(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            throw new ChatToolException("ChatNotFound", "Chat id cannot be empty.");
        }

        for (PrivateChannel channel : jda.getPrivateChannels()) {
            if (channel.getId().equals(channelId)) return channel;
        }

        for (TextChannel channel : jda.getTextChannels()) {
            if (channel.getId().equals(channelId)) return channel;
        }

        throw new ChatToolException(
                "ChatNotFound",
                "No Discord chat found with id '" + channelId + "'."
        );
    }

    private boolean matchesName(String username, String globalName, String wanted) {
        return username.equalsIgnoreCase(wanted)
                || (globalName != null && globalName.equalsIgnoreCase(wanted));
    }
}
