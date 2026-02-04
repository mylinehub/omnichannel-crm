package com.mylinehub.crm.whatsapp.controller.enums;

import static com.mylinehub.crm.controller.ApiMapping.WHATS_APP_DICT_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.whatsapp.enums.ADDRESS_TYPE;
import com.mylinehub.crm.whatsapp.enums.CALENDER;
import com.mylinehub.crm.whatsapp.enums.COMPONENT_SUB_TYPE;
import com.mylinehub.crm.whatsapp.enums.COMPONENT_TYPE;
import com.mylinehub.crm.whatsapp.enums.DAY_OF_WEEK;
import com.mylinehub.crm.whatsapp.enums.EMAIL_TYPE;
import com.mylinehub.crm.whatsapp.enums.LANGUAGE_CODE;
import com.mylinehub.crm.whatsapp.enums.LANGUAGE_POLICY;
import com.mylinehub.crm.whatsapp.enums.MEDIA_SELECTION_CRITERIA;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import com.mylinehub.crm.whatsapp.enums.MESSAGING_PRODUCT;
import com.mylinehub.crm.whatsapp.enums.PHONE_NUMBER_TYPE;
import com.mylinehub.crm.whatsapp.enums.RECEPIENT_TYPE;
import com.mylinehub.crm.whatsapp.enums.SEND_MESSAGE_KEYS;
import com.mylinehub.crm.whatsapp.enums.TEMPLATE_VARIABLES;
import com.mylinehub.crm.whatsapp.enums.TEMPLATE_VARIALE_TYPE;
import com.mylinehub.crm.whatsapp.enums.URL_TYPE;
import com.mylinehub.crm.whatsapp.enums.CurrencyCode;
import com.mylinehub.crm.whatsapp.enums.chat.CONVERSATION_TYPE;
import com.mylinehub.crm.whatsapp.enums.user.BLOCK_USER_PARAMTERS;
import com.mylinehub.crm.whatsapp.enums.webhook.AD_SOURCE;
import com.mylinehub.crm.whatsapp.enums.webhook.CHANGE_FIELD_TYPE;
import com.mylinehub.crm.whatsapp.enums.webhook.CONVERSATION_CATEGORY;
import com.mylinehub.crm.whatsapp.enums.webhook.MESSAGE_STATUS_TYPE;
import com.mylinehub.crm.whatsapp.enums.webhook.PAYMENT_STATUS;
import com.mylinehub.crm.whatsapp.enums.webhook.PRICING_MODEL;
import com.mylinehub.crm.whatsapp.enums.webhook.TYPE_OF_INTERACTIVE;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = WHATS_APP_DICT_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class WhatsEnumController {
	private final JwtConfiguration jwtConfiguration;
	private final EmployeeRepository employeeRepository;
	private final SecretKey secretKey;
	 
	
	@GetMapping("/getAllWhatsAppCurrencyCodes")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppCurrencyCodes(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(CurrencyCode.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppConversationTypes")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppConversationTypes(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(CONVERSATION_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppAddressTypes")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppAddressTypes(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(ADDRESS_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppCalender")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppCalender(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(CALENDER.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppComponentSubType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppComponentSubType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(COMPONENT_SUB_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppComponentType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppComponentType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(COMPONENT_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppDayOfWeek")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppDayOfWeek(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(DAY_OF_WEEK.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppEmailType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppEmailType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(EMAIL_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppLanguageCode")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppLanguageCode(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(LANGUAGE_CODE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppLanguagePolicy")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppLanguagePolicy(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(LANGUAGE_POLICY.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppMediaSelectedCriteria")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppMediaSelectedCriteria(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(MEDIA_SELECTION_CRITERIA.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppMessageType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppMessageType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(MESSAGE_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppMessageProduct")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppMessageProduct(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(MESSAGING_PRODUCT.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppPhoneNumberType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppPhoneNumberType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(PHONE_NUMBER_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppRecepientType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppRecepientType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(RECEPIENT_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppSendMessageKeys")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppSendMessageKeys(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(SEND_MESSAGE_KEYS.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	@GetMapping("/getAllWhatsAppTemplateVariables")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppTemplateVariables(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(TEMPLATE_VARIABLES.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppTemplateVariablesType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppTemplateVariablesType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(TEMPLATE_VARIALE_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppUrlType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppUrlType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(URL_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	
	//ENUM MAPPING FOR WEBHOOKS
	
	@GetMapping("/getAllWhatsAppAdSource")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppAdSource(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(AD_SOURCE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppChangeFieldType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppChangeFieldType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(CHANGE_FIELD_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppConversationCategory")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppConversationCategory(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(CONVERSATION_CATEGORY.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppMessageStatusType")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppMessageStatusType(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(MESSAGE_STATUS_TYPE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppPaymentStatus")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppPaymentStatus(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(PAYMENT_STATUS.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppPricingModel")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppPricingModel(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(PRICING_MODEL.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	
	@GetMapping("/getAllWhatsAppTypeOfInteractive")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppTypeOfInteractive(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(TYPE_OF_INTERACTIVE.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
	//ENUM MAPPING FOR USER
	
	@GetMapping("/getAllWhatsAppBlockUserParameter")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<List<String>> getAllWhatsAppBlockUserParameter(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
	        
		List<String> allDicValues = new ArrayList<String> ();
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//System.out.println(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		EnumSet.allOf(BLOCK_USER_PARAMTERS.class).forEach(value ->allDicValues.add(value.name()));
    		
    		return status(HttpStatus.OK).body(allDicValues);
    	}
    	else
    	{
    		//System.out.println("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(allDicValues);
    	} 	
	}
	
}
