package com.mylinehub.crm.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallDashboardCallDetailsDTO {
	double totalAmount;
	double totalSpend;
	int totalCalls;
	int incomingCalls;
	int outgoingCalls;
	int converted;
	int callsConnected;
	String month;
	String year;
}
