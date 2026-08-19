package com.akshay.assistant.tools.reminder.dto.request;
import java.util.UUID;

import com.akshay.assistant.tools.reminder.dto.ReminderRequestDTO;

public class UpdateRequestDTO extends ReminderRequestDTO {
    public UUID reminder_id;
    UpdateRequestDTO(){
        super();
    }

    @Override
    public void print(){
        System.out.println("Reminder ID: " + reminder_id);
        super.print();
    }
    

    
}