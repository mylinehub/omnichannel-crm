package com.mylinehub.crm.rag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultDTO {

	private Long embeddingId;
    private String organization;
    private Long documentChunkId;
    private String chunkText;
    private Long documentId;
    private String originalFilename;
    private String fileHash;
    private Double distance;
    private Integer rank;
}
