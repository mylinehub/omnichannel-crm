/*
 * Auto-formatted: src/main/java/com/mylinehub/voicebridge/rag/RagService.java
 */
package com.mylinehub.voicebridge.service;

import reactor.core.publisher.Mono;

/**
 * Retrieval-Augmented-Generation (RAG) service.
 *
 * Multi-tenant note:
 *  - We MUST know which stasis_app_name (tenant) the request belongs to,
 *    because each tenant may have its own:
 *      * base URL
 *      * RAG endpoint
 *      * system login credentials (token)
 */
public interface RagService {

    /**
     * Fetch contextual snippets for AI prompt building.
     *
     * @param stasisAppName  The active ARI app name (tenant config selector)
     * @param organization   Organization / namespace within tenant
     * @param text           Input text to search context for
     * @return Mono emitting aggregated context
     */
    Mono<String> fetchContext(String stasisAppName, String organization, String text);
}
