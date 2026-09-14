package com.akshay.assistant.tools.notes;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Note(
        UUID id,
        String title,
        String content,
        OffsetDateTime created_at,
        OffsetDateTime updated_at
) {

}
