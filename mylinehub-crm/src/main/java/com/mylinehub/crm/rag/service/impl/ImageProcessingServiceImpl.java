package com.mylinehub.crm.rag.service.impl;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.mylinehub.crm.rag.service.ImageProcessingService;

import java.io.File;
import java.util.StringJoiner;

@Service
public class ImageProcessingServiceImpl implements ImageProcessingService {

    @Value("${tesseract.data.path:}")
    private String tessDataPath;

    private ITesseract tesseract;

    private ITesseract getTesseract() {
        if (tesseract == null) {
            Tesseract t = new Tesseract();
            if (tessDataPath != null && !tessDataPath.isBlank()) {
                t.setDatapath(tessDataPath);
            }
            // you may set language via t.setLanguage("eng");
            tesseract = t;
        }
        return tesseract;
    }

    @Override
    public String extractTextFromImage(File imageFile) throws Exception {
        try {
            ITesseract t = getTesseract();
            String result = t.doOCR(imageFile);
            if (result == null) return "";
            return result.replaceAll("\\s+", " ").trim();
        } catch (TesseractException te) {
            System.err.println("[ImageProcessing] OCR failed: " + te.getMessage());
            return "";
        } catch (Exception e) {
            System.err.println("[ImageProcessing] Unexpected OCR error: " + e.getMessage());
            return "";
        }
    }

    @Override
    public String extractMetadataText(File imageFile) throws Exception {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            StringJoiner out = new StringJoiner(" ");
            for (Directory dir : metadata.getDirectories()) {
                for (Tag tag : dir.getTags()) {
                    out.add(tag.toString());
                }
            }
            return out.toString().replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            // optional failure; not fatal
            System.err.println("[ImageProcessing] metadata extraction failed: " + e.getMessage());
            return "";
        }
    }
}