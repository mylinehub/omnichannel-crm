package com.mylinehub.crm.whatsapp.dto.general;

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
public class MediaDto {

	String id;
    String link;
    boolean animated;
    String caption;
    String fileName;
    String mime_type;
    String sha256;
}
