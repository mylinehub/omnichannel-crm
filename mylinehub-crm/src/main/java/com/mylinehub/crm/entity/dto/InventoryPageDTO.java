package com.mylinehub.crm.entity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPageDTO {
    private Long totalRecords;
    private Long withAreaRecords;
    private Integer numberOfPages;
    private List<CustomerPropertyInventoryDTO> data;
}
