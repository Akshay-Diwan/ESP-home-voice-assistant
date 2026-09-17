// package com.akshay.assistant.tools.calendar;

// import com.google.api.services.calendar.Calendar;
// import com.google.api.services.calendar.model.Event;
// import org.junit.jupiter.api.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.integration.annotation.IntegrationComponentScan;
// import org.springframework.integration.config.EnableIntegration;

// import java.io.IOException;
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest(properties = {
//     "mqtt.url=tcp://localhost:1883",
//     "mqtt.username=user1",
//     "mqtt.password=akshay"
// }) 
// @EnableIntegration 
// @IntegrationComponentScan(basePackages = "com.akshay.assistant.gateway")
// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// class CalendarToolIntegrationTest {

//     @Autowired
//     private CalendarTool calendarTool;

//     @Autowired
//     private Calendar googleCalendar;

//     @Value("${calendar.google.calendar-id}")
//     private String calendarId;

//     @Value("${calendar.google.timezone}")
//     private String timezone;

//     private final List<String> createdEventIds = new java.util.ArrayList<>();

//     @AfterEach
//     void cleanup() throws Exception {

//         /*
//          * CalendarTool uses its own public UUID as event_id.
//          *
//          * We therefore need to delete through CalendarTool,
//          * rather than directly using the Google event ID.
//          */
//         for (String eventId : createdEventIds) {
//             try {
//                 calendarTool.deleteEvent(eventId);
//             } catch (Exception ignored) {
//                 // Cleanup should not make the test fail.
//             }
//         }

//         createdEventIds.clear();
//     }

//     @Test
//     void createEvent_shouldCreateEventSuccessfully() {

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(10)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         LocalDateTime end = start.plusHours(1);

//         CalendarResponse response = calendarTool.createEvent(
//                 "Integration Test Event",
//                 start,
//                 end,
//                 "Created by integration test"
//         );

//         assertEquals("success", response.status());
//         assertNull(response.error());
//         assertNotNull(response.event());

//         assertEquals(
//                 "Integration Test Event",
//                 response.event().eventName()
//         );

//         assertEquals(
//                 "Created by integration test",
//                 response.event().description()
//         );

//         assertEquals(start, response.event().startTime());
//         assertEquals(end, response.event().endTime());

//         assertNotNull(response.event().eventId());

//         createdEventIds.add(response.event().eventId());
//     }

//     @Test
//     void createEvent_shouldUseOneHourDuration_whenEndTimeIsNotProvided() {

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(11)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         CalendarResponse response = calendarTool.createEvent(
//                 "One Hour Event",
//                 start,
//                 "Integration test"
//         );

//         assertEquals("success", response.status());
//         assertNotNull(response.event());

//         assertEquals(
//                 start.plusHours(1),
//                 response.event().endTime()
//         );

//         createdEventIds.add(response.event().eventId());
//     }

//     @Test
//     void createEvent_shouldReturnConflict_whenTimeOverlaps() {

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(13)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         LocalDateTime end = start.plusHours(1);

//         // First event
//         CalendarResponse first = calendarTool.createEvent(
//                 "First Integration Event",
//                 start,
//                 end,
//                 "First event"
//         );

//         assertEquals("success", first.status());
//         assertNotNull(first.event());

//         createdEventIds.add(first.event().eventId());

//         // Second event overlaps first event
//         CalendarResponse second = calendarTool.createEvent(
//                 "Second Integration Event",
//                 start.plusMinutes(30),
//                 end.plusMinutes(30),
//                 "Second event"
//         );

//         assertEquals("error", second.status());
//         assertEquals("ConflictError", second.error());

//         assertNotNull(second.conflictingEvents());
//         assertFalse(second.conflictingEvents().isEmpty());

//         assertEquals(
//                 "First Integration Event",
//                 second.conflictingEvents().getFirst().eventName()
//         );
//     }

//     @Test
//     void createEvent_shouldAllowAdjacentEvents() {

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(15)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         LocalDateTime end = start.plusHours(1);

//         // 15:00 - 16:00
//         CalendarResponse first = calendarTool.createEvent(
//      
                            // "First Adjacent Event",
//                 start,
//                 end,
//                 "First"
//         );

//         assertEquals("success", first.status());
//         assertNotNull(first.event());

//         createdEventIds.add(first.event().eventId());

//         // 16:00 - 17:00
//         CalendarResponse second = calendarTool.createEvent(
//                 "Second Adjacent Event",
//                 end,
//                 end.plusHours(1),
//                 "Second"
//         );

//         assertEquals("success", second.status());
//         assertNotNull(second.event());

//         createdEventIds.add(second.event().eventId());
//     }

//     @Test
//     void updateEvent_shouldUpdateOnlyProvidedFields() throws IOException{

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(9)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         LocalDateTime end = start.plusHours(1);

//         CalendarResponse created = calendarTool.createEvent(
//                 "Original Event",
//                 start,
//                 end,
//                 "Original description"
//         );

//         assertEquals("success", created.status());
//         assertNotNull(created.event());

//         String eventId = created.event().eventId();
//         createdEventIds.add(eventId);

//         /*
//          * Update ONLY the event name.
//          */
//         CalendarResponse updated = calendarTool.updateEvent(
//                 eventId,
//                 "Updated Event",
//                 null,
//                 null,
//                 null
//         );

//         assertEquals("success", updated.status());
//         assertNull(updated.error());

//         /*
//          * Your updateEvent() intentionally returns success(null),
//          * so verify the actual Google Calendar event.
//          */

//         Event googleEvent = googleCalendar.events()
//                 .list(calendarId)
//                 .setPrivateExtendedProperty(
//                         List.of("calendar_tool_id=" + eventId)
//                 )
//                 .setSingleEvents(false)
//                 .setShowDeleted(false)
//                 .execute()
//                 .getItems()
//                 .getFirst();

//         assertEquals(
//                 "Updated Event",
//                 googleEvent.getSummary()
//         );

//         // These must remain unchanged.
//         assertEquals(
//                 "Original description",
//                 googleEvent.getDescription()
//         );

//         ZoneId zoneId = ZoneId.of(timezone);
//         assertEquals(
//                 start.atZone(zoneId).toInstant().toEpochMilli(),
//                 googleEvent.getStart()
//                         .getDateTime()
//                         .getValue()
//         );

//     }

//     @Test
//     void updateEvent_shouldUpdateDescription() throws IOException{

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(17)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         LocalDateTime end = start.plusHours(1);

//         CalendarResponse created = calendarTool.createEvent(
//                 "Description Test",
//                 start,
//                 end,
//                 "Old description"
//         );

//         assertEquals("success", created.status());

//         String eventId = created.event().eventId();
//         createdEventIds.add(eventId);

//         CalendarResponse updated = calendarTool.updateEvent(
//                 eventId,
//                 null,
//                 null,
//                 null,
//                 "New description"
//         );

//         assertEquals("success", updated.status());
//         Event googleEvent = googleCalendar.events()
//                 .list(calendarId)
//                 .setPrivateExtendedProperty(
//                         List.of("calendar_tool_id=" + eventId)
//                 )
//                 .setSingleEvents(false)
//                 .setShowDeleted(false)
//                 .execute()
//                 .getItems()
//                 .getFirst();

//         assertEquals(
//                 "Description Test",
//                 googleEvent.getSummary()
//         );

//         assertEquals(
//                 "New description",
//                 googleEvent.getDescription()
//         );
//     }

//     @Test
//     void updateEvent_shouldReturnNotFound_forUnknownId() {

//         CalendarResponse response = calendarTool.updateEvent(
//                 "does-not-exist",
//                 "Updated",
//                 null,
//                 null,
//                 null
//         );

//         assertEquals("error", response.status());
//         assertEquals("EventNotFoundError", response.error());
//     }

//     @Test
//     void deleteEvent_shouldDeleteEventSuccessfully() throws Exception {

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(19)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         LocalDateTime end = start.plusHours(1);

//         CalendarResponse created = calendarTool.createEvent(
//                 "Delete Integration Test",
//                 start,
//                 end,
//                 "Temporary event"
//         );

//         assertEquals("success", created.status());

//         String publicId = created.event().eventId();

//         /*
//          * Don't add it to cleanup because we're explicitly
//          * deleting it here.
//          */
//         CalendarResponse deleted =
//                 calendarTool.deleteEvent(publicId);

//         assertEquals("success", deleted.status());
//         assertNotNull(deleted.deletedEvent());

//         assertEquals(
//                 publicId,
//                 deleted.deletedEvent().eventId()
//         );

//         /*
//          * Verify it no longer exists using Google Calendar.
//          */
//         EventsResult result = findEventByPublicId(publicId);

//         assertTrue(result.events().isEmpty());
//     }

//     @Test
//     void listEvents_shouldReturnCreatedEvents() {

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(20)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         LocalDateTime end = start.plusHours(1);

//         CalendarResponse created = calendarTool.createEvent(
//                 "List Integration Test",
//                 start,
//                 end,
//                 "List test"
//         );

//         assertEquals("success", created.status());

//         String eventId = created.event().eventId();
//         createdEventIds.add(eventId);

//         CalendarResponse response =
//                 calendarTool.listEvents(start.minusMinutes(5), end.plusMinutes(5));

//         assertEquals("success", response.status());
//         assertNotNull(response.eventList());

//         assertTrue(
//                 response.eventList()
//                         .stream()
//                         .anyMatch(event ->
//                                 eventId.equals(event.eventId())
//                         )
//         );
//     }

//     @Test
//     void searchEvents_shouldFindCreatedEvent() {

//         LocalDateTime start =
//                 LocalDateTime.now()
//                         .plusDays(1)
//                         .withHour(21)
//                         .withMinute(0)
//                         .withSecond(0)
//                         .withNano(0);

//         CalendarResponse created = calendarTool.createEvent(
//                 "Unique Search Integration Test",
//                 start,
//                 start.plusHours(1),
//                 "Search test"
//         );

//         assertEquals("success", created.status());

//         String eventId = created.event().eventId();
//         createdEventIds.add(eventId);

//         CalendarResponse response =
//                 calendarTool.searchEvents(
//                         "Unique Search Integration Test"
//                 );

//         assertEquals("success", response.status());

//         assertTrue(
//                 response.eventList()
//                         .stream()
//                         .anyMatch(event ->
//                                 eventId.equals(event.eventId())
//                         )
//         );
//     }

//     @Test
//     void createEvent_shouldRejectEmptyEventName() {

//         CalendarResponse response = calendarTool.createEvent(
//                 "",
//                 LocalDateTime.now().plusDays(1),
//                 LocalDateTime.now().plusDays(1).plusHours(1),
//                 "Test"
//         );

//         assertEquals("error", response.status());
//         assertEquals("InvalidData", response.error());
//     }

//     @Test
//     void createEvent_shouldRejectInvalidTimeRange() {

//         LocalDateTime start =
//                 LocalDateTime.now().plusDays(1);

//         CalendarResponse response = calendarTool.createEvent(
//                 "Invalid Time",
//                 start,
//                 start.minusHours(1),
//                 "Test"
//         );

//         assertEquals("error", response.status());
//         assertEquals("InvalidTimeRange", response.error());
//     }

//     @Test
//     void updateEvent_shouldRejectNullData() {

//         CalendarResponse response =
//                 calendarTool.updateEvent(
//                         "some-id",
//                         (EventUpdate) null
//                 );

//         assertEquals("error", response.status());
//         assertEquals("NoDataProvided", response.error());
//     }

//     /*
//      * Small helper for verifying deletion.
//      */
//     private EventsResult findEventByPublicId(String publicId)
//             throws Exception {

//         var events = googleCalendar.events()
//                 .list(calendarId)
//                 .setPrivateExtendedProperty(
//                         List.of("calendar_tool_id=" + publicId)
//                 )
//                 .setSingleEvents(false)
//                 .setShowDeleted(false)
//                 .execute();

//         return new EventsResult(events.getItems());
//     }

//     private record EventsResult(List<Event> events) {
//     }
// }