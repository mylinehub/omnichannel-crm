package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BotOutputDTO {
	
	    private String organization;
	    private String messagetype;
	    private String message;
	    private String format;
	    private String domain;

}
