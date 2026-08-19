// package com.akshay.assistant.tools.reminder.entity;
// import java.time.LocalDateTime;
// import java.util.UUID;

// import com.akshay.assistant.tools.reminder.models.Day;
// import com.akshay.assistant.tools.reminder.models.Priority;
// import com.akshay.assistant.tools.reminder.models.Repeat;
// import com.akshay.assistant.tools.reminder.models.Status;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// @Entity
// @Table(name = "reminders")
// public class Reminder {
//     @Id
//     @GeneratedValue(strategy = GenerationType.UUID)
//     private UUID reminder_id;
//     private String reminder_name;
//     private String description;
//     private Repeat repeat;
//     private Priority priority;
//     private LocalDateTime reminder_time;
//     private int version;
//     private Day[] repeat_days;
//     private Status status;

//     public Reminder() {}
//     public Reminder(Builder builder){
//         this.reminder_id = builder.reminder_id;
//         this.reminder_name = builder.reminder_name;
//         this.description = builder.description;
//         this.repeat = builder.repeat;
//         this.priority = builder.priority;
//         this.reminder_time = builder.reminder_time; 
//         this.version = builder.version;
//         this.repeat_days = builder.repeat_days;
//         this.status = builder.status;
//     }
//     public static class Builder{
//         private final UUID reminder_id;
//         private final String reminder_name;
//         private final LocalDateTime reminder_time;
//         private String description = "";
//         private Repeat repeat = Repeat.NONE;
//         private Priority priority = Priority.MEDIUM;
//         private final int version = 1;
//         private Day[] repeat_days = {};
//         private final Status status = Status.ACTIVE;

//         public Builder(String reminder_name, LocalDateTime reminder_time){
//             this.reminder_id = UUID.randomUUID();
//             this.reminder_name = reminder_name;
//             this.reminder_time = reminder_time;
//         }
//         public Builder setDescription(String description){
//             this.description = description;
//             return this;
//         }
//         public Builder setRepeat(Repeat repeat){
//             this.repeat = repeat;
//             return this;
//         }
//         public Builder setPriority(Priority priority){
//             this.priority = priority;
//             return this;
//         }
//         public Builder setRepeatDays(Day[] repeat_days){
//             if(repeat != Repeat.CUSTOM){
//                 throw new IllegalArgumentException("Repeat days can only be set if repeat is set to CUSTOM");
//             }
//             this.repeat_days = repeat_days;
//             return this;
//         }

//         public Reminder build(){
//             return new Reminder(this);
//         }
//     }
//         public String getName(){
//             return this.reminder_name;
//         }
//         public UUID getId(){
//             return this.reminder_id;
//         }
//         public LocalDateTime getTime(){
//             return this.reminder_time;
//         }
//         public Repeat getRepeat(){
//             return this.repeat;
//         }
//         public Priority getPriority(){
//             return this.priority;
//         }
//         public String getDescription(){
//             return this.description;
//         }
//         public Day[] getRepeatDays(){
//             return this.repeat_days;
//         }
//         public Status getStatus(){
//             return this.status;
//         }
//         public int getVersion(){
//             return this.version;
//         }

//         public void setName(String name){
//             if(name == null || name.length() == 0){
//                 throw new IllegalArgumentException("Name cannot be null or empty");
//             }
//             this.reminder_name = name;
//         }
//         public void setDescription(String description){
//             this.description = description;
//         }
//         public void setRepeat(Repeat repeat){
//             this.repeat = repeat;
//         }
//         public void setPriority(Priority priority){
//             this.priority = priority;
//         }
//         public void setTime(LocalDateTime time){
//             LocalDateTime now = LocalDateTime.now();
//             if(time.isBefore(now)){
//                 throw new IllegalArgumentException("Time cannot be in the past");
//             }
//             this.reminder_time = time;
//         }
//         public void setRepeatDays(Day[] repeat_days){
//             if(repeat != Repeat.CUSTOM){
//                 throw new IllegalArgumentException("Repeat days can only be set if repeat is set to CUSTOM");
//             }
//             this.repeat_days = repeat_days;
//         }
//         public void setStatus(Status status){
//             this.status = status;
//         }
//         public void incrementVersion(){
//             this.version += 1;
//         }
// }