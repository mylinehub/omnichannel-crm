package com.mylinehub.crm.service;

import com.mylinehub.crm.entity.dto.BulkUploadEmployeeDto;
import com.mylinehub.crm.security.email.EmailBuilder;
import com.mylinehub.crm.security.email.EmailService;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendTemplateMessageClient;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.enums.LANGUAGE_CODE;
import com.mylinehub.crm.whatsapp.enums.MESSAGING_PRODUCT;
import com.mylinehub.crm.whatsapp.requests.WhatsAppTemplateVariableRequest;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.entity.Organization;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.util.*;



/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class AdminService implements CurrentTimeInterface {

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final EmailService emailService;
    private Environment env;
    private final OkHttpSendTemplateMessageClient okHttpSendTemplateMessageClient;
 
    
    /**
     * The task of the method is to send new org email to parent
     * @param email email of the user
     * @return enable user account
     */
    public Boolean sendNewOrgEmailtoParent(String organization, String employeeName, String employeePhoneNumber) {
    	Boolean toReturn = false;
    	String parentEmail = env.getProperty("spring.parentorginization.email");
    	String supportEmail = env.getProperty("spring.parentorginization.support.email");
    	String adminName = env.getProperty("spring.parentorginization.admin.name");
    	toReturn = true;
		System.out.println("Inside sendNewOrgEmailtoParent service.");
		String body = EmailBuilder.buildOrgRegistrationEmail(adminName,organization,employeeName,employeePhoneNumber);
		emailService.send(parentEmail, body,"** New Organization Registered To MylineHub CRM **",supportEmail);
		return toReturn;
    }
    

    /**
     * The task of the method is to send new org email to parent
     * @param email email of the user
     * @return enable user account
     */
    public Boolean sendRechargeDoneEmail(String organization,String employeeEmail, String amount) {
    	Boolean toReturn = false;
    	String supportEmail = env.getProperty("spring.parentorginization.support.email");
    	toReturn = true;
		System.out.println("Inside sendNewOrgEmailtoParent service.");
		String body = EmailBuilder.buildRechargeDoneEmail(amount, organization);
		emailService.send(employeeEmail, body,"** MylineHub CRM Recharge Successful **",supportEmail);
		return toReturn;
    }
    
    
    /**
     * The task of the method is to send new org email to parent
     * @param email email of the user
     * @return enable user account
     */
    public Boolean sendLowFundEmail(String organization,String employeeEmail) {
    	Boolean toReturn = false;
    	String supportEmail = env.getProperty("spring.parentorginization.support.email");
    	toReturn = true;
		System.out.println("Inside sendNewOrgEmailtoParent service.");
		String body = EmailBuilder.buildLowFundEmail(organization);
		emailService.send(employeeEmail, body,"** MylineHub CRM Low Funds Information **",supportEmail);
		return toReturn;
    }
    
    
    /**
     * The task of the method is to send new org email to parent
     * @param email email of the user
     * @return enable user account
     */
    public Boolean sendAccountDeactivatedEmail(String organization,String employeeEmail) {
    	Boolean toReturn = false;
    	String supportEmail = env.getProperty("spring.parentorginization.support.email");
    	toReturn = true;
		System.out.println("Inside sendNewOrgEmailtoParent service.");
		String body = EmailBuilder.buildAccountDeactivatedEmail(organization);
		emailService.send(employeeEmail, body,"** MylineHub CRM Account Deactivated **",supportEmail);
		return toReturn;
    }
    

    public boolean sendEmployeeOnboardingWhatsAppMessageToParent(BulkUploadEmployeeDto employee) throws Exception {
    	boolean toReturn = false;
    	try {
    		LoggerUtils.log.debug("****************** sendEmployeeOnboardingWhatsAppMessageToParent ******************");
    		
    		WhatsAppTemplateVariableRequest whatsAppTemplateVariableRequest = new WhatsAppTemplateVariableRequest();
    		whatsAppTemplateVariableRequest.setEmployee(employee.getEmployee());
    		whatsAppTemplateVariableRequest.setName(employee.getEmployee().getFirstName()+" "+employee.getEmployee().getLastName());
    		whatsAppTemplateVariableRequest.setEmail(employee.getEmployee().getEmail());
    		whatsAppTemplateVariableRequest.setCode(employee.getEmployee().getOrganization());
    		Map<String,Organization> organizationMap = OrganizationData.workWithAllOrganizationData(employee.getEmployee().getOrganization(),null,"get-one",null);
    		
    		if(organizationMap == null || organizationMap.size() == 0) {
    			throw new Exception("Organization not found for employee having org : "+ employee.getEmployee().getOrganization());
    		}
    		
    		Organization Organization = organizationMap.get(employee.getEmployee().getOrganization());
    		whatsAppTemplateVariableRequest.setOrganization(Organization);
    		
    		LoggerUtils.log.debug("Setting template for parameter values");
    		String newUserInfoToAdmin = env.getProperty("spring.template.newUserInfoToAdmin");
    		String phone = env.getProperty("spring.whatsapp.phone");
    		String parentPhone = env.getProperty("spring.parentorginization.phone");
    		
    		LoggerUtils.log.debug("Fetching parent templates");
    		//Add detail to memory Data
    		Map<String,List<WhatsAppPhoneNumberTemplates>> allTemplatesMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(phone,null,"get-one");
    		
    		if(allTemplatesMap == null || allTemplatesMap.size() == 0) {
    				throw new Exception("No template found for parent. Please connect with support.");
    		}
    		
    		List<WhatsAppPhoneNumberTemplates> allTemplates = allTemplatesMap.get(phone);

    				
    		LoggerUtils.log.debug("Finding correct match for templates amongst all");
    		allTemplates.forEach((element)->{

    			if(element.getTemplateName().equalsIgnoreCase(newUserInfoToAdmin)) {
    				try {
    					
    	    			whatsAppTemplateVariableRequest.setWhatsAppPhoneNumberTemplate(element);
    	    			LoggerUtils.log.debug("Sending whats app message for template : "+ element.getTemplateName());
    					JSONObject jsonObject = okHttpSendTemplateMessageClient.sendMessage(MESSAGING_PRODUCT.whatsapp.name(),parentPhone, whatsAppTemplateVariableRequest, LANGUAGE_CODE.en.name(),  element.getWhatsAppPhoneNumber().getWhatsAppProject().getApiVersion(), element.getWhatsAppPhoneNumber().getPhoneNumberID(), element.getWhatsAppPhoneNumber().getWhatsAppProject().getAccessToken());
    					
    					System.out.println("*****************************************************************");
    					System.out.println("Response Data From Whats App");
    					System.out.println("*****************************************************************");
    					
    					System.out.println("Response : "+jsonObject);

    				} catch (Exception e) {
						// TODO Auto-generated catch block
    					LoggerUtils.log.debug("Exception while sending onboarding message");
						e.printStackTrace();
					}
            		
    			}
    		});
    		toReturn = true;
    	}
    	catch(Exception  e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return toReturn;
    }
    
    

}
