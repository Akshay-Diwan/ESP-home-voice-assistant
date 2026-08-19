package com.akshay.assistant.tools.reminder.dto;

import java.time.LocalDateTime;

import com.akshay.assistant.tools.reminder.models.Day;
import com.akshay.assistant.tools.reminder.models.Priority;
import com.akshay.assistant.tools.reminder.models.Repeat;
// public class ReminderRequestDTO {
//     public String reminder_name;
//     public String description;
//     public Repeat repeat;
//     public Priority priority;
//     public LocalDateTime reminder_time;
//     public Day[] repeat_days;

//     public ReminderRequestDTO(Builder builder){
//         this.reminder_name = builder.reminder_name;
//         this.description = builder.description;
//         this.repeat = builder.repeat;
//         this.priority = builder.priority;
//         this.reminder_time = builder.reminder_time;
//         this.repeat_days = builder.repeat_days;
//     }
//     public static class Builder{
//         private String reminder_name;
//         private String description;
//         private Repeat repeat;
//         private Priority priority;
//         private LocalDateTime reminder_time;
//         private Day[] repeat_days;
//         public Builder(String reminder_name, LocalDateTime reminder_time){
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
//             if(this.repeat != Repeat.CUSTOM){
//                 throw new IllegalArgumentException("Repeat days cannot be empty for custom reminders");
//             }
//             this.repeat_days = repeat_days;
//             return this;
//         }
//         public ReminderRequestDTO build(){
//             return new ReminderRequestDTO(this);
//         }
//     }
// }
public class ReminderRequestDTO {
    public String reminder_name;
    public String description;
    public Repeat repeat;
    public Priority priority;
    public LocalDateTime reminder_time;
    public Day[] repeat_days = new Day[0];

    public ReminderRequestDTO(){}
    public void print(){
        System.out.println("Name: " + reminder_name);
        System.out.println("Description: " + description);
        System.out.println("Repeat: " + repeat);
        System.out.println("Priority: " + priority);
        System.out.println("Class of Priority" + priority.getClass());
        System.out.println("Reminder time: " + reminder_time);
        for(Day day: repeat_days){
            System.out.print(day + " ");
        }
        System.out.println();
    }
}