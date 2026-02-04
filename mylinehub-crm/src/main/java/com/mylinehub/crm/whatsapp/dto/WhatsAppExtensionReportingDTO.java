package com.mylinehub.crm.whatsapp.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppExtensionReportingDTO {
	private Long totalMessagesSend;
}
