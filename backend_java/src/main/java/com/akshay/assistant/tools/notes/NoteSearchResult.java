package com.akshay.assistant.tools.notes;

import java.util.List;

public record NoteSearchResult(
        int count,
        List<Note> note_list
) {}
