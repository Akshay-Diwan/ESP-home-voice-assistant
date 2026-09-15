package com.akshay.assistant.tools.messaging;

public class ChatToolException extends RuntimeException {
    private final String errorCode;

    public ChatToolException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ChatToolException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
