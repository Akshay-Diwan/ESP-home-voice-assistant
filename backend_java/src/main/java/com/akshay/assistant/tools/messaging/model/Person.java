package com.akshay.assistant.tools.messaging.model;

public record Person(
        String personId,
        boolean contact,
        String displayName,
        String username
) {}
