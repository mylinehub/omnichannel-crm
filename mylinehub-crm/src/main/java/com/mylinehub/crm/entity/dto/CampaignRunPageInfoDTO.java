package com.mylinehub.crm.entity.dto;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignRunPageInfoDTO {

    double totalRecords;
    double numberOfPages;
    int runNumber;
    private String campaignName;
    private String campaignRunDate;
    private Long totalCost;
    private Long totalDialed;
    Map<String, Integer> stateCounts; 
    // FIX: this must be the row DTO, not CampaignRunPageInfoDTO
    List<CampaignRunCallLogRowDTO> data;
}