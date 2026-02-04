package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mylinehub.crm.entity.StatementReconciliation;
import com.mylinehub.crm.entity.dto.StatementReconciliationDTO;

@Mapper(componentModel = "spring")
public interface StatementReconciliationMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "byExtension", source = "byExtension")
	@Mapping(target = "bankName", source = "bankName")
	@Mapping(target = "rawFileCount", source = "rawFileCount")
	@Mapping(target = "comparedFileCount", source = "comparedFileCount")
	@Mapping(target = "isFoundCount", source = "isFoundCount")
	@Mapping(target = "isExtraCount", source = "isExtraCount")
	@Mapping(target = "isMismatchCount", source = "isMismatchCount")
	@Mapping(target = "rawUtrColumn", source = "rawUtrColumn")
	@Mapping(target = "rawAmountColumn", source = "rawAmountColumn")
	@Mapping(target = "rawCashFlowDirection", source = "rawCashFlowDirection")
	@Mapping(target = "compareUtrColumn", source = "compareUtrColumn")
	@Mapping(target = "compareAmountColumn", source = "compareAmountColumn")
	@Mapping(target = "compareCashFlowDirection", source = "compareCashFlowDirection")
	@Mapping(target = "client", source = "client")
	@Mapping(target = "reportHeading", source = "reportHeading")
	@Mapping(target = "observations", source = "observations")
	@Mapping(target = "createdOn", source = "createdOn")
	@Mapping(target = "lastUpdatedOn", source = "lastUpdatedOn")
	@Mapping(target = "noObservationFound", source = "noObservationFound")
	@Mapping(target = "numberOfParallelThreads", source = "numberOfParallelThreads")
	@Mapping(target = "rawSheetName", source = "rawSheetName")
	@Mapping(target = "compareSheetName", source = "compareSheetName")
	@Mapping(target = "zipPath", source = "zipPath")
	@Mapping(target = "complete", source = "complete")
	StatementReconciliationDTO mapStatementReconciliationToDTO(StatementReconciliation statementReconciliation);
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "byExtension", source = "byExtension")
	@Mapping(target = "rawFileCount", source = "rawFileCount")
	@Mapping(target = "comparedFileCount", source = "comparedFileCount")
	@Mapping(target = "isFoundCount", source = "isFoundCount")
	@Mapping(target = "isExtraCount", source = "isExtraCount")
	@Mapping(target = "isMismatchCount", source = "isMismatchCount")
	@Mapping(target = "rawUtrColumn", source = "rawUtrColumn")
	@Mapping(target = "rawAmountColumn", source = "rawAmountColumn")
	@Mapping(target = "rawCashFlowDirection", source = "rawCashFlowDirection")
	@Mapping(target = "compareUtrColumn", source = "compareUtrColumn")
	@Mapping(target = "compareAmountColumn", source = "compareAmountColumn")
	@Mapping(target = "compareCashFlowDirection", source = "compareCashFlowDirection")
	@Mapping(target = "client", source = "client")
	@Mapping(target = "reportHeading", source = "reportHeading")
	@Mapping(target = "observations", source = "observations")
	@Mapping(target = "noObservationFound", source = "noObservationFound")
	@Mapping(target = "numberOfParallelThreads", source = "numberOfParallelThreads")
	@Mapping(target = "rawSheetName", source = "rawSheetName")
	@Mapping(target = "compareSheetName", source = "compareSheetName")
	@Mapping(target = "zipPath", source = "zipPath")
	@Mapping(target = "complete", source = "complete")
	StatementReconciliation mapDTOToStatementReconciliation(StatementReconciliationDTO statementReconciliationDTO);
	
}