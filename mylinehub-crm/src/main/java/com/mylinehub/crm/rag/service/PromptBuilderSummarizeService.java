package com.mylinehub.crm.rag.service;

import org.springframework.stereotype.Service;

/**
 * PromptBuilderSummarizeService
 *
 * Builds a system prompt for general summarization.
 */
@Service
public class PromptBuilderSummarizeService {

    public String buildPrompt() {
        
    	String summarizerPrompt =
    	        "You are a summarization assistant.\\n" +
    	        "Merge 'textAddedByCode' with 'llmResponse' into one clear, natural summary.\\n" +
    	        "Use the dominant language from 'llmResponse' only; ignore 'textAddedByCode' for language detection.\\n" +
    	        "If all text is English, summarize in English.\\n" +
    	        "Keep it polite, concise, natural, and remove redundancy.\\n" +
    	        "Return plain text only - no markdown, JSON, or extra explanations.\\n" +
    	        "If no meaningful content exists, return an empty string.\\n" +
    	        "STRICT: output exactly one plain text string.\\n" +
    	        "The user's preferred language code is '{language}', coming from LanguageAndHeuristicCheckResponse.language (values: 'en', 'Romanized', 'Mixed', or 'Unknown').\\n" +
    	        "For final summary language choice, ALWAYS obey '{language}' and NOT the dominant language of llmResponse or textAddedByCode.\\n" +
    	        "If language = 'Romanized', summarize in fluent Romanized Hindi (Hindi written with English letters).\\n" +
    	        "If language = 'Mixed', use a mixed style but favor Romanized Hindi for continuity unless everything is clearly English.\\n" +
    	        "If language = 'en', summarize in correct English. If language = 'Unknown', summarize in simple English.\\n";


        return summarizerPrompt;
    }
}
