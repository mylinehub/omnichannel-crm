package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.SIP_PROVIDER_REST_URL;
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

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.SipProviderDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.requests.IdRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.SipProviderService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = SIP_PROVIDER_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class SipProviderController {

    private final EmployeeRepository employeeRepository;
    private final SipProviderService sipProviderService;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	
	@PostMapping("/enableSipProviderOnIdAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> enableSipProviderOnIdAndOrganization(@RequestBody IdRequest request,@RequestHeader (name="Authorization") String token){
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		sipProviderService.enableSipProviderOnOrganization(request.getId(), request.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	
	@PostMapping("/disableSipProviderOnIdAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> disableSipProviderOnIdAndOrganization(@RequestBody IdRequest request,@RequestHeader (name="Authorization") String token){
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		sipProviderService.disableSipProviderOnOrganization(request.getId(), request.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	@GetMapping("/getAllSipProvidersByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<SipProviderDTO>> getAllSipProvidersByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<SipProviderDTO> SipProviders = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		SipProviders= sipProviderService.getAllSipProvidersOnOrganization(organization);
    		return status(HttpStatus.OK).body(SipProviders);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(SipProviders);
    	} 	
	}
	
	@GetMapping("/getSipProviderByPhoneNumberAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<SipProviderDTO> getSipProviderByPhoneNumberAndOrganization(@RequestParam String phoneNumber,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	     
		phoneNumber = phoneNumber.trim();
		CharSequence s = "+";
		if(!phoneNumber.contains(s))
		{
			//System.out.println("Inside '+' logic");
			phoneNumber = "+"+phoneNumber.trim();
		}
		
		SipProviderDTO sipProvider = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		sipProvider= sipProviderService.getSipProviderByPhoneNumberAndOrganization(phoneNumber, organization);
    		return status(HttpStatus.OK).body(sipProvider);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(sipProvider);
    	} 	
	}
	
	@PostMapping("/createSipProviderByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createSipProviderByOrganization(@RequestBody SipProviderDTO sipProviderDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()) && (employee.getOrganization().trim().equals(sipProviderDTO.getOrganization().trim())))
    	{
    		toReturn = sipProviderService.createsipProviderByOrganization(sipProviderDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updateSipProviderByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSipProviderByOrganization(@RequestBody SipProviderDTO sipProviderDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an Sip Provider");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = sipProviderService.updateSipProviderByOrganization(sipProviderDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deleteSipProviderByPhoneNumberAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteSipProviderByPhoneNumberAndOrganization(@RequestParam String phoneNumber,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		Boolean toReturn = false;
		
		phoneNumber = phoneNumber.trim();
		CharSequence s = "+";
		if(!phoneNumber.contains(s))
		{
			//System.out.println("Inside '+' logic");
			phoneNumber = "+"+phoneNumber.trim();
		}
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn  = sipProviderService.deletesipProviderByPhoneNumberAndOrganization(phoneNumber, organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
}