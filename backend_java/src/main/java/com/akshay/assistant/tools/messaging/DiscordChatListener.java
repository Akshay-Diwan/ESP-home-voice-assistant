package com.akshay.assistant.tools.messaging;

import com.akshay.assistant.tools.messaging.entity.AvailableChatEntity;
import com.akshay.assistant.tools.messaging.model.Chat;
import com.akshay.assistant.tools.messaging.model.ChatType;
import com.akshay.assistant.tools.messaging.model.Direction;
import com.akshay.assistant.tools.messaging.model.Message;
import com.akshay.assistant.tools.messaging.model.Person;
import com.akshay.assistant.tools.messaging.repository.AvailableChatRepository;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.springframework.context.ApplicationEventPublisher;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiscordChatListener extends ListenerAdapter {

    private final DiscordChatState state;
    private final ApplicationEventPublisher events;
    private final net.dv8tion.jda.api.JDA jda;
    private final AvailableChatRepository chatRepository;
    public DiscordChatListener(
            DiscordChatState state,
            ApplicationEventPublisher events,
            net.dv8tion.jda.api.JDA jda,
            AvailableChatRepository chatRepository
    ) {
        this.chatRepository = chatRepository;
        this.state = state;
        this.events = events;
        this.jda = jda;
    }

    @PostConstruct
    void register() {
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        MessageChannel channel = event.getChannel();
        Chat chat = toChat(channel);
        User author = event.getAuthor();
        if(channel.getType() == ChannelType.PRIVATE){
            chatRepository.save(new AvailableChatEntity(author.getId(),channel.getId(), author.getName(),author.getGlobalName()));
        }

        Message message = new Message(
                event.getMessageId(),
                new Person(
                        author.getId(),
                        true,
                        author.getGlobalName() != null ? author.getGlobalName() : author.getName(),
                        author.getName()
                ),
                event.getMessage().getContentDisplay(),
                event.getMessage().getTimeCreated().toInstant(),
                Direction.INCOMING
        );
        System.out.println(
            "Message Id : " + event.getMessageId()
            + "\nAuthor Id: " + author.getId()
            + "\nChannel Id: " + channel.getId()
        );
        state.addIncoming(chat.chatId(), message);
        events.publishEvent(new DiscordMessageReceivedEvent(chat, message));
    }

    private Chat toChat(MessageChannel channel) {
        if (channel instanceof net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel privateChannel) {
            User user = privateChannel.getUser();
            return new Chat(
                    channel.getId(),
                    user.getGlobalName() != null ? user.getGlobalName() : user.getName(),
                    ChatType.INDIVIDUAL,
                    List.of(new Person(user.getId(), true,
                            user.getGlobalName() != null ? user.getGlobalName() : user.getName(),
                            user.getName()))
            );
        }

        if (channel instanceof net.dv8tion.jda.api.entities.channel.concrete.TextChannel textChannel) {
            return new Chat(
                    channel.getId(),
                    textChannel.getName(),
                    ChatType.GROUP,
                    List.of()
            );
        }

        return new Chat(channel.getId(), channel.getName(), ChatType.GROUP, List.of());
    }
}
