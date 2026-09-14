package com.akshay.assistant.tools.calendar;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarToolTest {

    @Mock
    private Calendar googleCalendar;

    @Mock
    private Calendar.Events googleEvents;

    @Mock
    private Calendar.Events.List listRequest;

    @Mock
    private Calendar.Events.Insert insertRequest;

    @Mock
    private Calendar.Events.Update updateRequest;

    @Mock
    private Calendar.Events.Delete deleteRequest;

    private CalendarTool calendarTool;

    private static final String CALENDAR_ID = "primary";

    private static final ZoneId ZONE =
            ZoneId.of("Asia/Kolkata");

    private static final LocalDateTime START =
            LocalDateTime.of(2026, 9, 15, 10, 0);

    private static final LocalDateTime END =
            LocalDateTime.of(2026, 9, 15, 11, 0);

    @BeforeEach
    void setUp() {
        calendarTool = new CalendarTool(
                googleCalendar,
                CALENDAR_ID,
                "Asia/Kolkata"
        );
    }

    // ============================================================
    // createEvent()
    // ============================================================

    @Test
    void createEvent_shouldCreateEventSuccessfully()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        // Conflict check
        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setTimeMin(any()))
                .thenReturn(listRequest);

        when(listRequest.setTimeMax(any()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(new Events());

        // Insert
        when(googleEvents.insert(
                eq(CALENDAR_ID),
                any(Event.class)
        )).thenReturn(insertRequest);

        Event createdEvent = googleEvent(
                "Team Meeting",
                START,
                END,
                "Weekly meeting",
                "tool-id-123"
        );

        when(insertRequest.execute())
                .thenReturn(createdEvent);

        CalendarResponse response =
                calendarTool.createEvent(
                        "Team Meeting",
                        START,
                        END,
                        "Weekly meeting"
                );

        assertEquals("success", response.status());
        assertNull(response.error());

        assertNotNull(response.event());

        assertEquals(
                "Team Meeting",
                response.event().eventName()
        );

        assertEquals(
                START,
                response.event().startTime()
        );

        assertEquals(
                END,
                response.event().endTime()
        );

        assertEquals(
                "Weekly meeting",
                response.event().description()
        );

        verify(googleEvents).insert(
                eq(CALENDAR_ID),
                any(Event.class)
        );
    }

    @Test
    void createEvent_shouldUseOneHourDefaultEndTime()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setTimeMin(any()))
                .thenReturn(listRequest);

        when(listRequest.setTimeMax(any()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(new Events());

        when(googleEvents.insert(
                eq(CALENDAR_ID),
                any(Event.class)
        )).thenReturn(insertRequest);

        Event createdEvent = googleEvent(
                "Meeting",
                START,
                START.plusHours(1),
                null,
                "tool-id-123"
        );

        when(insertRequest.execute())
                .thenReturn(createdEvent);

        CalendarResponse response =
                calendarTool.createEvent(
                        "Meeting",
                        START,
                        null,
                        null
                );

        assertEquals("success", response.status());

        assertEquals(
                START.plusHours(1),
                response.event().endTime()
        );
    }

    @Test
    void createEvent_shouldRejectEmptyEventName() {

        CalendarResponse response =
                calendarTool.createEvent(
                        "",
                        START,
                        END,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "InvalidData",
                response.error()
        );

        assertEquals(
                "event_name cannot be empty.",
                response.message()
        );

        verifyNoInteractions(googleCalendar);
    }

    @Test
    void createEvent_shouldRejectNullEventName() {

        CalendarResponse response =
                calendarTool.createEvent(
                        null,
                        START,
                        END,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "InvalidData",
                response.error()
        );

        verifyNoInteractions(googleCalendar);
    }

    @Test
    void createEvent_shouldRejectNullStartTime() {

        CalendarResponse response =
                calendarTool.createEvent(
                        "Meeting",
                        null,
                        END,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "InvalidData",
                response.error()
        );

        assertEquals(
                "start_time cannot be null.",
                response.message()
        );

        verifyNoInteractions(googleCalendar);
    }

    @Test
    void createEvent_shouldRejectInvalidTimeRange() {

        CalendarResponse response =
                calendarTool.createEvent(
                        "Meeting",
                        END,
                        START,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "InvalidTimeRange",
                response.error()
        );

        assertEquals(
                "start_time must be earlier than end_time.",
                response.message()
        );

        verifyNoInteractions(googleCalendar);
    }

    @Test
    void createEvent_shouldReturnConflictWhenEventsOverlap()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setTimeMin(any()))
                .thenReturn(listRequest);

        when(listRequest.setTimeMax(any()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        Event existingEvent = googleEvent(
                "Existing Meeting",
                LocalDateTime.of(
                        2026, 9, 15, 10, 30
                ),
                LocalDateTime.of(
                        2026, 9, 15, 11, 30
                ),
                null,
                "existing-id"
        );

        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(existingEvent))
                );

        CalendarResponse response =
                calendarTool.createEvent(
                        "New Meeting",
                        START,
                        END,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "ConflictError",
                response.error()
        );

        assertNotNull(
                response.conflictingEvents()
        );

        assertEquals(
                1,
                response.conflictingEvents().size()
        );

        assertEquals(
                "Existing Meeting",
                response.conflictingEvents()
                        .getFirst()
                        .eventName()
        );

        verify(googleEvents, never())
                .insert(
                        anyString(),
                        any(Event.class)
                );
    }

    @Test
    void createEvent_shouldAllowAdjacentEvents()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setTimeMin(any()))
                .thenReturn(listRequest);

        when(listRequest.setTimeMax(any()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        Event existingEvent = googleEvent(
                "Previous Meeting",
                LocalDateTime.of(
                        2026, 9, 15, 9, 0
                ),
                LocalDateTime.of(
                        2026, 9, 15, 10, 0
                ),
                null,
                "existing-id"
        );

        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(existingEvent))
                );

        when(googleEvents.insert(
                eq(CALENDAR_ID),
                any(Event.class)
        )).thenReturn(insertRequest);

        Event newEvent = googleEvent(
                "New Meeting",
                START,
                END,
                null,
                "new-id"
        );

        when(insertRequest.execute())
                .thenReturn(newEvent);

        CalendarResponse response =
                calendarTool.createEvent(
                        "New Meeting",
                        START,
                        END,
                        null
                );

        assertEquals("success", response.status());

        verify(googleEvents).insert(
                eq(CALENDAR_ID),
                any(Event.class)
        );
    }

    @Test
    void createEvent_shouldReturnOperationErrorWhenGoogleFails()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setTimeMin(any()))
                .thenReturn(listRequest);

        when(listRequest.setTimeMax(any()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenThrow(
                        new IOException("Google Calendar unavailable")
                );

        CalendarResponse response =
                calendarTool.createEvent(
                        "Meeting",
                        START,
                        END,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "OperationError",
                response.error()
        );

        assertEquals(
                "Google Calendar unavailable",
                response.message()
        );
    }

    // ============================================================
    // updateEvent()
    // ============================================================

    @Test
    void updateEvent_shouldUpdateEventSuccessfully()
            throws IOException {

        Event existingEvent = googleEvent(
                "Old Meeting",
                START,
                END,
                "Old description",
                "event-uuid"
        );

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        // findByPublicId()
        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setPrivateExtendedProperty(anyList()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(false))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(existingEvent))
                );

        when(googleEvents.update(
                eq(CALENDAR_ID),
                eq(existingEvent.getId()),
                eq(existingEvent)
        )).thenReturn(updateRequest);

        when(updateRequest.execute())
                .thenReturn(existingEvent);

        CalendarResponse response =
                calendarTool.updateEvent(
                        "event-uuid",
                        "Updated Meeting",
                        null,
                        null,
                        null
                );

        assertEquals("success", response.status());

        assertEquals(
                "Updated Meeting",
                existingEvent.getSummary()
        );

        verify(googleEvents).update(
                eq(CALENDAR_ID),
                eq(existingEvent.getId()),
                eq(existingEvent)
        );
    }

    @Test
    void updateEvent_shouldUpdateOnlyProvidedFields()
            throws IOException {

        Event existingEvent = googleEvent(
                "Old Meeting",
                START,
                END,
                "Old description",
                "event-uuid"
        );

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setPrivateExtendedProperty(anyList()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(false))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(existingEvent))
                );

        when(googleEvents.update(
                eq(CALENDAR_ID),
                eq(existingEvent.getId()),
                eq(existingEvent)
        )).thenReturn(updateRequest);

        when(updateRequest.execute())
                .thenReturn(existingEvent);

        LocalDateTime newStart =
                LocalDateTime.of(2026, 9, 15, 12, 0);

        CalendarResponse response =
                calendarTool.updateEvent(
                        "event-uuid",
                        null,
                        newStart,
                        null,
                        null
                );

        assertEquals("success", response.status());

        assertEquals(
                newStart,
                fromGoogleDateTime(
                        existingEvent.getStart()
                )
        );

        // These were not provided and therefore must remain unchanged.
        assertEquals(
                END,
                fromGoogleDateTime(
                        existingEvent.getEnd()
                )
        );

        assertEquals(
                "Old Meeting",
                existingEvent.getSummary()
        );

        assertEquals(
                "Old description",
                existingEvent.getDescription()
        );
    }

    @Test
    void updateEvent_shouldReturnNoDataProvided()
            throws IOException {

        CalendarResponse response =
                calendarTool.updateEvent(
                        "event-uuid",
                        null,
                        null,
                        null,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "NoDataProvided",
                response.error()
        );

        verifyNoInteractions(googleCalendar);
    }

    @Test
    void updateEvent_shouldReturnEventNotFound()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setPrivateExtendedProperty(anyList()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(false))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(new Events());

        CalendarResponse response =
                calendarTool.updateEvent(
                        "does-not-exist",
                        "New Name",
                        null,
                        null,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "EventNotFoundError",
                response.error()
        );

        verify(googleEvents, never())
                .update(
                        anyString(),
                        anyString(),
                        any(Event.class)
                );
    }

    @Test
    void updateEvent_shouldRejectInvalidTimeRange()
            throws IOException {

        Event existingEvent = googleEvent(
                "Meeting",
                START,
                END,
                null,
                "event-uuid"
        );

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setPrivateExtendedProperty(anyList()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(false))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(existingEvent))
                );

        CalendarResponse response =
                calendarTool.updateEvent(
                        "event-uuid",
                        null,
                        END,
                        START,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "InvalidTimeRange",
                response.error()
        );

        verify(googleEvents, never())
                .update(
                        anyString(),
                        anyString(),
                        any(Event.class)
                );
    }

    @Test
    void updateEvent_shouldRejectConflict()
            throws IOException {

        Event existingEvent = googleEvent(
                "Current Meeting",
                START,
                END,
                null,
                "event-uuid"
        );

        Event conflictingEvent = googleEvent(
                "Other Meeting",
                LocalDateTime.of(
                        2026, 9, 15, 12, 0
                ),
                LocalDateTime.of(
                        2026, 9, 15, 13, 0
                ),
                null,
                "other-event"
        );

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setPrivateExtendedProperty(anyList()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(false))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        /*
         * First execute() -> findByPublicId()
         */
        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(existingEvent))
                );

        /*
         * After finding the event, updateEvent() performs
         * another list() call for conflict detection.
         *
         * Mockito returns the same request mock, so the second
         * execute() needs to return the conflicting event.
         */
        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(existingEvent))
                )
                .thenReturn(
                        new Events()
                                .setItems(List.of(conflictingEvent))
                );

        LocalDateTime newStart =
                LocalDateTime.of(2026, 9, 15, 12, 30);

        LocalDateTime newEnd =
                LocalDateTime.of(2026, 9, 15, 13, 30);

        CalendarResponse response =
                calendarTool.updateEvent(
                        "event-uuid",
                        null,
                        newStart,
                        newEnd,
                        null
                );

        assertEquals("error", response.status());

        assertEquals(
                "ConflictError",
                response.error()
        );

        assertNotNull(
                response.conflictingEvents()
        );

        assertEquals(
                1,
                response.conflictingEvents().size()
        );

        verify(googleEvents, never())
                .update(
                        anyString(),
                        anyString(),
                        any(Event.class)
                );
    }

    // ============================================================
    // deleteEvent()
    // ============================================================

    @Test
    void deleteEvent_shouldDeleteSuccessfully()
            throws IOException {

        Event existingEvent = googleEvent(
                "Meeting",
                START,
                END,
                "Description",
                "event-uuid"
        );

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setPrivateExtendedProperty(anyList()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(false))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(existingEvent))
                );

        when(googleEvents.delete(
                CALENDAR_ID,
                existingEvent.getId()
        )).thenReturn(deleteRequest);

        when(deleteRequest.execute())
                .thenReturn(null);

        CalendarResponse response =
                calendarTool.deleteEvent("event-uuid");

        assertEquals("success", response.status());

        assertNotNull(
                response.deletedEvent()
        );

        assertEquals(
                "event-uuid",
                response.deletedEvent().eventId()
        );

        assertEquals(
                "Meeting",
                response.deletedEvent().eventName()
        );

        verify(googleEvents).delete(
                CALENDAR_ID,
                existingEvent.getId()
        );
    }

    @Test
    void deleteEvent_shouldReturnEventNotFound()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setPrivateExtendedProperty(anyList()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(false))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(new Events());

        CalendarResponse response =
                calendarTool.deleteEvent(
                        "does-not-exist"
                );

        assertEquals("error", response.status());

        assertEquals(
                "EventNotFoundError",
                response.error()
        );

        verify(googleEvents, never())
                .delete(anyString(), anyString());
    }

    // ============================================================
    // listEvents()
    // ============================================================

    @Test
    void listEvents_shouldReturnEvents()
            throws IOException {

        Event event1 = googleEvent(
                "Morning Meeting",
                LocalDateTime.of(
                        2026, 9, 15, 9, 0
                ),
                LocalDateTime.of(
                        2026, 9, 15, 10, 0
                ),
                null,
                "event-1"
        );

        Event event2 = googleEvent(
                "Afternoon Meeting",
                LocalDateTime.of(
                        2026, 9, 15, 14, 0
                ),
                LocalDateTime.of(
                        2026, 9, 15, 15, 0
                ),
                null,
                "event-2"
        );

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setTimeMin(any()))
                .thenReturn(listRequest);

        when(listRequest.setTimeMax(any()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setOrderBy("startTime"))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(
                                        List.of(event2, event1)
                                )
                );

        CalendarResponse response =
                calendarTool.listEvents(
                        LocalDateTime.of(
                                2026, 9, 15, 0, 0
                        ),
                        LocalDateTime.of(
                                2026, 9, 16, 0, 0
                        )
                );

        assertEquals("success", response.status());

        assertEquals(
                2,
                response.count()
        );

        assertEquals(
                "Morning Meeting",
                response.eventList()
                        .get(0)
                        .eventName()
        );

        assertEquals(
                "Afternoon Meeting",
                response.eventList()
                        .get(1)
                        .eventName()
        );
    }

    @Test
    void listEvents_shouldReturnEmptyList()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setTimeMin(any()))
                .thenReturn(listRequest);

        when(listRequest.setTimeMax(any()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setOrderBy("startTime"))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(new Events());

        CalendarResponse response =
                calendarTool.listEvents(
                        LocalDateTime.of(
                                2026, 9, 15, 0, 0
                        ),
                        LocalDateTime.of(
                                2026, 9, 16, 0, 0
                        )
                );

        assertEquals("success", response.status());

        assertEquals(
                0,
                response.count()
        );

        assertTrue(
                response.eventList().isEmpty()
        );
    }

    @Test
    void listEvents_shouldRejectInvalidTimeRange() {

        CalendarResponse response =
                calendarTool.listEvents(
                        END,
                        START
                );

        assertEquals("error", response.status());

        assertEquals(
                "InvalidTimeRange",
                response.error()
        );

        verifyNoInteractions(googleCalendar);
    }

    @Test
    void listEvents_shouldReturnOperationError()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setTimeMin(any()))
                .thenReturn(listRequest);

        when(listRequest.setTimeMax(any()))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setOrderBy("startTime"))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenThrow(
                        new IOException("Network error")
                );

        CalendarResponse response =
                calendarTool.listEvents(
                        START.minusHours(1),
                        END.plusHours(1)
                );

        assertEquals("error", response.status());

        assertEquals(
                "OperationError",
                response.error()
        );

        assertEquals(
                "Network error",
                response.message()
        );
    }

    // ============================================================
    // searchEvents()
    // ============================================================

    @Test
    void searchEvents_shouldReturnMatchingEvents()
            throws IOException {

        Event event = googleEvent(
                "Doctor Appointment",
                LocalDateTime.of(
                        2026, 9, 20, 18, 0
                ),
                LocalDateTime.of(
                        2026, 9, 20, 19, 0
                ),
                "Dental cleaning",
                "doctor-event"
        );

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setQ("doctor"))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setOrderBy("startTime"))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(
                        new Events()
                                .setItems(List.of(event))
                );

        CalendarResponse response =
                calendarTool.searchEvents("doctor");

        assertEquals("success", response.status());

        assertEquals(
                1,
                response.count()
        );

        assertEquals(
                "Doctor Appointment",
                response.eventList()
                        .getFirst()
                        .eventName()
        );

        assertEquals(
                "Dental cleaning",
                response.eventList()
                        .getFirst()
                        .description()
        );

        verify(listRequest).setQ("doctor");
    }

    @Test
    void searchEvents_shouldReturnEmptyList()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setQ("xyz"))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setOrderBy("startTime"))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenReturn(new Events());

        CalendarResponse response =
                calendarTool.searchEvents("xyz");

        assertEquals("success", response.status());

        assertEquals(
                0,
                response.count()
        );

        assertTrue(
                response.eventList().isEmpty()
        );
    }

    @Test
    void searchEvents_shouldRejectEmptyQuery() {

        CalendarResponse response =
                calendarTool.searchEvents("")
        ;

        assertEquals("error", response.status());

        assertEquals(
                "InvalidQuery",
                response.error()
        );

        assertEquals(
                "Search query cannot be empty.",
                response.message()
        );

        verifyNoInteractions(googleCalendar);
    }

    @Test
    void searchEvents_shouldRejectNullQuery() {

        CalendarResponse response =
                calendarTool.searchEvents(null);

        assertEquals("error", response.status());

        assertEquals(
                "InvalidQuery",
                response.error()
        );

        verifyNoInteractions(googleCalendar);
    }

    @Test
    void searchEvents_shouldReturnOperationError()
            throws IOException {

        when(googleCalendar.events())
                .thenReturn(googleEvents);

        when(googleEvents.list(CALENDAR_ID))
                .thenReturn(listRequest);

        when(listRequest.setQ("meeting"))
                .thenReturn(listRequest);

        when(listRequest.setSingleEvents(true))
                .thenReturn(listRequest);

        when(listRequest.setOrderBy("startTime"))
                .thenReturn(listRequest);

        when(listRequest.setShowDeleted(false))
                .thenReturn(listRequest);

        when(listRequest.execute())
                .thenThrow(
                        new IOException("Google Calendar unavailable")
                );

        CalendarResponse response =
                calendarTool.searchEvents("meeting");

        assertEquals("error", response.status());

        assertEquals(
                "OperationError",
                response.error()
        );

        assertEquals(
                "Google Calendar unavailable",
                response.message()
        );
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private Event googleEvent(
            String name,
            LocalDateTime start,
            LocalDateTime end,
            String description,
            String publicId
    ) {

        Event event = new Event()
                .setId("google-" + publicId)
                .setSummary(name)
                .setDescription(description);

        event.setStart(
                new EventDateTime()
                        .setDateTime(
                                new com.google.api.client.util.DateTime(
                                        start.atZone(ZONE)
                                                .toInstant()
                                                .toEpochMilli()
                                )
                        )
        );

        event.setEnd(
                new EventDateTime()
                        .setDateTime(
                                new com.google.api.client.util.DateTime(
                                        end.atZone(ZONE)
                                                .toInstant()
                                                .toEpochMilli()
                                )
                        )
        );

        event.setExtendedProperties(
                new Event.ExtendedProperties()
                        .setPrivate(
                                Map.of(
                                        "calendar_tool_id",
                                        publicId
                                )
                        )
        );

        return event;
    }

    private LocalDateTime fromGoogleDateTime(
            EventDateTime eventDateTime
    ) {

        return java.time.Instant
                .ofEpochMilli(
                        eventDateTime
                                .getDateTime()
                                .getValue()
                )
                .atZone(ZONE)
                .toLocalDateTime();
    }
}
