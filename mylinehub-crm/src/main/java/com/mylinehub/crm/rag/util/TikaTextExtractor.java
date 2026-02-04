package com.mylinehub.crm.rag.util;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.FileInputStream;

@Component
public class TikaTextExtractor {

    /**
     * Extracts full text from a file using Apache Tika.
     * This version removes the default 100,000 character limit.
     *
     * @param file the input file
     * @return extracted text, or null if extraction failed
     */
    public String extractTextFromFile(File file) {
        try (FileInputStream input = new FileInputStream(file)) {
            System.out.println("[Tika] Extracting text from file: " + file.getName());

            AutoDetectParser parser = new AutoDetectParser();
            // -1 = unlimited characters, remove Tika's default limit
            ContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            parser.parse(input, handler, metadata, context);

            String text = handler.toString();
            System.out.println("[Tika] Extraction length: " + (text != null ? text.length() : 0));
            System.out.println("*********************TIKA***************************");
            System.out.println(text);
            System.out.println("*********************TIKA***************************");

            return text;

        } catch (Exception e) {
            System.out.println("[Tika] Extraction failed: " + e.getMessage());
            return null;
        }
    }
}
