package com.mylinehub.crm.entity.dto;

import java.util.Date;

import com.mylinehub.crm.entity.FileCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaDto {
	
	private Long id;
    private String name;
    private String error;
    private String caption;
    private FileCategory fileCategory;
    private String extension;
  //Only used in case of whats app. Or else extension and fromExtension are same. It is because in case of whats app extension is filled with organization.
    //This is beacause any employee can upload document and it should be visible to everyone.
    private String fromExtension;
    private Long whatsAppPhoneNumberId;
    private String type;
    private long size;
    private String whatsAppMediaId;
    private String whatsAppMediaType;
    private Date whatsAppUploadDate;
    private String sha256;
    private boolean externalPartyUploadSuccessful;
    private boolean received;
    private String organization;
    private String mediaUploadModule;
	private byte[] byteData;

}
