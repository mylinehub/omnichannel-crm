package com.mylinehub.crm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StringValueRequest {
	
	public Long id;
	public String email;
	public String organization;
	public String value;

}
