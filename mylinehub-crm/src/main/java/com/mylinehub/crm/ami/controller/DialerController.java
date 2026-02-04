package com.mylinehub.crm.ami.controller;

import static com.mylinehub.crm.controller.ApiMapping.ASTERISK_DIALER_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.response.ManagerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.ami.ConnectionStream;
import com.mylinehub.crm.ami.ManagerFunctionalityWrapper;
import com.mylinehub.crm.ami.ManagerStream;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.requests.AttemptedTransferCallRequest;
import com.mylinehub.crm.requests.BlindTransferRequest;
import com.mylinehub.crm.requests.BridgeTwoActiveCallsRequest;
import com.mylinehub.crm.requests.ChangeMonitorRequest;
import com.mylinehub.crm.requests.ChannelRequest;
import com.mylinehub.crm.requests.ConfbridgeListRoomsRequest;
import com.mylinehub.crm.requests.ConfbridgeMemberRequest;
import com.mylinehub.crm.requests.CustomConferenceRequest;
import com.mylinehub.crm.requests.DisconnectAfterXSecondsRequest;
import com.mylinehub.crm.requests.ExtensionStateRequest;
import com.mylinehub.crm.requests.HungUpRequest;
import com.mylinehub.crm.requests.ListenQuietlyRequest;
import com.mylinehub.crm.requests.MonitorActionRequest;
import com.mylinehub.crm.requests.OriginateCallRequest;
import com.mylinehub.crm.requests.OriginateDataCallRequest;
import com.mylinehub.crm.requests.ParkForTimeoutRequest;
import com.mylinehub.crm.requests.PublicConferenceRequest;
import com.mylinehub.crm.requests.RequestStateForAllAgentsRequest;
import com.mylinehub.crm.requests.SendMessageToChannelRequest;
import com.mylinehub.crm.requests.SingleConfbridgeRequest;
import com.mylinehub.crm.requests.StatusForSpecificChannelRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = ASTERISK_DIALER_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class DialerController {
	
	private final EmployeeRepository employeeRepository;
	private final ErrorRepository errorRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
    
	
	@PostMapping("/originateCall")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<ManagerResponse> originateCall(@RequestBody OriginateCallRequest request,@RequestHeader (name="Authorization") String token){
	    
		ManagerStream managerStream = new ManagerStream();
		ConnectionStream connectionStream = new ConnectionStream();
		ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
		ManagerResponse response = null;
		
		if(managerConnection !=null)
		{

	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(request.getOrganization()))
	    	{
	    		try {
	    			response= managerStream.originateCall(request.getOrganization(), request.getChannelToCall(), request.getCallerID(), request.getContext(), request.getExtensionToCall(), request.getPriority(), request.getTimeOut(), request.isAsync(), managerConnection);
				} catch (IOException | AuthenticationFailedException | TimeoutException e) {
					// TODO Auto-generated catch block
					Report.addError("No Data", "originateCall","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
					e.printStackTrace();
				}

	    		return status(HttpStatus.OK).body(response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(response);
	    	} 		
		}
		else
		{
			return status(HttpStatus.UNAUTHORIZED).body(response);
		}

	} 

	
	@PostMapping("/listenQuietly")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<ManagerResponse> listenQuietly(@RequestBody ListenQuietlyRequest request,@RequestHeader (name="Authorization") String token){
	    
		ManagerFunctionalityWrapper managerStream = new ManagerFunctionalityWrapper();
		ConnectionStream connectionStream = new ConnectionStream();
		ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
		ManagerResponse response = null;
		
		if(managerConnection !=null)
		{

	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(request.getOrganization()))
	    	{
	    		try {	
	    			response= managerStream.listenQuietly(request.getOrganization(), request.getCallingChannel(), request.getChannelToCall(), request.getCallerID(),  request.getPriority(), request.getTimeOut(), request.isAsync(), managerConnection);
				} catch (IOException | AuthenticationFailedException | TimeoutException e) {
					// TODO Auto-generated catch block
					Report.addError("No Data", "listenQuietly","DialerController", "Issue while listening quietly",request.getOrganization(),errorRepository);			
					e.printStackTrace();
				}
	    		return status(HttpStatus.OK).body(response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(response);
	    	} 		
		}
		else
		{
			return status(HttpStatus.UNAUTHORIZED).body(response);
		}

	}
	
	@PostMapping("/publicConferenceRequest")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<ManagerResponse> publicConferenceRequest(@RequestBody PublicConferenceRequest request,@RequestHeader (name="Authorization") String token){

		ManagerFunctionalityWrapper managerStream = new ManagerFunctionalityWrapper();
		ConnectionStream connectionStream = new ConnectionStream();
		ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
		ManagerResponse response = null;
		
		if(managerConnection !=null)
		{

	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//System.out.println(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(request.getOrganization()))
	    	{
	    		try {	    
	    			response= managerStream.createPublicConference(request.getOrganization(), request.getChannelToCall(), request.getCallerID(),  request.getPriority(), request.getTimeOut(), request.isAsync(), managerConnection);
				} catch (IOException | AuthenticationFailedException | TimeoutException e) {
					// TODO Auto-generated catch block
					Report.addError("No Data", "publicConferenceRequest","DialerController", "Issue while publicConferenceRequest for ",request.getOrganization(),errorRepository);			
					e.printStackTrace();
				}

	    		return status(HttpStatus.OK).body(response);
	    	}
	    	else
	    	{
	    		//System.out.println("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(response);
	    	} 		
		}
		else
		{
  		      return status(HttpStatus.UNAUTHORIZED).body(response);
		}
	 }
		
		@PostMapping("/customConferenceRequest")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> customConferenceRequest(@RequestBody CustomConferenceRequest request,@RequestHeader (name="Authorization") String token){

			ManagerFunctionalityWrapper managerStream = new ManagerFunctionalityWrapper();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {	
		    		  response= managerStream.createCustomConference(request.getOrganization(), request.getChannelToCall(), request.getCallerID(),  request.getPriority(), request.getTimeOut(), request.isAsync(),request.getBridge(),request.getUserprofile(),request.getMenu(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "customConferenceRequest","DialerController", "Issue while customConferenceRequest for ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}
		}
		
		
		@PostMapping("/requestStateForAllAgents")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> requestStateForAllAgents(@RequestBody RequestStateForAllAgentsRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.requestStateForAllAgents(request.getOrganization(), request.getTimeOut(),  managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "requestStateForAllAgents","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/statusForSpecificChannel")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> statusForSpecificChannel(@RequestBody StatusForSpecificChannelRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.statusForSpecificChannel(request.getOrganization(), request.getChannelID(),request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "statusForSpecificChannel","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/sendAnonymousTextToChannel")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> sendAnonymousTextToChannel(@RequestBody SendMessageToChannelRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.sendAnonymousTextToChannel(request.getOrganization(), request.getChannelID(), request.getMessage(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "sendAnonymousTextToChannel","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/bridgeTwoActiveCalls")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> bridgeTwoActiveCalls(@RequestBody BridgeTwoActiveCallsRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.bridgeTwoActiveCalls(request.getOrganization(), request.getChannelToCall(), request.getCallingChannel(),request.getTimeOut(),request.isTone(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "bridgeTwoActiveCalls","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/originateDataCall")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> originateDataCall(@RequestBody OriginateDataCallRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.originateDataCall(request.getOrganization(), request.getChannelToCall(), request.getCallerID(), request.getData(),  request.getPriority(), request.getTimeOut(), request.isAsync(),request.getApplication(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "originateDataCall","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/hungUpCall")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> hungUpCall(@RequestBody HungUpRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.hungUpCall(request.getOrganization(), request.getChannelToHungUp(), request.getCause(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "hungUpCall","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/blindTransferCall")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> blindTransferCall(@RequestBody BlindTransferRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.blindTransferCall(request.getOrganization(), request.getChannelFromTransfer(), request.getCallerID(), request.getContext(), request.getExtensionToTransfer(), request.getPriority(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "blindTransferCall","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/attemptedTransferCall")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> attemptedTransferCall(@RequestBody AttemptedTransferCallRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.attemptedTransferCall(request.getOrganization(), request.getChannelFromTransfer(), request.getCallerID(), request.getContext(), request.getExtensionToTransfer(), request.getPriority(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "attemptedTransferCall","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		@PostMapping("/disconnectAfterXSeconds")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> disconnectAfterXSeconds(@RequestBody DisconnectAfterXSecondsRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.disconnectAfterXSeconds(request.getOrganization(), request.getChannelToHungUp(), request.getSeconds(), request.getTimeout(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "disconnectAfterXSeconds","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		@PostMapping("/confbridgeListRooms")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeListRooms(@RequestBody ConfbridgeListRoomsRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeListRooms(request.getOrganization(),  request.getTimeOut(),  managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeListRooms","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		@PostMapping("/confbridgeListMembers")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeListMembers(@RequestBody SingleConfbridgeRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeListMembers(request.getOrganization(), request.getConferenceID(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeListMembers","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		@PostMapping("/confbridgeLock")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeLock(@RequestBody SingleConfbridgeRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeLock(request.getOrganization(), request.getConferenceID(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeLock","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		@PostMapping("/confbridgeUnlock")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeUnlock(@RequestBody SingleConfbridgeRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeUnlock(request.getOrganization(), request.getConferenceID(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeUnlock","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		@PostMapping("/confbridgeMuteMember")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeMuteMember(@RequestBody ConfbridgeMemberRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeMuteMember(request.getOrganization(), request.getConferenceID(),request.getChannelID(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeMuteMember","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/confbridgeUnmuteMember")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeUnmuteMember(@RequestBody ConfbridgeMemberRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeUnmuteMember(request.getOrganization(), request.getConferenceID(),request.getChannelID(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeUnmuteMember","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/confbridgeSetSingleVideoSrcMember")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeSetSingleVideoSrcMember(@RequestBody ConfbridgeMemberRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeSetSingleVideoSrcMember(request.getOrganization(), request.getConferenceID(),request.getChannelID(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeSetSingleVideoSrcMember","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/confbridgeStartRecord")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeStartRecord(@RequestBody SingleConfbridgeRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeStartRecord(request.getPath(),request.getOrganization(),request.getConferenceID(), request.getTimeOut(),managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeStartRecord","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/confbridgeStopRecord")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeStopRecord(@RequestBody SingleConfbridgeRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeStopRecord(request.getOrganization(), request.getConferenceID(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeStopRecord","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		}
		
		@PostMapping("/confbridgeKickMember")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> confbridgeKickMember(@RequestBody ConfbridgeMemberRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.confbridgeKickMember(request.getOrganization(), request.getConferenceID(),request.getChannelID(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "confbridgeStopRecord","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/extensionState")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> extensionState(@RequestBody ExtensionStateRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.extensionState(request.getOrganization(), request.getExten(), request.getContext(), request.getTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "extensionState","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/parkForTimeOut")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> parkForTimeOut(@RequestBody ParkForTimeoutRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.parkForTimeOut(request.getOrganization(), request.getChannelTosendText(), request.getChannelToPlayMusic(), request.getParkTimeOut(),request.getActionTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "parkForTimeOut","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/monitorAction")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> monitorAction(@RequestBody MonitorActionRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.monitorAction(request.getOrganization(), request.getChannel(), request.getFormat(), request.getMix(),request.getActionTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "monitorAction","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/unpauseMonitorAction")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> unpauseMonitorAction(@RequestBody ChannelRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.unpauseMonitorAction(request.getOrganization(), request.getChannel(),request.getActionTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "unpauseMonitorAction","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/pauseMonitorAction")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> pauseMonitorAction(@RequestBody ChannelRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.pauseMonitorAction(request.getOrganization(), request.getChannel(),request.getActionTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "pauseMonitorAction","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/stopMonitorAction")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> stopMonitorAction(@RequestBody ChannelRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.stopMonitorAction(request.getOrganization(),request.getChannel(),request.getActionTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "stopMonitorAction","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 
		
		
		@PostMapping("/changeMonitorAction")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<ManagerResponse> changeMonitorAction(@RequestBody ChangeMonitorRequest request,@RequestHeader (name="Authorization") String token){
		    
			ManagerStream managerStream = new ManagerStream();
			ConnectionStream connectionStream = new ConnectionStream();
			ManagerConnection managerConnection = connectionStream.getConnection(request.getDomain(),request.getSecondDomain(),request.isSecondLine());
			ManagerResponse response = null;
			
			if(managerConnection !=null)
			{

		        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
		    	
		    	//System.out.println(token);
		    	
		    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		    	
		    	if(employee.getOrganization().trim().equals(request.getOrganization()))
		    	{
		    		try {
		    			response= managerStream.changeMonitorAction(request.getOrganization(),request.getChannel(),request.getFile(),request.getActionTimeOut(), managerConnection);
					} catch (IOException | AuthenticationFailedException | TimeoutException e) {
						// TODO Auto-generated catch block
						Report.addError("No Data", "changeMonitorAction","DialerController", "Issue for organization ",request.getOrganization(),errorRepository);			
						e.printStackTrace();
					}

		    		return status(HttpStatus.OK).body(response);
		    	}
		    	else
		    	{
		    		//System.out.println("I am in else controller");
		    		
		    		return status(HttpStatus.UNAUTHORIZED).body(response);
		    	} 		
			}
			else
			{
				return status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} 

}
