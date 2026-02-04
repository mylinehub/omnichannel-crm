package com.mylinehub.crm.whatsapp.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.whatsapp.dto.general.contact.UrlDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaUploadDto {

	String mediaId;
    String type;
    String error;
    String link;
    boolean externalPartyUploadSuccessful;
    boolean allowedUpload;
}
