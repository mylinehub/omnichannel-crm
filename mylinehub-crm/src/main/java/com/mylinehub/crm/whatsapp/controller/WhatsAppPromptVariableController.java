package com.mylinehub.crm.whatsapp.controller;

import static com.mylinehub.crm.controller.ApiMapping.WHATS_APP_PROMPT_VARIABLE_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;
import java.util.Optional;
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
import com.mylinehub.crm.whatsapp.dto.WhatsAppPromptVariablesDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPrompt;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPromptRepository;
import com.mylinehub.crm.whatsapp.requests.WhatsAppMainControllerRequest;
import com.mylinehub.crm.whatsapp.service.WhatsAppPromptVariableService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = WHATS_APP_PROMPT_VARIABLE_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class WhatsAppPromptVariableController {
	private final WhatsAppPromptVariableService whatsAppPromptVariableService; 
	private final WhatsAppPromptRepository whatsAppPromptRepository;
	private final JwtConfiguration jwtConfiguration;
	private final EmployeeRepository employeeRepository;
	private final SecretKey secretKey;
	    
	@PostMapping("/create")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> create(@RequestBody WhatsAppPromptVariablesDto whatsAppPromptVariablesDto,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		
		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppPromptVariablesDto.getOrganization().trim()))
    	{
    		toReturn= whatsAppPromptVariableService.create(whatsAppPromptVariablesDto);
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
	public ResponseEntity<Boolean> update(@RequestBody WhatsAppPromptVariablesDto whatsAppPromptVariablesDto,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		
		token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(whatsAppPromptVariablesDto.getOrganization().trim()))
    	{
    		toReturn= whatsAppPromptVariableService.update(whatsAppPromptVariablesDto);
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
    		toReturn= whatsAppPromptVariableService.delete(id);
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
	public ResponseEntity<List<WhatsAppPromptVariablesDto>> getAllByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<WhatsAppPromptVariablesDto> toReturn = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn= whatsAppPromptVariableService.findAllByOrganization(organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
    
    @PostMapping("/getAllByOrganizationAndWhatsAppPrompt")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppPromptVariablesDto>> getAllByOrganizationAndWhatsAppPrompt(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppPromptVariablesDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		Optional<WhatsAppPrompt> whatsAppPrompt = whatsAppPromptRepository.findById(whatsAppControllerRequest.getWhatsappProjectId());
       		
       		if(!whatsAppPrompt.isEmpty())
       		toReturn= whatsAppPromptVariableService.getAllByOrganizationAndWhatsAppPrompt(whatsAppControllerRequest.getOrganization(),whatsAppPrompt.get());
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
    
    
    @PostMapping("/getAllByOrganizationAndWhatsAppPromptAndActive")
   	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
   	public ResponseEntity<List<WhatsAppPromptVariablesDto>> getAllByOrganizationAndWhatsAppPromptAndActive(@RequestBody WhatsAppMainControllerRequest whatsAppControllerRequest,@RequestHeader (name="Authorization") String token){
   	        
   		List<WhatsAppPromptVariablesDto> toReturn = null;
   		
       	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
       	
       	//System.out.println(token);
       	
       	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
       	
       	if(employee.getOrganization().trim().equals(whatsAppControllerRequest.getOrganization().trim()))
       	{
       		Optional<WhatsAppPrompt> whatsAppPrompt = whatsAppPromptRepository.findById(whatsAppControllerRequest.getWhatsappProjectId());
       		
       		if(!whatsAppPrompt.isEmpty())
       		toReturn= whatsAppPromptVariableService.getAllByOrganizationAndWhatsAppPromptAndActive(whatsAppControllerRequest.getOrganization(),whatsAppPrompt.get(),whatsAppControllerRequest.isActive());
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
	
}
