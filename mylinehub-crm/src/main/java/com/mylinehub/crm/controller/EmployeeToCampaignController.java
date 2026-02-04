package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.EMPLOYEE_TO_CAMPAIGN_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.EmployeeToCampaignDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.CampaignService;
import com.mylinehub.crm.service.EmployeeToCampaignService;

import lombok.AllArgsConstructor;



@RestController
@RequestMapping(produces="application/json", path = EMPLOYEE_TO_CAMPAIGN_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class EmployeeToCampaignController {

    private final EmployeeToCampaignService employeeToCampaignService;
    private final CampaignService campaignService;
    private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	
	
	@GetMapping("/findAllByEmployeeAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeToCampaignDTO>> findAllByEmployeeAndOrganization(@RequestParam String extension,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeToCampaignDTO> employeesToCampaign = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employeesToCampaign= employeeToCampaignService.findAllByEmployeeAndOrganization(extension,organization);
    		return status(HttpStatus.OK).body(employeesToCampaign);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(employeesToCampaign);
    	} 	
	}
	
	
	@GetMapping("/findAllByCampaignAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<EmployeeToCampaignDTO>> findAllByCampaignAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<EmployeeToCampaignDTO> employeesToCampaign = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		employeesToCampaign= employeeToCampaignService.findAllByCampaignAndOrganization(id,organization);
    		return status(HttpStatus.OK).body(employeesToCampaign);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(employeesToCampaign);
    	} 	
	}
	
	
	@PostMapping("/createEmployeeToCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> createEmployeeToCampaignByOrganization(@RequestBody List<EmployeeToCampaignDTO> employeeToCampaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		int toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = employeeToCampaignService.createEmployeeToCampaignByOrganization(employeeToCampaignDTO,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	
	@PostMapping("/updateEmployeeToCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> updateEmployeeToCampaignByOrganization(@RequestBody List<EmployeeToCampaignDTO> employeeToCampaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		int toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = employeeToCampaignService.updateEmployeeToCampaignByOrganization(employeeToCampaignDTO,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	
	
	@PostMapping("/deleteEmployeeToCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteEmployeeToCampaignByOrganization(@RequestBody List<EmployeeToCampaignDTO> employeeToCampaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		int toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = employeeToCampaignService.deleteEmployeeToCampaignByOrganization(employeeToCampaignDTO,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	
	@DeleteMapping("/deleteAllByCampaignAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteAllByCampaignAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	Campaign campaign = campaignService.findCampaignByIdAndOrganization(id, organization);
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = employeeToCampaignService.deleteAllByCampaignAndOrganization(campaign,organization,employee.getExtension(),employee.getDomain());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
}