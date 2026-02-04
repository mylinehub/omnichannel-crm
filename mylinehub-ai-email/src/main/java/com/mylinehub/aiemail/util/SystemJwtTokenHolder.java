package com.mylinehub.aiemail.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Holds the system JWT token in static variables so that all services
 * (RAG, other backend calls) can reuse it.
 *
 * - If token is null or expired => you must call login API to refresh it.
 */
public final class SystemJwtTokenHolder {

    private static volatile String token;
    private static volatile Instant expiresAt;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SystemJwtTokenHolder() {
    }

    /**
     * Store a new JWT token and compute its expiration time from the "exp" claim.
     */
    public static synchronized void updateToken(String jwt) {
        token = jwt;
        expiresAt = parseExpiry(jwt);
    }

    public static String getToken() {
        return token;
    }

    /**
     * Returns true if we have a token and it is not expired (with small safety margin).
     */
    public static boolean isTokenValid() {
        if (token == null || expiresAt == null) {
            return false;
        }
        // 60 seconds safety margin
        Instant now = Instant.now();
        return now.isBefore(expiresAt.minusSeconds(60));
    }

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
        } catch (Exception e) {
            // ignore, we'll just treat token as non-expiring and rely on 401s
        }
        return null;
    }
}
