package com.akshay.assistant.tools.response;
import java.util.Optional;

public class Response{
    public final ResponseStatus status;
    public final String error;
    public final String message;
    
    public Response(ResponseStatus status, Optional<String> error, Optional<String> message){
        this.status = status;
        this.error = error.orElse(null);
        this.message = message.orElse(null);
    }
}