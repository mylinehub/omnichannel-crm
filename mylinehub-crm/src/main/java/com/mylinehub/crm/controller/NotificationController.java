package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.Notification_REST_URL;
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

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.NotificationDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.requests.ExtensionRequest;
import com.mylinehub.crm.requests.MultipleIDRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.NotificationService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = Notification_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class NotificationController {
	
	
	private final NotificationService notificationService;
	private final EmployeeRepository employeeRepository;
	private final JwtConfiguration jwtConfiguration;
	private final SecretKey secretKey;
	
	@PostMapping("/getAllNotificationsByExtensionAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<NotificationDTO>> getAllNotificationsByExtensionAndOrganization(@RequestBody ExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
		List<NotificationDTO> toReturn = null;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = notificationService.findAllByExtensionAndOrganizationAndIsDeleted(request.getExtension(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	@PostMapping("/deleteAllNotificationsByExtensionAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteAllNotificationsByExtensionAndOrganization(@RequestBody ExtensionRequest request,@RequestHeader (name="Authorization") String token){
	    
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = notificationService.deleteAllByForExtensionAndOrganizationAndIsDeleted(request.getExtension(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@PostMapping("/deleteNotificationByIdsAndExtensionsAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteNotificationByIdsAndExtensionsAndOrganization(@RequestBody MultipleIDRequest request,@RequestHeader (name="Authorization") String token){
	    
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = notificationService.deleteByIdAndForExtensionAndExtensionWithAndOrganizationAndIsDeleted(request.getIds(),request.getExtension(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	

}
