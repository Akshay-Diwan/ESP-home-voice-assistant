package com.akshay.assistant.tools.notes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class NotionAdapter implements NoteProvider {

    private static final int NOTION_RICH_TEXT_LIMIT = 1800;

    private final RestClient client;
    private final ObjectMapper mapper;
    private final String dataSourceId;
    private final String categoryProperty;

    public NotionAdapter(
            RestClient notionRestClient,
            ObjectMapper mapper,
            @Value("${notion.data-source-id}") String dataSourceId,
            @Value("${notion.category-property:Category}") String categoryProperty
    ) {
        this.client = notionRestClient;
        this.mapper = mapper;
        this.dataSourceId = dataSourceId;
        this.categoryProperty = categoryProperty;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<UUID> create_note(
            String title,
            String content,
            List<String> tags
    ) {

        if (title == null || title.isBlank()) {
            System.out.println("[NotionAdapter ERROR] create_note failed: Title cannot be empty");
            return NoteResult.error(
                    "DataInvalid",
                    "Title cannot be empty"
            );
        }

        try {
            ObjectNode body = mapper.createObjectNode();

            /*
             * Parent configured as data_source / database.
             */
            ObjectNode parent = body.putObject("parent");
            parent.put("database_id", dataSourceId);

            /*
             * Page properties: Title + Multi-select Category/Tags
             */
            ObjectNode properties = body.putObject("properties");

            properties.set("title", NotionProperties.title(title));

            if (tags != null && !tags.isEmpty()) {
                properties.set(categoryProperty, createMultiSelectProperty(tags));
            }

            /*
             * Page content blocks
             */
            ArrayNode children = body.putArray("children");
            appendContentBlocks(children, content);

            JsonNode response = client.post()
                    .uri("/pages")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.path("id").isMissingNode()) {
                System.out.println("[NotionAdapter ERROR] create_note failed: Notion returned invalid response -> " + response);
                return NoteResult.error(
                        "OperationError",
                        "Notion returned an invalid response"
                );
            }

            UUID noteId = UUID.fromString(
                    response.path("id").asString()
            );

            return NoteResult.success(noteId);

        } catch (RestClientResponseException ex) {
            String err = notionError(ex);
            System.out.println("[NotionAdapter ERROR] create_note RestClient Exception (" + ex.getStatusCode() + "): " + err);
            return NoteResult.error(
                    "OperationError",
                    err
            );
        } catch (Exception ex) {
            String err = safeMessage(ex);
            System.out.println("[NotionAdapter ERROR] create_note Unexpected Exception: " + err);
            ex.printStackTrace();
            return NoteResult.error(
                    "OperationError",
                    err
            );
        }
    }

    public NoteResult<UUID> create_note(
            String title,
            String content
    ) {
        return create_note(title, content, List.of());
    }

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<Note> get_note(UUID noteId) {

        if (noteId == null) {
            System.out.println("[NotionAdapter ERROR] get_note failed: note_id cannot be null");
            return NoteResult.error(
                    "DataInvalid",
                    "note_id cannot be null"
            );
        }

        try {
            JsonNode page = client.get()
                    .uri("/pages/{id}", noteId)
                    .retrieve()
                    .body(JsonNode.class);

            if (page == null) {
                System.out.println("[NotionAdapter ERROR] get_note failed: No note with id " + noteId + " found");
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            return NoteResult.success(readNote(page));

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                System.out.println("[NotionAdapter ERROR] get_note failed: Note " + noteId + " not found (404)");
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            String err = notionError(ex);
            System.out.println("[NotionAdapter ERROR] get_note RestClient Exception (" + ex.getStatusCode() + "): " + err);
            return NoteResult.error(
                    "OperationError",
                    err
            );
        } catch (Exception ex) {
            String err = safeMessage(ex);
            System.out.println("[NotionAdapter ERROR] get_note Unexpected Exception: " + err);
            ex.printStackTrace();
            return NoteResult.error(
                    "OperationError",
                    err
            );
        }
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<Void> update_note(
            UUID noteId,
            NoteUpdate data
    ) {

        if (noteId == null) {
            System.out.println("[NotionAdapter ERROR] update_note failed: note_id cannot be null");
            return NoteResult.error(
                    "DataInvalid",
                    "note_id cannot be null"
            );
        }

        if (data == null ||
                (data.title() == null && data.content() == null && data.tags() == null)
        ) {
            System.out.println("[NotionAdapter ERROR] update_note failed: No data parameters provided for note " + noteId);
            return NoteResult.error(
                    "NoDataProvided",
                    "provide atleast one parameter"
            );
        }

        if (data.title() != null && data.title().isBlank()) {
            System.out.println("[NotionAdapter ERROR] update_note failed: Title cannot be empty for note " + noteId);
            return NoteResult.error(
                    "DataInvalid",
                    "Title cannot be empty"
            );
        }

        try {
            /*
             * Update title/tags through page properties.
             */
            if (data.title() != null || data.tags() != null) {

                ObjectNode body = mapper.createObjectNode();
                ObjectNode properties = body.putObject("properties");

                if (data.title() != null) {
                    properties.set("title", NotionProperties.title(data.title()));
                }

                if (data.tags() != null) {
                    properties.set(categoryProperty, createMultiSelectProperty(data.tags()));
                }

                client.patch()
                        .uri("/pages/{id}", noteId)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
            }

            /*
             * Replace body content if supplied.
             */
            if (data.content() != null) {
                replaceContent(noteId, data.content());
            }

            return NoteResult.success(null);

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                System.out.println("[NotionAdapter ERROR] update_note failed: Note " + noteId + " not found (404)");
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            String err = notionError(ex);
            System.out.println("[NotionAdapter ERROR] update_note RestClient Exception (" + ex.getStatusCode() + "): " + err);
            return NoteResult.error(
                    "OperationError",
                    err
            );
        } catch (Exception ex) {
            String err = safeMessage(ex);
            System.out.println("[NotionAdapter ERROR] update_note Unexpected Exception: " + err);
            ex.printStackTrace();
            return NoteResult.error(
                    "OperationError",
                    err
            );
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<Note> delete_note(UUID noteId) {

        if (noteId == null) {
            System.out.println("[NotionAdapter ERROR] delete_note failed: note_id cannot be null");
            return NoteResult.error(
                    "DataInvalid",
                    "note_id cannot be null"
            );
        }

        try {
            JsonNode page = client.get()
                    .uri("/pages/{id}", noteId)
                    .retrieve()
                    .body(JsonNode.class);

            if (page == null) {
                System.out.println("[NotionAdapter ERROR] delete_note failed: No note with id " + noteId + " found");
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            Note note = readNote(page);

            ObjectNode body = mapper.createObjectNode();
            body.put("in_trash", true);

            client.patch()
                    .uri("/pages/{id}", noteId)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            return NoteResult.success(note);

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                System.out.println("[NotionAdapter ERROR] delete_note failed: Note " + noteId + " not found (404)");
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            String err = notionError(ex);
            System.out.println("[NotionAdapter ERROR] delete_note RestClient Exception (" + ex.getStatusCode() + "): " + err);
            return NoteResult.error(
                    "OperationError",
                    err
            );
        } catch (Exception ex) {
            String err = safeMessage(ex);
            System.out.println("[NotionAdapter ERROR] delete_note Unexpected Exception: " + err);
            ex.printStackTrace();
            return NoteResult.error(
                    "OperationError",
                    err
            );
        }
    }

    // -------------------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<NoteSearchResult> search_notes(
            String query,
            int limit,
            int offset
    ) {

        if (limit <= 0) {
            System.out.println("[NotionAdapter ERROR] search_notes failed: limit must be greater than 0");
            return NoteResult.error(
                    "OperationError",
                    "limit must be greater than 0"
            );
        }

        if (offset < 0) {
            System.out.println("[NotionAdapter ERROR] search_notes failed: offset must not be negative");
            return NoteResult.error(
                    "OperationError",
                    "offset must not be negative"
            );
        }

        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        try {
            int matchingCount = 0;
            List<Note> result = new ArrayList<>(Math.min(limit, 20));
            String cursor = null;

            do {
                ObjectNode body = mapper.createObjectNode();
                body.put("page_size", 100);

                if (cursor != null) {
                    body.put("start_cursor", cursor);
                }

                JsonNode response = client.post()
                        .uri("/databases/{id}/query", dataSourceId)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);

                if (response == null) {
                    break;
                }

                for (JsonNode page : response.path("results")) {
                    Note note = readNote(page);

                    boolean matchesQuery = normalizedQuery.isEmpty()
                            || containsIgnoreCase(note.title(), normalizedQuery)
                            || containsIgnoreCase(note.content(), normalizedQuery);

                    if (!matchesQuery) {
                        continue;
                    }

                    matchingCount++;

                    if (matchingCount > offset && result.size() < limit) {
                        result.add(note);
                    }
                }

                cursor = response.path("has_more").asBoolean(false)
                        ? response.path("next_cursor").asString(null)
                        : null;

            } while (cursor != null);

            return NoteResult.success(new NoteSearchResult(matchingCount, result));

        } catch (RestClientResponseException ex) {
            String err = notionError(ex);
            System.out.println("[NotionAdapter ERROR] search_notes RestClient Exception (" + ex.getStatusCode() + "): " + err);
            return NoteResult.error(
                    "OperationError",
                    err
            );
        } catch (Exception ex) {
            String err = safeMessage(ex);
            System.out.println("[NotionAdapter ERROR] search_notes Unexpected Exception: " + err);
            ex.printStackTrace();
            return NoteResult.error(
                    "OperationError",
                    err
            );
        }
    }

    public NoteResult<NoteSearchResult> search_notes(String query) {
        return search_notes(query, 20, 0);
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------

    private ObjectNode createMultiSelectProperty(List<String> tags) {
        ObjectNode multiSelectNode = mapper.createObjectNode();
        ArrayNode options = multiSelectNode.putArray("multi_select");

        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                options.addObject().put("name", tag.trim());
            }
        }
        return multiSelectNode;
    }

    private void replaceContent(UUID pageId, String content) {
        JsonNode children = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/blocks/{id}/children")
                        .queryParam("page_size", 100)
                        .build(pageId))
                .retrieve()
                .body(JsonNode.class);

        if (children != null) {
            for (JsonNode child : children.path("results")) {
                String blockId = child.path("id").asString(null);

                if (blockId == null || blockId.isBlank()) {
                    continue;
                }

                client.delete()
                        .uri("/blocks/{id}", blockId)
                        .retrieve()
                        .toBodilessEntity();
            }
        }

        ObjectNode body = mapper.createObjectNode();
        ArrayNode blocks = body.putArray("children");

        appendContentBlocks(blocks, content);

        if (blocks.isEmpty()) {
            return;
        }

        client.patch()
                .uri("/blocks/{id}/children", pageId)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void appendContentBlocks(ArrayNode children, String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        String[] paragraphs = content.split("\\R\\R+");

        for (String paragraph : paragraphs) {
            String text = paragraph.strip();

            if (text.isEmpty()) {
                continue;
            }

            for (int start = 0; start < text.length(); start += NOTION_RICH_TEXT_LIMIT) {
                String chunk = text.substring(
                        start,
                        Math.min(start + NOTION_RICH_TEXT_LIMIT, text.length())
                );

                ObjectNode block = children.addObject();
                block.put("object", "block");
                block.put("type", "paragraph");

                ObjectNode paragraphNode = block.putObject("paragraph");
                ArrayNode richText = paragraphNode.putArray("rich_text");

                ObjectNode textNode = richText.addObject();
                textNode.put("type", "text");
                textNode.putObject("text").put("content", chunk);
            }
        }
    }

private Note readNote(JsonNode page) {
    UUID id = UUID.fromString(page.path("id").asString());
    JsonNode properties = page.path("properties");

    // The property key in your database schema is "Doc name"
    String title = extractTitle(properties.path("Doc name"));
    String content = readContent(id);

    OffsetDateTime createdAt = OffsetDateTime.parse(
            page.path("created_time").asString()
    );

    OffsetDateTime updatedAt = OffsetDateTime.parse(
            page.path("last_edited_time").asString()
    );

    return new Note(
            id,
            title,
            content,
            createdAt,
            updatedAt
    );
}

private String extractTitle(JsonNode docNameProperty) {
    // "Doc name" contains a nested array under "title"
    JsonNode titleArray = docNameProperty.path("title");
    if (titleArray.isArray() && !titleArray.isEmpty()) {
        return titleArray.get(0).path("plain_text").asString("");
    }
    return "";
}

    private String readContent(UUID pageId) {
        StringBuilder content = new StringBuilder();
        String cursor = null;

        do {
            final String currentCursor = cursor;

            JsonNode response = client.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/blocks/{id}/children")
                                .queryParam("page_size", 100);

                        if (currentCursor != null) {
                            builder.queryParam("start_cursor", currentCursor);
                        }

                        return builder.build(pageId);
                    })
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                break;
            }

            for (JsonNode block : response.path("results")) {
                appendBlockText(content, block);
            }

            cursor = response.path("has_more").asBoolean(false)
                    ? response.path("next_cursor").asString(null)
                    : null;

        } while (cursor != null);

        return content.toString().strip();
    }

    private void appendBlockText(StringBuilder out, JsonNode block) {
        String type = block.path("type").asString();

        if (type.isBlank()) {
            return;
        }

        JsonNode richText = block.path(type).path("rich_text");

        if (!richText.isArray()) {
            return;
        }

        for (JsonNode item : richText) {
            String text = item.path("plain_text").asString(
                    item.path("text").path("content").asString("")
            );

            if (text.isBlank()) {
                continue;
            }

            if (!out.isEmpty()) {
                out.append('\n');
            }

            out.append(text);
        }
    }


    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String notionError(RestClientResponseException ex) {
        try {
            String response = ex.getResponseBodyAsString();

            if (response != null && !response.isBlank()) {
                JsonNode body = mapper.readTree(response);
                String message = body.path("message").asString(null);

                if (message != null && !message.isBlank()) {
                    return message;
                }
            }
        } catch (Exception ignored) {
        }

        return ex.getStatusText() + " (" + ex.getStatusCode().value() + ")";
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }
}