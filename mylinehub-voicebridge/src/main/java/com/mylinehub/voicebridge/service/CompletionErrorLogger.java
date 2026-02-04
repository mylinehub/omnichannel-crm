package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.models.LlmCompletionError;
import com.mylinehub.voicebridge.repository.LlmCompletionErrorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompletionErrorLogger {

  private final LlmCompletionErrorRepository repo;

  public void logError(String stasisAppName,
                       String organization,
                       String channelId,
                       String callerNumber,
                       String model,
                       int attempt,
                       String errorType,
                       String errorMessage,
                       String promptTemplate,
                       String transcriptEn,
                       String rawOutput) {

    repo.save(LlmCompletionError.builder()
        .stasisAppName(stasisAppName)
        .organization(organization)
        .channelId(channelId)
        .callerNumber(callerNumber)
        .model(model)
        .attempt(attempt)
        .errorType(errorType != null ? errorType : "UNKNOWN")
        .errorMessage(errorMessage)
        .promptTemplate(promptTemplate)
        .transcriptEn(transcriptEn)
        .rawOutput(rawOutput)
        .build());
  }
}
