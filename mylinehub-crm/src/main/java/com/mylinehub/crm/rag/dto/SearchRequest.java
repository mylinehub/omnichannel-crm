package com.mylinehub.crm.rag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRequest {
    private String organization;
    private String botName;
    private String sessionId;
    private String customerId;
    private String originalMessage;
    private String language;
    private String mimeType;
    private String previousMessageResponse;
    private String messageHistory;
    AiInterfaceInputDto context;
    private int topK = 5;
}
