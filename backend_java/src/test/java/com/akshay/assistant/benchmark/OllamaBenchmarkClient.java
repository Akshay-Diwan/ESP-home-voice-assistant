package com.akshay.assistant.benchmark;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.akshay.assistant.tools.calendar.CalendarTool;
import com.akshay.assistant.tools.messaging.mcp.DiscordTools;
import com.akshay.assistant.tools.notes.NotesTool;
import com.akshay.assistant.tools.reminder.mcp.ReminderTools;

import java.time.Duration;
import java.util.List;

@Component
public class OllamaBenchmarkClient {

    private final RestClient client;
    @Autowired 
    private DiscordTools discordTools;
    @Autowired 
    private ReminderTools reminderTools;
    @Autowired 
    @Qualifier("mockCalendarTool")
    private CalendarTool calendarTools;
    @Autowired 
    @Qualifier("mockNotesTool")
    private NotesTool notesTools;

    public OllamaBenchmarkClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofMinutes(5));
        factory.setConnectTimeout(Duration.ofSeconds(10));

        this.client = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .requestFactory(factory)
                .build();
    }

    // Standard run with prompt string
    public OllamaResponse run(String model, String prompt) {
        ToolMapper.fromSpringAiComponent(discordTools);
        ToolMapper.fromSpringAiComponent(reminderTools);
        ToolMapper.fromSpringAiComponent(calendarTools);
        ToolMapper.fromSpringAiComponent(notesTools);
        List<OllamaRequest.Tool> tools = ToolMapper.tools;
        

        return run(model, List.of(OllamaRequest.Message.user(prompt)), tools);
    }

    // Full run supporting messages and optional tool declarations
    public OllamaResponse run(String model, List<OllamaRequest.Message> messages, List<OllamaRequest.Tool> tools) {
        OllamaRequest request = new OllamaRequest(model, messages, tools, false);

        return client.post()
                .uri("/api/chat")
                .body(request)
                .retrieve()
                .body(OllamaResponse.class);
    }
}