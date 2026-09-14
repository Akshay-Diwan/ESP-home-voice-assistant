package com.akshay.assistant.tools.calendar;

import java.time.LocalDateTime;

public record EventDto(
        String eventId,
        String eventName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description
) {
}
