package com.mylinehub.crm.rag.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

public class ArchiveExtractor {

    /**
     * Extracts ZIP archive to target directory. Returns true if successful.
     */
    public static void extractZip(File zipFile, Path targetDir) throws Exception {
        try (ZipFile zf = new ZipFile(zipFile)) {
            Enumeration<ZipArchiveEntry> entries = zf.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                Path out = targetDir.resolve(entry.getName()).normalize();
                Files.createDirectories(out.getParent());
                try (InputStream is = zf.getInputStream(entry)) {
                    Files.copy(is, out);
                }
            }
        }
    }

    // RAR extraction is optional — you can use junrar in similar approach.
}