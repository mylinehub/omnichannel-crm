package com.mylinehub.crm.utils;

import java.time.Instant;

public class TicketUtils {

    /**
     * Generates a unique support ticket ID in the format:
     * REQ{epoch-seconds}
     * Example: REQ1729328472
     */
    public static String generateTicketId() {
        long epochSeconds = Instant.now().getEpochSecond(); // current time in seconds
        return "REQ" + epochSeconds;
    }
}
