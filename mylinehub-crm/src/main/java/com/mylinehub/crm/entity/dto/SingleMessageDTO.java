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
public class SingleMessageDTO {
	public String messageType;
	public List <LineDTO> lines;
	public String blobMessage;
	public String blobType;
	public String fileName;
	public String fileSizeInMB;
	public Date dateTime; 
}
