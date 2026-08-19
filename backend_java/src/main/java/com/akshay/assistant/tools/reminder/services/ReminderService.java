package com.akshay.assistant.tools.reminder.services;
import com.akshay.assistant.tools.errors.EntryNotFound;
import com.akshay.assistant.tools.errors.InvalidDataException;
import com.akshay.assistant.tools.reminder.dto.ReminderRequestDTO;
import com.akshay.assistant.tools.reminder.dto.request.UpdateRequestDTO;
import com.akshay.assistant.tools.reminder.entity.ReminderEntity;
import com.akshay.assistant.tools.reminder.repository.ReminderRepository;
import com.akshay.assistant.tools.reminder.models.ReminderModel;
import com.akshay.assistant.tools.reminder.models.Status;

import java.util.UUID;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.PriorityBlockingQueue;

class TimeComparator implements Comparator<ReminderModel>{
    @Override
    public int compare(ReminderModel a, ReminderModel b) {
       return a.reminder_time.isAfter(b.reminder_time) ?-1:1;
    }
}
@Service
public class ReminderService {
    // This class will contain the business logic for reminders
    private ReminderRepository repository;
    private PriorityBlockingQueue<ReminderModel> pq;
    private HashMap<UUID, Integer> latestVersionMap;
    public ReminderService(ReminderRepository repository){
        this.repository = repository;
        pq = new PriorityBlockingQueue<ReminderModel>(10, new TimeComparator());
        
        latestVersionMap = new HashMap<UUID, Integer>();
    }

    public ReminderModel create(ReminderRequestDTO reminderRequestDTO) throws InvalidDataException{
        // Add to DB       
        ReminderEntity reminder = new ReminderEntity(reminderRequestDTO);

        ReminderModel rem = new ReminderModel(repository.save(reminder), 1);
        // Add to cache
        pq.add(rem);
        latestVersionMap.put(rem.reminder_id, 1);
        return rem;
        

    }
    public ReminderModel update(UpdateRequestDTO updateRequestDTO){
        //  reminder = new ReminderEntity(updateRequestDTO);
        ReminderEntity reminder = repository.findByIdWithRepeatDays(updateRequestDTO.reminder_id)
        .orElseThrow(() -> new EntryNotFound("Reminder not found")); 
        reminder.updateValues(updateRequestDTO);

        ReminderModel rem = new ReminderModel(repository.save(reminder), latestVersionMap.getOrDefault(updateRequestDTO.reminder_id, 0) + 1);
        pq.add(rem);
        latestVersionMap.put(rem.reminder_id, latestVersionMap.getOrDefault(rem.reminder_id, 1));
        
        return rem;
    }

    public ReminderModel getReminder(UUID reminderId) {
        ReminderEntity reminder =
            repository.findByIdWithRepeatDays(reminderId)
                .orElseThrow(() ->
                    new EntryNotFound("Reminder Not Found")
                );

        int version =
            latestVersionMap.getOrDefault(reminderId, 1);

        return new ReminderModel(
            reminder,
            version
        );
    }
     public List<ReminderModel> listReminders(String status) {
        List<ReminderEntity> reminders;

        /*
         * ALL -> return everything.
         */
        reminders = repository.findAllWithRepeatDays();

        List<ReminderModel> result =
            new ArrayList<>();

        for (ReminderEntity reminder : reminders) {

            int version =
                latestVersionMap.getOrDefault(
                    reminder.getReminder_id(),
                    1
                );

            result.add(
                new ReminderModel(
                    reminder,
                    version
                )
            );
        }
        return result;
    }

    // ============================================================
    // DELETE REMINDER
    // ============================================================

    public void deleteReminder(UUID reminderId) {
        // First verify that the reminder exists. 
        repository.findById(reminderId)
                .orElseThrow(() ->
                    new EntryNotFound("Reminder not found")
                );
        // Delete from database.
        repository.deleteById(reminderId);

        //Invalidate this reminder in the scheduler/cache.
        latestVersionMap.remove(reminderId);

    }

    // ============================================================
    // SNOOZE REMINDER
    // ============================================================

    public ReminderModel snoozeReminder(
        UUID reminderId,
        long snooze_minutes
    ) {
       
        
        ReminderEntity reminder = repository.findById(reminderId)
        .orElseThrow(() ->
            new EntryNotFound("Reminder not found")
        );
    
        // Update reminder time
        reminder.snoozeReminder(snooze_minutes);
    
        int newVersion = latestVersionMap.getOrDefault(reminderId, 0) + 1;
        ReminderModel rem = new ReminderModel(reminder, newVersion);
        pq.add(rem);

        latestVersionMap.put(
            reminderId,
            newVersion
        );

        return rem;
    }
    @Scheduled(cron = "0 0 0 * * *")
    public void loadReminders(){
        List<ReminderEntity> rem_list = repository.findByStatusIn(List.of(Status.ACTIVE, Status.SNOOZED));
        List<ReminderModel> reminder_list = new LinkedList<ReminderModel>();
        for(ReminderEntity rem: rem_list){
            reminder_list.add(new ReminderModel(rem, 1));
        }
        pq.addAll(reminder_list);
    }
    // ============================================================
    // GETTERS FOR SCHEDULER
    // ============================================================

    public PriorityBlockingQueue<ReminderModel> getPriorityQueue() {
        return pq;
    }

    public HashMap<UUID, Integer> getLatestVersionMap() {
        return latestVersionMap;
    }

}
