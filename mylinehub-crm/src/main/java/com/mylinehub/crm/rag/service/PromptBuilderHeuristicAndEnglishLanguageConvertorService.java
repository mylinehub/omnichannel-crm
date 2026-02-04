package com.mylinehub.crm.rag.service;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderHeuristicAndEnglishLanguageConvertorService {

    public String buildPrompt() {
    	
    	String languageAndHeuristicPrompt =
    	        "You are a language & intent detector. Return only JSON per LanguageAndHeuristicCheckResponse.\\n" +
    	        "Rules:\\n" +
    	        "1. language: One of 'en', 'Romanized', 'Mixed', or 'Unknown' (symbols/emojis only).\\n" +
    	        "   - 'en' = English written properly.\\n" +
    	        "   - 'Romanized' = Hindi or another language written using English letters.\\n" +
    	        "   - 'Mixed' = English + Romanized combined.\\n" +
    	        "2. englishTranslation: always provide the fully corrected English version of the message.\\n" +
    	        "   Set to null ONLY if the original message is already correct English without typos, informal spelling, or grammar issues.\\n" +
    	        "3. customerStillWriting: true ONLY if the message clearly looks incomplete (for example, a sentence abruptly cut or ending mid-word).\\n" +
    	        "   If there is any doubt, set false.\\n" +
    	        "4. noFurtherTextRequired: true ONLY if the message is a brief acknowledgment like 'ok', 'yes', 'thanks', 'done', or 'got it'.\\n" +
    	        "   If unsure, set false.\\n" +
    	        "5. calculationRequired: true if the message mentions or implies pricing, quote, cost, total, seats, recharge, plan, payment, estimation, or number of channels.\\n" +
    	        "   If there are numbers and pricing keywords even with slight doubt, set true.\\n" +
    	        "6. customerMaybeAskingDemoVideo: true if the message asks for demo, tutorial, walkthrough, how-to, or video explanation.\\n" +
    	        "   If there is slight doubt, set true; only false if clearly not asking for video.\\n" +
    	        "7. Maintain response consistency - if the user's message is in Romanized or Hindi, prefer the same language style in AI replies for continuity.\\n" +
    	        "IMPORTANT: Correct typos, slang, and half-written words before judgment. Identify the user's intent and tone first.\\n" +
    	        "STRICT: customerStillWriting and noFurtherTextRequired must be true only if 100% certain.\\n" +
    	        "Output: A single clean JSON object following LanguageAndHeuristicCheckResponse - no commentary, extra text, or formatting beyond JSON.\\n" +
    	        "Downstream assistants will use the 'language' field to choose reply language; 'englishTranslation' is only for understanding and RAG and must NOT be used to decide reply language.\\n";

        return languageAndHeuristicPrompt;
    }
}
