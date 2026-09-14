package com.akshay.assistant.tools.notes;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Public tool surface.
 *
 * These are normal Java methods. There are deliberately no @GetMapping,
 * @PostMapping, @RestController, or other HTTP endpoint annotations here.
 */
@Service
public class NotesTool {

    private final NoteProvider provider;

    public NotesTool(NoteProvider provider) {
        this.provider = provider;
    }

    public NoteResult<UUID> create_note(
            String title,
            String content,
            List<String> tags
    ) {
        return provider.create_note(title, content, tags);
    }

    public NoteResult<UUID> create_note(
            String title,
            String content
    ) {
        return provider.create_note(title, content, List.of());
    }

    public NoteResult<Note> get_note(String note_id) {
        try {
            if(note_id == null) throw new IllegalArgumentException();
            return provider.get_note(UUID.fromString(note_id));
        } catch (IllegalArgumentException ex) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id must be a valid UUID"
            );
        }
    }

    public NoteResult<Void> update_note(
            String note_id,
            NoteUpdate data
    ) {
        try {
            return provider.update_note(UUID.fromString(note_id), data);
        } catch (IllegalArgumentException ex) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id must be a valid UUID"
            );
        }
    }

    public NoteResult<Note> delete_note(String note_id) {
        try {
            return provider.delete_note(UUID.fromString(note_id));
        } catch (IllegalArgumentException ex) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id must be a valid UUID"
            );
        }
    }

    public NoteResult<NoteSearchResult> search_notes(
            String query,
            List<String> tags,
            int limit,
            int offset
    ) {
        return provider.search_notes(query, tags, limit, offset);
    }

    public NoteResult<NoteSearchResult> search_notes(String query) {
        return provider.search_notes(query, List.of(), 20, 0);
    }

    public NoteResult<Void> add_tag(String note_id, String tag) {
        try {
            return provider.add_tag(UUID.fromString(note_id), tag);
        } catch (IllegalArgumentException ex) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id must be a valid UUID"
            );
        }
    }

    public NoteResult<Void> remove_tag(String note_id, String tag) {
        try {
            return provider.remove_tag(UUID.fromString(note_id), tag);
        } catch (IllegalArgumentException ex) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id must be a valid UUID"
            );
        }
    }
}
