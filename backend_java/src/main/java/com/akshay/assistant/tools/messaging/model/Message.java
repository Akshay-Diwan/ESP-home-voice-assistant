package com.akshay.assistant.tools.messaging.model;

import java.time.Instant;

public record Message(
        String messageId,
        Person sender,
        String message,
        Instant timestamp,
        Direction direction
) {}
