package com.mylinehub.aiemail.service;

import reactor.core.publisher.Mono;

/**
 * Uses OpenAI (or compatible) API to turn context + inbound email into a reply body.
 */
public interface OpenAiEmailComposerService {

    /**
     * Compose an email reply for a given organization.
     *
     * @param organization      organization identifier / name
     * @param inboundSummary    summarised inbound email text (original language)
     * @param ragContext        contextual string from RAG (in English)
     * @param languageCode      detected language code from LanguageHeuristicResult
     *                          (e.g. 'en', 'Romanized', 'Mixed', 'Unknown')
     * @return Mono with final reply text
     */
    Mono<String> composeReply(String organization,
                              String inboundSummary,
                              String ragContext,
                              String languageCode);
}
