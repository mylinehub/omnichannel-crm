package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.SSH_CONNECTION_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import org.asteriskjava.manager.TimeoutException;
import org.springframework.context.ApplicationContext;
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
import com.mylinehub.crm.entity.dto.SshConnectionDTO;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.requests.IdRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.SshConnectionService;
import com.mylinehub.shh.SshWrapper;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = SSH_CONNECTION_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class SshConnectionController {

    private final EmployeeRepository employeeRepository;
    private final SshConnectionService sshConnectionService;
    private final ApplicationContext applicationContext;
    private final ErrorRepository errorRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	
	@PostMapping("/enableSshConnectionOnIdAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> enableSshConnectionOnIdAndOrganization(@RequestBody IdRequest request,@RequestHeader (name="Authorization") String token){
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		sshConnectionService.enableSshConnectionOnOrganization(request.getId(), request.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	
	@PostMapping("/disableSshConnectionOnIdAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> disableSshConnectionOnIdAndOrganization(@RequestBody IdRequest request,@RequestHeader (name="Authorization") String token){
	    
			
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		sshConnectionService.disableSshConnectionOnOrganization(request.getId(), request.getOrganization());
    		toReturn = true;
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 		
	} 
	
	
	@GetMapping("/getAllSshConnectionsByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<SshConnectionDTO>> getAllSshConnectionsByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<SshConnectionDTO> sshConnections = null;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		sshConnections= sshConnectionService.getAllSshConnectionsOnOrganization(organization);
    		return status(HttpStatus.OK).body(sshConnections);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(sshConnections);
    	} 	
	}
	
	@PostMapping("/createSshConnectionByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> createSshConnectionByOrganization(@RequestBody SshConnectionDTO sshConnectionDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	//System.out.println(String.valueOf(sshConnectionDTO));
    	
    	//System.out.println(String.valueOf(organization));
    	
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim())&& (employee.getOrganization().trim().equals(sshConnectionDTO.getOrganization().trim())))
    	{
    		toReturn = sshConnectionService.createsshConnectionByOrganization(sshConnectionDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@PostMapping("/updateSshConnectionByOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> updateSshConnectionByOrganization(@RequestBody SshConnectionDTO sshConnectionDTO,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	    
		//System.out.println("Let us update an employee");
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
        //System.out.println("Email : "+employeeDTO.getEmail());
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn = sshConnectionService.updatesshConnectionByOrganization(sshConnectionDTO);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	} 
	
	@DeleteMapping("/deleteSshConnectionByDomainAndOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deleteSshConnectionByDomainAndOrganization(@RequestParam String domain,@RequestParam String organization,@RequestHeader (name="Authorization") String token) throws Exception{
	    
		Boolean toReturn = false;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		toReturn  = sshConnectionService.deletesSshConnectionByDomainAndOrganization(domain, organization);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
    	
		
	} 
	
	

	@GetMapping("/refreshSshConnectionForOrganization")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> refreshSshConnectionForOrganization(@RequestParam String domain,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		Boolean toReturn = true;
		SshWrapper sshWrapper = new SshWrapper();
		
		try
    	{
			try
			{
				SshWrapper.allChannel.remove(organization);
			}
			catch(Exception e)
			{
				Report.addError(e.getMessage(), "refreshSsh","EmployeeController", "cannot connect to ssh",organization,errorRepository);		
				
			}
			try
			{
				SshWrapper.allSession.remove(organization);
			}
			catch(Exception e)
			{
				Report.addError(e.getMessage(), "refreshSsh","EmployeeController", "cannot connect to ssh",organization,errorRepository);		
				
			}
			try
			{
				SshWrapper.allJSch.remove(organization);
			}
			catch(Exception e)
			{
				Report.addError(e.getMessage(), "refreshSsh","EmployeeController", "cannot connect to ssh",organization,errorRepository);		
				
			}
			
			
			
			
    		List<SshConnectionDTO> sshConnections = sshConnectionService.getAllsshConnectionsOnIsEnabledAndOrganization(true,organization);

        	sshConnections.forEach(
                    (sshConnection) -> { 
                    	//System.out.println("Creating AMI Connection and adding listner");
                 		SshConnectionDTO current = sshConnection;
                 		
                 		if(current.getPassword() != null)
                 		{
                 			try {
								sshWrapper.configureOrGetChannelUsingPassword(current.getOrganization(), current.getPassword(), current.getSshUser(), current.getDomain(),applicationContext);
							} catch (IllegalArgumentException | IllegalStateException | IOException
									| TimeoutException e) {
								// TODO Auto-generated catch block
								Report.addError(e.getMessage(), "Ssh Connection","Startup System", "cannot connect to ssh",current.getOrganization(),errorRepository);		
							}	
                 		}
                 		else
                 		{
                 			try {
                 				//System.out.println("creating system using pem file");
								sshWrapper.configureOrGetChannelUsingPem(current.getOrganization(),current.getPemFileName(), current.getSshUser(), current.getDomain(),applicationContext);
								//System.out.println("received channel");
								//System.out.println(channel.getId());
								//System.out.println(channel.getExitStatus());
								//System.out.println("Is SSH Connection Connected : " + channel.isConnected());
								//System.out.println(channel.toString());
                 			} catch (IllegalArgumentException | IllegalStateException | IOException
									| TimeoutException e) {
								// TODO Auto-generated catch block
                 				e.printStackTrace();
								Report.addError(e.getMessage(), "refreshSsh","EmployeeController", "cannot connect to ssh",current.getOrganization(),errorRepository);		
							}	
                     		
                 		}
                    });      
    	}
    	catch(Exception e)
    	{
    		//System.out.println(e.getMessage());
    		Report.addError(e.getMessage(), "Ssh Connection","Startup System", "cannot connect to SSH","N/A",errorRepository);

    	}
		
		return status(HttpStatus.OK).body(toReturn);
		
	}
	
}
