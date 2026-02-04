package com.mylinehub.crm.entity.dto;

import java.time.Instant;
import java.util.List;

import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.dto.MemoryStatsDTO;
import com.mylinehub.crm.entity.dto.StatementReconciliationLineDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatementReconciliationDTO {
    private Long id;
    
    private  String name;
    
    private  String organization;
    private  String byExtension;
    
    int rawFileCount;
    int comparedFileCount;
    int isFoundCount;
    int isExtraCount;
    int isMismatchCount;
    
    int rawUtrColumn;
    int rawAmountColumn;
    int rawCashFlowDirection;
    int compareUtrColumn;
    int compareAmountColumn;
    int compareCashFlowDirection; 
    private  String client;
    private String reportHeading;
    private String bankName;
    private List<StatementReconciliationLineDTO> observations;
    int noObservationFound;
    int numberOfParallelThreads;
    private  String rawSheetName;
    private  String compareSheetName;
    private String zipPath;
    private boolean complete;
    private Instant createdOn;
    private Instant lastUpdatedOn;
    
    
}


