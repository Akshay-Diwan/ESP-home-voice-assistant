package com.akshay.assistant.tools.reminder.models;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.Set;

import com.akshay.assistant.tools.errors.InvalidDataException;
import com.akshay.assistant.tools.reminder.dto.ReminderRequestDTO;
import com.akshay.assistant.tools.reminder.entity.ReminderEntity;

public class ReminderModel {
    public final UUID reminder_id;
    public String reminder_name;
    public String description = "";
    public Repeat repeat;
    public Priority priority;
    public LocalDateTime reminder_time;
    public int version;
    public Set<Day> repeat_days;
    public Status status;
    public ReminderModel(ReminderEntity rem, int version){
        reminder_id = rem.getReminder_id();
        reminder_name = rem.getReminder_name();
        description = rem.getDescription();
        repeat = rem.getRepeat();
        priority = rem.getPriority();
        reminder_time = rem.getReminder_time();
        repeat_days = rem.getRepeat_days();
        status = rem.getStatus();
        this.version = version;

    }
    public ReminderModel(ReminderRequestDTO rem){
        reminder_name = "name";
        reminder_id = UUID.randomUUID();
    }
    public ReminderModel(Builder builder){
        this.reminder_id = builder.reminder_id;
        this.reminder_name = builder.reminder_name;
        this.description = builder.description;
        this.repeat = builder.repeat;
        this.priority = builder.priority;
        this.reminder_time = builder.reminder_time; 
        this.version = builder.version;
        this.repeat_days = builder.repeat_days;
        this.status = builder.status;
    }
    
    public static class Builder{
        private final UUID reminder_id;
        private final String reminder_name;
        private final LocalDateTime reminder_time;
        private String description = "";
        private Repeat repeat = Repeat.NONE;
        private Priority priority = Priority.MEDIUM;
        private final int version = 1;
        private Set<Day> repeat_days = new HashSet<>(Arrays.asList(new Day[]{Day.NONE}));
        private final Status status = Status.ACTIVE;
        
        public Builder(String reminder_name, LocalDateTime reminder_time){
            this.reminder_id = UUID.randomUUID();
            this.reminder_name = reminder_name;
            if(reminder_name.isEmpty()) throw new InvalidDataException("Name must not be empty");
            this.reminder_time = reminder_time;
        }
        public Builder setDescription(String description){
            this.description = description;
            return this;
        }
        public Builder setRepeat(Repeat repeat){
            this.repeat = repeat;
            return this;
        }
        public Builder setPriority(Priority priority){
            this.priority = priority;
            return this;
        }
        public Builder setRepeatDays(Set<Day> repeat_days){
            if(repeat != Repeat.CUSTOM){
                throw new IllegalArgumentException("Repeat days can only be set if repeat is set to CUSTOM");
            }
            this.repeat_days = repeat_days;
            return this;
        }

        public ReminderModel build(){
            return new ReminderModel(this);
        }
        
    }

}