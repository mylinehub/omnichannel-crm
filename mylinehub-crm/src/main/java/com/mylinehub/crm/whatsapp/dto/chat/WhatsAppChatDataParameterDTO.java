package com.mylinehub.crm.whatsapp.dto.chat;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.repository.WhatsAppChatHistoryRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppChatDataParameterDTO {
	String phoneNumberMain;
	String phoneNumberWith;
	String action;
	String organization;
	ApplicationContext applicationContext;
	WhatsAppChatHistoryRepository whatsAppChatHistoryRepository;
	WhatsAppChatHistory details;
	String whatsAppMessageId;
	String error;
	String conversationId;
	
}
