package com.akshay.assistant.tools.calendar;

import java.time.LocalDateTime;

/**
 * Optional fields for CalendarTool.updateEvent(...).
 * A null field means "leave the existing value unchanged".
 */
public record EventUpdate(
        String eventName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description
) {}
