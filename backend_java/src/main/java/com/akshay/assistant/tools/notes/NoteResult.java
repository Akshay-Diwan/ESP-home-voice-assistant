package com.akshay.assistant.tools.notes;

public record NoteResult<T>(
        String status,
        T data,
        String error,
        String message
) {
    public static <T> NoteResult<T> success(T data) {
        return new NoteResult<>("success", data, null, null);
    }

    public static <T> NoteResult<T> error(String error, String message) {
        return new NoteResult<>("error", null, error, message);
    }
}
