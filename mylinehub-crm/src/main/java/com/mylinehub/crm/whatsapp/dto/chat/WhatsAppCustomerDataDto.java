package com.mylinehub.crm.whatsapp.dto.chat;

import java.util.Date;
import java.util.List;

import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppCustomerDataDto {
	
	//Create or store customer
	Customers customer;
	Date lastVerified;
	
	//Whats App AI
	boolean isBlockedForAI;
	Date lastBlockedTime;
	String whatsAppBotThread;
	String languageThread;
	String summarizeThread;
	String script;
	String language;
	
	//Chat
	private List<WhatsAppChatHistory> chatList;
	String customerHeuristicMessageCollationEnglish;
	String customerHeuristicMessageCollationOriginal;
	
	int isBlockedForAICount;
	boolean sessionFirstMessage;
	
	//record level
	Date lastUpdated;
}
