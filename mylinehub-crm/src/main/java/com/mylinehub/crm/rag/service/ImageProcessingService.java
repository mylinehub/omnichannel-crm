package com.mylinehub.crm.rag.service;

import java.io.File;

public interface ImageProcessingService {
    /**
     * Run OCR on the provided image file and return extracted text (empty string if none).
     */
    String extractTextFromImage(File imageFile) throws Exception;

    /**
     * Extract any useful caption-like texts (alt text already handled on page),
     * or metadata if needed. Return a compact string (or empty).
     */
    String extractMetadataText(File imageFile) throws Exception;
}