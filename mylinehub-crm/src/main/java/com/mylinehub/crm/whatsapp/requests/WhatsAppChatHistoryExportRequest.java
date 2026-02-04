package com.mylinehub.crm.whatsapp.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhatsAppChatHistoryExportRequest {
    private String organization;

    // Optional: if you want export per main phone number. If null/blank -> export all org.
    private String phoneMain;

    // Date strings in "yyyy-MM-dd" (IST). Example: "2025-12-01"
    private String startDate;
    private String endDate;
}
