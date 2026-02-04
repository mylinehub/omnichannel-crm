package com.mylinehub.crm.rag.repository;

import com.mylinehub.crm.rag.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(Long documentId);
    void deleteByDocumentId(Long documentId); // delete all chunks for a document
}
