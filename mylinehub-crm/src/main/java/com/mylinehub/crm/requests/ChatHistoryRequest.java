package com.mylinehub.crm.requests;

import java.util.List;

import com.mylinehub.crm.entity.dto.ChatKeyValueDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatHistoryRequest {
	
	public String organization;
	public String extensionMain;
	public String extensionWith;
	public  List<ChatKeyValueDTO> chat;

}
