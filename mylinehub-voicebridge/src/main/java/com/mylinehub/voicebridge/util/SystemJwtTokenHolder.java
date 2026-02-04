package com.mylinehub.voicebridge.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Multi-tenant JWT token holder.
 *
 * Stores and manages JWT tokens per stasis_app_name.
 *
 * Each tenant (stasis_app_name) has its own:
 *  - token
 *  - expiration time
 *
 * Thread-safe & lock-free for reads.
 */
public final class SystemJwtTokenHolder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * tokenStore maps:
     *    stasis_app_name -> TokenRecord(token, expiry)
     */
    private static final ConcurrentMap<String, TokenRecord> tokenStore =
            new ConcurrentHashMap<>();

    private SystemJwtTokenHolder() {}

    /**
     * Store a new JWT for a specific stasis_app_name.
     */
    public static void updateToken(String stasisAppName, String jwt) {
        Instant expiry = parseExpiry(jwt);
        tokenStore.put(stasisAppName, new TokenRecord(jwt, expiry));
    }

    /**
     * Retrieve the token for stasis_app_name.
     */
    public static String getToken(String stasisAppName) {
        TokenRecord rec = tokenStore.get(stasisAppName);
        return rec != null ? rec.token : null;
    }

    /**
     * Check if token exists and is not expired (60s safety margin).
     */
    public static boolean isTokenValid(String stasisAppName) {
        TokenRecord rec = tokenStore.get(stasisAppName);
        if (rec == null || rec.expiresAt == null) {
            return false;
        }
        return Instant.now().isBefore(rec.expiresAt.minusSeconds(60));
    }

    /**
     * Parse JWT "exp" field to compute expiration time.
     */
    private static Instant parseExpiry(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String payloadJson = new String(
                Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
            );

            JsonNode node = MAPPER.readTree(payloadJson);
            if (node.has("exp")) {
                long expSeconds = node.get("exp").asLong();
                return Instant.ofEpochSecond(expSeconds);
            }

        } catch (Exception ignored) {
            // fallback: no expiry => treat as invalid in isTokenValid()
        }
        return null;
    }

    /**
     * Holds token + expiration for each tenant.
     */
    private record TokenRecord(String token, Instant expiresAt) {}
}
