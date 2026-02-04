package com.mylinehub.crm.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.List;

@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return (attribute == null || attribute.isEmpty())
                    ? null
                    : mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting list to JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return Collections.emptyList();
            }

            String s = dbData.trim();

            // If already JSON array
            if (s.startsWith("[")) {
                return mapper.readValue(s, new TypeReference<List<String>>() {});
            }

            // Backward compatibility: old single-value string in DB
            return Collections.singletonList(s);

        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to list. dbData=" + dbData, e);
        }
    }

}
