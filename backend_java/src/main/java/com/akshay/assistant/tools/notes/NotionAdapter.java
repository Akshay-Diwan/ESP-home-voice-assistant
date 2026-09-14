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
import java.util.Objects;
import java.util.UUID;

@Component
public class NotionAdapter implements NoteProvider {

    /*
     * Notion rich_text content has a size limit.
     */
    private static final int NOTION_RICH_TEXT_LIMIT = 1800;

    private final RestClient client;
    private final ObjectMapper mapper;

    private final String dataSourceId;
    private final String titleProperty;
    private final String tagsProperty;

    public NotionAdapter(
            RestClient.Builder builder,
            ObjectMapper mapper,
            @Value("${notion.token}") String token,
            @Value("${notion.page-id}") String dataSourceId,
            @Value("${notion.title-property:Name}") String titleProperty,
            @Value("${notion.tags-property:Tags}") String tagsProperty,
            @Value("${notion.version}") String apiVersion
    ) {
        this.mapper = mapper;
        this.dataSourceId = dataSourceId;
        this.titleProperty = titleProperty;
        this.tagsProperty = tagsProperty;

        this.client = builder
                .baseUrl("https://api.notion.com/v1/pages")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Notion-Version", apiVersion)
                .defaultHeader("Content-Type", "application/json")
                .build();
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
            return NoteResult.error(
                    "DataInvalid",
                    "Title cannot be empty"
            );
        }

        List<String> safeTags = normalizeTags(tags);

