package com.akshay.assistant.tools.reminder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class reminderProviderTest {

    @Autowired
    private ReminderProvider reminderProvider;
    @Autowired
    private ObjectMapper objectMapper;



    // =========================================================
    // CREATE REMINDER
    // =========================================================

    @Test
    void createReminder_success() throws Exception {

        String json = """
        {
            "reminder_name": "Meeting",
            "reminder_time": "2030-07-20T10:00:00",
            "description": "Project discussion",
            "repeat": "NONE",
            "priority": "HIGH"
        }
        """;

        String response = reminderProvider.create_reminder(json);
        JsonNode result = objectMapper.readTree(response);
        System.out.println("Create Response: " + response);
        assertEquals("SUCCESS", result.get("status").asString());
        assertTrue(result.has("reminder_id"));
        assertFalse(result.get("reminder_id").asString().isEmpty());
    }

    @Test
    void createReminder_emptyName() throws Exception {

        String json = """
        {
            "reminder_name": "",
            "reminder_time": "2030-07-20T10:00:00",
            "description": "Project discussion",
            "repeat": "NONE",
            "priority": "MEDIUM"
        }
        """;

        String response = reminderProvider.create_reminder(json);

        JsonNode result = objectMapper.readTree(response);
        System.out.println("Response: " + response);
        assertEquals("ERROR", result.get("status").asString());
        assertEquals(
            "InvalidData",
            result.get("error").asString()
        );
    }

    @Test
    void createReminder_pastTime() throws Exception {

        String json = """
        {
            "reminder_name": "Meeting",
            "reminder_time": "2020-07-20T10:00:00",
            "description": "Project discussion",
            "repeat": "NONE",
            "priority": "MEDIUM"
        }
        """;

        String response = reminderProvider.create_reminder(json);

        JsonNode result = objectMapper.readTree(response);

        assertEquals("ERROR", result.get("status").asString());
        assertEquals(
            "InvalidData",
            result.get("error").asString()
        );
    }


    // =========================================================
    // GET REMINDER
    // =========================================================

    @Test
    void getReminder_success() throws Exception {

        String createJson = """
        {
            "reminder_name": "Meeting",
            "reminder_time": "2030-07-20T10:00:00",
            "description": "Project discussion",
            "repeat": "NONE",
            "priority": "HIGH"
        }
        """;

        String createResponse =
                reminderProvider.create_reminder(createJson);

        JsonNode createResult =
                objectMapper.readTree(createResponse);

        String id =
                createResult.get("reminder_id").asString();

        String getJson = """
        {
            "reminder_id": "%s"
        }
        """.formatted(id);


        String response =
                reminderProvider.getReminder(getJson);

        System.out.println("get Response : " + response);

        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "SUCCESS",
            result.get("status").asString()
        );

        assertEquals(
            id,
            result.get("data").get("reminder_id").asString()
        );

        assertEquals(
            "Meeting",
            result.get("data").get("reminder_name").asString()
        );

        assertEquals(
            "Project discussion",
            result.get("data").get("description").asString()
        );

        assertEquals(
            "NONE",
            result.get("data").get("repeat").asString()
        );

        assertEquals(
            "HIGH",
            result.get("data").get("priority").asString()
        );

        assertEquals(
            "ACTIVE",
            result.get("data").get("status").asString()
        );
    }

    @Test
    void getReminder_notFound() throws Exception {

        String json = """
        {
            "reminder_id": "00000000-0000-0000-0000-000000000000"
        }
        """;

        String response =
                reminderProvider.getReminder(json);

        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "ERROR",
            result.get("status").asString()
        );

        assertEquals(
            "EntryNotFound",
            result.get("error").asString()
        );

    }


    // =========================================================
    // LIST REMINDERS
    // =========================================================

    @Test
    void listReminders_all() throws Exception {

        createReminder("Meeting");
        createReminder("Doctor Appointment");

        String json = """
        {
            "status": "ALL"
        }
        """;

        String response =
                reminderProvider.listReminders(json);
        System.out.println("Response List : " + response);
        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "SUCCESS",
            result.get("status").asString()
        );

        assertTrue(
            result.get("reminder_list").isArray()
        );
    }

    @Test
    void listReminders_active() throws Exception {

        createReminder("Meeting");

        String json = """
        {
            "status": "ACTIVE"
        }
        """;

        String response =
                reminderProvider.listReminders(json);

        System.out.println("Response List : " + response);
        
        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "SUCCESS",
            result.get("status").asString()
        );

        assertEquals(
            "ACTIVE",
            result.get("reminder_list")
                    .get(0)
                    .get("status")
                    .asString()
        );
    }


    // =========================================================
    // UPDATE REMINDER
    // =========================================================

    @Test
    void updateReminder_success() throws Exception {

        String id = createReminder("Meeting");

        String json = """
        {
            "reminder_id": "%s",
            "reminder_name": "Updated Meeting",
            "priority": "HIGH"
        }
        """.formatted(id);

        String response =
                reminderProvider.update_reminder(json);

        JsonNode result = objectMapper.readTree(response);

        assertEquals(
            "SUCCESS",
            result.get("status").asString()
        );

        assertEquals(
            "Reminder updated successfully.",
            result.get("message").asString()
        );
    }

    @Test
    void updateReminder_notFound() throws Exception {

        String json = """
        {
            "reminder_id": "00000000-0000-0000-0000-000000000000",
            "reminder_name": "Updated Meeting"
        }
        """;

        String response =
                reminderProvider.update_reminder(json);

        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "ERROR",
            result.get("status").asString()
        );

        assertEquals(
            "EntryNotFound",
            result.get("error").asString()
        );
    }

    @Test
    void updateReminder_pastTime() throws Exception {

        String id = createReminder("Meeting");

        String json = """
        {
            "reminder_id": "%s",
            "reminder_time": "2020-07-20T10:00:00"
        }
        """.formatted(id);

        String response =
                reminderProvider.update_reminder(json);

        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "ERROR",
            result.get("status").asString()
        );

        assertEquals(
            "InvalidData",
            result.get("error").asString()
        );
    }


    // =========================================================
    // DELETE REMINDER
    // =========================================================

    @Test
    void deleteReminder_success() throws Exception {

        String id = createReminder("Meeting");

        String json = """
        {
            "reminder_id": "%s"
        }
        """.formatted(id);

        String response =
                reminderProvider.deleteReminder(json);

        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "SUCCESS",
            result.get("status").asString()
        );

        assertEquals(
            "Reminder deleted successfully.",
            result.get("message").asString()
        );
    }

    @Test
    void deleteReminder_notFound() throws Exception {

        String json = """
        {
            "reminder_id": "00000000-0000-0000-0000-000000000000"
        }
        """;

        String response =
                reminderProvider.deleteReminder(json);

        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "ERROR",
            result.get("status").asString()
        );

        assertEquals(
            "EntryNotFound",
            result.get("error").asString()
        );
    }


    // =========================================================
    // SNOOZE REMINDER
    // =========================================================

    @Test
    void snoozeReminder_success() throws Exception {

        String id = createReminder("Meeting");

        String json = """
        {
            "reminder_id": "%s",
            "duration_minutes": 10
        }
        """.formatted(id);

        String response =
                reminderProvider.snoozeReminder(json);
        System.out.println("Snooze Response: " + response);
        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "SUCCESS",
            result.get("status").asString()
        );

        assertEquals(
            "Reminder snoozed successfully.",
            result.get("message").asString()
        );
    }

    @Test
    void snoozeReminder_notFound() throws Exception {

        String json = """
        {
            "reminder_id": "00000000-0000-0000-0000-000000000000",
            "duration_minutes": 10
        }
        """;

        String response =
                reminderProvider.snoozeReminder(json);

        JsonNode result =
                objectMapper.readTree(response);

        assertEquals(
            "ERROR",
            result.get("status").asString()
        );

        assertEquals(
            "EntryNotFound",
            result.get("error").asString()
        );
    }


    // =========================================================
    // HELPER
    // =========================================================

    private String createReminder(String name)
            throws Exception {

        String json = """
        {
            "reminder_name": "%s",
            "reminder_time": "2030-07-20T10:00:00",
            "description": "",
            "repeat": "NONE",
            "priority": "MEDIUM"
        }
        """.formatted(name);

        String response =
                reminderProvider.create_reminder(json);

        JsonNode result =
                objectMapper.readTree(response);

        return result.get("reminder_id").asString();
    }
}