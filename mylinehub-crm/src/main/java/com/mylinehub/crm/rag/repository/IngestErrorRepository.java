package com.mylinehub.crm.rag.repository;

import com.mylinehub.crm.rag.model.IngestError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestErrorRepository extends JpaRepository<IngestError, Long> {
}
