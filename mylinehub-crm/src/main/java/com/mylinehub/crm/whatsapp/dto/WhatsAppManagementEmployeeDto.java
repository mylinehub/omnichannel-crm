package com.mylinehub.crm.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppManagementEmployeeDto {
	    private Long id;
	    private String firstName;
	    private String lastName;
	    private String role;
	    private String departmentName;
	    private String phonenumber;
	    private String extension;
	    private String email;
}
