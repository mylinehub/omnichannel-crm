package com.mylinehub.crm.controller.application;

import static com.mylinehub.crm.controller.ApiMapping.Organization_App_REST_URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


import static org.springframework.http.ResponseEntity.status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.SystemConfigData;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.UsedRegistrationToken;
import com.mylinehub.crm.entity.dto.UpdateOrgDTO;
import com.mylinehub.crm.gst.api.cloud.wrapper.OkHttpSendIdfyTaskProcessorAdvanceGST;
import com.mylinehub.crm.gst.api.cloud.wrapper.OkHttpSendVueAdvanceGstClient;
import com.mylinehub.crm.gst.api.cloud.wrapper.OkHttpSendVueAuthClient;
import com.mylinehub.crm.gst.data.GSTVerificationEngineData;
import com.mylinehub.crm.gst.data.TokenData;
import com.mylinehub.crm.gst.data.dto.GstVerificationEngineDataParameterDto;
import com.mylinehub.crm.gst.data.dto.TokenDataDto;
import com.mylinehub.crm.gst.data.dto.TokenDataParameterDto;
import com.mylinehub.crm.gst.entity.GstVerificationEngine;
import com.mylinehub.crm.gst.entity.RegisteredErroredGST;
import com.mylinehub.crm.gst.entity.RegisteredGST;
import com.mylinehub.crm.gst.repository.GstVerificationEngineRepository;
import com.mylinehub.crm.gst.repository.RegisterGstErrorsRepository;
import com.mylinehub.crm.gst.repository.RegisterGstRepository;
import com.mylinehub.crm.repository.UsedRegistrationTokenRepository;
import com.mylinehub.crm.service.MenuService;
import com.mylinehub.crm.service.OrganizationService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = Organization_App_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class OrganizationApplicationController {

	private final OrganizationService organizationService;
	private final RegisterGstRepository registerGstRepository;
	private final RegisterGstErrorsRepository registerGstErrorsRepository;
	private final OkHttpSendVueAdvanceGstClient okHttpSendVueAdvanceGstClient;
	private final OkHttpSendIdfyTaskProcessorAdvanceGST okHttpSendIdfyTaskProcessorAdvanceGST;
	private final GstVerificationEngineRepository gstVerificationEngineRepository;
	private final OkHttpSendVueAuthClient okHttpSendVueAuthClient;
	private final MenuService menuService;
	private final UsedRegistrationTokenRepository usedRegistrationTokenRepository;
	private final Environment env;
	
	@PostMapping("/getExisting")
    public ResponseEntity<Organization> getExisting(@RequestBody UpdateOrgDTO updateOrgDTO) throws Exception {
        try {
        	
        	System.out.println("******************Get Existing Organization******************");
        	
        	if(organizationService.isValidToken(updateOrgDTO.getToken())) {
        		return status(HttpStatus.OK).body(organizationService.findByBusinessId(updateOrgDTO.getBusinessId()));
        	}
        	else {
            	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        	}
         }
        catch(Exception e)
        {
        	e.printStackTrace();
        	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
	
    @PostMapping("/verifyBusinessIdentificationAndCreate")
    public ResponseEntity<String> create(@RequestBody UpdateOrgDTO updateOrgDTO) throws Exception {

		boolean hitSecondAsFirstNotWorking = false;
		
    	Employee toReturn = null;
    	ObjectMapper mapper = new ObjectMapper();
    	String parentPhoneNumber = env.getProperty("spring.parentorginization.phone");
    	String defaultcountryCode = env.getProperty("spring.parentorginization.phone.country.code");
    	
        try {
        	
        	
        	System.out.println("******************Start Creating Organization******************");
        	if(organizationService.isValidToken(updateOrgDTO.getToken())) {	
        		
              	if(updateOrgDTO.getBusinessId() == null)
            	{
            		throw new Exception("Business Id Cannot Be Null");
            	}
              	
              	UsedRegistrationToken usedRegistrationToken = null;
              	
              	usedRegistrationToken = usedRegistrationTokenRepository.getUsedRegistrationTokenByUsedToken(updateOrgDTO.getToken());
              	
              	if(usedRegistrationToken==null) {
              		usedRegistrationToken = new UsedRegistrationToken();
              		usedRegistrationToken.setUsedToken(updateOrgDTO.getToken());
              		usedRegistrationTokenRepository.save(usedRegistrationToken);
              	}else {
					throw new Exception("Verification Token Already Used. What's App "+parentPhoneNumber+" for support.");
				}
            	
            	System.out.println("******************Verify if we already have Organization ******************");
            	
            	//Cannot do this, because we create organization just on basis on GSTIN
            	
            	
            	System.out.println("******************GST Verification******************");
            	RegisteredGST registerGst = registerGstRepository.getByBusinessId(updateOrgDTO.getBusinessId());
            	JSONObject mainObject = null;
            	
            	boolean hitAPI = false;
            	
            	if(registerGst != null && registerGst.getGstResponse() != null) {
            		System.out.println("Gst already present");
            		mainObject = new JSONObject(registerGst.getGstResponse());
            		
            		if(registerGst.getOrigin().equals(SystemConfigData.systemConfig.getGstEngineNameSecond())) {
            			System.out.println("Engine used was second in database record, hence making hitSecondAsFirstNotWorking true");
            			hitSecondAsFirstNotWorking = true;
            		}
            		
            	}else if(registerGst != null && registerGst.getError() != null){
            		System.out.println("Gst already present but went into error state previously");
            		
            		if(registerGst.getError().contains("9625048379"))
            		{
            			registerGstRepository.delete(registerGst);
            			hitAPI = true;
            		}
            		else	{
                		throw new Exception(registerGst.getError());
            		}
            	}
            	else {
            		hitAPI = true;
            	}
            	
            	if(hitAPI) {	
            		System.out.println("Gst not present. Will fetch from database.");
            		//Hit GST API Now
            		//****************
            		// Use Original API Call
            		//****************
            		
            		try {

            			String engineString = SystemConfigData.systemConfig.getGstEngineName();
            			TokenDataParameterDto tokenDataParameterDto = new TokenDataParameterDto();
            			tokenDataParameterDto.setAction("get-one");
            			tokenDataParameterDto.setEngine(engineString);
            			tokenDataParameterDto.setGstVerificationEngineRepository(gstVerificationEngineRepository);
            			tokenDataParameterDto.setOkHttpSendVueAuthClient(okHttpSendVueAuthClient);
            			
            			Map<String, TokenDataDto> tokenMap = TokenData.workWithTokenData(tokenDataParameterDto);
            			
            			TokenDataDto tokenDataDto = null;
            			
            			if(tokenMap == null) {
            				System.out.println("Token Map null.");
            				System.out.println("Second engine will be used");
            				hitSecondAsFirstNotWorking = true;
            				engineString = SystemConfigData.systemConfig.getGstEngineNameSecond();
            				System.out.println("Not Registered! Technical Issue. Token not found for engine : "+tokenDataParameterDto.getEngine()+". WhatsApp "+parentPhoneNumber+" for support.");
//            				throw new Exception("Not Registered! Technical Issue. Token not found for engine : "+tokenDataParameterDto.getEngine()+". WhatsApp "+parentPhoneNumber+" for support.");
            			}
            			else {
                			
            				tokenDataDto = tokenMap.get(tokenDataParameterDto.getEngine());
                			if(tokenDataDto == null) {
                				System.out.println("tokenDataDto is null");
                				System.out.println("Second engine will be used");
                				hitSecondAsFirstNotWorking = true;
                				engineString = SystemConfigData.systemConfig.getGstEngineNameSecond();
                				System.out.println("Not Registered! Technical Issue. Token not found for engine : \"+tokenDataParameterDto.getEngine()+\". WhatsApp \"+parentPhoneNumber+\" for support.");
//                				throw new Exception("Not Registered! Technical Issue. Token not found for engine : "+tokenDataParameterDto.getEngine()+". WhatsApp "+parentPhoneNumber+" for support.");
                			}
            			}
            			
            			GstVerificationEngine engine = null;
            			GstVerificationEngineDataParameterDto gstVerificationEngineDataParameterDto = new GstVerificationEngineDataParameterDto();
                		gstVerificationEngineDataParameterDto.setEngineName(engineString);
                		gstVerificationEngineDataParameterDto.setDetails(null);
                		gstVerificationEngineDataParameterDto.setAction("get-one");
                		
                		System.out.println("After retrieving engine information.");
                		
                		System.out.println("engineString : "+engineString);
                		
                		Map<String, GstVerificationEngine> gstEngineMap = GSTVerificationEngineData.workWithGstVerificationData(gstVerificationEngineDataParameterDto);
                		
                		if(gstEngineMap==null) {
                			System.out.println("Gst Engine Map Null");
                			throw new Exception("Gst Engine Details Not Found For '"+engineString+"'. Whats app on "+parentPhoneNumber+" for resolution.");
                		}
                		else {
                			engine = gstEngineMap.get(engineString);
                		}
                		
                		if(engine!=null) {
                			System.out.println("Sending third party call to fetch GST information");
                			JSONObject responseBody = null;
                			
                			if(!hitSecondAsFirstNotWorking) {
                				
                				try {
                					System.out.println("Hitting first engine : "+engineString);
                					responseBody = okHttpSendVueAdvanceGstClient.sendMessage(tokenDataDto.getToken(),engine.getClientId(),engine.getCientSecret(),updateOrgDTO.getBusinessId());
                					
                					if(responseBody == null) {
                						System.out.println("First engine response was null. Hence throwing exception to verify and start second engine processing");
                						throw new Exception("Starting second engine processing ...");
                					}
                				}
                				catch(Exception e) {
                					e.printStackTrace();
                					System.out.println("Issue while hitting first engine advance gst api");
                    				System.out.println("Second engine will be used");
                					hitSecondAsFirstNotWorking = true;
                    				engineString = SystemConfigData.systemConfig.getGstEngineNameSecond();
                    				gstVerificationEngineDataParameterDto.setEngineName(engineString);
                            		System.out.println("engineString : "+engineString);
                            		gstEngineMap = GSTVerificationEngineData.workWithGstVerificationData(gstVerificationEngineDataParameterDto);
                            		
                            		if(gstEngineMap==null) {
                            			System.out.println("Gst Engine Map Null");
                            			throw new Exception("Gst Engine Details Not Found For '"+engineString+"'. Whats app on "+parentPhoneNumber+" for resolution.");
                            		}
                            		else {
                            			engine = gstEngineMap.get(engineString);
                            		}
                            		
                				}
                			}

                			if(hitSecondAsFirstNotWorking && (engine!=null)) {
                				System.out.println("Hitting second engine : "+engineString);
                				responseBody = okHttpSendIdfyTaskProcessorAdvanceGST.getGSTAdvanceDetails(updateOrgDTO.getBusinessId(),engine.getApiKey(),engine.getAccountId());
                			}
                			
                			if(responseBody == null) {
                				throw new Exception("Reponse Body was null. Both GST Engines did not work");
                			}
                			
                    		registerGst = new RegisteredGST();
                    		registerGst.setBusinessId(updateOrgDTO.getBusinessId());
                    		registerGst.setGstResponse(responseBody.toString());
                    		registerGst.setOrigin(engineString);
                    		mainObject = new JSONObject(registerGst.getGstResponse());
                    		registerGstRepository.save(registerGst);
                		}
                		else {
                			throw new Exception("Gst Engine Details Not Found For '"+engineString+"'. Whats app on "+parentPhoneNumber+" for resolution.");
                		}
            		}
            		catch(Exception e) {
            			System.out.println("Error while doing gst verification. This instance will be saved in database.");
            			throw e;
            		}
            	}
            	
            	//Save Gst response
            	
            	if(mainObject != null) {
            		
            		System.out.println("GST Response / mainObject is not null.");
            		
            		JSONObject data = null;
            		
            		
            		if(!hitSecondAsFirstNotWorking) {
            			System.out.println("Filling data from first engine rsponse");
                		data = (JSONObject) mainObject.get("data");
            		}

            		if(hitSecondAsFirstNotWorking) {
            			System.out.println("Filling data from second engine rsponse");
            			JSONObject interim = (JSONObject) mainObject.get("result");
                		data = (JSONObject) interim.get("details");
            		}
            		
            		if(data == null) {
                		throw new Exception("GST details not found. WhatsApp "+parentPhoneNumber+" for support.");
            		}
            		
            		
            		String gstin_status = data.getString("gstin_status").trim();
            		String pan_number = data.getString("pan_number").trim();
            		String legal_name = data.getString("legal_name").trim();
            		String gstin = data.getString("gstin").trim();
            		String business_name = data.getString("business_name").trim();

            		System.out.println("gstin_status : "+gstin_status);
            		System.out.println("legal_name : "+legal_name);
            		System.out.println("gstin : "+gstin);
            		System.out.println("business_name : "+business_name);
            		
            		if(!gstin_status.equalsIgnoreCase("Active"))
            		{
                		throw new Exception("GST Number Not Active. WhatsApp "+parentPhoneNumber+" for support.");
            		}
            		
            		Map<String,Organization> map= 	OrganizationData.workWithAllOrganizationData(business_name,null,"get-one",null);
            		
            		if(map!=null && map.containsKey(business_name))
            			throw new Exception("Organization with same name already registered. WhatsApp "+parentPhoneNumber+" for support.");

            		
            		System.out.println("Getting contact details");
            		JSONObject contact_details = (JSONObject) data.get("contact_details");
            		
            		updateOrgDTO.setOrganization(business_name);
            		updateOrgDTO.setPan(pan_number);
            		updateOrgDTO.setFirstName(legal_name.substring(0, legal_name.indexOf(" ")).trim());
            		updateOrgDTO.setLastName(legal_name.substring(legal_name.indexOf(" ")).trim());
            		
            		
            		Organization organization = new Organization();
            		TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
            		organization.setOrganization(business_name);
            		organization.setBusinessIdentificationNumber(gstin);
            		
            		List<String> allAriApplication = new ArrayList<>();
            		allAriApplication.add(gstin);
            		organization.setAriApplication(allAriApplication);
            		
            		List<String> allAriApplicationDomain = new ArrayList<>();
            		allAriApplication.add("https://localhost");
            		organization.setAriApplication(allAriApplicationDomain);
            		
            		List<String> allAriApplicationPort = new ArrayList<>();
            		allAriApplication.add("8082");
            		organization.setAriApplication(allAriApplicationPort);
            		
            		
            		if(contact_details != null) {
            			
            			System.out.println("Contact details not null");
            			
            			JSONObject principal = (JSONObject) contact_details.get("principal");
            			
            			if(principal != null) {
            				
            				System.out.println("Principal details not null");
            				
            				String address = principal.getString("address").trim();
                    		String email = principal.getString("email").trim();
                    		String mobile = principal.getString("mobile").trim();
                    		String nature_of_business = principal.getString("nature_of_business").trim();
                    		
                    		System.out.println("address : "+address);
                    		System.out.println("email : "+email);
                    		System.out.println("mobile : "+mobile);
                    		System.out.println("nature_of_business : "+address);
                    		String domain = env.getProperty("spring.domain").trim();
                    		

                       		organization.setEmail(email);
                       		updateOrgDTO.setEmail(email);
                       		organization.setDomain(domain);
                       		organization.setSecondDomain(domain);
                    		organization.setPhoneNumber(defaultcountryCode+mobile);
                    		updateOrgDTO.setPhoneNumber(defaultcountryCode+mobile);
                    		updateOrgDTO.setAddress(address);
                    		organization.setAddress(address);
                    		updateOrgDTO.setNatureOfBusiness(nature_of_business);
                    		organization.setNatureOfBusiness(nature_of_business);
                    		organization = OrganizationService.setOrganizationDefault(organization);
                    	    
            			}
            			else {
                    		throw new Exception("Contact details associated with GST not found. WhatsApp "+parentPhoneNumber+" for support.");
            			}
                		
            		}
            		else {
                		throw new Exception("Contact details associated with GST not found. WhatsApp "+parentPhoneNumber+" for support.");
					}
            		
            		organization.setPhoneContext("from-internal");
            	    organization.setTimezone(istTimeZone);
            	    organization.setTotalWhatsAppMessagesAmount(Integer.parseInt(env.getProperty("spring.whatsapp.initial.free.recharge")));
            	    organization.setLastRechargedOn(new Date());
            	    JSONArray menu = new JSONArray();
            	    menu = menuService.generateMenu(menu,updateOrgDTO.getMenuDto());
            		organization.setMenuAccess(menu.toString());
            		
            		System.out.println("Menu : "+ menu.toString());

            		
            		try {
            			//Even org is saved insside this function.
            			//Here on GST is saved.
            			//If some error occurs , GST does not restricts user to re-register.
            			//This is important to make sure data is consistent.
            			toReturn = organizationService.createInitialDataForOrg(updateOrgDTO,organization,parentPhoneNumber);
            		}
            		catch(Exception e) {
            			
            			throw new Exception("Issue reported while creating initial org data. WhatsApp "+parentPhoneNumber+" for support.");
            		}
            	}
            	
        	}
        	else {
        		throw new Exception("Invalid Internal Token");
        	}
         }
        catch(Exception e)
        {
        	//Save gst response error
        	System.out.println("Saving GST Details");
    		RegisteredErroredGST registeredErroredGST = new RegisteredErroredGST();
    		registeredErroredGST.setBusinessId(updateOrgDTO.getBusinessId());
    		registeredErroredGST.setError(e.getMessage());
    		registeredErroredGST.setErrorTrace(e.getLocalizedMessage());
    		registeredErroredGST.setOrigin("Failed");
    		registerGstErrorsRepository.save(registeredErroredGST);
        	
        	System.out.println("*********** Last Error after saving to table****************");
        	
        	e.printStackTrace();
        	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return status(HttpStatus.OK).body(mapper.writeValueAsString(toReturn));
    }

    
    @PostMapping("/createOrgFromBackendWithoutGST")
    public ResponseEntity<String> createOrgFromBackendWithoutGST(@RequestBody UpdateOrgDTO updateOrgDTO) throws Exception {
    	Employee toReturn = null;
    	ObjectMapper mapper = new ObjectMapper();
    	String parentPhoneNumber = env.getProperty("spring.parentorginization.phone");
    	String defaultcountryCode = env.getProperty("spring.parentorginization.phone.country.code");
    	
        try {
        	System.out.println("******************Start Creating Organization Without GST******************");
        	if(updateOrgDTO.getToken().equals("MYLINEHUB100010001")) {	
        		
              	if(updateOrgDTO.getBusinessId() == null)
            	{
            		throw new Exception("Business Id Cannot Be Null");
            	}

            	//Save Gst response
            	
              	String gstin_status = updateOrgDTO.getGstin_status();
        		String legal_name = updateOrgDTO.getLegal_name();
        		String gstin = updateOrgDTO.getGstin();
        		String business_name = updateOrgDTO.getBusiness_name();

        		System.out.println("gstin_status : "+gstin_status);
        		System.out.println("legal_name : "+legal_name);
        		System.out.println("gstin : "+gstin);
        		System.out.println("business_name : "+business_name);
  				
				String address = updateOrgDTO.getAddress();
        		String email = updateOrgDTO.getEmail();
        		String mobile = updateOrgDTO.getPhoneNumber();
        		String nature_of_business = updateOrgDTO.getNatureOfBusiness();
        		
        		System.out.println("address : "+address);
        		System.out.println("email : "+email);
        		System.out.println("mobile : "+mobile);
        		System.out.println("nature_of_business : "+address);
        		String domain = updateOrgDTO.getDomain();
        		
        		
        		Map<String,Organization> map= 	OrganizationData.workWithAllOrganizationData(business_name,null,"get-one",null);
        		
        		if(map!=null && map.containsKey(business_name))
        			throw new Exception("Organization with same name already registered.");
        		
        		Organization organization = new Organization();
        		TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        		organization.setOrganization(business_name);
        		organization.setBusinessIdentificationNumber(gstin);
        		List<String> allAriApplication = new ArrayList<>();
        		allAriApplication.add(gstin);
        		organization.setAriApplication(allAriApplication);
        		

           		organization.setEmail(email);
           		updateOrgDTO.setEmail(email);
           		organization.setDomain(domain);
           		organization.setSecondDomain(domain);
        		organization.setPhoneNumber(defaultcountryCode+mobile);
        		updateOrgDTO.setPhoneNumber(defaultcountryCode+mobile);
        		updateOrgDTO.setAddress(address);
        		organization.setAddress(address);
        		updateOrgDTO.setNatureOfBusiness(nature_of_business);
        		organization.setNatureOfBusiness(nature_of_business);
        		organization = OrganizationService.setOrganizationDefault(organization);
        	    
        		organization.setPhoneContext("from-internal");
        	    organization.setTimezone(istTimeZone);
        	    organization.setTotalWhatsAppMessagesAmount(Integer.parseInt(env.getProperty("spring.whatsapp.initial.free.recharge")));
        	    organization.setLastRechargedOn(new Date());
        	    JSONArray menu = new JSONArray();
        	    menu = menuService.generateMenu(menu,updateOrgDTO.getMenuDto());
        		organization.setMenuAccess(menu.toString());
        		
        		System.out.println("Menu : "+ menu.toString());
        		

        		try {
        			//Even org is saved insside this function.
        			//Here on GST is saved.
        			//If some error occurs , GST does not restricts user to re-register.
        			//This is important to make sure data is consistent.
        			toReturn = organizationService.createInitialDataForOrg(updateOrgDTO,organization,parentPhoneNumber);
        		}
        		catch(Exception e) {
        			
        			throw new Exception("Issue reported while creating initial org data. WhatsApp "+parentPhoneNumber+" for support.");
        		}
        		
            	
        	}
        	else {
        		throw new Exception("Invalid Internal Token");
        	}
         }
        catch(Exception e)
        {
        	//Save gst response error
        	System.out.println("Saving Exception Details");
    		RegisteredErroredGST registeredErroredGST = new RegisteredErroredGST();
    		registeredErroredGST.setBusinessId(updateOrgDTO.getBusinessId());
    		registeredErroredGST.setError(e.getMessage());
    		registeredErroredGST.setErrorTrace(e.getLocalizedMessage());
    		registeredErroredGST.setOrigin("Failed");
    		registerGstErrorsRepository.save(registeredErroredGST);
        	
        	System.out.println("*********** Last Error after saving to table****************");
        	
        	e.printStackTrace();
        	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return status(HttpStatus.OK).body(mapper.writeValueAsString(toReturn));
    }
    
    
    @PostMapping("/getExistingViaInternalToken")
    public ResponseEntity<Organization> getExistingViaInternalToken(@RequestBody UpdateOrgDTO updateOrgDTO) throws Exception {
        try {
        	
        	System.out.println("******************Get Existing Organization******************");
        	
        	if(updateOrgDTO.getToken().equals("MYLINEHUB100010001")) {
        		return status(HttpStatus.OK).body(organizationService.findByBusinessId(updateOrgDTO.getBusinessId()));
        	}
        	else {
            	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        	}
         }
        catch(Exception e)
        {
        	e.printStackTrace();
        	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    
    @PostMapping("/updateExistingViaInternalToken")
    public ResponseEntity<Organization> updateExistingViaInternalToken(@RequestBody UpdateOrgDTO updateOrgDTO) throws Exception {
        try {
        	
        	System.out.println("******************Get Existing Organization******************");
        	
        	if(updateOrgDTO.getToken().equals("MYLINEHUB100010001")) {
        		return status(HttpStatus.OK).body(organizationService.updateSelectiveViaInternalToken(updateOrgDTO));
        	}
        	else {
            	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        	}
         }
        catch(Exception e)
        {
        	e.printStackTrace();
        	return status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
}
