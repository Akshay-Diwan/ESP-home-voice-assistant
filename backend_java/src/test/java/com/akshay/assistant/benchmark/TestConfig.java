package com.akshay.assistant.benchmark;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.akshay.assistant.tools.calendar.CalendarTool;
import com.akshay.assistant.tools.messaging.DiscordChatTool;
import com.akshay.assistant.tools.notes.NotesTool;
import com.akshay.assistant.tools.reminder.mcp.ReminderTools;

@TestConfiguration 
@ComponentScan("com.akshay.test")
public class TestConfig {
    @Bean
    public DiscordChatTool mockDiscordChatTool() {
        return Mockito.mock(DiscordChatTool.class);
    }
    @Bean 
    public ReminderTools mockReminderTools(){
        return Mockito.mock(ReminderTools.class);
    }
    @Bean
    public NotesTool mockNotesTool(){
        return Mockito.mock(NotesTool.class);
    }
    @Bean
    public CalendarTool mockCalendarTool(){
        return Mockito.mock(CalendarTool.class);
    }

}