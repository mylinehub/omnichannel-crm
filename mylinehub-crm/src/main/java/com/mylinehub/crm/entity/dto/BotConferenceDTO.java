package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BotConferenceDTO {
	
	private String organization;
    private String conferenceId;
    private String messagetype;
    private String message;
    private String format;
    private String domain;
    private String fromExtension;
    private String firstName;
    private String lastName;
    private String role;
    private String fileName;
    private String blobType;
    private String fileSizeInMB;

}
