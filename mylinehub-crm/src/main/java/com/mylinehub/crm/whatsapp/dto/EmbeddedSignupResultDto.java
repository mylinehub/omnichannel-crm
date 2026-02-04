package com.mylinehub.crm.whatsapp.dto;

import lombok.Data;

@Data
public class EmbeddedSignupResultDto {
    private String organization;

    // Meta code (required)
    private String code;

    // Meta embedded signup sessionInfo (required)
    private String waba_id;
    private String phone_number_id;
    private String business_id;

    // optional, only if you really use it
    private String state;
}

