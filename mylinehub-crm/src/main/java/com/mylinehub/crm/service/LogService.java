package com.mylinehub.crm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.mylinehub.crm.entity.dto.LogsDTO;
import com.mylinehub.crm.mapper.LogMapper;
import com.mylinehub.crm.repository.LogRepository;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class LogService implements CurrentTimeInterface{

	/**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final LogRepository logRepository;
    private final LogMapper logMapper;
	
	
	
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<LogsDTO> getAllIvrsOnOrganization(String organization){
        return logRepository.findAllByOrganization(organization)
                .stream()
                .map(logMapper::mapLogToDTO)
                .collect(Collectors.toList());
    }
}