package com.mylinehub.crm.rag.data.dto;

import java.util.List;

import com.mylinehub.crm.rag.model.AssistantEntity;
import com.mylinehub.crm.rag.model.SystemPrompts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromptAndAssistantDataDTO {
	
	private String action;
	private String organization;
	private SystemPrompts prompt;
	private AssistantEntity assistantEntity;
	private List<SystemPrompts> batchPrompt;
	private List<AssistantEntity> batchAssistantEntity;

}
