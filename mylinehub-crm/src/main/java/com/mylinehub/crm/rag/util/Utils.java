package com.mylinehub.crm.rag.util;

public class Utils {
    public static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
