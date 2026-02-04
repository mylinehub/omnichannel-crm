package com.mylinehub.crm.rag.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FlexibleMapDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        JsonNode node = p.getCodec().readTree(p);

        if (node == null || node.isNull()) {
            return new HashMap<>();
        }

        ObjectMapper mapper = (ObjectMapper) p.getCodec();

        if (node.isArray() && node.size() == 0) {
            // Empty array → empty map
            return new HashMap<>();
        } else if (node.isObject()) {
            // Convert JSON object directly into a Map<String,String>
            return mapper.convertValue(node, new TypeReference<Map<String, String>>() {});
        } else if (node.isTextual()) {
            // Handle cases like "[key=value]" or "{key=value}"
            String text = node.asText().trim();
            Map<String, String> result = new HashMap<>();

            // Remove [] or {} brackets
            text = text.replaceAll("[\\[\\]{}]", "");
            if (text.contains("=")) {
                String[] parts = text.split("=", 2);
                if (parts.length == 2) {
                    result.put(parts[0].trim(), parts[1].trim());
                }
            }
            return result;
        }

        // Anything else (number, boolean, etc.) → empty map
        return new HashMap<>();
    }
}
