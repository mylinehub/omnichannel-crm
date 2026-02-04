package com.mylinehub.crm.rag.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IngestRequest {
    private String organization;
    private String uploader;
    private MultipartFile file; // for file upload
    private String url; // optional: for URL ingest
}
