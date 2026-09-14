package com.akshay.assistant.tools.notes;

import java.util.List;
import java.util.UUID;

public interface NoteProvider {

    NoteResult<UUID> create_note(String title, String content, List<String> tags);

    NoteResult<Note> get_note(UUID noteId);

    NoteResult<Void> update_note(UUID noteId, NoteUpdate data);

    NoteResult<Note> delete_note(UUID noteId);

    NoteResult<NoteSearchResult> search_notes(
            String query,
            int limit,
            int offset
    );
}
