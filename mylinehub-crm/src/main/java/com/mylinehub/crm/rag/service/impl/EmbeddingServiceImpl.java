package com.mylinehub.crm.rag.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.rag.dto.ResultDTO;
import com.mylinehub.crm.rag.repository.EmbeddingRepository;
import com.mylinehub.crm.rag.service.EmbeddingService;
import com.mylinehub.crm.rag.service.FileProcessingService;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private final ObjectMapper mapper;
    private final OkHttpClient client;
    private final EmbeddingRepository embeddingRepository;
    
    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${embedding.model}")
    private String embeddingModel;
    
    @Value("${chunk.size.tokens}")
    private int chunkSizeTokens;

    @Value("${chunk.overlap.tokens}")
    private int chunkOverlapTokens;

    public EmbeddingServiceImpl(OkHttpClient client, ObjectMapper mapper, EmbeddingRepository embeddingRepository) {
    	this.client=client;
        this.mapper = mapper;
        this.embeddingRepository = embeddingRepository;
    }
    
    @Override
    public List<String> chunkText(String text) {
        if (text == null) return List.of();
        int chunkChars = chunkSizeTokens * 4;
        int overlapChars = chunkOverlapTokens * 4;
        List<String> out = new ArrayList<>();
        int start = 0;
        int len = text.length();
        while (start < len) {
            int end = Math.min(len, start + chunkChars);
            out.add(text.substring(start, end).trim());
            start = Math.max(end - overlapChars, end);
        }
        return out;
    }

    @Override
    public List<String> searchSimilarEmbeddings(String organization, String input) throws Exception {
    	
    	List<String> allResults  = new ArrayList<>();
    	//Convert into embedding & fetch RAG response
		List<String> chunkedText = null;
		List<float[]> chunkedVectorList = null;
		
    	//System.out.println("chunking text");
		chunkedText = chunkText(input);
		
		if(chunkedText != null && chunkedText.size() > 0) {
			
		//System.out.println("Generate embedding");
		chunkedVectorList = embedTextBatch(chunkedText);
		
		   if(chunkedVectorList != null && chunkedVectorList.size() > 0) {
				for (float[] vector : chunkedVectorList) {
					       List<ResultDTO> results = this.embeddingRepository.findNearestByVector(organization, vector, 2);
					       if (results.size() > 0) {
					    	   for(ResultDTO result:results) {
						    	   allResults.add(result.getChunkText());
					    	   }
					       }
				  }
			} 
		    else {
				allResults = null;
		    }
		  }
		 else {
			allResults = null;
		  }
			
        return allResults;
    }
    
    @Override
    public float[] embedText(String text) throws Exception {
    	
    	//System.out.println("embedText API call");
	    
        //System.out.println("[EmbeddingService] Preparing embedding request...");
        String url = "https://api.openai.com/v1/embeddings";

        JsonNode requestBody = mapper.createObjectNode()
                .put("model", embeddingModel)
                .set("input", mapper.getNodeFactory().textNode(text));


        RequestBody body = RequestBody.create( MediaType.parse("application/json"),mapper.writeValueAsString(requestBody));

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + openAiApiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
        	String respBody =  new OkHttpResponseFunctions().extractStringFromResponseAndCloseBuffer(response);
            //System.out.println("[EmbeddingService] Embedding response received...");
            JsonNode root = mapper.readTree(respBody);
            JsonNode embeddings = root.at("/data/0/embedding");
            if (embeddings == null || !embeddings.isArray()) {
                throw new IOException("No embeddings in response");
            }
            float[] vector = new float[embeddings.size()];
            for (int i = 0; i < embeddings.size(); i++) {
                vector[i] = (float) embeddings.get(i).asDouble();
            }
            //System.out.println("[EmbeddingService] Embedding vector length: " + vector.length);
            return vector;
        }
    }

    @Override
    public List<float[]> embedTextBatch(List<String> texts) throws Exception {
        if (texts == null || texts.isEmpty()) return List.of();

        //System.out.println("embedTextBatch API call");
	    
        String url = "https://api.openai.com/v1/embeddings";

        // Build JSON payload: {"model": "...", "input": ["chunk1", "chunk2", ...]}
        JsonNode requestBody = mapper.createObjectNode()
                .put("model", embeddingModel)
                .set("input", mapper.valueToTree(texts)); // array of strings

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                mapper.writeValueAsString(requestBody)
        );

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + openAiApiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respBody = new OkHttpResponseFunctions().extractStringFromResponseAndCloseBuffer(response);
            JsonNode root = mapper.readTree(respBody);
            JsonNode data = root.get("data");
            if (data == null || !data.isArray()) throw new IOException("Invalid embedding response");

            List<float[]> embeddings = new ArrayList<>();
            for (JsonNode item : data) {
                JsonNode vecNode = item.get("embedding");
                float[] vec = new float[vecNode.size()];
                for (int i = 0; i < vecNode.size(); i++) {
                    vec[i] = (float) vecNode.get(i).asDouble();
                }
                embeddings.add(vec);
            }
            return embeddings;
        }
    }
}
