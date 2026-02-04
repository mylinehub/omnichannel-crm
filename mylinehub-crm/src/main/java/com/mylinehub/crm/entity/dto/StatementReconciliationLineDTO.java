package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatementReconciliationLineDTO {

	String utr;
	String cashFlowDirection;
	String realAmount;
	String comparedAmount;
	boolean isFound;
	boolean isExtra;
	String color;
	String comments;
	
}
