package com.mylinehub.crm.requests;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MultipleIDRequest {
	
	public List<Long> ids;
	public String extension;
	public String organization;

}
