package com.akshay.assistant.tools.calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CalendarTool {

    private static final String TOOL_ID_PROPERTY = "calendar_tool_id";

    private final Calendar googleCalendar;
    private final String calendarId;
    private final ZoneId zoneId;

    public CalendarTool(
            Calendar googleCalendar,
            @Value("${calendar.google.calendar-id}") String calendarId,
            @Value("${calendar.google.timezone}") String timezone
    ) {
        this.googleCalendar = googleCalendar;
        this.calendarId = calendarId;
        this.zoneId = ZoneId.of(timezone);
    }

    public CalendarResponse createEvent(
            String eventName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description
    ) {
        if (isBlank(eventName)) {
            return CalendarResponse.error("InvalidData", "event_name cannot be empty.");
        }

        if (startTime == null) {
            return CalendarResponse.error("InvalidData", "start_time cannot be null.");
        }

        if (endTime == null) {
            endTime = startTime.plusHours(1);
        }

        if (!startTime.isBefore(endTime)) {
            return CalendarResponse.error(
                    "InvalidTimeRange",
                    "start_time must be earlier than end_time."
            );
        }

        try {
            List<Event> conflicts = findOverlappingEvents(startTime, endTime);
            if (!conflicts.isEmpty()) {
                return CalendarResponse.conflict(conflicts);
            }

            String publicId = UUID.randomUUID().toString();

            Event event = new Event()
                    .setSummary(eventName)
                    .setDescription(description)
                    .setStart(toEventDateTime(startTime))
                    .setEnd(toEventDateTime(endTime))
                    .setExtendedProperties(
                            new Event.ExtendedProperties()
                                    .setPrivate(Map.of(TOOL_ID_PROPERTY, publicId))
                    );

            Event created = googleCalendar.events()
                    .insert(calendarId, event)
                    .execute();

            return CalendarResponse.success(toEventDto(created, publicId));

        } catch (IOException e) {
            return CalendarResponse.error("OperationError", e.getMessage());
        }
    }

    public CalendarResponse createEvent(
            String eventName,
            LocalDateTime startTime,
            String description
    ) {
        return createEvent(eventName, startTime, startTime.plusHours(1), description);
    }

    /**
     * SRS-shaped update API:
     *
     * updateEvent(event_id, data)
     */
    public CalendarResponse updateEvent(String eventId, EventUpdate data) {
        if (data == null) {
            return CalendarResponse.error(
                    "NoDataProvided",
                    "provide atleast one parameter"
            );
        }

        return updateEvent(
                eventId,
                data.eventName(),
                data.startTime(),
                data.endTime(),
                data.description()
        );
    }

    public CalendarResponse updateEvent(
            String eventId,
            String eventName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String description
    ) {
        if (isBlank(eventId)) {
            return CalendarResponse.error("InvalidData", "event_id cannot be empty.");
        }

        if (eventName == null && startTime == null && endTime == null && description == null) {
            return CalendarResponse.error(
                    "NoDataProvided",
                    "provide atleast one parameter"
            );
        }

        try {
            Event existing = findByPublicId(eventId);

            if (existing == null) {
                return CalendarResponse.error(
                        "EventNotFoundError",
                        "No event found with the given id."
                );
            }

            LocalDateTime oldStart = fromEventDateTime(existing.getStart());
            LocalDateTime oldEnd = fromEventDateTime(existing.getEnd());

            LocalDateTime newStart = startTime != null ? startTime : oldStart;
            LocalDateTime newEnd = endTime != null ? endTime : oldEnd;

            if (!newStart.isBefore(newEnd)) {
                return CalendarResponse.error(
                        "InvalidTimeRange",
                        "start_time must be earlier than end_time."
                );
            }

            if (startTime != null || endTime != null) {
                List<Event> conflicts = findOverlappingEvents(
                        newStart,
                        newEnd,
                        existing.getId()
                );

                if (!conflicts.isEmpty()) {
                    return CalendarResponse.conflict(conflicts);
                }
            }

            if (eventName != null) {
                existing.setSummary(eventName);
            }

            if (description != null) {
                existing.setDescription(description);
            }

            if (startTime != null) {
                existing.setStart(toEventDateTime(startTime));
            }

            if (endTime != null) {
                existing.setEnd(toEventDateTime(endTime));
            }

            googleCalendar.events()
                    .update(calendarId, existing.getId(), existing)
                    .execute();

            return CalendarResponse.success(null);

        } catch (IOException e) {
            return CalendarResponse.error("OperationError", e.getMessage());
        }
    }

    public CalendarResponse deleteEvent(String eventId) {
        if (isBlank(eventId)) {
            return CalendarResponse.error("InvalidData", "event_id cannot be empty.");
        }

        try {
            Event existing = findByPublicId(eventId);

            if (existing == null) {
                return CalendarResponse.error(
                        "EventNotFoundError",
                        "No event found with the given id."
                );
            }

            EventDto deleted = toEventDto(existing, eventId);

            googleCalendar.events()
                    .delete(calendarId, existing.getId())
                    .execute();

            return CalendarResponse.deleted(deleted);

        } catch (IOException e) {
            return CalendarResponse.error("OperationError", e.getMessage());
        }
    }

    public CalendarResponse listEvents(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            return CalendarResponse.error(
                    "InvalidTimeRange",
                    "start_time must be earlier than end_time."
            );
        }

        try {
            Events events = googleCalendar.events()
                    .list(calendarId)
                    .setTimeMin(toGoogleDateTime(startTime))
                    .setTimeMax(toGoogleDateTime(endTime))
                    .setSingleEvents(true)
                    .setOrderBy("startTime")
                    .setShowDeleted(false)
                    .execute();

            List<EventDto> result = new ArrayList<>();

            for (Event event : events.getItems()) {
                if (event.getStatus() == null || !"cancelled".equals(event.getStatus())) {
                    String publicId = getPublicId(event);
                    result.add(toEventDto(event, publicId));
                }
            }

            result.sort(Comparator.comparing(EventDto::startTime));
            return CalendarResponse.events(result);

        } catch (IOException e) {
            return CalendarResponse.error("OperationError", e.getMessage());
        }
    }

    public CalendarResponse searchEvents(String query) {
        if (isBlank(query)) {
            return CalendarResponse.error(
                    "InvalidQuery",
                    "Search query cannot be empty."
            );
        }

        try {
            Events events = googleCalendar.events()
                    .list(calendarId)
                    .setQ(query)
                    .setSingleEvents(true)
                    .setOrderBy("startTime")
                    .setShowDeleted(false)
                    .execute();

            List<EventDto> result = events.getItems()
                    .stream()
                    .filter(e -> !"cancelled".equals(e.getStatus()))
                    .map(e -> toEventDto(e, getPublicId(e)))
                    .sorted(Comparator.comparing(EventDto::startTime))
                    .toList();

            return CalendarResponse.events(result);

        } catch (IOException e) {
            return CalendarResponse.error("OperationError", e.getMessage());
        }
    }

    private Event findByPublicId(String publicId) throws IOException {
        Events events = googleCalendar.events()
                .list(calendarId)
                .setPrivateExtendedProperty(
                        List.of(TOOL_ID_PROPERTY + "=" + publicId)
                )
                .setSingleEvents(false)
                .setShowDeleted(false)
                .execute();

        return events.getItems()
                .stream()
                .findFirst()
                .orElse(null);
    }

    private List<Event> findOverlappingEvents(
            LocalDateTime newStart,
            LocalDateTime newEnd
    ) throws IOException {
        return findOverlappingEvents(newStart, newEnd, null);
    }

    private List<Event> findOverlappingEvents(
            LocalDateTime newStart,
            LocalDateTime newEnd,
            String ignoredGoogleEventId
    ) throws IOException {

        /*
         * Google Calendar's timeMax/timeMin give us a candidate set.
         * We then perform the exact interval test:
         *
         * existing.start < requested.end
         * &&
         * existing.end > requested.start
         *
         * Adjacent events such as 10:00-11:00 and 11:00-12:00
         * therefore do not conflict.
         */
        Events events = googleCalendar.events()
                .list(calendarId)
                .setTimeMin(toGoogleDateTime(newStart))
                .setTimeMax(toGoogleDateTime(newEnd))
                .setSingleEvents(true)
                .setShowDeleted(false)
                .execute();

        List<Event> conflicts = new ArrayList<>();

        for (Event event : events.getItems()) {
            if (ignoredGoogleEventId != null &&
                    ignoredGoogleEventId.equals(event.getId())) {
                continue;
            }

            if (event.getStart() == null || event.getEnd() == null ||
                    event.getStart().getDateTime() == null ||
                    event.getEnd().getDateTime() == null) {
                continue;
            }

            LocalDateTime existingStart = fromEventDateTime(event.getStart());
            LocalDateTime existingEnd = fromEventDateTime(event.getEnd());

            if (existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart)) {
                conflicts.add(event);
            }
        }

        return conflicts;
    }

    private EventDateTime toEventDateTime(LocalDateTime time) {
        return new EventDateTime()
                .setDateTime(toGoogleDateTime(time))
                .setTimeZone(zoneId.getId());
    }

    private DateTime toGoogleDateTime(LocalDateTime time) {
        return new DateTime(
                time.atZone(zoneId).toInstant().toEpochMilli()
        );
    }

    private LocalDateTime fromEventDateTime(EventDateTime eventDateTime) {
        return Instant.ofEpochMilli(eventDateTime.getDateTime().getValue())
                .atZone(zoneId)
                .toLocalDateTime();
    }

    private String getPublicId(Event event) {
        if (event.getExtendedProperties() != null &&
                event.getExtendedProperties().getPrivate() != null) {
            return event.getExtendedProperties()
                    .getPrivate()
                    .get(TOOL_ID_PROPERTY);
        }

        // Events created outside this tool do not have our UUID.
        // The Google event id is still returned so they remain usable.
        return event.getId();
    }

    private EventDto toEventDto(Event event, String publicId) {
        return new EventDto(
                publicId,
                event.getSummary(),
                fromEventDateTime(event.getStart()),
                fromEventDateTime(event.getEnd()),
                event.getDescription()
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
