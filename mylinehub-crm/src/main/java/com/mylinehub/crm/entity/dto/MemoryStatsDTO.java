package com.mylinehub.crm.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemoryStatsDTO {
	Long heapSize;
	Long heapMaxSize;
	Long heapFreeSize;
	int availableProcessors;
	String version;
}
