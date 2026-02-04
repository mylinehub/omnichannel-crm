package com.mylinehub.crm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.mylinehub.crm.entity.dto.ErrorDTO;
import com.mylinehub.crm.mapper.ErrorMapper;
import com.mylinehub.crm.repository.ErrorRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class ErrorService implements CurrentTimeInterface{

	/**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final ErrorMapper errorMapper;
    private final ErrorRepository errorRepository;
    
	 
	
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<ErrorDTO> getAllErrorOnOrganization(String organization){
        return errorRepository.findAllByOrganization(organization)
                .stream()
                .map(errorMapper::mapErrorToDTO)
                .collect(Collectors.toList());
    }
}