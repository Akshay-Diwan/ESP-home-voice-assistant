package com.akshay.assistant.tools.reminder.mcp;

import com.akshay.assistant.tools.reminder.dto.ReminderRequestDTO;
import com.akshay.assistant.tools.reminder.dto.request.UpdateRequestDTO;
import com.akshay.assistant.tools.reminder.models.ReminderModel;
import com.akshay.assistant.tools.reminder.services.ReminderService;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ReminderMcpTools {

    private final ReminderService reminderService;

    public ReminderMcpTools(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Tool(
        name = "create_reminder",
        description = "Create a new reminder"
    )
    public ReminderModel createReminder(
            ReminderRequestDTO request
    ) throws Exception {

        return reminderService.create(request);
    }

    @Tool(
        name = "get_reminder",
        description = "Get a reminder by its ID"
    )
    public ReminderModel getReminder(
            UUID reminderId
    ) {

        return reminderService.getReminder(reminderId);
    }

    @Tool(
        name = "list_reminders",
        description = "List reminders, optionally filtered by status"
    )
    public List<ReminderModel> listReminders(
            String status
    ) {

        return reminderService.listReminders(status);
    }

    @Tool(
        name = "update_reminder",
        description = "Update an existing reminder"
    )
    public ReminderModel updateReminder(
            UpdateRequestDTO request
    ) {

        return reminderService.update(request);
    }

    @Tool(
        name = "delete_reminder",
        description = "Delete a reminder by ID"
    )
    public String deleteReminder(
            UUID reminderId
    ) {

        reminderService.deleteReminder(reminderId);

        return "Reminder deleted successfully";
    }

    @Tool(
        name = "snooze_reminder",
        description = "Snooze a reminder for a specified number of minutes"
    )
    public ReminderModel snoozeReminder(
            UUID reminderId,
            long minutes
    ) {

        return reminderService.snoozeReminder(
                reminderId,
                minutes
        );
    }
}