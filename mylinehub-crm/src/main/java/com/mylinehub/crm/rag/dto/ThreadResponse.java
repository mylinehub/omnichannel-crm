package com.mylinehub.crm.rag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThreadResponse {
    private String id;
    private String object;
    private long created_at;
    private Map<String, Object> metadata;
}
