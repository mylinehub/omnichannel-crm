package com.mylinehub.crm.whatsapp.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppUpdateChatHistoryDTO {
	public String conversationId;
	public boolean sent;
	public boolean delivered;
	public boolean read;
	public boolean readSelf;
	public boolean failed;
	public boolean deleted;
    public boolean whatsAppActualBillable;
    public String whatsAppError;
    private Instant createdOn;
}
