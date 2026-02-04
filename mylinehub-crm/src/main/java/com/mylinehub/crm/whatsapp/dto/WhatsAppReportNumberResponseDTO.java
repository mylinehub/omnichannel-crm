package com.mylinehub.crm.whatsapp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppReportNumberResponseDTO {
	String adminFirstName;
	String adminLastName;
	String adminPhoneNumber;
	String adminExtension;
	String adminEmail;
	String phoneNumberMain;
	List<WhatsAppMessageCountForNumberDTO> messageDetails;

}
