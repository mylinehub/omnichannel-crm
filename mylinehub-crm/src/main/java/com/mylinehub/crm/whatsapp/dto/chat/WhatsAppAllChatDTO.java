package com.mylinehub.crm.whatsapp.dto.chat;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WhatsAppAllChatDTO {

//	private Long chatId;
    private Long phoneNumberId;
    private Long projectID;
    private String phoneNumber;
    public int badgeText;
    private String firstName;
    private String lastName;
    private String email;
}
