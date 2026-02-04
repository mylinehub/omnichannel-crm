package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueDTO {

		private Long id;
	    public String phoneContext;
	    public String organization;
	    public String extension;
	    public String protocol;
	    public String domain;
	    public String name;
	    public String type;
	    public boolean isactive;
}
