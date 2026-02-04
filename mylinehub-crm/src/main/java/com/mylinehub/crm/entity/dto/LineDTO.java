package com.mylinehub.crm.entity.dto;

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
public class LineDTO {
	
	public String messageSubType;
	public String stringMessage;
	public List<String> anchorMessage;
	public Date dateTime;
}
