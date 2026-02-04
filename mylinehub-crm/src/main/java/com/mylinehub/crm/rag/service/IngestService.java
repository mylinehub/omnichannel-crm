package com.mylinehub.crm.rag.service;

import com.mylinehub.crm.rag.dto.IngestResponse;
import com.mylinehub.crm.rag.dto.IngestRequest;

public interface IngestService {
    IngestResponse ingest(IngestResponse request) throws Exception;

    IngestResponse deleteByHashes(String organization, java.util.List<String> fileHashes);
    
    IngestResponse ingestUrl(IngestRequest req);
}
