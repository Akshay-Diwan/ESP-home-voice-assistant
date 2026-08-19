package com.akshay.assistant.tools.errors;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(){
        super();
    }
    public InvalidDataException(String message){
        super(message);
    }
    public InvalidDataException(String message, Throwable cause){
        super(message, cause);
    }
}
