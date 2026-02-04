package com.mylinehub.crm.rag.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlexibleStringListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        List<String> list = new ArrayList<>();

        if (node.isArray()) {
            for (JsonNode element : node) {
                list.add(element.asText());
            }
        } else if (node.isTextual()) {
            // single string : wrap into list
            list.add(node.asText());
        } else if (!node.isNull()) {
            // Fallback: convert any other type to string
            list.add(node.toString());
        }

        return list;
    }
}

