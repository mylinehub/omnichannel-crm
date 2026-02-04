package com.mylinehub.crm.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageInfoDTO {

	int totalPages;
	int currentPage;
	int recordOfPage;
}
