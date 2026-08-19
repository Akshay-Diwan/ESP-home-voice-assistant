package com.akshay.assistant.tools.errors;

public class EntryNotFound extends RuntimeException{
        public EntryNotFound(){
        super();
    }
    public EntryNotFound(String message){
        super(message);
    }
    public EntryNotFound(String message, Throwable cause){
        super(message, cause);
    }
}
