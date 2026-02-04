package com.mylinehub.aiemail.service;

import reactor.core.publisher.Mono;

/**
 * Logs in with system credentials and manages the system JWT token.
 */
public interface LoginService {

    /**
     * Returns a valid system token.
     * If current static token is null or expired, it will call the login API and refresh it.
     */
    Mono<String> getValidSystemToken();
}
