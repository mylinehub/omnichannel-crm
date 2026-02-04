package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.CUSTOMER_TO_CAMPAIGN_REST_URL;

import static org.springframework.http.ResponseEntity.status;

import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.mylinehub.crm.entity.dto.CustomerToCampaignCreateDTO;
import com.mylinehub.crm.entity.dto.CustomerToCampaignDTO;
import com.mylinehub.crm.entity.dto.CustomerToCampaignPageDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.CampaignService;
import com.mylinehub.crm.service.CustomerToCampaignService;


import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = CUSTOMER_TO_CAMPAIGN_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class CustomerToCampaignController {

    private final CustomerToCampaignService customerToCampaignService;
    private final CampaignService campaignService;
    private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	
	
	@GetMapping("/findAllByCustomerAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<CustomerToCampaignDTO>> findAllByCustomerAndOrganization(@RequestParam String phoneNumber,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<CustomerToCampaignDTO> customerToCampaign = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	phoneNumber = phoneNumber.trim();
    	if(phoneNumber.contains("+"))
    	{
    		
    	}
    	else
    	{
    		phoneNumber = "+"+phoneNumber;
    	}
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customerToCampaign= customerToCampaignService.findAllByCustomerAndOrganization(phoneNumber,organization);
    		return status(HttpStatus.OK).body(customerToCampaign);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customerToCampaign);
    	} 	
	}
	
	
	@GetMapping("/findAllByCampaignAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<CustomerToCampaignPageDTO> findAllByCampaignAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
	        
		Pageable pageable;
		if(pageNumber <0)
		{
			pageable= PageRequest.of(0, 1000000000);
		}
		else
		{
			pageable = PageRequest.of(pageNumber, size);
		}
		
		CustomerToCampaignPageDTO customerToCampaign = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		customerToCampaign= customerToCampaignService.findAllByCampaignAndOrganization(id,organization,searchText,pageable);
    		return status(HttpStatus.OK).body(customerToCampaign);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(customerToCampaign);
    	} 	
	}
	
	
	@PostMapping("/createCustomerToCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createCustomerToCampaignByOrganization(@RequestBody CustomerToCampaignCreateDTO customerToCampaignCreateDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = customerToCampaignService.createCustomerToCampaignByOrganization(customerToCampaignCreateDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	@PostMapping("/getCountForCustomerToCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Long> getCountForCustomerToCampaignByOrganization(@RequestBody CustomerToCampaignCreateDTO customerToCampaignCreateDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Long toReturn = 0L;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
//    		System.out.println(customerToCampaignCreateDTO.toString());
    		toReturn = customerToCampaignService.getCountForCustomerToCampaignByOrganization(customerToCampaignCreateDTO);
//    		System.out.println("After extracting row count");
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	@PostMapping("/updateCustomerToCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> updateCustomerToCampaignByOrganization(@RequestBody List<CustomerToCampaignDTO> customerToCampaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		int toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = customerToCampaignService.updateCustomerToCampaignByOrganization(customerToCampaignDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@PostMapping("/deleteCustomerToCampaignByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteCustomerToCampaignByOrganization(@RequestBody List<CustomerToCampaignDTO> customerToCampaignDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		int toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = customerToCampaignService.deleteCustomerToCampaignByOrganization(customerToCampaignDTO);
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
    	
//    	System.out.println("deleteAllByCampaignAndOrganization");
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	Campaign campaign = campaignService.findCampaignByIdAndOrganization(id, organization);
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
//    		System.out.println("deletingAllRecordsNow");
    		toReturn = customerToCampaignService.deleteAllByCampaignAndOrganization(campaign,organization);
//    		System.out.println("returning value");
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
}