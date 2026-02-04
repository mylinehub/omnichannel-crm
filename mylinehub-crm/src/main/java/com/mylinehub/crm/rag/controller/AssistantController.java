package com.mylinehub.crm.rag.controller;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.rag.dto.ThreadResponse;
import com.mylinehub.crm.rag.model.*;
import com.mylinehub.crm.rag.service.AssistantService;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;

import lombok.AllArgsConstructor;

import static com.mylinehub.crm.controller.ApiMapping.OPEN_API_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(produces="application/json", path = OPEN_API_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class AssistantController {
	
	    private final AssistantService service;
	    private final EmployeeRepository employeeRepository;
	    private final JwtConfiguration jwtConfiguration;
	    private final SecretKey secretKey;

	    @PostMapping("/assistant")
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public ResponseEntity<AssistantEntity> createAssistant(
	            @RequestParam String name,
	            @RequestParam String organization,
	            @RequestParam String systemPromptId,
	            @RequestParam String instructions,
	            @RequestParam(defaultValue = "gpt-4o-mini") String model,@RequestHeader (name="Authorization") String token) throws Exception {
	       
	    	AssistantEntity toReturn = null;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
		    	toReturn = service.createAssistant(name, organization, systemPromptId, instructions, model);
	    	}
	    	else {
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	}

	    	return status(HttpStatus.OK).body(toReturn);
	    }

	    @PutMapping("/assistant")
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public ResponseEntity<AssistantEntity> updateAssistant(
	            @RequestParam String name,
	            @RequestParam String organization,
	            @RequestParam String systemPromptId,
	            @RequestParam String newInstructions,
	            @RequestParam(defaultValue = "gpt-4o-mini") String newModel,@RequestHeader (name="Authorization") String token) throws Exception {
	       
	    	AssistantEntity toReturn = null;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		toReturn=service.updateAssistant(null,name, organization,systemPromptId, newInstructions, newModel);
	    	}
	    	else {
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	}
	    	
	    	return status(HttpStatus.OK).body(toReturn); 
	    }

	    @DeleteMapping("/assistant")
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public ResponseEntity<String> deleteAssistant(
	            @RequestParam String name,
	            @RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception {
	        
	    	String toReturn = null;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		toReturn=service.deleteAssistant(name, organization);
	    	}
	    	else {
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	}
	    	
	    	return status(HttpStatus.OK).body(toReturn);
	    }

	    @PostMapping("/create/thread")
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public ResponseEntity<ThreadResponse> createThread(@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception {
	        
	    	ThreadResponse toReturn = null;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		toReturn=service.createThread();
	    	}
	    	else {
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	}
	    	
	    	return status(HttpStatus.OK).body(toReturn);
	    }

//	    @PostMapping("/run/stream")
//	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	    public ResponseEntity<String> addMessageAndStream(
//	            @RequestParam String assistantId,
//	            @RequestParam String threadId,
//	            @RequestParam String userMessage,
//	            @RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception {
//	        
//	    	String toReturn = null;
//	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//	    	
//	    	//System.out.println(token);
//	    	
//	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//	    	
//	    	if(employee.getOrganization().trim().equals(organization.trim()))
//	    	{
//	    		toReturn = service.addMessageAndStream(organization,assistantId, threadId, userMessage);
//	    	}
//	    	else {
//	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
//	    	}
//	    	
//	    	return status(HttpStatus.OK).body(toReturn);
//	    }
}
