package com.akshay.assistant.tools.reminder;

import com.akshay.assistant.tools.reminder.dto.ReminderRequestDTO;
import com.akshay.assistant.tools.reminder.dto.request.UpdateRequestDTO;
import com.akshay.assistant.tools.reminder.models.ReminderModel;
import com.akshay.assistant.tools.reminder.services.ReminderService;
import com.akshay.assistant.tools.errors.InvalidDataException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // ============================================================
    // CREATE REMINDER
    // ============================================================

    @PostMapping
    public ResponseEntity<ReminderModel> createReminder(
            @RequestBody ReminderRequestDTO request
    ) throws InvalidDataException {

        ReminderModel reminder =
                reminderService.create(request);

        return ResponseEntity.ok(reminder);
    }

    // ============================================================
    // GET REMINDER
    // ============================================================

    @GetMapping("/{reminderId}")
    public ResponseEntity<ReminderModel> getReminder(
            @PathVariable UUID reminderId
    ) {

        ReminderModel reminder =
                reminderService.getReminder(reminderId);

        return ResponseEntity.ok(reminder);
    }

    // ============================================================
    // LIST REMINDERS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<ReminderModel>> listReminders(
            @RequestParam(required = false) String status
    ) {

        List<ReminderModel> reminders =
                reminderService.listReminders(status);

        return ResponseEntity.ok(reminders);
    }

    // ============================================================
    // UPDATE REMINDER
    // ============================================================

    @PutMapping("/{reminderId}")
    public ResponseEntity<ReminderModel> updateReminder(
            @PathVariable UUID reminderId,
            @RequestBody UpdateRequestDTO request
    ) {

        request.reminder_id = reminderId;

        ReminderModel reminder =
                reminderService.update(request);

        return ResponseEntity.ok(reminder);
    }

    // ============================================================
    // DELETE REMINDER
    // ============================================================

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable UUID reminderId
    ) {

        reminderService.deleteReminder(reminderId);

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // SNOOZE REMINDER
    // ============================================================

    @PostMapping("/{reminderId}/snooze")
    public ResponseEntity<ReminderModel> snoozeReminder(
            @PathVariable UUID reminderId,
            @RequestParam long minutes
    ) {

        ReminderModel reminder =
                reminderService.snoozeReminder(
                        reminderId,
                        minutes
                );

        return ResponseEntity.ok(reminder);
    }
}

