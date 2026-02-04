package com.mylinehub.crm.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mylinehub.crm.entity.StatementReconciliation;
import com.mylinehub.crm.entity.dto.StatementReconciliationDTO;
import com.mylinehub.crm.mapper.StatementReconciliationMapper;
import com.mylinehub.crm.repository.StatementReconciliationRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class StatementReconciliationService implements CurrentTimeInterface{
	

	 /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final StatementReconciliationRepository statementReconciliationRepository;
    private final StatementReconciliationMapper statementReconciliationMapper;
	
    
    /**
     * The method is to retrieve all StatementReconciliation from the database and display them.
     *
     * After downloading all the data about the StatementReconciliation,
     * the data is mapped to dto which will display only those needed
     * @return list of all StatementReconciliation with specification of data in StatementReconciliationDTO
     */
    
    public List<StatementReconciliationDTO> getAllStatementReconciliationByOrganization(String organization){
        return statementReconciliationRepository.findAllByOrganization(organization)
                .stream()
                .map(statementReconciliationMapper::mapStatementReconciliationToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve one StatementReconciliation as per batch from the database and display complete info about it.
     *
     * After downloading all the data about the StatementReconciliation,
     * the data is mapped to dto which will display only those needed
     * @return Find one StatementReconciliation with specification of data in StatementReconciliationDTO
     */
    
    public StatementReconciliation getStatementReconciliationByBatchIdAndOrganization(Long batchId,String organization){
        return statementReconciliationRepository.findByIdAndOrganization(batchId,organization);
    }
    
    
    /**
     * The method is to save one StatementReconciliation.
     * @return save one StatementReconciliation
     */
    
    public StatementReconciliation saveStatementReconciliationByOrganization(StatementReconciliationDTO statementReconciliationDTO){
    	StatementReconciliation statementReconciliation = statementReconciliationMapper.mapDTOToStatementReconciliation(statementReconciliationDTO);
    	statementReconciliation = statementReconciliationRepository.save(statementReconciliation);
        return statementReconciliation;
    }
    
    /**
     * The method is to save one StatementReconciliation.
     * @return save one StatementReconciliation
     */
    
    public Long updateStatementReconciliationByOrganization(StatementReconciliationDTO statementReconciliationDTO){
    	StatementReconciliation statementReconciliation = statementReconciliationRepository.getOne(statementReconciliationDTO.getId());
    	if(statementReconciliation!=null)
    	{
        	statementReconciliation = statementReconciliationRepository.save(statementReconciliation);
        	statementReconciliation.setBankName(statementReconciliationDTO.getBankName());
        	statementReconciliation.setByExtension(statementReconciliationDTO.getByExtension());
        	statementReconciliation.setClient(statementReconciliationDTO.getClient());
        	statementReconciliation.setCompareAmountColumn(statementReconciliationDTO.getCompareAmountColumn());
        	statementReconciliation.setCompareCashFlowDirection(statementReconciliationDTO.getCompareCashFlowDirection());
        	statementReconciliation.setComparedFileCount(statementReconciliationDTO.getComparedFileCount());
        	statementReconciliation.setCompareSheetName(statementReconciliationDTO.getCompareSheetName());
        	statementReconciliation.setCompareUtrColumn(statementReconciliationDTO.getCompareUtrColumn());
        	statementReconciliation.setIsExtraCount(statementReconciliationDTO.getIsExtraCount());
        	statementReconciliation.setIsFoundCount(statementReconciliationDTO.getIsFoundCount());
        	statementReconciliation.setIsMismatchCount(statementReconciliationDTO.getIsMismatchCount());
        	statementReconciliation.setLastUpdatedOn(Instant.now());
        	statementReconciliation.setName(statementReconciliationDTO.getName());
        	statementReconciliation.setNoObservationFound(statementReconciliationDTO.getNoObservationFound());
        	statementReconciliation.setNumberOfParallelThreads(statementReconciliationDTO.getNumberOfParallelThreads());
        	statementReconciliation.setObservations(statementReconciliationDTO.getObservations());
        	statementReconciliation.setOrganization(statementReconciliationDTO.getOrganization());
        	statementReconciliation.setRawAmountColumn(statementReconciliationDTO.getRawAmountColumn());
        	statementReconciliation.setRawCashFlowDirection(statementReconciliationDTO.getRawCashFlowDirection());
        	statementReconciliation.setRawFileCount(statementReconciliationDTO.getRawFileCount());
        	statementReconciliation.setRawSheetName(statementReconciliationDTO.getRawSheetName());
        	statementReconciliation.setReportHeading(statementReconciliationDTO.getReportHeading());
        	statementReconciliation.setZipPath(statementReconciliationDTO.getZipPath());
        	statementReconciliation.setComplete(statementReconciliationDTO.isComplete());
            return statementReconciliation.getId();
    	}
    	else
    	{
    		return null;
    	}
    }
    
    
    /**
     * The method is to delete one StatementReconciliation.
     * @return Delete one StatementReconciliation
     */
    
    public boolean deleteStatementReconciliationByBatchIdAndOrganization(Long batchId,String organization){
    	
    	StatementReconciliation statementReconciliation = statementReconciliationRepository.findByIdAndOrganization(batchId, organization);
    	if(statementReconciliation!=null)
    	{
    		statementReconciliationRepository.deleteById(batchId);
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    	
    }

    
    public void uploadRawStatementDataZipExcels(MultipartFile file,String email,String organization) throws Exception {
        try {
        	
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
    }
    
    public void compareInputZipExcelsDataToRawStatementCRMData(MultipartFile file,String email,String organization) throws Exception {
        try {
        	
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
    }
}