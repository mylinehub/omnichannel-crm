package com.mylinehub.crm.rag.repository;

import com.mylinehub.crm.rag.model.EmbeddingEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EmbeddingRepository extends JpaRepository<EmbeddingEntity, Long>, EmbeddingRepositoryCustom {
    // Additional derived queries if required
	    @Modifying
	    @Query("DELETE FROM EmbeddingEntity e WHERE e.documentChunkId IN :chunkIds")
	    void deleteByDocumentChunkIds(List<Long> chunkIds);
}
