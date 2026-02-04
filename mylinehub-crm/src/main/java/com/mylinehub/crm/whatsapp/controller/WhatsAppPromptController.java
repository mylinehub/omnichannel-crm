package com.mylinehub.crm.whatsapp.controller;

import static com.mylinehub.crm.controller.ApiMapping.WHATS_APP_PROMPT_REST_URL;
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
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPromptDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.requests.WhatsAppMainControllerRequest;
import com.mylinehub.crm.whatsapp.service.WhatsAppPhoneNumberService;
import com.mylinehub.crm.whatsapp.service.WhatsAppPromptService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = WHATS_APP_PROMPT_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class WhatsAppPromptController {
	private final WhatsAppPromptService whatsAppPromptService; 
	private final WhatsAppPhoneNumberService whatsAppPhoneNumberService;
	private final JwtConfiguration jwtConfiguration;
	private final EmployeeRepository employeeRepository;
	private final SecretKey secretKey;
	 
	@PostMapping("/create")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> create(@RequestBody WhatsAppPromptDto whatsAppPromptDto,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		
		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppPromptDto.getOrganization().trim()))
    	{
    		toReturn= whatsAppPromptService.create(whatsAppPromptDto);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 
	}
	
	@PostMapping("/update")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> update(@RequestBody WhatsAppPromptDto whatsAppPromptDto,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		
		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppPromptDto.getOrganization().trim()))
    	{
    		toReturn= whatsAppPromptService.update(whatsAppPromptDto);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 
	}
	
	
	@DeleteMapping("/delete")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> delete(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		
		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization))
    	{
    		toReturn= whatsAppPromptService.delete(id);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 
	}
	
    @GetMapping("/getAllByOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppPromptDto>> getAllByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<WhatsAppPromptDto> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn= whatsAppPromptService.findAllByOrganization(organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
    
    @PostMapping("/getAllByOrganizationAndWhatsAppPhoneNumber")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppPromptDto>> getAllByOrganizationAndWhatsAppPhoneNumber(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppPromptDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberService.findByPhoneNumber(whatsAppControllerRequest.getPhoneNumber());
       		
       		if(whatsAppPhoneNumber != null)
       		toReturn= whatsAppPromptService.getAllByOrganizationAndWhatsAppPhoneNumber(whatsAppControllerRequest.getOrganization(),whatsAppPhoneNumber);
       		else 
       		return status(HttpStatus.NO_CONTENT).body(toReturn);
       		
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
    
    @PostMapping("/getAllByOrganizationAndWhatsAppPhoneNumberAndActive")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppPromptDto>> getAllByOrganizationAndWhatsAppPhoneNumberAndActive(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppPromptDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberService.findByPhoneNumber(whatsAppControllerRequest.getPhoneNumber());
       		
       		if(whatsAppPhoneNumber != null)
       		toReturn= whatsAppPromptService.getAllByOrganizationAndWhatsAppPhoneNumberAndActive(whatsAppControllerRequest.getOrganization(),whatsAppPhoneNumber,whatsAppControllerRequest.isActive());
       		else 
       		return status(HttpStatus.NO_CONTENT).body(toReturn);
       		
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
    
    
    @PostMapping("/getAllByOrganizationAndCategory")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppPromptDto>> getAllByOrganizationAndCategory(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppPromptDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		toReturn= whatsAppPromptService.getAllByOrganizationAndCategory(whatsAppControllerRequest.getOrganization(),whatsAppControllerRequest.getCategory());
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
    
    
    @PostMapping("/getAllByOrganizationAndCategoryAndActive")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppPromptDto>> getAllByOrganizationAndCategoryAndActive(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppPromptDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		toReturn= whatsAppPromptService.getAllByOrganizationAndCategoryAndActive(whatsAppControllerRequest.getOrganization(),whatsAppControllerRequest.getCategory(),whatsAppControllerRequest.isActive());
       		return status(HttpStatus.OK).body(toReturn);
       	}
       	else
       	{
       		//System.out.println("I am in else controller");
       		
       		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
       	} 	
   	}
}
