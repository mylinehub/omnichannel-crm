package com.mylinehub.crm.rag.service;

import java.util.List;

import com.mylinehub.crm.rag.dto.ResultDTO;

public interface EmbeddingService {
	
	List<String> chunkText(String text);
    /**
     * Return embedding as float[] for given text
     */
    float[] embedText(String text) throws Exception;
    
    /**
     * Return embeddings for multiple chunks at once
     */
    List<float[]> embedTextBatch(List<String> texts) throws Exception;
    
    List<String> searchSimilarEmbeddings(String organization, String input) throws Exception;
    
}
