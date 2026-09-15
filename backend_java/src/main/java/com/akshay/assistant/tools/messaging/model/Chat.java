package com.akshay.assistant.tools.messaging.model;

import java.util.List;

public record Chat(
        String chatId,
        String chatName,
        ChatType type,
        List<Person> memberList
) {}
