package com.mylinehub.crm.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConferenceDTO {
	
		private Long id;
	    private String confextension;
	    private String confname;
	    private String domain;
	    private String organization;
	    private String phonecontext;
	    private String owner;
	    private String bridge;
	    private String userprofile;
	    private String menu;
	    private boolean isdynamic;
	    private boolean isroomactive;
	    private boolean isconferenceactive;
	    private String protocol;
	    
}

