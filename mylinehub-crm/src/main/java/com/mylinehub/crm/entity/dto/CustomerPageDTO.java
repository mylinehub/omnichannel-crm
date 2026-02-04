package com.mylinehub.crm.entity.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerPageDTO {
	
	double converted;
	double diverted;
	double totalRecords;
	double numberOfPages;
	List<CustomerDTO> data;

}
