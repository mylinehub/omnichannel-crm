package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.models.StasisAppConfig;

import reactor.core.publisher.Mono;

/**
 * LoginService:
 *  - Performs system login per stasis_app_name (tenant)
 *  - Caches JWT tokens per tenant
 */
public interface LoginService {

    /**
     * Returns a valid system token for the given stasis_app_name.
     * If cached token is expired or missing, performs login.
     *
     * @param stasisAppName The ARI app / tenant identifier
     * @return Mono with a valid JWT token
     */
    Mono<String> getValidSystemToken(StasisAppConfig cfg);
}
