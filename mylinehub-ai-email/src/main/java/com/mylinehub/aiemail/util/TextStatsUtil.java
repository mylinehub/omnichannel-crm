package com.mylinehub.aiemail.util;

/**
 * Simple helpers for word and approximate token counting.
 *
 * NOTE:
 *  - Word count = split on whitespace.
 *  - Token count = rough approximation based on character length.
 *    (For OpenAI-style tokens, you can later plug in a tokenizer lib.)
 */
public final class TextStatsUtil {

    private TextStatsUtil() {
    }

    public static int wordCount(String text) {
        if (text == null) return 0;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return 0;
        // split on whitespace
        return trimmed.split("\\s+").length;
    }

    /**
     * Very rough token estimate:
     * - Many English/Gujarati/Hinglish messages are ~3–4 chars per token.
     * - We use chars / 4 as a quick approximation.
     */
    public static int approxTokenCount(String text) {
        if (text == null) return 0;
        int chars = text.length();
        if (chars == 0) return 0;
        return (int) Math.ceil(chars / 4.0);
    }
}
