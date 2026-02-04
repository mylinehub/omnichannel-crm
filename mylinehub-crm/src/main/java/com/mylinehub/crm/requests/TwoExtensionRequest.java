package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwoExtensionRequest {
	
	  public String organization;
	  public String extensionMain;
	  public String extensionWith;
	  public Integer lastReadIndex;
}
