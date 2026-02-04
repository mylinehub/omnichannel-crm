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
public class CallDashboardRequest {
	
	public String dateRange;
	public List<String> extensions;
	public String organization;

}
