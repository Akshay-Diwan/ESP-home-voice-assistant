package com.akshay.assistant.tools.reminder.entity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import com.akshay.assistant.tools.errors.InvalidDataException;
import com.akshay.assistant.tools.reminder.dto.ReminderRequestDTO;
import com.akshay.assistant.tools.reminder.dto.request.UpdateRequestDTO;
import com.akshay.assistant.tools.reminder.models.Day;
import com.akshay.assistant.tools.reminder.models.Priority;
import com.akshay.assistant.tools.reminder.models.Repeat;
import com.akshay.assistant.tools.reminder.models.Status;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
@Entity
@Getter
@Table(
    name = "reminders",
    indexes = {
        @Index(name = "idx_reminder_time", columnList = "reminder_time"),
        @Index(name = "idx_status", columnList = "status")
    }
    
)
public class ReminderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reminder_id", updatable = false, nullable = false)
    private UUID reminder_id;
    @Column(name = "reminder_name", nullable = false, length = 100)
    private String reminder_name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private Repeat repeat;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "reminder_time", nullable = false)
    private LocalDateTime reminder_time;

    @ElementCollection(targetClass = Day.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "repeat_days",
        joinColumns = @JoinColumn(name = "reminder_id")
    )
    @Column(name = "day")
    private Set<Day> repeat_days = EnumSet.noneOf(Day.class);

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public ReminderEntity(String reminder_name, String description, Repeat repeat, Priority priority, LocalDateTime reminder_time, Set<Day> repeat_days) {
        this.reminder_name = reminder_name;
        this.description = description;
        this.repeat = repeat;
        this.priority = priority;
        this.reminder_time = reminder_time;
        this.repeat_days = repeat_days;

    }
    public ReminderEntity() {}

    public ReminderEntity(ReminderRequestDTO dto){
        if(dto.reminder_name == null || dto.reminder_name.isEmpty()){
            throw new InvalidDataException("reminder name cannot be empty or null");
        }
        this.reminder_name = dto.reminder_name;
        this.description = dto.description == null? "": dto.description;
        this.priority = dto.priority == null? Priority.MEDIUM: dto.priority;
        this.repeat = dto.repeat == null ? Repeat.NONE: dto.repeat;
        if(dto.reminder_time == null){
            throw new InvalidDataException("reminder time cannot be null");
        }
        LocalDateTime now = LocalDateTime.now();
        if(dto.reminder_time.isBefore(now)){
            throw new InvalidDataException("reminder time cannot be in past");
        }
        this.reminder_time = dto.reminder_time;
        if(dto.repeat == Repeat.CUSTOM && dto.repeat_days.length == 0){
            throw new InvalidDataException("Please provide repeat days");
        }

        this.repeat_days = new HashSet<Day>(Arrays.asList(dto.repeat_days));
        System.out.println("Entity : ");
        System.out.println("reminder name : " + reminder_name);
        System.out.println("descritpion : " + description);
        System.out.println("repeat : " + repeat);
        System.out.println("priority : " + priority);
    }
    public ReminderEntity(UpdateRequestDTO dto){
        this.reminder_id = dto.reminder_id;
        ReminderRequestDTO data = dto;
        this(data);
    }
    public void snoozeReminder(long time_minutes){
        this.reminder_time = this.reminder_time.plusMinutes(time_minutes);
        this.status = Status.SNOOZED;
    }
    public void markAsExpired(){
        this.status = Status.EXPIRED;
    }
    public void setReminderTime(LocalDateTime new_time){
        if(new_time.isBefore(LocalDateTime.now())) throw new InvalidDataException("reminder time should not be in past");
        this.reminder_time = new_time;
    }
    public void updateValues(UpdateRequestDTO request){
        if(request.description != null){
            this.description = request.description;
        }
        if(request.reminder_name != null) {
            if(request.reminder_name.isEmpty()) throw new InvalidDataException("Name cannot be empty");
            else this.reminder_name = request.reminder_name;
        }
        if(request.reminder_time != null){
            this.setReminderTime(request.reminder_time);
        }
        if(request.priority != null){
            this.priority = request.priority;
        }
        if(request.repeat != null) {
            this.repeat = request.repeat;
        }
        if(request.repeat == Repeat.CUSTOM){
            if(request.repeat_days.length == 0) throw new InvalidDataException("No days given for custom repeat");
            this.repeat_days.addAll(Arrays.asList(request.repeat_days));
        }
        else {
            this.repeat_days.clear();
        }
    }
}