package com.mylinehub.crm.rag.service;

import com.mylinehub.crm.rag.data.PromptAndAssistantData;
import com.mylinehub.crm.rag.data.dto.PromptAndAssistantDataDTO;
import com.mylinehub.crm.rag.model.SystemPrompts;
import com.mylinehub.crm.rag.repository.SystemPromptRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing system prompts.
 * Performs CRUD operations on DB first and then updates in-memory cache.
 * Provides methods to fetch all prompts, active prompts,
 * and prompts filtered by type and organization.
 * 
 * Author: Anand Goel
 * Version: 1.1
 */
@Service
@AllArgsConstructor
public class SystemPromptService {

    private final SystemPromptRepository systemPromptRepository;

    /**
     * Fetch all prompts for a given organization
     */
    public List<SystemPrompts> getAllPromptsByOrganization(String organization) {
        return systemPromptRepository.findAllByOrganization(organization);
    }

    /**
     * Fetch all prompts for a given type and organization
     */
    public List<SystemPrompts> getAllPromptsByTypeAndOrganization(String type, String organization) {
        return systemPromptRepository.findAllByTypeAndOrganization(type, organization);
    }

    /**
     * Fetch only active prompts for a given organization
     */
    public List<SystemPrompts> getActivePromptsByOrganization(String organization) {
        return systemPromptRepository.findAllByOrganizationAndActiveTrue(organization);
    }

    /**
     * Fetch only active prompts for a given type and organization
     */
    public List<SystemPrompts> getActivePromptsByTypeAndOrganization(String type, String organization) {
        return systemPromptRepository.findAllByTypeAndOrganizationAndActiveTrue(type, organization);
    }

    /**
     * Fetch a prompt by ID
     */
    public Optional<SystemPrompts> getPromptById(Long id) {
        return systemPromptRepository.findById(id);
    }

    /**
     * Create a new prompt
     * Saves to DB and then updates in-memory cache
     */
    public SystemPrompts createPrompt(SystemPrompts prompt) {
        SystemPrompts saved = systemPromptRepository.save(prompt);

        // Update in-memory cache
        PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
        dto.setAction("update");
        dto.setOrganization(saved.getOrganization());
        dto.setPrompt(saved);
        PromptAndAssistantData.workWithAllSystemPrompts(dto);

        return saved;
    }

    /**
     * Update an existing prompt
     * Updates DB first, then in-memory cache
     */
    public SystemPrompts updatePrompt(SystemPrompts prompt) {
        if (prompt.getId() == null) {
            throw new IllegalArgumentException("Prompt ID cannot be null for update.");
        }

        SystemPrompts updated = systemPromptRepository.save(prompt);

        // Update in-memory cache
        PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
        dto.setAction("update");
        dto.setOrganization(updated.getOrganization());
        dto.setPrompt(updated);
        PromptAndAssistantData.workWithAllSystemPrompts(dto);

        return updated;
    }

    /**
     * Delete a prompt by ID
     * Deletes from DB and then removes from in-memory cache
     */
    public void deletePrompt(Long id) {
        Optional<SystemPrompts> existing = systemPromptRepository.findById(id);
        if (existing.isPresent()) {
            SystemPrompts prompt = existing.get();
            systemPromptRepository.deleteById(id);

            // Remove from in-memory cache
            PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
            dto.setAction("delete");
            dto.setOrganization(prompt.getOrganization());
            PromptAndAssistantData.workWithAllSystemPrompts(dto);
        }
    }
}
