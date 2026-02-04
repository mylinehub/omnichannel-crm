package com.mylinehub.aiemail.service;

import reactor.core.publisher.Mono;

/**
 * Fetches contextual snippets from MyLineHub RAG/vector store.
 */
public interface RagService {

    Mono<String> fetchContext(String organization, String text);
}
