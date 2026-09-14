package com.akshay.assistant.tools.calendar;

import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

public record CalendarResponse(
        String status,
        String error,
        String message,
        EventDto event,
        EventDto deletedEvent,
        Integer count,
        List<EventDto> eventList,
        List<EventDto> conflictingEvents
) {

    public static CalendarResponse success(EventDto event) {
        return new CalendarResponse(
                "success", null, null, event, null,
                event == null ? null : 1,
                event == null ? List.of() : List.of(event),
                null
        );
    }

    public static CalendarResponse deleted(EventDto event) {
        return new CalendarResponse(
                "success", null, null, null, event,
                1, List.of(), null
        );
    }

    public static CalendarResponse events(List<EventDto> events) {
        return new CalendarResponse(
                "success", null, null, null, null,
                events.size(), events, null
        );
    }

    public static CalendarResponse conflict(List<Event> conflicts) {
        List<EventDto> dto = new ArrayList<>();

        for (Event event : conflicts) {
            String id = event.getId();

            if (event.getExtendedProperties() != null &&
                    event.getExtendedProperties().getPrivate() != null &&
                    event.getExtendedProperties().getPrivate().get("calendar_tool_id") != null) {
                id = event.getExtendedProperties()
                        .getPrivate()
                        .get("calendar_tool_id");
            }

            dto.add(new EventDto(
                    id,
                    event.getSummary(),
                    event.getStart().getDateTime() == null
                            ? null
                            : java.time.Instant.ofEpochMilli(
                                    event.getStart().getDateTime().getValue()
                              ).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                    event.getEnd().getDateTime() == null
                            ? null
                            : java.time.Instant.ofEpochMilli(
                                    event.getEnd().getDateTime().getValue()
                              ).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
                    event.getDescription()
            ));
        }

        String firstName = dto.isEmpty() ? "event" : dto.getFirst().eventName();

        return new CalendarResponse(
                "error",
                "ConflictError",
                firstName + " already scheduled at the given time",
                null,
                null,
                dto.size(),
                List.of(),
                dto
        );
    }

    public static CalendarResponse error(String error, String message) {
        return new CalendarResponse(
                "error", error, message, null, null,
                null, List.of(), null
        );
    }
}
