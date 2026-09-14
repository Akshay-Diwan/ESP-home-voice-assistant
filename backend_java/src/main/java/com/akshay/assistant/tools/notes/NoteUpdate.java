package com.akshay.assistant.tools.notes;

import java.util.List;

/**
 * null means "do not change this field".
 * An empty tags list means "clear all tags".
 */
public record NoteUpdate(
        String title,
        String content,
        List<String> tags
) {}
