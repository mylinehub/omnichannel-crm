package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.CHAT_HISTORY_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.entity.ChatHistory;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.AllChatEmployeeDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.requests.ChatHistoryRequest;
import com.mylinehub.crm.requests.ExtensionRequest;
import com.mylinehub.crm.requests.TwoExtensionRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.ChatHistoryService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = CHAT_HISTORY_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class ChatHistoryController {
	
	private final ChatHistoryService chatHistoryService;
	private final EmployeeRepository employeeRepository;
	private final JwtConfiguration jwtConfiguration;
	private final SecretKey secretKey;
	
	
	@PostMapping("/getAllChatHistoryCandidatesByExtensionAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<AllChatEmployeeDTO>> getAllChatHistoryCandidatesByExtensionAndOrganization(@RequestBody ExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("getAllChatHistoryCandidatesByExtensionAndOrganization");
		List<AllChatEmployeeDTO> toReturn = null;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
//    	System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
//    	System.out.println("employee.getOrganization() : "+employee.getOrganization());
//    	System.out.println("request.getOrganization() : "+request.getOrganization());
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = chatHistoryService.getAllChatHistoryByExtensionMainAndOrganization(request.getExtension(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
//    		System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@PostMapping("/getAllChatHistoryByTwoExtensionsAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<ChatHistory> getAllChatHistoryByTwoExtensionsAndOrganization(@RequestBody TwoExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("getAllChatHistoryByTwoExtensionsAndOrganization");
		ChatHistory toReturn = null;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		try
    		{
//    			System.out.println("Calling getAllChatHistoryByExtensionMainAndExtensionWithAndOrganization service");
    			toReturn = chatHistoryService.getAllChatHistoryByExtensionMainAndExtensionWithAndOrganization(request.getExtensionMain(),request.getExtensionWith(), request.getOrganization());
        		
    		}
    		catch(Exception e)
    		{
    			toReturn = null;
    			return status(HttpStatus.INTERNAL_SERVER_ERROR).body(toReturn);
    		}
    		
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@PostMapping("/deleteAllChatHistoryByExtensionAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteAllChatHistoryByExtensionAndOrganization(@RequestBody ExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("deleteAllChatHistoryByExtensionAndOrganization");
		
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = chatHistoryService.deleteAllChatHistoryByExtensionMainAndOrganization(request.getExtension(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@PostMapping("/deleteAllChatHistoryByTwoExtensionsAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteAllChatHistoryByTwoExtensionsAndOrganization(@RequestBody TwoExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("deleteAllChatHistoryByTwoExtensionsAndOrganization");
		
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = chatHistoryService.deleteChatHistoryByExtensionMainAndExtensionWithAndOrganization(request.getExtensionMain(),request.getExtensionWith(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	@PostMapping("/updateLastReadIndexByTwoExtensionsAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> updateLastReadIndexByTwoExtensionsAndOrganization(@RequestBody TwoExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("updateLastReadIndexByTwoExtensionsAndOrganization");
		
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = chatHistoryService.updateLastReadIndexByExtensionMainAndExtensionWithAndOrganizationAndIsDeleted(request.getExtensionMain(),request.getExtensionWith(), request.getOrganization(),request.getLastReadIndex());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@PostMapping("/appendChatHistoryByTwoExtensionsAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> appendChatHistoryByTwoExtensionsAndOrganization(@RequestBody ChatHistoryRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("appendChatHistoryByTwoExtensionsAndOrganization");
		
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		//Adding chat for initiator to receiver. Both maintains separate database records.
    		toReturn = chatHistoryService.softAppendChatHistoryByExtensionMainAndExtensionWithAndOrganization(request.getExtensionMain(),request.getExtensionWith(), request.getOrganization(),request.getChat());
    		
    		//Reversing and adding chat for receiver to initiator. Both maintains separate database records.
    		toReturn = chatHistoryService.softAppendChatHistoryByExtensionMainAndExtensionWithAndOrganization(request.getExtensionWith(),request.getExtensionMain(), request.getOrganization(),request.getChat());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
}
