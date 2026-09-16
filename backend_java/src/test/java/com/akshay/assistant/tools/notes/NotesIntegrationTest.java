// package com.akshay.assistant.tools.notes;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.util.List;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest 
// class NotesIntegrationTest {
        
// @Autowired
// private NotesTool notesTool;

// /*
//  * Keep track of notes created by the test.
//  * They will be deleted during cleanup.
//  */
// private UUID createdNoteId;

// private static final String TEST_TITLE =
//         "Integration Test Note";

// private static final String TEST_CONTENT =
//         "This note was created by the Notes integration test.";

// private static final List<String> TEST_TAGS =
//         List.of("integration-test", "spring");

// // ========================================================================
// // SETUP
// // ========================================================================

// @BeforeEach
// void setUp() {
//     createdNoteId = null;
// }

// // ========================================================================
// // CLEANUP
// // ========================================================================

// @AfterEach
// void cleanup() {

//     if (createdNoteId == null) {
//         return;
//     }

//     /*
//      * Delete the note created during the test.
//      *
//      * We intentionally don't fail the test if cleanup fails,
//      * because cleanup failure should not hide the original test failure.
//      */
//     try {
//         notesTool.delete_note(
//                 createdNoteId.toString()
//         );
//     } catch (Exception ignored) {
//     }
// }

// // ========================================================================
// // CREATE
// // ========================================================================

// @Test
// void createNote_shouldCreateNoteInNotion() {

//     NoteResult<UUID> result =
//             notesTool.create_note(
//                     TEST_TITLE,
//                     TEST_CONTENT,
//                     TEST_TAGS
//             );

//     assertNotNull(result);
//     assertEquals(
//             "success",
//             result.status()
//     );

//     assertNotNull(result.data());

//     createdNoteId = result.data();

//     /*
//      * The returned ID must be a valid UUID.
//      */
//     assertDoesNotThrow(
//             () -> UUID.fromString(
//                     result.data().toString()
//             )
//     );
// }

// // ========================================================================
// // CREATE + GET
// // ========================================================================

// @Test
// void getNote_shouldReturnCreatedNote() {

//     NoteResult<UUID> createResult =
//             notesTool.create_note(
//                     TEST_TITLE,
//                     TEST_CONTENT,
//                     TEST_TAGS
//             );

//     assertEquals(
//             "success",
//             createResult.status()
//     );

//     createdNoteId = createResult.data();

//     NoteResult<Note> getResult =
//             notesTool.get_note(
//                     createdNoteId.toString()
//             );

//     assertEquals(
//             "success",
//             getResult.status()
//     );

//     assertNotNull(getResult.data());

//     Note note = getResult.data();

//     assertEquals(
//             createdNoteId,
//             note.id()
//     );

//     assertEquals(
//             TEST_TITLE,
//             note.title()
//     );

//     assertEquals(
//             TEST_CONTENT,
//             note.content()
//     );

//     assertNotNull(note.created_at());
//     assertNotNull(note.updated_at());
// }

// // ========================================================================
// // UPDATE TITLE
// // ========================================================================

// @Test
// void updateNote_shouldUpdateTitle() {

//     createTestNote();

//     NoteUpdate update =
//             new NoteUpdate(
//                     "Updated Integration Test Note",
//                     null,
//                     null
//             );

//     NoteResult<Void> updateResult =
//             notesTool.update_note(
//                     createdNoteId.toString(),
//                     update
//             );

//     assertEquals(
//             "success",
//             updateResult.status()
//     );

//     /*
//      * Read the note again from Notion.
//      */
//     NoteResult<Note> getResult =
//             notesTool.get_note(
//                     createdNoteId.toString()
//             );

//     assertEquals(
//             "success",
//             getResult.status()
//     );

//     assertEquals(
//             "Updated Integration Test Note",
//             getResult.data().title()
//     );

//     /*
//      * Content and tags should remain unchanged.
//      */
//     assertEquals(
//             TEST_CONTENT,
//             getResult.data().content()
//     );

// }

// // ========================================================================
// // UPDATE CONTENT
// // ========================================================================

// @Test
// void updateNote_shouldUpdateContent() {

//     createTestNote();

//     String updatedContent =
//             "This is the updated content.";

//     NoteUpdate update =
//             new NoteUpdate(
//                     null,
//                     updatedContent,
//                     null
//             );

//     NoteResult<Void> result =
//             notesTool.update_note(
//                     createdNoteId.toString(),
//                     update
//             );

//     assertEquals(
//             "success",
//             result.status()
//     );

