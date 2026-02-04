package com.mylinehub.crm.rag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mylinehub.crm.rag.model.SystemPrompts;
import java.util.List;


public interface SystemPromptRepository extends JpaRepository<SystemPrompts, Long> {

    // All prompts for a given organization
    List<SystemPrompts> findAllByOrganization(String organization);

    // All prompts for a given type and organization
    List<SystemPrompts> findAllByTypeAndOrganization(String type, String organization);

    // Only active prompts for a given organization
    List<SystemPrompts> findAllByOrganizationAndActiveTrue(String organization);

    // Only active prompts for a given type and organization
    List<SystemPrompts> findAllByTypeAndOrganizationAndActiveTrue(String type, String organization);
}