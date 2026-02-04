package com.mylinehub.crm.entity.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatKeyValueListDTO {
	
	public List<ChatKeyValueDTO> allChats;

}
