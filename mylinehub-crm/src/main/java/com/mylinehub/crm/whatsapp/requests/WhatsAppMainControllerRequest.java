package com.mylinehub.crm.whatsapp.requests;

import java.util.Date;
import java.util.List;

import com.mylinehub.crm.whatsapp.dto.WhatsAppManagementEmployeeDto;

import lombok.Data;

@Data
public class WhatsAppMainControllerRequest {

	Long id;
	Date dayUpdated;
	String phoneNumber;
	String typeOfReport;
	String category;
	String organization;
	Long whatsappProjectId;
	Long adminEmployeeID;
	boolean active;
	List<WhatsAppManagementEmployeeDto> employeeExtensionAccessList;
}
