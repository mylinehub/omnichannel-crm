package com.mylinehub.aiemail.dto;

import lombok.*;

import java.time.Instant;

/**
 * DTO sent to MyLineHub CRM for analytics / reporting.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailReportDTO {

    private String organization;
    private String emailAddress;

    private String from;
    private String to;
    private String subject;

    private Instant receivedAt;
    private Instant repliedAt;

    private boolean aiHandled;
    private String aiSummary;

    /**
     * Detected language code (e.g. 'en', 'Romanized', 'Mixed', 'Unknown')
     * from LanguageHeuristicResult.
     */
    private String language;
    
    /** Word count of original inbound email (after basic trim). */
    private int inputWordCount;

    /** Approx token count of inbound email (rough estimate). */
    private int inputTokenCount;

    /** Word count of AI-generated reply. */
    private int aiWordCount;

    /** Approx token count of AI-generated reply. */
    private int aiTokenCount;
    
}
