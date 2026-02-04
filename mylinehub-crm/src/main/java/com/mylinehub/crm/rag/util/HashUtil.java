package com.mylinehub.crm.rag.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashUtil {
    public static String sha256OfFile(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] block = new byte[4096];
            int len;
            while ((len = fis.read(block)) > 0) {
                digest.update(block, 0, len);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
