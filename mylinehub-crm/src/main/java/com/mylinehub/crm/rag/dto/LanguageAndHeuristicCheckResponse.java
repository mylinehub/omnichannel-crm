package com.mylinehub.crm.rag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageAndHeuristicCheckResponse {
    private String language;
    private String englishTranslation;
    private boolean customerStillWriting;
    private boolean noFurtherTextRequired;
    private boolean calculationRequired;
    private boolean customerMaybeAskingDemoVideo;
}
