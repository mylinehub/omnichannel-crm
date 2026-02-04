package com.mylinehub.crm.whatsapp.dto.chat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppChatKeyValueListDTO {
	
	public List<WhatsAppChatKeyValueDTO> allChats;

}
