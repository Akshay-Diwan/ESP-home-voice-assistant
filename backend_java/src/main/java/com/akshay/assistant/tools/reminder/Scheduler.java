package com.akshay.assistant.tools.reminder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.stereotype.Component;

import java.util.UUID;

import com.akshay.assistant.tools.reminder.entity.ReminderEntity;
import com.akshay.assistant.tools.reminder.models.ReminderModel;
import com.akshay.assistant.tools.reminder.models.Repeat;
import com.akshay.assistant.tools.reminder.repository.ReminderRepository;
import com.akshay.assistant.tools.reminder.services.ReminderService;

@Component
public class Scheduler extends Thread {
    private Object lock;
    private ReminderService reminderService;
    private ReminderRepository reminderRepository;
    @Override
    public void run() {
        PriorityBlockingQueue<ReminderModel> pq = reminderService.getPriorityQueue();
        HashMap<UUID, Integer> map = reminderService.getLatestVersionMap();
        while(true){
            try{
                ReminderModel top = pq.take();
                while(!map.containsKey(top.reminder_id) || top.version < map.get(top.reminder_id) ){
                    pq.poll();
                    top = pq.take();
                }
                synchronized(lock){
                    Duration sleepTime = Duration.between(LocalDateTime.now(), top.reminder_time);
                    lock.wait(sleepTime.getSeconds() * 1000);
                    if(top.reminder_time.isBefore(LocalDateTime.now())){
                        System.out.println("Reminder: " + top.reminder_id + " " + top.reminder_name);
                        pq.poll();
                        markExpireInDB(top.reminder_id);
                    }
                }
                
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }

        }
    }
    private void updateTime(ReminderEntity reminder){
        Repeat repeat_status = reminder.getRepeat();
        if(repeat_status == Repeat.DAILY){
            LocalDateTime new_time = reminder.getReminder_time().plusDays(1);
            reminder.setReminderTime(new_time);
        }
        else if(repeat_status == Repeat.MONTHLY){
            LocalDateTime new_time = reminder.getReminder_time().plusMonths(1);
            reminder.setReminderTime(new_time);
        }
        else if(repeat_status == Repeat.WEEKLY){
            LocalDateTime new_time = reminder.getReminder_time().plusWeeks(1);
            reminder.setReminderTime(new_time);
        }
        else {
            // Logic for custom Days
        }

    }
    private void markExpireInDB(UUID reminder_id){
        ReminderEntity reminder = reminderRepository.findById(reminder_id)
        .orElseThrow(() -> new RuntimeException("ReminderNotFound"));
        if(reminder.getRepeat() == Repeat.NONE)
            reminder.markAsExpired();
        else {
            //change timing
            updateTime(reminder);
        }
        reminderRepository.save(reminder);
    }
    public void informScheduler(){
        synchronized(lock){
            lock.notify();
        }
    }
}
