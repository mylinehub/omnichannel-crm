package com.mylinehub.voicebridge.service;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class CompletionPromptBuilder {

  public String build(String template,
                      String organization,
                      String callerNumber,
                      String channelId,
                      String original,
                      String transcriptEn,
                      String summary) {

    if (template == null) return null;

    return template
        .replace("{{organization}}", safe(organization))
        .replace("{{callerNumber}}", safe(callerNumber))
        .replace("{{channelId}}", safe(channelId))
        .replace("{{transcriptEn}}", safe(transcriptEn))
        .replace("{{original}}", safe(original))
        .replace("{{summary}}", safe(summary))
        .replace("{{time}}", new Date().toString());
    
  }

  private String safe(String s) {
    return s == null ? "" : s;
  }
}
