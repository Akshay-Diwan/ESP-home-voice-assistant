package com.akshay.assistant.tools.notes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotesToolTest {
@Mock
private NoteProvider provider;

private NotesTool notesTool;

private static final UUID NOTE_ID =
        UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

private static final String NOTE_ID_STRING =
        "22222222-2222-2222-2222-222222222222";

@BeforeEach
void setUp() {
    notesTool = new NotesTool(provider);
}

// ========================================================================
// CREATE NOTE
// ========================================================================

@Test
void createNote_shouldDelegateToProvider() {

    List<String> tags =
            List.of("java", "spring");

    NoteResult<UUID> expected =
            NoteResult.success(NOTE_ID);

    when(provider.create_note(
            "Spring Boot",
            "Learn Spring Boot",
            tags
    )).thenReturn(expected);

    NoteResult<UUID> result =
            notesTool.create_note(
                    "Spring Boot",
                    "Learn Spring Boot",
                    tags
            );

    assertSame(expected, result);

    verify(provider).create_note(
            "Spring Boot",
            "Learn Spring Boot",
            tags
    );

    verifyNoMoreInteractions(provider);
}

@Test
void createNote_withoutTags_shouldUseEmptyList() {

    NoteResult<UUID> expected =
            NoteResult.success(NOTE_ID);

    when(provider.create_note(
            "Spring Boot",
            "Learn Spring Boot",
            List.of()
    )).thenReturn(expected);

    NoteResult<UUID> result =
            notesTool.create_note(
                    "Spring Boot",
                    "Learn Spring Boot"
            );

    assertSame(expected, result);

    verify(provider).create_note(
            "Spring Boot",
            "Learn Spring Boot",
            List.of()
    );

    verifyNoMoreInteractions(provider);
}

@Test
void createNote_shouldReturnProviderError() {

    NoteResult<UUID> expected =
            NoteResult.error(
                    "DataInvalid",
                    "Title cannot be empty"
            );

    when(provider.create_note(
            "",
            "content",
            List.of()
    )).thenReturn(expected);

    NoteResult<UUID> result =
            notesTool.create_note(
                    "",
                    "content",
                    List.of()
            );

    assertSame(expected, result);

    assertEquals("error", result.status());
    assertEquals("DataInvalid", result.error());

    verify(provider).create_note(
            "",
            "content",
            List.of()
    );
}

// ========================================================================
// GET NOTE
// ========================================================================

@Test
void getNote_shouldConvertStringIdAndDelegate() {

    Note note = new Note(
            NOTE_ID,
            "Spring Boot",
            "Learn Spring Boot",
            OffsetDateTime.parse(
                    "2026-09-14T10:00:00Z"
            ),
            OffsetDateTime.parse(
                    "2026-09-14T11:00:00Z"
            ),
            List.of("java", "spring")
    );

    NoteResult<Note> expected =
            NoteResult.success(note);

    when(provider.get_note(NOTE_ID))
            .thenReturn(expected);

    NoteResult<Note> result =
            notesTool.get_note(NOTE_ID_STRING);

    assertSame(expected, result);

    assertEquals(
            NOTE_ID,
            result.data().id()
    );

    verify(provider).get_note(NOTE_ID);

    verifyNoMoreInteractions(provider);
}

@Test
void getNote_withInvalidUuid_shouldReturnDataInvalid() {

    NoteResult<Note> result =
            notesTool.get_note("not-a-uuid");

    assertEquals(
            "error",
            result.status()
    );

    assertEquals(
            "DataInvalid",
            result.error()
    );

    assertEquals(
            "note_id must be a valid UUID",
            result.message()
    );

    verifyNoInteractions(provider);
}

@Test
void getNote_withNullId_shouldReturnDataInvalid() {

    NoteResult<Note> result =
            notesTool.get_note(null);

    assertEquals(
            "error",
            result.status()
    );

    assertEquals(
            "DataInvalid",
            result.error()
    );

    verifyNoInteractions(provider);
}

@Test
void getNote_shouldReturnProviderError() {

    NoteResult<Note> expected =
            NoteResult.error(
                    "NoteNotFoundError",
                    "No note with id " + NOTE_ID + " found"
            );

    when(provider.get_note(NOTE_ID))
            .thenReturn(expected);

    NoteResult<Note> result =
            notesTool.get_note(NOTE_ID_STRING);

    assertSame(expected, result);

    assertEquals(
            "NoteNotFoundError",
            result.error()
    );

    verify(provider).get_note(NOTE_ID);
}

// ========================================================================
// UPDATE NOTE
// ========================================================================

@Test
void updateNote_shouldConvertIdAndDelegate() {

    NoteUpdate update =
            new NoteUpdate(
                    "New title",
                    "New content",
                    List.of("java", "updated")
            );

    NoteResult<Void> expected =
            NoteResult.success(null);

    when(provider.update_note(
            NOTE_ID,
            update
    )).thenReturn(expected);

    NoteResult<Void> result =
            notesTool.update_note(
                    NOTE_ID_STRING,
                    update
            );

    assertSame(expected, result);

    verify(provider).update_note(
            NOTE_ID,
            update
    );

    verifyNoMoreInteractions(provider);
}

@Test
void updateNote_shouldSupportPartialUpdate() {

    NoteUpdate update =
            new NoteUpdate(
                    null,
                    "Only content changed",
                    null
            );

    NoteResult<Void> expected =
            NoteResult.success(null);

    when(provider.update_note(
            NOTE_ID,
            update
    )).thenReturn(expected);

    NoteResult<Void> result =
            notesTool.update_note(
                    NOTE_ID_STRING,
                    update
            );

    assertSame(expected, result);

    verify(provider).update_note(
            NOTE_ID,
            update
    );
}

@Test
void updateNote_withInvalidUuid_shouldReturnDataInvalid() {

    NoteUpdate update =
            new NoteUpdate(
                    "New title",
                    null,
                    null
            );

    NoteResult<Void> result =
            notesTool.update_note(
                    "invalid-id",
                    update
            );

    assertEquals(
            "error",
            result.status()
    );

    assertEquals(
            "DataInvalid",
            result.error()
    );

    assertEquals(
            "note_id must be a valid UUID",
            result.message()
    );

    verifyNoInteractions(provider);
}

@Test
void updateNote_shouldReturnProviderError() {

    NoteUpdate update =
            new NoteUpdate(
                    "New title",
                    null,
                    null
            );

    NoteResult<Void> expected =
            NoteResult.error(
                    "NoteNotFoundError",
                    "No note with id " + NOTE_ID + " found"
            );

    when(provider.update_note(
            NOTE_ID,
            update
    )).thenReturn(expected);

    NoteResult<Void> result =
            notesTool.update_note(
                    NOTE_ID_STRING,
                    update
            );

    assertSame(expected, result);

    assertEquals(
            "NoteNotFoundError",
            result.error()
    );

    verify(provider).update_note(
            NOTE_ID,
            update
    );
}

// ========================================================================
// DELETE NOTE
// ========================================================================

@Test
void deleteNote_shouldConvertIdAndDelegate() {

    Note note = new Note(
            NOTE_ID,
            "Spring Boot",
            "Learn Spring Boot",
            OffsetDateTime.parse(
                    "2026-09-14T10:00:00Z"
            ),
            OffsetDateTime.parse(
                    "2026-09-14T11:00:00Z"
            ),
            List.of("java")
    );

    NoteResult<Note> expected =
            NoteResult.success(note);

    when(provider.delete_note(NOTE_ID))
            .thenReturn(expected);

    NoteResult<Note> result =
            notesTool.delete_note(
                    NOTE_ID_STRING
            );

    assertSame(expected, result);

    assertEquals(
            NOTE_ID,
            result.data().id()
    );

    verify(provider).delete_note(NOTE_ID);

    verifyNoMoreInteractions(provider);
}

@Test
void deleteNote_withInvalidUuid_shouldReturnDataInvalid() {

    NoteResult<Note> result =
            notesTool.delete_note(
                    "invalid-id"
            );

    assertEquals(
            "error",
            result.status()
    );

    assertEquals(
            "DataInvalid",
            result.error()
    );

    assertEquals(
            "note_id must be a valid UUID",
            result.message()
    );

    verifyNoInteractions(provider);
}

@Test
void deleteNote_shouldReturnProviderError() {

    NoteResult<Note> expected =
            NoteResult.error(
                    "NoteNotFoundError",
                    "No note with id " + NOTE_ID + " found"
            );

    when(provider.delete_note(NOTE_ID))
            .thenReturn(expected);

    NoteResult<Note> result =
            notesTool.delete_note(
                    NOTE_ID_STRING
            );

    assertSame(expected, result);

    assertEquals(
            "NoteNotFoundError",
            result.error()
    );

    verify(provider).delete_note(NOTE_ID);
}

// ========================================================================
// SEARCH NOTES
// ========================================================================

@Test
void searchNotes_shouldDelegateToProvider() {

    String query = "spring";
    List<String> tags = List.of("java");

    NoteSearchResult searchResult =
            new NoteSearchResult(
                    1,
                    List.of()
            );

    NoteResult<NoteSearchResult> expected =
            NoteResult.success(searchResult);

    when(provider.search_notes(
            query,
            tags,
            20,
            0
    )).thenReturn(expected);

    NoteResult<NoteSearchResult> result =
            notesTool.search_notes(
                    query,
                    tags,
                    20,
                    0
            );

    assertSame(expected, result);

    verify(provider).search_notes(
            query,
            tags,
            20,
            0
    );

    verifyNoMoreInteractions(provider);
}

@Test
void searchNotes_shouldSupportPagination() {

    NoteSearchResult searchResult =
            new NoteSearchResult(
                    50,
                    List.of()
            );

    NoteResult<NoteSearchResult> expected =
            NoteResult.success(searchResult);

    when(provider.search_notes(
            "spring",
            List.of("java"),
            10,
            20
    )).thenReturn(expected);

    NoteResult<NoteSearchResult> result =
            notesTool.search_notes(
                    "spring",
                    List.of("java"),
                    10,
                    20
            );

    assertSame(expected, result);

    verify(provider).search_notes(
            "spring",
            List.of("java"),
            10,
            20
    );
}

@Test
void searchNotes_withoutOptionalArguments_shouldUseDefaults() {

    NoteSearchResult searchResult =
            new NoteSearchResult(
                    1,
                    List.of()
            );

    NoteResult<NoteSearchResult> expected =
            NoteResult.success(searchResult);

    when(provider.search_notes(
            "spring",
            List.of(),
            20,
            0
    )).thenReturn(expected);

    NoteResult<NoteSearchResult> result =
            notesTool.search_notes("spring");

    assertSame(expected, result);

    verify(provider).search_notes(
            "spring",
            List.of(),
            20,
            0
    );

    verifyNoMoreInteractions(provider);
}

@Test
void searchNotes_shouldReturnEmptyResult() {

    NoteSearchResult searchResult =
            new NoteSearchResult(
                    0,
                    List.of()
            );

    NoteResult<NoteSearchResult> expected =
            NoteResult.success(searchResult);

    when(provider.search_notes(
            "does-not-exist",
            List.of(),
            20,
            0
    )).thenReturn(expected);

    NoteResult<NoteSearchResult> result =
            notesTool.search_notes(
                    "does-not-exist"
            );

    assertSame(expected, result);

    assertEquals(
            0,
            result.data().count()
    );

    assertTrue(
            result.data().note_list().isEmpty()
    );

    verify(provider).search_notes(
            "does-not-exist",
            List.of(),
            20,
            0
    );
}

// ========================================================================
// ADD TAG
// ========================================================================

@Test
void addTag_shouldConvertIdAndDelegate() {

    NoteResult<Void> expected =
            NoteResult.success(null);

    when(provider.add_tag(
            NOTE_ID,
            "spring"
    )).thenReturn(expected);

    NoteResult<Void> result =
            notesTool.add_tag(
                    NOTE_ID_STRING,
                    "spring"
            );

    assertSame(expected, result);

    verify(provider).add_tag(
            NOTE_ID,
            "spring"
    );

    verifyNoMoreInteractions(provider);
}

@Test
void addTag_withInvalidUuid_shouldReturnDataInvalid() {

    NoteResult<Void> result =
            notesTool.add_tag(
                    "invalid-id",
                    "spring"
            );

    assertEquals(
            "error",
            result.status()
    );

    assertEquals(
            "DataInvalid",
            result.error()
    );

    assertEquals(
            "note_id must be a valid UUID",
            result.message()
    );

    verifyNoInteractions(provider);
}

@Test
void addTag_shouldReturnProviderError() {

    NoteResult<Void> expected =
            NoteResult.error(
                    "NoteNotFoundError",
                    "No note with id " + NOTE_ID + " found"
            );

    when(provider.add_tag(
            NOTE_ID,
            "spring"
    )).thenReturn(expected);

    NoteResult<Void> result =
            notesTool.add_tag(
                    NOTE_ID_STRING,
                    "spring"
            );

    assertSame(expected, result);

    assertEquals(
            "NoteNotFoundError",
            result.error()
    );

    verify(provider).add_tag(
            NOTE_ID,
            "spring"
    );
}

// ========================================================================
// REMOVE TAG
// ========================================================================

@Test
void removeTag_shouldConvertIdAndDelegate() {

    NoteResult<Void> expected =
            NoteResult.success(null);

    when(provider.remove_tag(
            NOTE_ID,
            "spring"
    )).thenReturn(expected);

    NoteResult<Void> result =
            notesTool.remove_tag(
                    NOTE_ID_STRING,
                    "spring"
            );

    assertSame(expected, result);

    verify(provider).remove_tag(
            NOTE_ID,
            "spring"
    );

    verifyNoMoreInteractions(provider);
}

@Test
void removeTag_withInvalidUuid_shouldReturnDataInvalid() {

    NoteResult<Void> result =
            notesTool.remove_tag(
                    "invalid-id",
                    "spring"
            );

    assertEquals(
            "error",
            result.status()
    );

    assertEquals(
            "DataInvalid",
            result.error()
    );

    assertEquals(
            "note_id must be a valid UUID",
            result.message()
    );

    verifyNoInteractions(provider);
}

@Test
void removeTag_shouldReturnProviderError() {

    NoteResult<Void> expected =
            NoteResult.error(
                    "NoteNotFoundError",
                    "No note with id " + NOTE_ID + " found"
            );

    when(provider.remove_tag(
            NOTE_ID,
            "spring"
    )).thenReturn(expected);

    NoteResult<Void> result =
            notesTool.remove_tag(
                    NOTE_ID_STRING,
                    "spring"
            );

    assertSame(expected, result);

    assertEquals(
            "NoteNotFoundError",
            result.error()
    );

    verify(provider).remove_tag(
            NOTE_ID,
            "spring"
    );
}

}

