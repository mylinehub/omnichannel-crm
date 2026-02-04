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
public class WhatsAppChatKeyValueDTO {
	public String fromExtension;
	public String fromName;
	public String fromTitle;
	public String messageOrigin;
	public String whatsAppMessageId;
	public boolean send;
	public boolean delivered;
	public boolean read;
	public boolean failed;
	public boolean deleted;
	public List <WhatsAppSingleMessageDTO> messages;
}
