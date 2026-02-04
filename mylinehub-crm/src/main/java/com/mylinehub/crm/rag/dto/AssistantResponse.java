package com.mylinehub.crm.rag.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssistantResponse {
    private String id;
    private String object;
    private String name;
    private String model;
    private String description;
    private String instructions;
    private Map<String, Object> metadata;

    // getters and setters
}