package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.SYSTEM_CONFIG_REST_URL;
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
import com.mylinehub.crm.entity.dto.MemoryStatsDTO;
import com.mylinehub.crm.entity.dto.SystemConfigDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.SystemConfigService;

import lombok.AllArgsConstructor;



@RestController
@RequestMapping(produces="application/json", path = SYSTEM_CONFIG_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class SystemConfigController {

    private final EmployeeRepository employeeRepository;
    private final SystemConfigService systemConfigService;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	
    
    @GetMapping("/getMemoryStatus")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<MemoryStatsDTO> getMemoryStatistics(@RequestParam String organization,@RequestHeader (name="Authorization") String token) {
    	MemoryStatsDTO stats = new MemoryStatsDTO();
    	
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		stats.setHeapSize(Runtime.getRuntime().totalMemory());
    	    stats.setHeapMaxSize(Runtime.getRuntime().maxMemory());
    	    stats.setHeapFreeSize(Runtime.getRuntime().freeMemory());
    	    stats.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
    	    stats.setVersion(Runtime.version().toString());
    	    
    		return status(HttpStatus.OK).body(stats);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(stats);
    	} 	
    	
    }
    
	@GetMapping("/getAllSystemConfigsByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<SystemConfigDTO>> getAllSystemConfigsByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<SystemConfigDTO> systemConfigs = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		systemConfigs= systemConfigService.getAllsystemConfigsOnOrganization(organization);
    		return status(HttpStatus.OK).body(systemConfigs);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(systemConfigs);
    	} 	
	}
	
//	@PostMapping("/createSystemConfigByOrganization")
//	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	public ResponseEntity<Boolean> createSystemConfigByOrganization(@RequestBody SystemConfigDTO systemConfigDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
//	    
//		Boolean toReturn = false;
//		
//        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//    	
//    	//System.out.println(token);
//    	
//    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//    	
//    	if(employee.getOrganization().trim().equals(organization.trim()) && (employee.getOrganization().trim().equals(systemConfigDTO.getOrganization().trim())))
//    	{
//    		toReturn = systemConfigService.createsystemConfigByOrganization(systemConfigDTO);
//    		return status(HttpStatus.OK).body(toReturn);
//    	}
//    	else
//    	{
//    		//System.out.println("I am in else controller");
//    		
//    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
//    	} 	
//	} 
	
	@PostMapping("/updateSystemConfigByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSystemConfigByOrganization(@RequestBody SystemConfigDTO systemConfigDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = systemConfigService.updatesystemConfigByOrganization(systemConfigDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
//	@DeleteMapping("/deleteSystemConfigByIdAndOrganization")
//	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
//	public ResponseEntity<Boolean> deleteSystemConfigByIdAndOrganization(@RequestParam Long id,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
//	    
//		Boolean toReturn = false;
//		
//        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
//    	
//    	//System.out.println(token);
//    	
//    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
//    	
//    	if(employee.getOrganization().trim().equals(organization.trim()))
//    	{
//    		toReturn  = systemConfigService.deletesystemConfigByOrganization(id);
//    		return status(HttpStatus.OK).body(toReturn);
//    	}
//    	else
//    	{
//    		//System.out.println("I am in else controller");
//    		
//    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
//    	} 	
//    	
//		
//	} 
	
}