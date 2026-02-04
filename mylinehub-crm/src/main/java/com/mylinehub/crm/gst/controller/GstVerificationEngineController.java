package com.mylinehub.crm.gst.controller;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.gst.entity.GstVerificationEngine;
import com.mylinehub.crm.gst.service.GstVerificationEngineService;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;

import lombok.AllArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;

import static com.mylinehub.crm.controller.ApiMapping.GST_REST_URL;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(produces="application/json", path = GST_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class GstVerificationEngineController {
	
    private final EmployeeRepository employeeRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	private Environment env;

	private final GstVerificationEngineService gstVerificationEngineService;
	
	
	@PostMapping("/updateGstDetails")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<GstVerificationEngine> updateGstDetails(@RequestBody GstVerificationEngine gstVerificationEngine,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		GstVerificationEngine toReturn = null;
		
		String parentorganization = env.getProperty("spring.parentorginization");
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equalsIgnoreCase(parentorganization))
    	{
    		toReturn = gstVerificationEngineService.updateGstVerificationEngineData(gstVerificationEngine);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
}
