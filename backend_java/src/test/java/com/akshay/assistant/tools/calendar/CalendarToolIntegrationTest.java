package com.akshay.assistant.tools.calendar;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "mqtt.url=tcp://localhost:1883",
    "mqtt.username=user1",
    "mqtt.password=akshay"
}) 
@EnableIntegration 
@IntegrationComponentScan (basePackages = "com.akshay.assistant.gateway")
@ActiveProfiles("test")
class CalendarToolIntegrationTest {

    @Autowired
    private CalendarTool calendarTool;

    @Test
    void listEvents_shouldGetEventsFromGoogleCalendar() {

        LocalDateTime startTime =
                LocalDateTime.of(
                        2026,
                        9,
                        1,
                        0,
                        0
                );

        LocalDateTime endTime =
                LocalDateTime.of(
                        2026,
                        10,
                        1,
                        0,
                        0
                );

        CalendarResponse response =
                calendarTool.listEvents(
                        startTime,
                        endTime
                );

        // Response itself must exist
        assertNotNull(response);

        // API operation must succeed
        assertEquals(
                "success",
                response.status()
        );

        // No error on successful operation
        assertNull(response.error());

        // Event list must exist
        assertNotNull(
                response.eventList()
        );

        // Count must match actual number of events
        assertEquals(
                response.eventList().size(),
                response.count()
        );

        // Print actual Google Calendar events
        System.out.println(
                "================================="
        );

        System.out.println(
                "Events found: " + response.count()
        );

        System.out.println(
                "================================="
        );

        response.eventList().forEach(event -> {

            System.out.println(
                    "Event ID       : "
                            + event.eventId()
            );

            System.out.println(
                    "Event Name     : "
                            + event.eventName()
            );

            System.out.println(
                    "Start Time     : "
                            + event.startTime()
            );

            System.out.println(
                    "End Time       : "
                            + event.endTime()
            );

            System.out.println(
                    "Description    : "
                            + event.description()
            );

            System.out.println(
                    "---------------------------------"
            );
        });
    }
}

