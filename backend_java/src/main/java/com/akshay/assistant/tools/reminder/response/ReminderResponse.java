package com.akshay.assistant.tools.reminder.response;
import java.util.List;

import com.akshay.assistant.tools.reminder.models.ReminderModel;
import com.akshay.assistant.tools.response.Response;
import com.akshay.assistant.tools.response.ResponseStatus;

public class ReminderResponse extends Response {
    public final String reminder_id;

    //GET type
    public ReminderModel data;

    //LIST type
    public List<ReminderModel> reminder_list;
    
    public ReminderResponse(Builder builder){
        super(builder.status, java.util.Optional.ofNullable(builder.error), java.util.Optional.ofNullable(builder.message));
        this.reminder_id = builder.reminder_id;
        this.data = builder.data;
        this.reminder_list = builder.reminder_list;
    }

    public static class Builder{
        final private ResponseStatus status;
        private String error;
        private String message;
        final private ResponseType type;
        //Create Type
        private String reminder_id;

        //GET type
        private ReminderModel data;

        //LIST type
        private List<ReminderModel> reminder_list;

        public Builder(ResponseStatus status, ResponseType type){
            this.status = status;
            this.type = type;
        }
        public Builder setError(String error){
            if(this.status != ResponseStatus.ERROR){
                throw new IllegalArgumentException("Error can only be set if status is ERROR");
            }
            this.error = error;
            return this;
        }
        public Builder setMessage(String message){
            if(this.status == ResponseStatus.SUCCESS && this.type != ResponseType.OTHERS){
                throw new IllegalArgumentException("Message can only be set if status is ERROR or type is OTHERS");
            }
            this.message = message;
            return this;
        }
        public Builder setReminderId(String reminder_id){
            if(this.type != ResponseType.CREATE_REMINDER){
                throw new IllegalArgumentException("Reminder ID can only be set if type is CREATE_REMINDER");
            }
            this.reminder_id = reminder_id;
            return this;
        }
        public Builder setData(ReminderModel data){
            this.data = data;
            return this;
        }
        public Builder setReminderList(List<ReminderModel> reminder_list){
            this.reminder_list = reminder_list;
            return this;    
        }
        public ReminderResponse build(){
            return new ReminderResponse(this);
        }
        

    }

    
}