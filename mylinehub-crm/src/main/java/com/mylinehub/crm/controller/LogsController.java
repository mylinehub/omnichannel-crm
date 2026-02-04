package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.LOGS_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.LogsDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.LogService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = LOGS_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class LogsController {

	private final EmployeeRepository employeeRepository;
    private final LogService logService;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	
	@GetMapping("/getAllLogsByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<LogsDTO>> getAllLogsByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<LogsDTO> logs = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		logs= logService.getAllIvrsOnOrganization(organization);
    		return status(HttpStatus.OK).body(logs);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(logs);
    	} 	
	}
	
}