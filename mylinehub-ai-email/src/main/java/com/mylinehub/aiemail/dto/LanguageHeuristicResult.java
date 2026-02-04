package com.mylinehub.aiemail.dto;

import lombok.*;

/**
 * Result from language & heuristic detection.
 *
 * Matches the JSON contract described in your PromptBuilderHeuristicAndEnglishLanguageConvertorService.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageHeuristicResult {

    /**
     * One of 'en', 'Romanized', 'Mixed', or 'Unknown'.
     */
    private String language;

    /**
     * Fully corrected English version of the message.
     * Can be null if the original is already proper English.
     */
    private String englishTranslation;

    private Boolean customerStillWriting;
    private Boolean noFurtherTextRequired;
    private Boolean calculationRequired;
    private Boolean customerMaybeAskingDemoVideo;
}
