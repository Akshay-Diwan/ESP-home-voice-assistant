package com.akshay.assistant.tools.reminder;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.akshay.assistant.tools.errors.EntryNotFound;
import com.akshay.assistant.tools.errors.InvalidDataException;
import com.akshay.assistant.tools.reminder.dto.ReminderRequestDTO;
import com.akshay.assistant.tools.reminder.dto.request.UpdateRequestDTO;
import com.akshay.assistant.tools.reminder.models.ReminderModel;
import com.akshay.assistant.tools.reminder.response.ReminderResponse;
import com.akshay.assistant.tools.reminder.response.ResponseType;
import com.akshay.assistant.tools.reminder.services.ReminderService;
import com.akshay.assistant.tools.response.ResponseStatus;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.List;

@Service
public class ReminderProvider {
    private ReminderService reminderService;
    private ObjectMapper mapper;

    public ReminderProvider(ReminderService reminderService,
                            ObjectMapper mapper) {
        this.reminderService = reminderService;
        this.mapper = mapper;
    }
    // ============================================================
    // CREATE REMINDER
    // ============================================================
    public String create_reminder(String json_request){
        System.out.println("Received Request : " + json_request);
        try{
            ReminderRequestDTO data = mapper.readValue(json_request, ReminderRequestDTO.class);
            data.print();
            ReminderModel model = reminderService.create(data);
            ReminderResponse response = new ReminderResponse.Builder(ResponseStatus.SUCCESS, ResponseType.CREATE_REMINDER)
            .setReminderId(model.reminder_id.toString())
            .build();
            String responseJSON = mapper.writeValueAsString(response);
            System.out.println(responseJSON);
            return responseJSON;
        }
        catch(JacksonException e){
            System.out.println("Error mapping JSON to ReminderRequestDTO: " + e.getMessage());
            return "Error";
        }
        catch(InvalidDataException e){
            System.out.println("Error is : " + e);
            ReminderResponse res = new ReminderResponse.Builder(ResponseStatus.ERROR, ResponseType.OTHERS).
            setError("InvalidData")
            .setMessage(e.getMessage()).build();
            String responseJSON = mapper.writeValueAsString(res);
            return responseJSON;
        }

    }

    // ============================================================
    // UPDATE REMINDER
    // ============================================================
    public String update_reminder(String json_request){
        UpdateRequestDTO data = mapper.readValue(json_request, UpdateRequestDTO.class);
        try{
             reminderService.update(data);
            ReminderResponse res = new ReminderResponse.Builder(ResponseStatus.SUCCESS, ResponseType.OTHERS)
            .setMessage("Reminder updated successfully.")
            .build();
            return mapper.writeValueAsString(res);
        }
        catch(EntryNotFound e){
            System.out.println(e);
            ReminderResponse res = new ReminderResponse.Builder(ResponseStatus.ERROR, ResponseType.OTHERS)
            .setError("EntryNotFound")
            .setMessage(e.getMessage()).build();
            return mapper.writeValueAsString(res);
        }
         catch(InvalidDataException e){
            System.out.println("Error is : " + e);
            ReminderResponse res = new ReminderResponse.Builder(ResponseStatus.ERROR, ResponseType.OTHERS).
            setError("InvalidData")
            .setMessage(e.getMessage()).build();
            String responseJSON = mapper.writeValueAsString(res);
            return responseJSON;
        }
    }


