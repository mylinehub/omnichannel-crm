package com.mylinehub.crm.rag.dto;

import lombok.Data;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IngestResponse {
    private boolean success;
    private String message;
    private List<String> processedFileHashes;
    private String organization;
    private String uploader;
    MultipartFile file;
    
}
