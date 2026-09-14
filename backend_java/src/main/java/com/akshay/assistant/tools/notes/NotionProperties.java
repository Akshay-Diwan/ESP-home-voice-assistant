package com.akshay.assistant.tools.notes;

import java.util.List;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

final class NotionProperties {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private NotionProperties() {}

    static ObjectNode title(String title) {
        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode values = root.putArray("title");

        ObjectNode text = values.addObject();
        text.put("type", "text");
        text.putObject("text").put("content", title);

        return root;
    }

    static ObjectNode multiSelect(List<String> tags) {
        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode values = root.putArray("multi_select");

        for (String tag : tags) {
            values.addObject().put("name", tag);
        }

        return root;
    }
}
