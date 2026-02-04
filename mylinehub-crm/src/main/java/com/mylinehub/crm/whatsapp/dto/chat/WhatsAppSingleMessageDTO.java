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
public class WhatsAppSingleMessageDTO {
	public String messageType;
	public List <WhatsAppLineDTO> lines;
	public String blobMediaId;
	public String blobType;
	public String fileName;
	public String fileSizeInMB;
	public Date dateTime; 
}
