/*
 * Auto-formatted: src/main/java/com/mylinehub/voicebridge/dto/Message.java
 */
package com.mylinehub.voicebridge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * File: src/main/java/com/mylinehub/voicebridge/dto/Message.java
 * Project: MyLineHub VoiceBridge (RAG + Org + OpenAI)
 *
 * Purpose:
 *  - Represents generic message payloads exchanged between
 *    telephony, AI, and RAG services.
 *
 * Notes:
 *  - Uses Lombok for boilerplate reduction.
 *  - Ignores unknown fields to remain forward-compatible.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
  private String role;
  private String content;
}