        try {
            ObjectNode body = mapper.createObjectNode();

            /*
             * Create page inside the configured Notion data source.
             */
            ObjectNode parent = body.putObject("parent");
            parent.put("data_source_id", dataSourceId);

            /*
             * Page properties.
             */
            ObjectNode properties = body.putObject("properties");

            properties.set(
                    titleProperty,
                    NotionProperties.title(title)
            );

            properties.set(
                    tagsProperty,
                    NotionProperties.multiSelect(safeTags)
            );

            /*
             * Page content.
             */
            ArrayNode children = body.putArray("children");

            appendContentBlocks(children, content);

            JsonNode response = client.post()
                    .uri("/pages")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.path("id").isMissingNode()) {
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
            return NoteResult.error(
                    "OperationError",
                    notionError(ex)
            );

        } catch (Exception ex) {
            return NoteResult.error(
                    "OperationError",
                    safeMessage(ex)
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
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            return NoteResult.success(
                    readNote(page)
            );

        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() == 404) {
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            return NoteResult.error(
                    "OperationError",
                    notionError(ex)
            );

        } catch (Exception ex) {
            return NoteResult.error(
                    "OperationError",
                    safeMessage(ex)
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
            return NoteResult.error(
                    "DataInvalid",
                    "note_id cannot be null"
            );
        }

        if (data == null ||
                (
                        data.title() == null &&
                        data.content() == null &&
                        data.tags() == null
                )
        ) {
            return NoteResult.error(
                    "NoDataProvided",
                    "provide atleast one parameter"
            );
        }

        if (data.title() != null && data.title().isBlank()) {
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
                    properties.set(
                            titleProperty,
                            NotionProperties.title(data.title())
                    );
                }

                if (data.tags() != null) {
                    properties.set(
                            tagsProperty,
                            NotionProperties.multiSelect(
                                    normalizeTags(data.tags())
                            )
                    );
                }

                client.patch()
                        .uri("/pages/{id}", noteId)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
            }

            /*
             * Replace content only if content was explicitly supplied.
             */
            if (data.content() != null) {
                replaceContent(noteId, data.content());
            }

            return NoteResult.success(null);

        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() == 404) {
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            return NoteResult.error(
                    "OperationError",
                    notionError(ex)
            );

        } catch (Exception ex) {
            return NoteResult.error(
                    "OperationError",
                    safeMessage(ex)
            );
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<Note> delete_note(UUID noteId) {

        if (noteId == null) {
            return NoteResult.error(
                    "DataInvalid",
                    "note_id cannot be null"
            );
        }

        try {

            /*
             * Get the note before deleting it because the SRS
             * requires deleted note information in the response.
             */
            JsonNode page = client.get()
                    .uri("/pages/{id}", noteId)
                    .retrieve()
                    .body(JsonNode.class);

            if (page == null) {
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            Note note = readNote(page);

            /*
             * Notion deletion is implemented by moving the page to trash.
             */
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
                return NoteResult.error(
                        "NoteNotFoundError",
                        "No note with id " + noteId + " found"
                );
            }

            return NoteResult.error(
                    "OperationError",
                    notionError(ex)
            );

        } catch (Exception ex) {
            return NoteResult.error(
                    "OperationError",
                    safeMessage(ex)
            );
        }
    }

    // -------------------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<NoteSearchResult> search_notes(
            String query,
            List<String> tags,
            int limit,
            int offset
    ) {

        if (limit <= 0) {
            return NoteResult.error(
                    "OperationError",
                    "limit must be greater than 0"
            );
        }

        if (offset < 0) {
            return NoteResult.error(
                    "OperationError",
                    "offset must not be negative"
            );
        }

        String normalizedQuery =
                query == null
                        ? ""
                        : query.trim().toLowerCase(Locale.ROOT);

        List<String> requiredTags = normalizeTags(tags);

        try {

            int matchingCount = 0;

            List<Note> result =
                    new ArrayList<>(Math.min(limit, 20));

            String cursor = null;

            do {

                ObjectNode body = mapper.createObjectNode();

                /*
                 * Maximum page size supported by the API.
                 */
                body.put("page_size", 100);

                if (cursor != null) {
                    body.put("start_cursor", cursor);
                }

                /*
                 * Tag filtering is delegated to Notion.
                 */
                if (!requiredTags.isEmpty()) {

                    ArrayNode and =
                            body.putObject("filter")
                                    .putArray("and");

                    for (String tag : requiredTags) {

                        ObjectNode filter = and.addObject();

                        filter.put(
                                "property",
                                tagsProperty
                        );

                        filter.putObject("multi_select")
                                .put("contains", tag);
                    }
                }

                JsonNode response = client.post()
                        .uri(
                                "/data_sources/{id}/query",
                                dataSourceId
                        )
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);

                if (response == null) {
                    break;
                }

                for (JsonNode page :
                        response.path("results")) {

                    Note note = readNote(page);

                    /*
                     * Notion cannot directly search arbitrary page body
                     * content through the data-source query, so content
                     * matching is performed here.
                     */
                    boolean matchesQuery =
                            normalizedQuery.isEmpty()
                                    || containsIgnoreCase(
                                            note.title(),
                                            normalizedQuery
                                    )
                                    || containsIgnoreCase(
                                            note.content(),
                                            normalizedQuery
                                    )
                                    || note.tags()
                                            .stream()
                                            .anyMatch(
                                                    tag ->
                                                            containsIgnoreCase(
                                                                    tag,
                                                                    normalizedQuery
                                                            )
                                            );

                    if (!matchesQuery) {
                        continue;
                    }

                    matchingCount++;

                    /*
                     * Apply offset after filtering.
                     */
                    if (matchingCount > offset &&
                            result.size() < limit) {

                        result.add(note);
                    }
                }

                cursor =
                        response.path("has_more")
                                .asBoolean(false)
                                ? response.path("next_cursor")
                                        .asString(null)
                                : null;

            } while (cursor != null);

            return NoteResult.success(
                    new NoteSearchResult(
                            matchingCount,
                            result
                    )
            );

        } catch (RestClientResponseException ex) {

            return NoteResult.error(
                    "OperationError",
                    notionError(ex)
            );

        } catch (Exception ex) {

            return NoteResult.error(
                    "OperationError",
                    safeMessage(ex)
            );
        }
    }

    public NoteResult<NoteSearchResult> search_notes(
            String query
    ) {
        return search_notes(
                query,
                List.of(),
                20,
                0
        );
    }

    // -------------------------------------------------------------------------
    // ADD TAG
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<Void> add_tag(
            UUID noteId,
            String tag
    ) {

        if (tag == null || tag.isBlank()) {
            return NoteResult.error(
                    "DataInvalid",
                    "tag cannot be empty"
            );
        }

        String normalizedTag = tag.trim();

        NoteResult<Note> current =
                get_note(noteId);

        if ("error".equals(current.status())) {
            return NoteResult.error(
                    current.error(),
                    current.message()
            );
        }

        List<String> tags =
                new ArrayList<>(current.data().tags());

        if (!tags.contains(normalizedTag)) {
            tags.add(normalizedTag);
        }

        return update_note(
                noteId,
                new NoteUpdate(
                        null,
                        null,
                        tags
                )
        );
    }

    // -------------------------------------------------------------------------
    // REMOVE TAG
    // -------------------------------------------------------------------------

    @Override
    public NoteResult<Void> remove_tag(
            UUID noteId,
            String tag
    ) {

        if (tag == null || tag.isBlank()) {
            return NoteResult.error(
                    "DataInvalid",
                    "tag cannot be empty"
            );
        }

        NoteResult<Note> current =
                get_note(noteId);

        if ("error".equals(current.status())) {
            return NoteResult.error(
                    current.error(),
                    current.message()
            );
        }

        List<String> tags =
                new ArrayList<>(current.data().tags());

        tags.remove(tag.trim());

        return update_note(
                noteId,
                new NoteUpdate(
                        null,
                        null,
                        tags
                )
        );
    }

    // -------------------------------------------------------------------------
    // CONTENT
    // -------------------------------------------------------------------------

    private void replaceContent(
            UUID pageId,
            String content
    ) {

        JsonNode children = client.get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/blocks/{id}/children")
                                .queryParam("page_size", 100)
                                .build(pageId)
                )
                .retrieve()
                .body(JsonNode.class);

        /*
         * Delete existing top-level blocks.
         */
        if (children != null) {

            for (JsonNode child :
                    children.path("results")) {

                String blockId =
                        child.path("id").asString(null);

                if (blockId == null || blockId.isBlank()) {
                    continue;
                }

                client.delete()
                        .uri(
                                "/blocks/{id}",
                                blockId
                        )
                        .retrieve()
                        .toBodilessEntity();
            }
        }

        /*
         * Add the new content.
         */
        ObjectNode body =
                mapper.createObjectNode();

        ArrayNode blocks =
                body.putArray("children");

        appendContentBlocks(
                blocks,
                content
        );

        if (blocks.isEmpty()) {
            return;
        }

        client.patch()
                .uri(
                        "/blocks/{id}/children",
                        pageId
                )
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void appendContentBlocks(
            ArrayNode children,
            String content
    ) {

        if (content == null || content.isBlank()) {
            return;
        }

        /*
         * Treat blank lines as paragraph separators.
         */
        String[] paragraphs =
                content.split("\\R\\R+");

        for (String paragraph : paragraphs) {

            String text = paragraph.strip();

            if (text.isEmpty()) {
                continue;
            }

            /*
             * Split long paragraphs because Notion rich text
             * has a maximum content size.
             */
            for (
                    int start = 0;
                    start < text.length();
                    start += NOTION_RICH_TEXT_LIMIT
            ) {

                String chunk =
                        text.substring(
                                start,
                                Math.min(
                                        start + NOTION_RICH_TEXT_LIMIT,
                                        text.length()
                                )
                        );

                ObjectNode block =
                        children.addObject();

                block.put(
                        "object",
                        "block"
                );

                block.put(
                        "type",
                        "paragraph"
                );

                ObjectNode paragraphNode =
                        block.putObject("paragraph");

                ArrayNode richText =
                        paragraphNode.putArray("rich_text");

                ObjectNode textNode =
                        richText.addObject();

                textNode.put(
                        "type",
                        "text"
                );

                textNode
                        .putObject("text")
                        .put(
                                "content",
                                chunk
                        );
            }
        }
    }

    // -------------------------------------------------------------------------
    // READ NOTE
    // -------------------------------------------------------------------------

    private Note readNote(JsonNode page) {

        UUID id =
                UUID.fromString(
                        page.path("id").asString()
                );

        JsonNode properties =
                page.path("properties");

        String title =
                extractTitle(
                        properties.path(titleProperty)
                );

        List<String> tags =
                extractTags(
                        properties.path(tagsProperty)
                );

        String content =
                readContent(id);

        OffsetDateTime createdAt =
                OffsetDateTime.parse(
                        page.path("created_time").asString()
                );

        OffsetDateTime updatedAt =
                OffsetDateTime.parse(
                        page.path("last_edited_time").asString()
                );

        return new Note(
                id,
                title,
                content,
                createdAt,
                updatedAt,
                tags
        );
    }

    // -------------------------------------------------------------------------
    // READ CONTENT
    // -------------------------------------------------------------------------

    private String readContent(UUID pageId) {

        StringBuilder content =
                new StringBuilder();

        String cursor = null;

        do {

            final String currentCursor = cursor;

            JsonNode response =
                    client.get()
                            .uri(
                                    uriBuilder -> {

                                        var builder =
                                                uriBuilder
                                                        .path(
                                                                "/blocks/{id}/children"
                                                        )
                                                        .queryParam(
                                                                "page_size",
                                                                100
                                                        );

                                        if (currentCursor != null) {
                                            builder.queryParam(
                                                    "start_cursor",
                                                    currentCursor
                                            );
                                        }

                                        return builder.build(
                                                pageId
                                        );
                                    }
                            )
                            .retrieve()
                            .body(JsonNode.class);

            if (response == null) {
                break;
            }

            for (JsonNode block :
                    response.path("results")) {

                appendBlockText(
                        content,
                        block
                );
            }

            cursor =
                    response.path("has_more")
                            .asBoolean(false)
                            ? response.path("next_cursor")
                                    .asString(null)
                            : null;

        } while (cursor != null);

        return content.toString().strip();
    }

    private void appendBlockText(
            StringBuilder out,
            JsonNode block
    ) {

        String type =
                block.path("type").asString();

        if (type.isBlank()) {
            return;
        }

        JsonNode richText =
                block
                        .path(type)
                        .path("rich_text");

        if (!richText.isArray()) {
            return;
        }

        for (JsonNode item : richText) {

            String text =
                    item.path("plain_text")
                            .asString(
                                    item.path("text")
                                            .path("content")
                                            .asString("")
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

    // -------------------------------------------------------------------------
    // PROPERTIES
    // -------------------------------------------------------------------------

    private String extractTitle(
            JsonNode property
    ) {

        StringBuilder title =
                new StringBuilder();

        for (JsonNode item :
                property.path("title")) {

            title.append(
                    item.path("plain_text")
                            .asString(
                                    item.path("text")
                                            .path("content")
                                            .asString("")
                            )
            );
        }

        return title.toString();
    }

    private List<String> extractTags(
            JsonNode property
    ) {

        List<String> tags =
                new ArrayList<>();

        for (JsonNode item :
                property.path("multi_select")) {

            String name =
                    item.path("name").asString();

            if (!name.isBlank()) {
                tags.add(name);
            }
        }

        return List.copyOf(tags);
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private List<String> normalizeTags(
            List<String> tags
    ) {

        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    private boolean containsIgnoreCase(
            String value,
            String query
    ) {

        return value != null &&
                value.toLowerCase(Locale.ROOT)
                        .contains(query);
    }

    private String notionError(
            RestClientResponseException ex
    ) {

        try {

            String response =
                    ex.getResponseBodyAsString();

            if (response != null &&
                    !response.isBlank()) {

                JsonNode body =
                        mapper.readTree(response);

                String message =
                        body.path("message")
                                .asString(null);

                if (message != null &&
                        !message.isBlank()) {

                    return message;
                }
            }

        } catch (Exception ignored) {
        }

        return ex.getStatusText()
                + " ("
                + ex.getStatusCode().value()
                + ")";
    }

    private String safeMessage(
            Exception ex
    ) {

        String message = ex.getMessage();

        return message == null ||
                message.isBlank()
                ? ex.getClass().getSimpleName()
                : message;
    }
}