    // ============================================================
    // GET REMINDER
    // ============================================================
    public String getReminder(String json_request) {
        try {
            JsonNode request = mapper.readTree(json_request);
            UUID reminderId = UUID.fromString(request.get("reminder_id").stringValue());

            ReminderModel reminder = reminderService.getReminder(reminderId);
            
            return mapper.writeValueAsString(new ReminderResponse.Builder(
                ResponseStatus.SUCCESS,
                ResponseType.GET_REMINDERS
            )
            .setData(reminder)
            .build());

        } 
        catch(EntryNotFound e){
            System.out.println(e);
            ReminderResponse res = new ReminderResponse.Builder(ResponseStatus.ERROR, ResponseType.OTHERS)
            .setError("EntryNotFound")
            .setMessage(e.getMessage()).build();
            return mapper.writeValueAsString(res);
        }
        catch (RuntimeException e) {
            System.out.println(e);
            System.out.println(e.getMessage());
            
            if ("ReminderNotFound".equals(e.getMessage())) {

                return mapper.writeValueAsString(
                new ReminderResponse.Builder(
                    ResponseStatus.ERROR,
                    ResponseType.GET_REMINDERS
                )
                .setError("ReminderNotFound")
                .setMessage("Reminder does not exist.")
                .build());
            }

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.ERROR,
                ResponseType.GET_REMINDERS
            )
            .setError("InvalidRequest")
            .setMessage(e.getMessage())
            .build());
        }
    }

    // ============================================================
    // LIST REMINDERS
    // ============================================================

    public String listReminders(String json) {
        try {
            JsonNode request = mapper.readTree(json);
            String status = "ALL";
            /*
             * status is optional.
             *
             * {}
             *
             * means ALL.
             */
            if (request.has("status") && !request.get("status").isNull()) {
                status = request.get("status").stringValue();
            }
            List<ReminderModel> reminders =
                reminderService.listReminders(status);

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.SUCCESS,
                ResponseType.LIST_REMINDERS
            )
            .setReminderList(reminders)
            .build());

        } catch (IllegalArgumentException e) {

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.ERROR,
                ResponseType.LIST_REMINDERS
            )
            .setError("InvalidStatus")
            .setMessage(e.getMessage())
            .build());

        } catch (Exception e) {

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.ERROR,
                ResponseType.LIST_REMINDERS
            )
            .setError("InvalidRequest")
            .setMessage(e.getMessage())
            .build());
        }
    }

    // ============================================================
    // DELETE REMINDER
    // ============================================================

    public String deleteReminder(String json) {

        try {
            JsonNode request = mapper.readTree(json);
            String reminderId = request.get("reminder_id").stringValue();

            UUID id = UUID.fromString(reminderId);

            reminderService.deleteReminder(id);

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.SUCCESS,
                ResponseType.OTHERS
            )
            .setMessage("Reminder deleted successfully.")
            .build());

        }
        catch(EntryNotFound e){
            System.out.println(e);
            ReminderResponse res = new ReminderResponse.Builder(ResponseStatus.ERROR, ResponseType.OTHERS)
            .setError("EntryNotFound")
            .setMessage(e.getMessage()).build();
            return mapper.writeValueAsString(res);
        }
        catch (RuntimeException e) {

            if ("ReminderNotFound".equals(e.getMessage())) {

                return mapper.writeValueAsString(
                new ReminderResponse.Builder(
                    ResponseStatus.ERROR,
                    ResponseType.OTHERS
                )
                .setError("ReminderNotFound")
                .setMessage("Reminder does not exist.")
                .build());
            }

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.ERROR,
                ResponseType.OTHERS
            )
            .setError("InvalidRequest")
            .setMessage(e.getMessage())
            .build());

        } catch (Exception e) {

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.ERROR,
                ResponseType.OTHERS
            )
            .setError("InvalidRequest")
            .setMessage(e.getMessage())
            .build());
        }
    }

    // ============================================================
    // SNOOZE REMINDER
    // ============================================================

    public String snoozeReminder(String json) {

        try {

            JsonNode request = mapper.readTree(json);
            String reminderId = request.get("reminder_id").stringValue();
            long snooze_minutes = request.has("snooze_minutes") ? request.get("snooze_minutes").asLong() : 10;

            UUID id = UUID.fromString(reminderId);

                reminderService.snoozeReminder(
                    id,
                    snooze_minutes
                );

            return mapper.writeValueAsString(new ReminderResponse.Builder(
                ResponseStatus.SUCCESS,
                ResponseType.OTHERS
            )
            .setMessage("Reminder snoozed successfully.")
            .build());

        }
        catch(EntryNotFound e){
            System.out.println(e);
            ReminderResponse res = new ReminderResponse.Builder(ResponseStatus.ERROR, ResponseType.OTHERS)
            .setError("EntryNotFound")
            .setMessage(e.getMessage()).build();
            return mapper.writeValueAsString(res);
        } 
        catch (RuntimeException e) {

            if ("ReminderNotFound".equals(e.getMessage())) {

                return mapper.writeValueAsString( 
                    new ReminderResponse.Builder(
                    ResponseStatus.ERROR,
                    ResponseType.GET_REMINDERS
                )
                .setError("ReminderNotFound")
                .setMessage("Reminder does not exist.")
                .build());
            }

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.ERROR,
                ResponseType.GET_REMINDERS
            )
            .setError("InvalidRequest")
            .setMessage(e.getMessage())
            .build());

        } catch (Exception e) {

            return mapper.writeValueAsString(
            new ReminderResponse.Builder(
                ResponseStatus.ERROR,
                ResponseType.GET_REMINDERS
            )
            .setError("InvalidRequest")
            .setMessage(e.getMessage())
            .build());
        }
    }
}
