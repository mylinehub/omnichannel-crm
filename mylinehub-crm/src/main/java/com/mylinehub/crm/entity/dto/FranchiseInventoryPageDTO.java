package com.mylinehub.crm.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FranchiseInventoryPageDTO {

    private List<CustomerFranchiseInventoryDTO> data;

    // Only filled for backend page=0 (like your PropertyInventory module)
    private Long totalRecords;
    private Integer numberOfPages;
}
