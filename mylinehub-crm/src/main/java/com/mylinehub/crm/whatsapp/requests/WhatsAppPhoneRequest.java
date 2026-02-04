package com.mylinehub.crm.whatsapp.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppPhoneRequest {
	
//	public Long id;
	public String phoneMain;
	public String phoneWith;
	public Integer lastReadIndex;
	public String organization;
	int startOffset;
	int endOffset;

}