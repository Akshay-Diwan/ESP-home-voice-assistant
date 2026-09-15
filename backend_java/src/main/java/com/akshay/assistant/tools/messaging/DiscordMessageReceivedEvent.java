package com.akshay.assistant.tools.messaging;

import com.akshay.assistant.tools.messaging.model.Chat;
import com.akshay.assistant.tools.messaging.model.Message;

public record DiscordMessageReceivedEvent(Chat chat, Message message) {}
