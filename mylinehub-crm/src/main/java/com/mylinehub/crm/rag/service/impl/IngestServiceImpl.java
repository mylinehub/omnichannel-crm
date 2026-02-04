package com.mylinehub.crm.rag.service.impl;

import com.mylinehub.crm.rag.dto.IngestResponse;
import com.mylinehub.crm.rag.dto.IngestRequest;
import com.mylinehub.crm.rag.model.Document;
import com.mylinehub.crm.rag.repository.DocumentRepository;
import com.mylinehub.crm.rag.service.EmbeddingService;
import com.mylinehub.crm.rag.service.FileProcessingService;
import com.mylinehub.crm.rag.service.IngestService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IngestServiceImpl implements IngestService {

    private final FileProcessingService fileProcessingService;
    private final EmbeddingService embeddingService;
    private final DocumentRepository documentRepository;

    @Value("${max.file.size.mb}")
    private int maxFileSizeMb;

    @Value("${chunk.size.tokens}")
    private int chunkSizeTokens;

    @Value("${chunk.overlap.tokens}")
    private int chunkOverlapTokens;
    
    public IngestServiceImpl(EmbeddingService embeddingService,FileProcessingService fileProcessingService, DocumentRepository documentRepository) {
        this.fileProcessingService = fileProcessingService;
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
    }

    public IngestResponse ingestUrl(IngestRequest req) { 
    	IngestResponse resp = new IngestResponse(); 
    	try { 
	    		System.out.println("[IngestService] Fetching URL: " + req.getUrl());
	    		org.jsoup.nodes.Document doc = Jsoup.connect(req.getUrl()).get(); 
	    		// Extract all body text 
	    		String textContent = extractTextFromBody(doc); 
	    		// Chunking (example: 1000 chars per chunk)
	    		List<String> chunks = embeddingService.chunkText(textContent);
	    		// Store in your vector DB (Postgres pgvector, etc.) 
//	    		for (String chunk : chunks) 
//	    		{ // embedding + insert // 
//	    			vectorStore.save(req.getOrganization(), chunk); 
//	    		} 
	    		
	    		resp.setSuccess(true); 
	    		resp.setMessage("Ingested " + chunks.size() + " chunks from " + req.getUrl()); 
    		} 
    	catch (Exception e) 
    	{ resp.setSuccess(false); 
    	resp.setMessage("Failed to ingest url: " + e.getMessage());
    	} 
    	
    	return resp;
    	
    }
    

    private String extractTextFromBody(org.jsoup.nodes.Document doc) {
        StringBuilder sb = new StringBuilder();
        for (Element el : doc.body().select("*")) {
            if (el.ownText() != null && !el.ownText().isEmpty()) {
                sb.append(el.ownText()).append(" ");
            }
        }
        return sb.toString().trim();
    }
    
    @Override
    @Transactional
    public IngestResponse ingest(IngestResponse request) throws Exception {
        System.out.println("[IngestService] Ingest request for org: " + request.getOrganization());
        // Resolve organization id from name - you said Organization entity exists.
        // For now assume organizationName -> organizationId resolution is implemented elsewhere.


        List<String> processedHashes = new ArrayList<>();
        MultipartFile f = request.getFile();
        if (f == null || f.isEmpty()) {
            return error("No file provided");
        }
        
        System.out.println("[IngestService] File Size: " + f.getSize());
        
        if (f.getSize() > (long) maxFileSizeMb * 1024 * 1024) {
            return error("File too large; max allowed: " + maxFileSizeMb + " MB");
        }

        List<String> single = fileProcessingService.ingestFile(f, request.getOrganization(), request.getUploader());
        processedHashes.addAll(single);

        System.out.println("[IngestService] Ingest finished for org: " + request.getOrganization());
        IngestResponse resp = new IngestResponse();
        resp.setSuccess(true);
        resp.setMessage("Processed");
        resp.setProcessedFileHashes(processedHashes);
        return resp;
    }

    @Override
    @Transactional
    public IngestResponse deleteByHashes(String organization, List<String> fileHashes) {
        System.out.println("[IngestService] Delete request for org: " + organization + " hashes:" + fileHashes);

        for (String h : fileHashes) {
            Optional<Document> dOpt = documentRepository.findByFileHashAndOrganization(h, organization);
            dOpt.ifPresent(d -> {
                documentRepository.delete(d);
                System.out.println("[IngestService] Deleted document id " + d.getId());
            });
        }
        IngestResponse resp = new IngestResponse();
        resp.setSuccess(true);
        resp.setMessage("Deleted requested documents");
        return resp;
    }

    private IngestResponse error(String msg) {
        IngestResponse r = new IngestResponse();
        r.setSuccess(false);
        r.setMessage(msg);
        return r;
    }


}
