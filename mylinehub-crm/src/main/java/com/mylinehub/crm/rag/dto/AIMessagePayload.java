package com.mylinehub.crm.rag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIMessagePayload {
    private String messageText;       // The actual message to be sent
    private String messageType;       // "text", "image", "video", "audio"
    private String mimeType;          // "text/plain", "image/jpeg", etc.
    private String mediaUrl;          // Optional media URL (null for text)
    private String fileName;
}
