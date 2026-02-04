package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDescriptionRequest {
	
	public String description;
	public boolean converted;
	public String phoneNumber;
	public Long id;
	public String organization;

}
