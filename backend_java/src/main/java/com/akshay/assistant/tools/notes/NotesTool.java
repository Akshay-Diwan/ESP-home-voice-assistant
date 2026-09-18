package com.akshay.assistant.tools.notes;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotesTool {

    private final NoteProvider provider;

    public NotesTool(NoteProvider provider) {
        this.provider = provider;
    }

    @Tool(
        name = "create_note",
        description = "Create a new note with a title, content, and optional tags."
    )
    public NoteResult<UUID> create_note(
            String title,
            String content,
            List<String> tags
    ) {
        return provider.create_note(
                title,
                content,
                tags != null ? tags : List.of()
        );
    }

    @Tool(
        name = "get_note",
        description = "Get a note by its UUID."
    )
    public NoteResult<Note> get_note(String note_id) {
        try {
            if (note_id == null) {
                throw new IllegalArgumentException();
            }

            return provider.get_note(UUID.fromString(note_id));

        } catch (IllegalArgumentException ex) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id must be a valid UUID"
            );
        }
    }

    @Tool(
        name = "update_note",
        description = "Update an existing note using its UUID and the fields to change."
    )
    public NoteResult<Void> update_note(
            String note_id,
            NoteUpdate data
    ) {
        try {
            return provider.update_note(
                    UUID.fromString(note_id),
                    data
            );

        } catch (IllegalArgumentException ex) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id must be a valid UUID"
            );
        }
    }

    @Tool(
        name = "delete_note",
        description = "Delete a note by its UUID."
    )
    public NoteResult<Note> delete_note(String note_id) {
        try {
            return provider.delete_note(
                    UUID.fromString(note_id)
            );

        } catch (IllegalArgumentException ex) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id must be a valid UUID"
            );
        }
    }

    @Tool(
        name = "search_notes",
        description = "Search the user's notes using a text query. Returns matching notes with pagination."
    )
    public NoteResult<NoteSearchResult> search_notes(
            String query,
            int limit,
            int offset
    ) {
        return provider.search_notes(
                query,
                limit,
                offset
        );
    }
}