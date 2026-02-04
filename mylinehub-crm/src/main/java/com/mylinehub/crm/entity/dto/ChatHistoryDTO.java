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
public class ChatHistoryDTO {
	public Long id;
    public String organization;
    public String extensionMain;
    public String extensionWith;
    public int lastReadIndex;
    public List<ChatKeyValueDTO> chats;
    public boolean isDeleted;
    private Date lastUpdateTime;
}
