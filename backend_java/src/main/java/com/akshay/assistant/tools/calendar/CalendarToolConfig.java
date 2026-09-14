package com.akshay.assistant.tools.calendar;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class CalendarToolConfig {

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public Calendar googleCalendarService(
            @Value("${calendar.google.application-name}") String applicationName,
            @Value("${calendar.google.credentials-file}") String credentialsFilePath,
            ResourceLoader resourceLoader
    ) throws GeneralSecurityException, IOException {

        // Use modern non-deprecated NetHttpTransport constructor
        NetHttpTransport transport = new NetHttpTransport();

        // Safe resource loading for classpath or external file locations
        Resource resource = resourceLoader.getResource(credentialsFilePath);
        if (!resource.exists()) {
            throw new FileNotFoundException(
                    "Service Account JSON file not found at: " + credentialsFilePath +
                    ". Place your JSON key file in src/main/resources/."
            );
        }

        // Load Service Account credentials with explicit Calendar scope
        GoogleCredentials credentials;
        try (InputStream input = resource.getInputStream()) {
            credentials = GoogleCredentials.fromStream(input)
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
        }

        // Build and return the Google Calendar service instance
        return new Calendar.Builder(
                transport,
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        )
        .setApplicationName(applicationName)
        .build();
    }
}