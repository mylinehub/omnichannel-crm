package com.mylinehub.crm.controller;

import com.mylinehub.crm.service.EmployeeService;
import com.mylinehub.crm.service.RefreshTokenService;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static com.mylinehub.crm.controller.ApiMapping.AUTH_REST_URL;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(produces="application/json", path = AUTH_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class AuthenticationController {
	
	 private final RefreshTokenService refreshTokenService;
	 private final EmployeeService employeeService;
	
	@GetMapping("/refreshToken")
	public ResponseEntity<String> refreshToken(@RequestParam String oldToken){
		String toReturn = "";
    	try
    	{
    		toReturn = refreshTokenService.refreshTokenOfEmployee(oldToken);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	catch(Exception e)
    	{
    		//System.out.println("I am in else controller");
    		return status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    	} 		
	} 
	
	
	@GetMapping("/googleLogin")
	public ResponseEntity<String> googleLogin(@RequestParam String googleToken){
	    
		String toReturn = googleToken;
    	try
    	{
    		toReturn = employeeService.googleLogin(googleToken);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	catch(Exception e)
    	{
    		//System.out.println("I am in else controller");
    		return status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    	} 		
		
	} 
	
}