//     NoteResult<Note> getResult =
//             notesTool.get_note(
//                     createdNoteId.toString()
//             );

//     assertEquals(
//             "success",
//             getResult.status()
//     );

//     assertEquals(
//             TEST_TITLE,
//             getResult.data().title()
//     );

//     assertEquals(
//             updatedContent,
//             getResult.data().content()
//     );
// }


// // ========================================================================
// // SEARCH
// // ========================================================================

// @Test
// void searchNotes_shouldFindNoteByTitle() {

//     createTestNote();

//     NoteResult<NoteSearchResult> result =
//             notesTool.search_notes(
//                     "Integration Test Note"
//             );

//     assertEquals(
//             "success",
//             result.status()
//     );

//     assertTrue(
//             result.data().count() >= 1
//     );

//     boolean found =
//             result.data()
//                     .note_list()
//                     .stream()
//                     .anyMatch(
//                             note ->
//                                     createdNoteId.equals(
//                                             note.id()
//                                     )
//                     );

//     assertTrue(
//             found,
//             "Created note should appear in search results"
//     );
// }

// // ========================================================================
// // SEARCH CONTENT
// // ========================================================================

// @Test
// void searchNotes_shouldFindNoteByContent() {

//     createTestNote();

//     NoteResult<NoteSearchResult> result =
//             notesTool.search_notes(
//                     "created by the Notes integration test"
//             );

//     assertEquals(
//             "success",
//             result.status()
//     );

//     boolean found =
//             result.data()
//                     .note_list()
//                     .stream()
//                     .anyMatch(
//                             note ->
//                                     createdNoteId.equals(
//                                             note.id()
//                                     )
//                     );

//     assertTrue(
//             found,
//             "Created note should be found using content"
//     );
// }

// // ========================================================================
// // DELETE
// // ========================================================================

// @Test
// void deleteNote_shouldDeleteNote() {

//     createTestNote();

//     NoteResult<Note> deleteResult =
//             notesTool.delete_note(
//                     createdNoteId.toString()
//             );

//     assertEquals(
//             "success",
//             deleteResult.status()
//     );

//     assertNotNull(
//             deleteResult.data()
//     );

//     assertEquals(
//             createdNoteId,
//             deleteResult.data().id()
//     );

//     /*
//      * Prevent @AfterEach from attempting to delete
//      * the note a second time.
//      */
//     createdNoteId = null;

//     /*
//      * The deleted page should no longer be retrievable.
//      */
//     NoteResult<Note> getResult =
//             notesTool.get_note(
//                     deleteResult.data()
//                             .id()
//                             .toString()
//             );

//     assertEquals(
//             "error",
//             getResult.status()
//     );

//     assertEquals(
//             "NoteNotFoundError",
//             getResult.error()
//     );
// }

// // ========================================================================
// // INVALID UUID
// // ========================================================================

// @Test
// void getNote_withInvalidUuid_shouldReturnDataInvalid() {

//     NoteResult<Note> result =
//             notesTool.get_note(
//                     "not-a-valid-uuid"
//             );

//     assertEquals(
//             "error",
//             result.status()
//     );

//     assertEquals(
//             "DataInvalid",
//             result.error()
//     );

//     assertEquals(
//             "note_id must be a valid UUID",
//             result.message()
//     );
// }

// @Test
// void updateNote_withInvalidUuid_shouldReturnDataInvalid() {

//     NoteUpdate update =
//             new NoteUpdate(
//                     "New title",
//                     null,
//                     null
//             );

//     NoteResult<Void> result =
//             notesTool.update_note(
//                     "not-a-valid-uuid",
//                     update
//             );

//     assertEquals(
//             "error",
//             result.status()
//     );

//     assertEquals(
//             "DataInvalid",
//             result.error()
//     );
// }

// @Test
// void deleteNote_withInvalidUuid_shouldReturnDataInvalid() {

//     NoteResult<Note> result =
//             notesTool.delete_note(
//                     "not-a-valid-uuid"
//             );

//     assertEquals(
//             "error",
//             result.status()
//     );

//     assertEquals(
//             "DataInvalid",
//             result.error()
//     );
// }

// // ========================================================================
// // HELPER
// // ========================================================================

// private void createTestNote() {

//     NoteResult<UUID> result =
//             notesTool.create_note(
//                     TEST_TITLE,
//                     TEST_CONTENT,
//                     TEST_TAGS
//             );

//     assertEquals(
//             "success",
//             result.status(),
//             "Test note creation failed: "
//                     + result.message()
//     );

//     assertNotNull(
//             result.data()
//     );

//     createdNoteId =
//             result.data();
// }
// }
