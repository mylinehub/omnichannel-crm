package com.mylinehub.crm.rag.repository;

import com.mylinehub.crm.rag.model.AIError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIErrorResponseRepository extends JpaRepository<AIError, Long> {
}