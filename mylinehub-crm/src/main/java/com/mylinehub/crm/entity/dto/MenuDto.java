package com.mylinehub.crm.entity.dto;


import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuDto {
	
		//Key is sub module and Value is list of children
		public Map<String,List<String>> whatsAppModule;
		public Map<String,List<String>> callingModule;
		public Map<String,List<String>> campaignModule;
		public Map<String,List<String>> organizationModule;
		public Map<String,List<String>> issueTrackingModule;
		public Map<String,List<String>> settingModule;
		public Map<String,List<String>> propertyManagementModule;
		private Map<String, List<String>> franchiseManagementModule;
}
