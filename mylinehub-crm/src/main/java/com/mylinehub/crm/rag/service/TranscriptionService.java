package com.mylinehub.crm.rag.service;

import java.io.File;

public interface TranscriptionService {
    /**
     * Synchronously call provider to transcribe media file.
     * Return the transcribed text.
     */
    String transcribe(File file, String mimeType) throws Exception;
}
