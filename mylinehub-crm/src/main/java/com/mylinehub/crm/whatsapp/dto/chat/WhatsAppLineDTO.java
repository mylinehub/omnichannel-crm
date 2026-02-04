package com.mylinehub.crm.whatsapp.dto.chat;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppLineDTO {
	
	public String messageSubType;
	public String stringMessage;
	public List<String> anchorMessage;
	public Date dateTime;
}
