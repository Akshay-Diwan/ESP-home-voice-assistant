package com.akshay.assistant.tools.calendar;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Example only.
 *
 * Do not expose these methods through @GetMapping/@PostMapping.
 * The LLM/tool layer should invoke CalendarTool directly.
 */

@Configuration
public class CalendarToolExample {

    @Bean
    CommandLineRunner calendarToolExamples(CalendarTool calendarTool) {
        return args -> {
            // Example:
            //
            // CalendarResponse response = calendarTool.createEvent(
            //         "Team Meeting",
            //         LocalDateTime.of(2026, 9, 15, 10, 0),
            //         LocalDateTime.of(2026, 9, 15, 11, 0),
            //         "Weekly project meeting"
            // );
            //
            // System.out.println(response);
        };
    }
}
