package com.mylinehub.crm.whatsapp.controller.chat;

import static com.mylinehub.crm.controller.ApiMapping.WHATSAPP_CHAT_HISTORY_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.time.Instant;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppAllChatDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.requests.WhatsAppChatHistoryExportRequest;
import com.mylinehub.crm.whatsapp.requests.WhatsAppPhoneRequest;
import com.mylinehub.crm.whatsapp.service.WhatsAppChatHistoryService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = WHATSAPP_CHAT_HISTORY_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class WhatsAppChatHistoryController {
	
	private final WhatsAppChatHistoryService whatsAppChatHistoryService;
	private final EmployeeRepository employeeRepository;
	private final JwtConfiguration jwtConfiguration;
	private final SecretKey secretKey;
	
	
	@PostMapping("/getAllChatHistoryForPhoneNumberMain")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppAllChatDTO>> getAllChatHistoryForPhoneNumberMain(@RequestBody WhatsAppPhoneRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("getAllChatHistoryCandidatesByExtensionAndOrganization");
		List<WhatsAppAllChatDTO> toReturn = null;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
//    	System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
//    	System.out.println("employee.getOrganization() : "+employee.getOrganization());
//    	System.out.println("request.getOrganization() : "+request.getOrganization());
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = whatsAppChatHistoryService.getAllChatHistoryForPhoneNumberMain(request.getPhoneMain(), request.getOrganization(),request.getStartOffset(),request.getEndOffset());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
//    		System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@PostMapping("/getAllChatHistoryByTwoPhoneNumbersAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<WhatsAppChatHistory>> getAllChatHistoryByTwoPhoneNumbersAndOrganization(@RequestBody WhatsAppPhoneRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("getAllChatHistoryByTwoExtensionsAndOrganization");
		List<WhatsAppChatHistory> toReturn = null;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		try
    		{
//    			System.out.println("Calling getAllChatHistoryByExtensionMainAndExtensionWithAndOrganization service");
    			toReturn = whatsAppChatHistoryService.getAllChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization(request.getPhoneMain(),request.getPhoneWith(), request.getOrganization());
        		
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
	
	@PostMapping("/deleteAllChatHistoryByPhoneNumberMainAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteAllChatHistoryByPhoneNumberMainAndOrganization(@RequestBody WhatsAppPhoneRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("deleteAllChatHistoryByExtensionAndOrganization");
		
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = whatsAppChatHistoryService.deleteAllChatHistoryByPhoneNumberMainAndOrganization(request.getPhoneMain(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	@PostMapping("/deleteAllChatHistoryByTwoPhoneNumbersAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> deleteAllChatHistoryByTwoPhoneNumbersAndOrganization(@RequestBody WhatsAppPhoneRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("deleteAllChatHistoryByTwoExtensionsAndOrganization");
		
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = whatsAppChatHistoryService.deleteChatHistoryByPhoneNumberMainAndphoneNumberWithAndOrganization(request.getPhoneMain(),request.getPhoneWith(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
	
	@PostMapping("/updateLastReadIndexByTwoPhoneNumbersAndOrganization")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Integer> updateLastReadIndexByTwoPhoneNumbersAndOrganization(@RequestBody WhatsAppPhoneRequest request,@RequestHeader (name="Authorization") String token){
	    
//		System.out.println("updateLastReadIndexByTwoExtensionsAndOrganization");
		
		Integer toReturn = 0;
		
        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(request.getOrganization()))
    	{
    		toReturn = whatsAppChatHistoryService.updateLastReadIndexByPhoneNumberMainAndphoneNumberWithAndOrganizationAndIsDeleted(request.getPhoneMain(),request.getPhoneWith(), request.getOrganization());
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	

	@PostMapping(value = "/exportChatHistoryExcelDbOnly")
	@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public void exportChatHistoryExcelDbOnly(
	        @RequestBody WhatsAppChatHistoryExportRequest req,
	        @RequestHeader(name = "Authorization") String token,
	        javax.servlet.http.HttpServletResponse response
	) throws java.io.IOException {

	    try {
	        if (req == null) {
	            response.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
	            return;
	        }

	        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	        if (employee == null) {
	            response.setStatus(javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
	            return;
	        }

	        byte[] bytes = whatsAppChatHistoryService.exportChatHistoryExcelDbOnly(
	                employee.getOrganization(),
	                req.getPhoneMain(),
	                req.getStartDate(),
	                req.getEndDate()
	        );

	        if (bytes == null) {
	            response.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            return;
	        }

	        String ts = String.valueOf(Instant.now().toEpochMilli());
	        String filename = "whatsapp_chat_history_" + employee.getOrganization() + "_" + ts + ".xlsx";

	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
	        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	        response.setHeader("Pragma", "no-cache");
	        response.setHeader("Expires", "0");

	        response.setContentLength(bytes.length);

	        try (java.io.OutputStream os = response.getOutputStream()) {
	            os.write(bytes);
	            os.flush();
	        }

	        response.setStatus(org.springframework.http.HttpStatus.OK.value());

	    } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	        response.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    }
	}
}
