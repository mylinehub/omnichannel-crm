package com.mylinehub.crm.whatsapp.requests;

import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppChatHistoryRequest {
	
	public String organization;
	public String phoneNumberMain;
	public String PhonenumberWith;
	public  WhatsAppChatHistory details;

}
