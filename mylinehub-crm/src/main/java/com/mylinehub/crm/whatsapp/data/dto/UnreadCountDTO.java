package com.mylinehub.crm.whatsapp.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ButtonDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnreadCountDTO {
    private String phoneNumberWith;
    private int unreadCount;

    public UnreadCountDTO(String phoneNumberWith, long unreadCount) {
        this.phoneNumberWith = phoneNumberWith;
        this.unreadCount = (int) unreadCount;
    }
}
