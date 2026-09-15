package com.akshay.assistant.tools.messaging.model;

import java.time.Instant;

public record ActiveConversation(
        Chat chat,
        Instant openedAt,
        Instant lastActivityAt
) {}
