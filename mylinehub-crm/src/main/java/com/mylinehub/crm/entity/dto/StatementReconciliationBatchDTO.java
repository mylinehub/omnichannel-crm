package com.mylinehub.crm.entity.dto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatementReconciliationBatchDTO {

	//UTR ID And Line Details
	public Map<String,StatementReconciliationLineDTO> allUtrs;
	public StatementReconciliationDTO statementReconciliationDTO;
}
