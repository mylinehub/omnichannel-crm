package com.mylinehub.crm.whatsapp.dto.chat;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.repository.WhatsAppNumberReportRepository;
import com.mylinehub.crm.whatsapp.service.WhatsAppNumberReportService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppReportDataParameterDTO {
	String phoneNumberMain;
	String phoneNumberWith;
	String action;
	String organization;
	WhatsAppNumberReportRepository whatsAppNumberReportRepository;
	WhatsAppNumberReportService whatsAppNumberReportService;
	ApplicationContext applicationContext;
	boolean isAiMessage;
	boolean isCampaignMessage;
	boolean isManualMessage;
	WhatsAppChatHistory inputDTO;
	WhatsAppPhoneNumber phoneNumber;
	Long amount;
	
}
