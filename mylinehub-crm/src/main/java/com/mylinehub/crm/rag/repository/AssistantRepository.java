package com.mylinehub.crm.rag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mylinehub.crm.rag.model.AssistantEntity;

import java.util.Optional;
import java.util.List;

public interface AssistantRepository extends JpaRepository<AssistantEntity, Long> {
    Optional<AssistantEntity> findByNameAndOrganization(String name, String organization);
    Optional<AssistantEntity> findByAssistantId(String assistantId);
    List<AssistantEntity> findByAssistantIdIn(List<String> assistantIds);
}