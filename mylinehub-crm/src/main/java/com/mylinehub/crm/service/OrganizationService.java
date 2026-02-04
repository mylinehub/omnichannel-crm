package com.mylinehub.crm.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.dto.OrganizationWorkingDTO;
import com.mylinehub.crm.entity.AmiConnection;
import com.mylinehub.crm.entity.Conference;
import com.mylinehub.crm.entity.Departments;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Ivr;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.entity.Queue;
import com.mylinehub.crm.entity.SshConnection;
import com.mylinehub.crm.entity.dto.BulkUploadEmployeeDto;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.entity.dto.UpdateOrgDTO;
import com.mylinehub.crm.enums.COST_CALCULATION;
import com.mylinehub.crm.enums.PRI_LINE_TYPE;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.excel.BulkUploadEmployeeToDatabase;
import com.mylinehub.crm.mapper.OrganizationMapper;
import com.mylinehub.crm.rag.enums.ParentPromptType;
import com.mylinehub.crm.rag.enums.PromptType;
import com.mylinehub.crm.rag.service.AssistantService;
import com.mylinehub.crm.rag.service.PromptBuilderHeuristicAndEnglishLanguageConvertorService;
import com.mylinehub.crm.rag.service.PromptBuilderSummarizeService;
import com.mylinehub.crm.rag.service.PromptBuilderWhatsAppBotService;
import com.mylinehub.crm.rag.service.SystemPromptService;
import com.mylinehub.crm.repository.AmiConnectionRepository;
import com.mylinehub.crm.repository.ConferenceRepository;
import com.mylinehub.crm.repository.DepartmentsRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.IvrRepository;
import com.mylinehub.crm.repository.OrganizationRepository;
import com.mylinehub.crm.repository.ProductRepository;
import com.mylinehub.crm.repository.QueueRepository;
import com.mylinehub.crm.repository.SshConnectionRepository;
import com.mylinehub.crm.security.CryptoUtils;
import com.mylinehub.crm.whatsapp.dto.WhatsAppOpenAiAccountDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberTemplateDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppPhoneNumberTemplateVariableDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppProjectDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.entity.WhatsAppProject;
import com.mylinehub.crm.whatsapp.enums.COMPONENT_TYPE;
import com.mylinehub.crm.whatsapp.enums.TEMPLATE_VARIABLES;
import com.mylinehub.crm.whatsapp.enums.TEMPLATE_VARIALE_TYPE;
import com.mylinehub.crm.whatsapp.enums.chat.CONVERSATION_TYPE;
import com.mylinehub.crm.whatsapp.service.CreateFileCategoryForOrgService;
import com.mylinehub.crm.whatsapp.service.WhatsAppNumberTemplateVariableService;
import com.mylinehub.crm.whatsapp.service.WhatsAppOpenAiAccountService;
import com.mylinehub.crm.whatsapp.service.WhatsAppPhoneNumberService;
import com.mylinehub.crm.whatsapp.service.WhatsAppPhoneNumberTemplatesService;
import com.mylinehub.crm.whatsapp.service.WhatsAppProjectService;
import com.mylinehub.crm.rag.model.SystemPrompts;
import com.mylinehub.crm.rag.model.AssistantEntity;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class OrganizationService implements CurrentTimeInterface{

	/**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final CreateFileCategoryForOrgService createFileCategoryForOrgService;
	private final EmployeeRepository employeeRepository;
	private final DepartmentsRepository departmentsRepository;
	private final AmiConnectionRepository amiConnectionRepository;
	private final QueueRepository queueRepository;
	private final IvrRepository ivrRepository;
	private final ConferenceRepository conferenceRepository;
	private final SshConnectionRepository sshConnectionRepository;
	private final ProductRepository productRepository;
	private final EmployeeService employeeService;
	private final AdminService adminService;
	private final BCryptPasswordEncoder passwordEncoder;
	private final WhatsAppProjectService whatsAppProjectService;
	private final Environment env;
	private final WhatsAppOpenAiAccountService whatsAppOpenAiAccountService;
	private final WhatsAppPhoneNumberService whatsAppPhoneNumberService;
	private final WhatsAppPhoneNumberTemplatesService whatsAppPhoneNumberTemplatesService;
	private final WhatsAppNumberTemplateVariableService whatsAppNumberTemplateVariableService;
	private final SystemPromptService systemPromptService; // Service with createPrompt(SystemPrompts)
    private final AssistantService assistantService;       // Service with createAssistant(...)
    private final PromptBuilderHeuristicAndEnglishLanguageConvertorService promptBuilderHeuristicAndEnglishLanguageConvertorService;
    private final PromptBuilderWhatsAppBotService promptBuilderWhatsAppBotService;
    private final PromptBuilderSummarizeService promptBuilderSummarizeService;
    private final MenuService menuService;
    
	public static int lastUsedRegistrationExtension = 0;
	
	@Transactional
    public Employee createInitialDataForOrg(UpdateOrgDTO updateOrgDTO, Organization organization,String parentPhoneNumber) throws Exception {
        
		
		Employee toReturn = null;
		String parentOrg = env.getProperty("spring.parentorginization");
		
        try {
        	
        	System.out.println("******************createInitialDataForOrg******************");
        	String domain = env.getProperty("spring.domain");
        	System.out.println("createFirstDepartment"); 
    		Departments department = new Departments();
    		department.setDepartmentName("Information-Technology-COE");
    		department.setOrganization(updateOrgDTO.getOrganization());
    		department.setCity(updateOrgDTO.getAddress());
    		department = departmentsRepository.save(department);
    		
    		
    		
    		System.out.println("createFirstAdminEmployee");
    		Employee newEmployee = new Employee();
    		newEmployee.setUserRole(USER_ROLE.ADMIN);
    		newEmployee.setOrganization(updateOrgDTO.getOrganization());
    		newEmployee.setFirstName(updateOrgDTO.getFirstName());
    		newEmployee.setLastName(updateOrgDTO.getLastName());
    		newEmployee.setDepartment(department);
    		newEmployee.setPhonenumber(updateOrgDTO.getPhoneNumber());
    		newEmployee.setEmail(updateOrgDTO.getEmail());
    		newEmployee.setDomain(domain);
    		newEmployee.setFreeDialOption(false);
    		newEmployee.setTransfer_phone_1("Unassigned");
    		newEmployee.setTransfer_phone_2("Unassigned");
    		newEmployee.setDoc2ImageType("GSTIN : "+updateOrgDTO.getBusinessId());
    
    		//Add extension password, drop email to user.
    		newEmployee.setPassword(passwordEncoder.encode(String.valueOf(env.getProperty("spring.onboarding.initialpassword")))); 
    		newEmployee.setDoc1ImageType("PAN : "+updateOrgDTO.getPan());
        	
    		System.out.println("Setting extension and password");
    		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndStateMap = EmployeeDataAndState.workOnAllEmployeeDataAndState(newEmployee.getExtension(), null, "get");
    		System.out.println("Map size : "+allEmployeeDataAndStateMap.size());
    		
    		if (allEmployeeDataAndStateMap == null || allEmployeeDataAndStateMap.size() <= 10) {
    		    System.out.println("[DEBUG] First user registration, root mylinehub user. Extension set to: 10000001");
    		    lastUsedRegistrationExtension = 9999991;
    		    newEmployee.setExtension("10000001");
    		    newEmployee.setExtensionpassword("mylinehub@10000001");
    		} else if (allEmployeeDataAndStateMap.size() == 11) {
    		    System.out.println("[DEBUG] Second user registration. Extension set to: 11000001");
    		    lastUsedRegistrationExtension = 10999991;
    		    newEmployee.setExtension("11000001");
    		    newEmployee.setExtensionpassword("mylinehub@11000001");
    		} else {
    		    // Find highest keys amongst all
    		    System.out.println("[DEBUG] Normal registration. Calculating new extension...");
    		    if (lastUsedRegistrationExtension != 0) {
    		        System.out.println("[DEBUG] lastUsedRegistrationExtension already available: " + lastUsedRegistrationExtension);
    		        // Do nothing, we already have latest value here
    		    } else {
    		        Set<String> allKeys = allEmployeeDataAndStateMap.keySet();
    		        for (String extension : allKeys) {
    		            try {
    		                int ext = Integer.parseInt(extension);
    		                if (lastUsedRegistrationExtension < ext) {
    		                    lastUsedRegistrationExtension = ext;
    		                }
    		            } catch (Exception e) {
    		                System.out.println("[DEBUG] Extension: " + extension + " could not be parsed, skipped.");
    		            }
    		        }
    		        System.out.println("[DEBUG] Highest existing extension found: " + lastUsedRegistrationExtension);
    		    }

    		    int extensionPrefix = (int) (lastUsedRegistrationExtension / 100) + 1;
    		    int newExtension = (extensionPrefix * 100) + 1;
    		    lastUsedRegistrationExtension = newExtension;

    		    System.out.println("[DEBUG] New extension calculated: " + lastUsedRegistrationExtension);

    		    newEmployee.setExtension(String.valueOf(lastUsedRegistrationExtension));
    		    newEmployee.setExtensionpassword("mylinehub@" + lastUsedRegistrationExtension);
    		}

    		
    		
    		EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
        	Employee verificationEmployee = null;
    		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(newEmployee.getExtension(), null, "get-one");
    		if(allEmployeeDataAndState != null)
    		{
    			employeeDataAndStateDTO = allEmployeeDataAndState.get(newEmployee.getExtension());
    		} 
    		
    		if(employeeDataAndStateDTO != null)
        	{
        		verificationEmployee = employeeDataAndStateDTO.getEmployee();
        	}
    		
    		if(verificationEmployee ==null)
        	{					
    			System.out.println("Verify Employee is null");
				//Employee is to be added
//	        		System.out.println("Employee added");
	        		
	        		//Default values will go here
	        		
    			    newEmployee = BulkUploadEmployeeToDatabase.addEmployeeDefault(newEmployee);
	        		newEmployee = employeeRepository.save(newEmployee);
	        		
	        		System.out.println("Resave Department With Its Manager");
	        		department.setManagers(newEmployee);
	        		department = departmentsRepository.save(department);
	        		
	        		
	        		
	        		try {
	                	System.out.println("******************Saving Organization Details To Database******************");
	                	organization.setSimSelectorRequired(true);
	                	organization.setSimSelector(newEmployee.getExtension());
	                	organization = create(organization);
	                	updateOrgDTO.setUpdates(organization);
	        		}
	        		catch(Exception e) {
	        			
	        			throw new Exception("Issue reported while creating organization. WhatsApp "+parentPhoneNumber+" for support.");
	        		}
	        		
	        		
	        		System.out.println("save first AMI connection");
	        		AmiConnection amiConnection = new AmiConnection();
	        		amiConnection.setAmiuser(String.valueOf(env.getProperty("spring.amiuser")));
	        		amiConnection.setDomain(String.valueOf(env.getProperty("spring.amidomain")));
	        		amiConnection.setIsactive(false);
	        		amiConnection.setOrganization(updateOrgDTO.getOrganization());
	        		amiConnection.setPassword(String.valueOf(env.getProperty("spring.amipassword")));
	        		amiConnection.setPhonecontext(String.valueOf(env.getProperty("spring.phonecontext")));
	        		amiConnection.setPort(Integer.parseInt(env.getProperty("spring.amiport")));
	        		amiConnection = amiConnectionRepository.save(amiConnection);
	        		
	        		
	        		System.out.println("save first SSH connection");
	        		SshConnection sshConnection = new SshConnection();
	        		sshConnection.setOrganization(updateOrgDTO.getOrganization());
	        		sshConnection.setDomain(String.valueOf(env.getProperty("spring.ssh.domain")));
	        		sshConnection.setActive(false);
	        		sshConnection.setPort(String.valueOf(env.getProperty("spring.ssh.port")));
	        		sshConnection.setSshUser(String.valueOf(env.getProperty("spring.ssh.ssh_user")));
	        		sshConnection.setPhonecontext(String.valueOf(env.getProperty("spring.phonecontext")));
	        		sshConnection.setPassword(String.valueOf(env.getProperty("spring.ssh.password")));
	        		sshConnection.setAuthType("");
	        		sshConnection.setConnectionString("");
	        		sshConnection.setType("");
	        		sshConnection = sshConnectionRepository.save(sshConnection);
	        		
	        		int extensionPrefix = (int)(lastUsedRegistrationExtension/100) + 1;
	        		int defaultQueueExtension = (extensionPrefix * 100) + 97;
	        		int defaultConfExtension = (extensionPrefix * 100) + 98;
	        		int defaultIVRExtension = (extensionPrefix * 100) + 99;
	        		System.out.println("Registering default queue");
	        		Queue defaultQueue = new Queue();
	        		defaultQueue.setDomain(domain);
	        		defaultQueue.setExtension(String.valueOf(defaultQueueExtension));
	        		defaultQueue.setIsactive(true);
	        		defaultQueue.setName("Default Queue");
	        		defaultQueue.setOrganization(updateOrgDTO.getOrganization());
	        		defaultQueue.setPhoneContext(String.valueOf(env.getProperty("spring.phonecontext")));
	        		defaultQueue.setProtocol(String.valueOf(env.getProperty("spring.protocol")));
	        		defaultQueue.setType(String.valueOf(env.getProperty("spring.queue.type")));
	        		defaultQueue=queueRepository.save(defaultQueue);
	        		
	        		System.out.println("Registering default ivr");
	        		Ivr defaultIvr = new Ivr();
	        		defaultIvr.setDomain(domain);
	        		defaultIvr.setExtension(String.valueOf(defaultIVRExtension));
	        		defaultIvr.setIsactive(true);
	        		defaultIvr.setName("Default IVR");
	        		defaultIvr.setOrganization(updateOrgDTO.getOrganization());
	        		defaultIvr.setPhoneContext(String.valueOf(env.getProperty("spring.phonecontext")));
	        		defaultIvr.setProtocol(String.valueOf(env.getProperty("spring.protocol")));
	        		defaultIvr=ivrRepository.save(defaultIvr);
	        		
	        		System.out.println("Registering default conference");
	        		Conference defaultConference = new Conference();
	        		defaultConference.setDomain(domain);
	           		defaultConference.setConfextension(String.valueOf(defaultConfExtension));
	           		defaultConference.setIsconferenceactive(true);
	        		defaultConference.setConfname("Default Conference");
	        		defaultConference.setOrganization(updateOrgDTO.getOrganization());
	        		defaultConference.setPhonecontext(String.valueOf(env.getProperty("spring.phonecontext")));
	        		defaultConference.setProtocol(String.valueOf(env.getProperty("spring.protocol")));
	        		defaultConference.setBridge("");
	        		defaultConference.setMenu("Require Setup");
	        		defaultConference.setOwner(String.valueOf(newEmployee.getId()));
	        		defaultConference.setUserprofile("Common");
	        		defaultConference.setIsdynamic(true);
	        		defaultConference.setIsroomactive(false);
	        		defaultConference=conferenceRepository.save(defaultConference);
	        		
	        		
	        		System.out.println("Registering default conference");
	        		Product defaultProduct = new Product();
	        		defaultProduct.setOrganization(updateOrgDTO.getOrganization());
	        		defaultProduct.setName("WhatsApp & Telecommunication");
	        		defaultProduct.setProductType("Internal");
	        		defaultProduct.setUnitsOfMeasure("INR");
	        		defaultProduct.setSellingPrice(2000d);
	        		defaultProduct.setPurchasePrice(1500d);
	        		defaultProduct.setProductStringType("Telecommunication Seat");
	        		defaultProduct.setUnits("Per Seat Per Month");
	        		defaultProduct = productRepository.save(defaultProduct);
	        		
	        		System.out.println("createFirstFacebookProject");
	        		//Create facebook project
	        		WhatsAppProjectDto whatsAppProjectDto = new WhatsAppProjectDto();
	        		whatsAppProjectDto.setAppName(parentOrg);
	        		whatsAppProjectDto.setAppEmail(updateOrgDTO.getEmail());
	        		whatsAppProjectDto.setAppID(env.getProperty("spring.facebook.app.id"));
	        		whatsAppProjectDto.setAppSecret(env.getProperty("spring.facebook.app.secret"));
	        		whatsAppProjectDto.setApiVersion(env.getProperty("spring.facebook.app.version"));
	        		whatsAppProjectDto.setBusinessID(env.getProperty("spring.facebook.app.businessid")); 
	        		whatsAppProjectDto.setBusinessPortfolio(env.getProperty("spring.facebook.app.businessportfolio"));
	        	    whatsAppProjectDto.setAccessToken(env.getProperty("spring.facebook.app.accesstoken"));
	        		whatsAppProjectDto.setClientToken(env.getProperty("spring.facebook.app.clienttoken")); 
	        		whatsAppProjectDto.setOrganization(updateOrgDTO.getOrganization());
	        		WhatsAppProject whatsAppProject = whatsAppProjectService.create(whatsAppProjectDto);
	        		
	        		
	        		System.out.println("Create First OpenAI Account Record");
	        		Date now = new Date();
	        		WhatsAppOpenAiAccountDto whatsAppOpenAiAccountDto = new WhatsAppOpenAiAccountDto();
	        		whatsAppOpenAiAccountDto.setEmail(updateOrgDTO.getEmail());  
	        		whatsAppOpenAiAccountDto.setChatBotName(parentOrg);
	        		whatsAppOpenAiAccountDto.setOrganization(updateOrgDTO.getOrganization());
	        		whatsAppOpenAiAccountDto.setLastUpdatedOn(now.toInstant());
	        		whatsAppOpenAiAccountDto.setProjectID(String.valueOf(whatsAppProject.getId()));
	        		whatsAppOpenAiAccountDto.setKey(env.getProperty("spring.openai.key"));
	        		whatsAppOpenAiAccountDto.setAdminKey(env.getProperty("spring.openai.adminKey"));
	        		whatsAppOpenAiAccountDto.setAssistantID(env.getProperty("spring.openai.assistantID"));
	        		whatsAppOpenAiAccountDto.setChatBotAccess(env.getProperty("spring.openai.chatBotAccess"));
	        		whatsAppOpenAiAccountDto.setClientSecret(env.getProperty("spring.openai.cientSecret"));
	        		whatsAppOpenAiAccountService.create(whatsAppOpenAiAccountDto);
	        		
	        		System.out.println("updateOrgDTO.getOrganization() : "+updateOrgDTO.getOrganization().trim());
	        		System.out.println("parentOrg : "+parentOrg.trim());
	        		if(updateOrgDTO.getOrganization().trim().equalsIgnoreCase(parentOrg.trim())) {
	        			System.out.println("******************It is Parent Organization - Lets setup Whats App Phone / Templates / Vatiables******************");
	        			//Create Phone Number
	        			System.out.println("Creating Phone Number");
	        			WhatsAppPhoneNumberDto whatsAppPhoneNumberDto = new WhatsAppPhoneNumberDto();
	        			whatsAppPhoneNumberDto.setWhatsAppProjectId(whatsAppProject.getId());
	        			whatsAppPhoneNumberDto.setOrganization(updateOrgDTO.getOrganization());
	        			whatsAppPhoneNumberDto.setAdminEmployeeId(newEmployee.getId());
	        			whatsAppPhoneNumberDto.setSecondAdminEmployeeId(null);
	        			whatsAppPhoneNumberDto.setEmployeeExtensionAccessList(null);
	        			whatsAppPhoneNumberDto.setPhoneNumber(env.getProperty("spring.whatsapp.phone"));
	        			whatsAppPhoneNumberDto.setVerifyToken(env.getProperty("spring.whatsapp.verifyToken"));
	        			whatsAppPhoneNumberDto.setPhoneNumberID(env.getProperty("spring.whatsapp.phone.id"));
	        			whatsAppPhoneNumberDto.setWhatsAppAccountID(env.getProperty("spring.whatsapp.whatsAppAccountID"));
	        			whatsAppPhoneNumberDto.setAiModel(env.getProperty("spring.whatsapp.aimodel"));
	        			whatsAppPhoneNumberDto.setCallBackURL(env.getProperty("spring.whatsapp.callBackURL"));
	        			whatsAppPhoneNumberDto.setCallBackSecret(env.getProperty("spring.whatsapp.callBackSecret"));
	        			whatsAppPhoneNumberDto.setCountry(env.getProperty("spring.whatsapp.country"));
	        			whatsAppPhoneNumberDto.setCurrency(env.getProperty("spring.whatsapp.currency"));
	        			whatsAppPhoneNumberDto.setAiCallExtension(String.valueOf(defaultIVRExtension));
	        			whatsAppPhoneNumberDto = whatsAppPhoneNumberService.setDefault(whatsAppPhoneNumberDto);
	        			WhatsAppPhoneNumber whatsAppPhoneNumber = whatsAppPhoneNumberService.create(whatsAppPhoneNumberDto);
	        			
	        			System.out.println("Creating Template & Its Variables");
	        			//Create default Templates and template Variables
	        			System.out.println("Creating 1st template");
	        			WhatsAppPhoneNumberTemplateDto whatsAppPhoneNumberTemplatesDto = new WhatsAppPhoneNumberTemplateDto();
	        			whatsAppPhoneNumberTemplatesDto.setWhatsAppPhoneNumberId(whatsAppPhoneNumber.getId());
	        		    whatsAppPhoneNumberTemplatesDto.setOrganization(updateOrgDTO.getOrganization());
	        		    whatsAppPhoneNumberTemplatesDto.setConversationType(CONVERSATION_TYPE.marketing.name());
	        		    whatsAppPhoneNumberTemplatesDto.setCurrency(env.getProperty("spring.whatsapp.currency"));
	        			whatsAppPhoneNumberTemplatesDto.setTemplateName(env.getProperty("spring.template.onboarding"));
	        			whatsAppPhoneNumberTemplatesDto.setFollowOrder(false);
	        			whatsAppPhoneNumberTemplatesDto.setLanguageCode("en");
	        			whatsAppPhoneNumberTemplatesDto.setProductId(defaultProduct.getId());
	        			WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplate1 = whatsAppPhoneNumberTemplatesService.create(defaultProduct,whatsAppPhoneNumberTemplatesDto);
	        			
	        			System.out.println("Creating 1st template variables");
	        			List<WhatsAppPhoneNumberTemplateVariableDto> variables = new ArrayList<>();
	        			WhatsAppPhoneNumberTemplateVariableDto variable1 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable1.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate1.getId());
	        			variable1.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable1.setOrganization(updateOrgDTO.getOrganization());
	        			variable1.setOrderNumber(1);
	        			variable1.setVariableHeaderType(COMPONENT_TYPE.header.name());
	        			variable1.setVariableName(TEMPLATE_VARIABLES.name.name());
	        			
	        			WhatsAppPhoneNumberTemplateVariableDto variable2 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable2.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate1.getId());
	        			variable2.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable2.setOrganization(updateOrgDTO.getOrganization());
	        			variables.add(variable1);
	        			variable2.setOrderNumber(2);
	        			variable2.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable2.setVariableName(TEMPLATE_VARIABLES.email.name());
	        			variables.add(variable2);
	        			whatsAppNumberTemplateVariableService.update(whatsAppPhoneNumberTemplate1.getId(), updateOrgDTO.getOrganization(), variables);
	        			
	        			
	        			System.out.println("Creating 2nd template");
	        			whatsAppPhoneNumberTemplatesDto.setConversationType(CONVERSATION_TYPE.authentication.name());
	        			whatsAppPhoneNumberTemplatesDto.setTemplateName(env.getProperty("spring.template.passwordrecovery"));
	        			whatsAppPhoneNumberTemplatesDto.setFollowOrder(true);
	        			whatsAppPhoneNumberTemplatesDto.setLanguageCode("en_US");
	        			whatsAppPhoneNumberTemplatesDto.setProductId(defaultProduct.getId());
	        			WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplate2 = whatsAppPhoneNumberTemplatesService.create(defaultProduct,whatsAppPhoneNumberTemplatesDto);
	        			System.out.println("Creating 2nd template variables");
	        			variables = new ArrayList<>();
	        			variable1 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable1.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate2.getId());
	        			variable1.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable1.setOrganization(updateOrgDTO.getOrganization());
	        			variable1.setOrderNumber(1);
	        			variable1.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable1.setVariableName(TEMPLATE_VARIABLES.code.name());
	        			variables.add(variable1);
	        			
	        			variable2 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable2.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate2.getId());
	        			variable2.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable2.setOrganization(updateOrgDTO.getOrganization());
	        			variable2.setOrderNumber(1);
	        			variable2.setVariableHeaderType(COMPONENT_TYPE.button.name());
	        			variable2.setVariableName(TEMPLATE_VARIABLES.code.name());
	        			variables.add(variable2);
	        			
	        			whatsAppNumberTemplateVariableService.update(whatsAppPhoneNumberTemplate2.getId(), updateOrgDTO.getOrganization(), variables);
	        			
	        			
	        			System.out.println("Creating 3rd template");
	        			whatsAppPhoneNumberTemplatesDto.setConversationType(CONVERSATION_TYPE.utility.name());
	        			whatsAppPhoneNumberTemplatesDto.setTemplateName(env.getProperty("spring.template.rechargesuccessful"));
	        			whatsAppPhoneNumberTemplatesDto.setFollowOrder(false);
	        			whatsAppPhoneNumberTemplatesDto.setLanguageCode("en");
	        			whatsAppPhoneNumberTemplatesDto.setProductId(defaultProduct.getId());
	        			WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplate3 = whatsAppPhoneNumberTemplatesService.create(defaultProduct,whatsAppPhoneNumberTemplatesDto);
	        			System.out.println("Creating 3rd template variables");
	        			variables = new ArrayList<>();
	        			variable1 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable1.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate3.getId());
	        			variable1.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable1.setOrganization(updateOrgDTO.getOrganization());
	        			variable1.setOrderNumber(1);
	        			variable1.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable1.setVariableName(TEMPLATE_VARIABLES.emp_name.name());
	        			variables.add(variable1);
	        			
	        			variable2 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable2.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate3.getId());
	        			variable2.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable2.setOrganization(updateOrgDTO.getOrganization());
	        			variable2.setOrderNumber(2);
	        			variable2.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable2.setVariableName(TEMPLATE_VARIABLES.amount.name());
	        			variables.add(variable2);
	        			whatsAppNumberTemplateVariableService.update(whatsAppPhoneNumberTemplate3.getId(), updateOrgDTO.getOrganization(), variables);
	        			
	        			System.out.println("Creating 4th template");
	        			whatsAppPhoneNumberTemplatesDto.setConversationType(CONVERSATION_TYPE.utility.name());
	        			whatsAppPhoneNumberTemplatesDto.setTemplateName(env.getProperty("spring.template.rechargerequired"));
	        			whatsAppPhoneNumberTemplatesDto.setFollowOrder(false);
	        			whatsAppPhoneNumberTemplatesDto.setLanguageCode("en");
	        			whatsAppPhoneNumberTemplatesDto.setProductId(defaultProduct.getId());
	        			WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplate4 = whatsAppPhoneNumberTemplatesService.create(defaultProduct,whatsAppPhoneNumberTemplatesDto);
	        			System.out.println("Creating 4th template variables");
	        			variables = new ArrayList<>();
	        			variable1 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable1.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate4.getId());
	        			variable1.setOrderNumber(1);
	        			variable1.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable1.setVariableName(TEMPLATE_VARIABLES.emp_name.name());
	        			variable1.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable1.setOrganization(updateOrgDTO.getOrganization());
	        			variables.add(variable1);
	        			whatsAppNumberTemplateVariableService.update(whatsAppPhoneNumberTemplate4.getId(), updateOrgDTO.getOrganization(), variables);
	        		
	        			System.out.println("Creating 5th template");
	        			whatsAppPhoneNumberTemplatesDto.setConversationType(CONVERSATION_TYPE.marketing.name());
	        			whatsAppPhoneNumberTemplatesDto.setTemplateName(env.getProperty("spring.template.newUserInfoToAdmin"));
	        			whatsAppPhoneNumberTemplatesDto.setFollowOrder(false);
	        			whatsAppPhoneNumberTemplatesDto.setLanguageCode("en");
	        			whatsAppPhoneNumberTemplatesDto.setProductId(defaultProduct.getId());
	        			WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplate5 = whatsAppPhoneNumberTemplatesService.create(defaultProduct,whatsAppPhoneNumberTemplatesDto);
	        			System.out.println("Creating 5th template variables");
	        			variables = new ArrayList<>();
	        			variable1 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable1.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate5.getId());
	        			variable1.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable1.setOrganization(updateOrgDTO.getOrganization());
	        			variable1.setOrderNumber(1);
	        			variable1.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable1.setVariableName(TEMPLATE_VARIABLES.organization.name());
	        			variables.add(variable1);
	        			variable2 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable2.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate5.getId());
	        			variable2.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable2.setOrganization(updateOrgDTO.getOrganization());
	        			variable2.setOrderNumber(2);
	        			variable2.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable2.setVariableName(TEMPLATE_VARIABLES.emp_name.name());
	        			variables.add(variable2);
	        			WhatsAppPhoneNumberTemplateVariableDto variable3 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable3.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate5.getId());
	        			variable3.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable3.setOrganization(updateOrgDTO.getOrganization());
	        			variable3.setOrderNumber(3);
	        			variable3.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable3.setVariableName(TEMPLATE_VARIABLES.emp_email.name());
	        			variables.add(variable3);
	        			WhatsAppPhoneNumberTemplateVariableDto variable4 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable4.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate5.getId());
	        			variable4.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable4.setOrganization(updateOrgDTO.getOrganization());
	        			variable4.setOrderNumber(4);
	        			variable4.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable4.setVariableName(TEMPLATE_VARIABLES.emp_phone.name());
	        			variables.add(variable4);
	        			WhatsAppPhoneNumberTemplateVariableDto variable5 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable5.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate5.getId());
	        			variable5.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable5.setOrganization(updateOrgDTO.getOrganization());
	        			variable5.setOrderNumber(5);
	        			variable5.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable5.setVariableName(TEMPLATE_VARIABLES.businessdesc.name());
	        			variables.add(variable5);
	        			whatsAppNumberTemplateVariableService.update(whatsAppPhoneNumberTemplate5.getId(), updateOrgDTO.getOrganization(), variables);
	        			
	        			
	        			System.out.println("Creating 6th template");
	        			whatsAppPhoneNumberTemplatesDto.setConversationType(CONVERSATION_TYPE.utility.name());
	        			whatsAppPhoneNumberTemplatesDto.setTemplateName(env.getProperty("spring.template.accountDeactivated"));
	        			whatsAppPhoneNumberTemplatesDto.setFollowOrder(false);
	        			whatsAppPhoneNumberTemplatesDto.setLanguageCode("en");
	        			whatsAppPhoneNumberTemplatesDto.setProductId(defaultProduct.getId());
	        			WhatsAppPhoneNumberTemplates whatsAppPhoneNumberTemplate6 = whatsAppPhoneNumberTemplatesService.create(defaultProduct,whatsAppPhoneNumberTemplatesDto);
	        			
	        			System.out.println("Creating 6th template variable");
	        			variables = new ArrayList<>();
	        			variable1 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable1.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate6.getId());
	        			variable1.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable1.setOrganization(updateOrgDTO.getOrganization());
	        			variable1.setOrderNumber(1);
	        			variable1.setVariableHeaderType(COMPONENT_TYPE.header.name());
	        			variable1.setVariableName(TEMPLATE_VARIABLES.organization.name());
	        			variables.add(variable1);
	        			
	        			variable2 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable2.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate6.getId());
	        			variable2.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable2.setOrganization(updateOrgDTO.getOrganization());
	        			variable2.setOrderNumber(2);
	        			variable2.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable2.setVariableName(TEMPLATE_VARIABLES.parentorg.name());
	        			variables.add(variable2);
	        			
	        			variable3 = new WhatsAppPhoneNumberTemplateVariableDto();
	        			variable3.setWhatsAppPhoneNumberTemplateId(whatsAppPhoneNumberTemplate6.getId());
	        			variable3.setVariableType(TEMPLATE_VARIALE_TYPE.text.name());
	        			variable3.setOrganization(updateOrgDTO.getOrganization());
	        			variable3.setOrderNumber(3);
	        			variable3.setVariableHeaderType(COMPONENT_TYPE.body.name());
	        			variable3.setVariableName(TEMPLATE_VARIABLES.reason.name());
	        			variables.add(variable3);
	        			whatsAppNumberTemplateVariableService.update(whatsAppPhoneNumberTemplate6.getId(), updateOrgDTO.getOrganization(), variables);
	        			
	        			//Below is commented as data go in memory because it is in service classes
	        			//System.out.println("******************Adding Phonenumber , template & Variable to memory******************");
	        			//Add Templates and variables to memory
	        			//whatsAppPhoneNumberService.resetPhoneMemoryDataAsPerPhoneNumber(whatsAppPhoneNumber.getPhoneNumber());
	        		}
	        		
	        		//Add employee to memory
	        		//Send Application STOMP Notification
	        		//Send Email		        		
	        		//Send Welcome notification to employee
	        		//Send Welcome Whats App message to N
	        		//Send Whats App to Employee
	        		//Send Admin Trigger for email and whats app
	        		System.out.println("****************** Send New User Notification ******************");
	        		BulkUploadEmployeeDto bulkUploadEmployeeDto = new BulkUploadEmployeeDto();
	        		bulkUploadEmployeeDto.setActualPassword(env.getProperty("spring.onboarding.initialpassword"));
	        		bulkUploadEmployeeDto.setEmployee(newEmployee);
	        		employeeService.newUserCreationAfterTriggers(bulkUploadEmployeeDto);
	        		
	        		System.out.println("****************** Create Prompt And Assistant ******************");
	        		//String model = env.getProperty("openai.model");
	        		//Create initial system prompts and assistants
	        		//initializeUserSystemPromptsAndAssistants(updateOrgDTO.getOrganization(),updateOrgDTO.getNatureOfBusiness(),model);
	        		
	        		//if(updateOrgDTO.getOrganization().trim().equalsIgnoreCase(parentOrg.trim())) {
	        			System.out.println("******************It is Parent Organization : Setting System Prompt / Assistant******************");
	        			//initializeParentSystemPromptsAndAssistants(updateOrgDTO.getOrganization(),model);
	        		//}
	        		
	        		toReturn = newEmployee;
        	}    	
         }
        catch(Exception e)
        {
        	e.printStackTrace();
        	return toReturn;
        }
        
        return toReturn;
    }
    
    
    /**
     * Initialize all system prompts and assistants for a given organization.
     *
     * @param organization The organization name
     * @param orgServices  Description of org services (used for WhatsApp prompts)
     * @param model        LLM model name for assistant
     */
    public void initializeUserSystemPromptsAndAssistants(String organization, String orgServices, String model) {
        for (PromptType type : PromptType.values()) {

            System.out.println("Processing PromptType: " + type.name());

            // -------------------------------
            // 1 Build system prompt text
            // -------------------------------
            String promptText = buildSystemPromptText(type, organization, orgServices);

            // -------------------------------
            // 2 Create and save SystemPrompt
            // -------------------------------
            SystemPrompts systemPrompt = new SystemPrompts();
            systemPrompt.setType(type.name());       // Enum name as type
            systemPrompt.setPrompt(promptText);      // Generated prompt text
            systemPrompt.setActive(true);            // Always active
            systemPrompt.setOrganization(organization); // Organization
            SystemPrompts savedPrompt = systemPromptService.createPrompt(systemPrompt);

            System.out.println("Saved SystemPrompt with ID: " + savedPrompt.getId());

            // -------------------------------
            // 3 Create Assistant linked to this SystemPrompt
            // -------------------------------
            try {
                String assistantName = assistantService.buildAssistantName(type.name(),organization); // e.g., "language Assistant"
                String instructions = savedPrompt.getPrompt();

                AssistantEntity assistant = assistantService.createAssistant(
                        assistantName,
                        organization,
                        String.valueOf(savedPrompt.getId()),
                        instructions,
                        model
                );

                System.out.println("Created Assistant: " + assistant.getName() + " with ID: " + assistant.getId());

            } catch (Exception e) {
                System.err.println("Failed to create assistant for PromptType " + type.name());
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * Initialize all parent system prompts such as for langauge which are common for all org and assistants for a given organization.
     *
     * @param organization The organization name
     * @param orgServices  Description of org services (used for WhatsApp prompts)
     * @param model        LLM model name for assistant
     */
    public void initializeParentSystemPromptsAndAssistants(String organization, String model) {
        for (ParentPromptType type : ParentPromptType.values()) {

            System.out.println("Processing PromptType: " + type.name());

            // -------------------------------
            // 1 Build system prompt text
            // -------------------------------
            String promptText = buildParentSystemPromptText(type);

            // -------------------------------
            // 2 Create and save SystemPrompt
            // -------------------------------
            SystemPrompts systemPrompt = new SystemPrompts();
            systemPrompt.setType(type.name());       // Enum name as type
            systemPrompt.setPrompt(promptText);      // Generated prompt text
            systemPrompt.setActive(true);            // Always active
            systemPrompt.setOrganization(organization); // Organization
            SystemPrompts savedPrompt = systemPromptService.createPrompt(systemPrompt);

            System.out.println("Saved SystemPrompt with ID: " + savedPrompt.getId());

            // -------------------------------
            // 3 Create Assistant linked to this SystemPrompt
            // -------------------------------
            try {
                String assistantName = assistantService.buildAssistantName(type.name(),organization); // e.g., "language Assistant"
                String instructions = savedPrompt.getPrompt();
                
                AssistantEntity assistant = assistantService.createAssistant(
                        assistantName,
                        organization,
                        String.valueOf(savedPrompt.getId()),
                        instructions,
                        model
                );

                System.out.println("Created Assistant: " + assistant.getName() + " with ID: " + assistant.getId());

            } catch (Exception e) {
                System.err.println("Failed to create assistant for PromptType " + type.name());
                e.printStackTrace();
            }
        }
    }
    

    /**
     * Build system prompt text for a given PromptType.
     *
     * @param type         PromptType enum
     * @param organization Organization name
     * @param orgServices  Description of org services (used for WhatsApp)
     * @return Prompt text string
     */
    private String buildSystemPromptText(PromptType type, String organization, String orgServices) {
	        switch (type) {
	            case whatsappbot:
	                return promptBuilderWhatsAppBotService.buildPrompt(organization, orgServices);
	            default:
	                throw new IllegalArgumentException("Unsupported PromptType: " + type);
	        }
	 }
	
    
    /**
     * Build system prompt text for a given PromptType.
     *
     * @param type         PromptType enum
     * @param organization Organization name
     * @param orgServices  Description of org services (used for WhatsApp)
     * @return Prompt text string
     */
    private String buildParentSystemPromptText(ParentPromptType type) {
	        switch (type) {
	            case heuristicAndLanguageDetector:
	                return promptBuilderHeuristicAndEnglishLanguageConvertorService.buildPrompt();
	            case summarize:
	                return promptBuilderSummarizeService.buildPrompt();   
	            default:
	                throw new IllegalArgumentException("Unsupported PromptType: " + type);
	        }
	 }
    

    public boolean loadNewOrganizationData(Organization organization) {
        System.out.println("Inside loadNewOrganizationData()");
        boolean toReturn = false;

        try {
            System.out.println("Updating static organization data for: " + organization.getOrganization());
            OrganizationData.workWithAllOrganizationData(organization.getOrganization(), organization, "update", null);

            System.out.println("Creating WhatsApp base category for: " + organization.getOrganization());
            createFileCategoryForOrgService.createBaseCategoryForWhatsAppOrgData(organization.getOrganization());

            toReturn = true;
            System.out.println("Organization data loaded successfully for: " + organization.getOrganization());
        } catch (Exception e) {
            System.out.println("Exception in loadNewOrganizationData()");
            e.printStackTrace();
            throw e;
        }

        return toReturn;
    }

     public boolean updateOrganizationMemoryData(Organization organization) {
        System.out.println("Inside loadNewOrganizationData()");
        boolean toReturn = false;

        try {
            System.out.println("Updating static organization data for: " + organization.getOrganization());
            OrganizationData.workWithAllOrganizationData(organization.getOrganization(), organization, "update", null);
            toReturn = true;
            System.out.println("Organization data updated successfully for: " + organization.getOrganization());
        } catch (Exception e) {
            System.out.println("Exception in loadNewOrganizationData()");
            e.printStackTrace();
            throw e;
        }

        return toReturn;
    }
    
    /**
     * The method is to create organization.
     *
     * @return created Organization
     * @throws Exception if validation fails
     */
    public Organization create(Organization organization) throws Exception {
        System.out.println("Inside create() for organization: " + organization.getOrganization());

        if (organization.getBusinessIdentificationNumber() == null) {
            System.out.println("Business identification number is null");
            throw new Exception("Cannot create business where identification number is null");
        }

        Organization existingOrg = organizationRepository.findByBusinessIdentificationNumber(organization.getBusinessIdentificationNumber());

        if (existingOrg != null) {
            System.out.println("Organization with business ID already exists: " + organization.getBusinessIdentificationNumber());
            throw new Exception("Cannot create business whose identification number is already present");
        }

        organization = organizationRepository.save(organization);
        System.out.println("Organization saved to DB: " + organization.getOrganization());

        loadNewOrganizationData(organization);

        return organization;
    }

    /**
     * The method is to update organization.
     *
     * @return updated Organization
     * @throws Exception if validation fails
     */
    public Organization update(Organization organization) throws Exception {
        System.out.println("Inside update() for organization: " + organization.getOrganization());

        if (organization.getBusinessIdentificationNumber() == null) {
            System.out.println("Business identification number is null");
            throw new Exception("Cannot update business where identification number is null");
        }

        Organization organizationFromDatabase = organizationRepository.findByBusinessIdentificationNumber(organization.getBusinessIdentificationNumber());

        if (organizationFromDatabase == null) {
            System.out.println("Organization not found with business ID: " + organization.getBusinessIdentificationNumber());
            throw new Exception("Cannot update business whose identification number is not present");
        }

        System.out.println("Mapping organization fields...");
        organizationMapper.updateOrgToOrg(organization, organizationFromDatabase);

        
        organization = organizationRepository.save(organizationFromDatabase);
        System.out.println("Organization updated in DB: " + organization.getOrganization());

        updateOrganizationMemoryData(organization);

        return organization;
    }

    
    @Transactional
    public Organization updateSelectiveViaInternalToken(UpdateOrgDTO dto) throws Exception {

        if (dto == null) throw new Exception("UpdateOrgDTO is null");

        String businessId = dto.getBusinessId();
        if (businessId == null || businessId.trim().isEmpty()) {
            throw new Exception("businessId is required");
        }

        Organization orgDb = organizationRepository.findByBusinessIdentificationNumber(businessId);
        if (orgDb == null) throw new Exception("Organization not found for businessId=" + businessId);

        boolean updatedTotalAmount = false;
        boolean updatedWhatsappAmount = false;

        // -----------------------------
        // Strings
        // -----------------------------
        if (dto.getPhoneContext() != null) {
            logChange("phoneContext", orgDb.getPhoneContext(), dto.getPhoneContext());
            orgDb.setPhoneContext(dto.getPhoneContext());
        }

        if (dto.getCostCalculation() != null) {
            logChange("costCalculation", orgDb.getCostCalculation(), dto.getCostCalculation());
            orgDb.setCostCalculation(dto.getCostCalculation());
        }

        if (dto.getTrunkNamesPrimary() != null) {
            logChange("trunkNamesPrimary", orgDb.getTrunkNamesPrimary(), dto.getTrunkNamesPrimary());
            orgDb.setTrunkNamesPrimary(dto.getTrunkNamesPrimary());
        }

        if (dto.getTrunkNamesSecondary() != null) {
            logChange("trunkNamesSecondary", orgDb.getTrunkNamesSecondary(), dto.getTrunkNamesSecondary());
            orgDb.setTrunkNamesSecondary(dto.getTrunkNamesSecondary());
        }

        if (dto.getUseSecondaryAllotedLine() != null) {
            logChange("useSecondaryAllotedLine", orgDb.isUseSecondaryAllotedLine(), dto.getUseSecondaryAllotedLine());
            orgDb.setUseSecondaryAllotedLine(dto.getUseSecondaryAllotedLine());
        }

        if (dto.getSecondDomain() != null) {
            logChange("secondDomain", orgDb.getSecondDomain(), dto.getSecondDomain());
            orgDb.setSecondDomain(dto.getSecondDomain());
        }

        if (dto.getPhoneTrunk() != null) {
            logChange("phoneTrunk", orgDb.getPhoneTrunk(), dto.getPhoneTrunk());
            orgDb.setPhoneTrunk(dto.getPhoneTrunk());
        }

        if (dto.getMenuAccess() != null) {
            logChange("menuAccess", orgDb.getMenuAccess(), dto.getMenuAccess());
            orgDb.setMenuAccess(dto.getMenuAccess());
        }

        if (dto.getProtocol() != null) {
            logChange("protocol", orgDb.getProtocol(), dto.getProtocol());
            orgDb.setProtocol(dto.getProtocol());
        }

        if (dto.getSipPath() != null) {
            logChange("sipPath", orgDb.getSipPath(), dto.getSipPath());
            orgDb.setSipPath(dto.getSipPath());
        }

        if (dto.getPriLineType() != null) {
            logChange("priLineType", orgDb.getPriLineType(), dto.getPriLineType());
            orgDb.setPriLineType(dto.getPriLineType());
        }

        if (dto.getAriApplication() != null) {
            logChange("ariApplication", orgDb.getAriApplication(), dto.getAriApplication());
            orgDb.setAriApplication(dto.getAriApplication());
        }
        
        if (dto.getAriApplicationDomain() != null) {
            logChange("ariApplicationDomain", orgDb.getAriApplicationDomain(), dto.getAriApplicationDomain());
            orgDb.setAriApplicationDomain(dto.getAriApplicationDomain());
        }
        
        if (dto.getAriApplicationPort() != null) {
            logChange("ariApplicationPort", orgDb.getAriApplicationPort(), dto.getAriApplicationPort());
            orgDb.setAriApplicationPort(dto.getAriApplicationPort());
        }

        if (dto.getAiCallChargeType() != null) {
            logChange("aiCallChargeType", orgDb.getAiCallChargeType(), dto.getAiCallChargeType());
            orgDb.setAiCallChargeType(dto.getAiCallChargeType());
        }

        // -----------------------------
        // Integers
        // -----------------------------
        if (dto.getCallLimit() != null) {
            logChange("callLimit", orgDb.getCallLimit(), dto.getCallLimit());
            orgDb.setCallLimit(dto.getCallLimit());
        }

        if (dto.getWhatsAppMessageLimit() != null) {
            logChange("whatsAppMessageLimit", orgDb.getWhatsAppMessageLimit(), dto.getWhatsAppMessageLimit());
            orgDb.setWhatsAppMessageLimit(dto.getWhatsAppMessageLimit());
        }

        if (dto.getAllowedEmbeddingConversion() != null) {
            logChange("allowedEmbeddingConversion", orgDb.getAllowedEmbeddingConversion(), dto.getAllowedEmbeddingConversion());
            orgDb.setAllowedEmbeddingConversion(dto.getAllowedEmbeddingConversion());
        }

        if (dto.getConsumedEmbeddingConversion() != null) {
            logChange("consumedEmbeddingConversion", orgDb.getConsumedEmbeddingConversion(), dto.getConsumedEmbeddingConversion());
            orgDb.setConsumedEmbeddingConversion(dto.getConsumedEmbeddingConversion());
        }

        if (dto.getAllowedUsers() != null) {
            logChange("allowedUsers", orgDb.getAllowedUsers(), dto.getAllowedUsers());
            orgDb.setAllowedUsers(dto.getAllowedUsers());
        }

        if (dto.getSipPort() != null) {
            logChange("sipPort", orgDb.getSipPort(), dto.getSipPort());
            orgDb.setSipPort(dto.getSipPort());
        }

        if (dto.getAiCallChargeAmount() != null) {
            logChange("aiCallChargeAmount", orgDb.getAiCallChargeAmount(), dto.getAiCallChargeAmount());
            orgDb.setAiCallChargeAmount(dto.getAiCallChargeAmount());
        }

        // -----------------------------
        // Doubles
        // -----------------------------
        if (dto.getAllowedUploadInMB() != null) {
            logChange("allowedUploadInMB", orgDb.getAllowedUploadInMB(), dto.getAllowedUploadInMB());
            orgDb.setAllowedUploadInMB(dto.getAllowedUploadInMB());
        }

        if (dto.getCurrentUploadInMB() != null) {
            logChange("currentUploadInMB", orgDb.getCurrentUploadInMB(), dto.getCurrentUploadInMB());
            orgDb.setCurrentUploadInMB(dto.getCurrentUploadInMB());
        }

        if (dto.getCallingTotalAmountLoaded() != null) {
            logChange("totalAmount", orgDb.getCallingTotalAmountLoaded(), dto.getCallingTotalAmountLoaded());
            orgDb.setCallingTotalAmountLoaded(orgDb.getCallingTotalAmountLoaded()+dto.getCallingTotalAmountLoaded());
        }

        if (dto.getTotalWhatsAppMessagesAmount() != null) {
            logChange("totalWhatsAppMessagesAmount",
                    orgDb.getTotalWhatsAppMessagesAmount(),
                    orgDb.getTotalWhatsAppMessagesAmount()+dto.getTotalWhatsAppMessagesAmount());
            orgDb.setTotalWhatsAppMessagesAmount(orgDb.getTotalWhatsAppMessagesAmount()+dto.getTotalWhatsAppMessagesAmount());
        }

        // -----------------------------
        // Booleans
        // -----------------------------
        if (dto.getAllowWhatsAppAutoAIMessage() != null) {
            logChange("allowWhatsAppAutoAIMessage",
                    orgDb.isAllowWhatsAppAutoAIMessage(),
                    dto.getAllowWhatsAppAutoAIMessage());
            orgDb.setAllowWhatsAppAutoAIMessage(dto.getAllowWhatsAppAutoAIMessage());
        }

        if (dto.getAllowWhatsAppAutoMessage() != null) {
            logChange("allowWhatsAppAutoMessage",
                    orgDb.isAllowWhatsAppAutoMessage(),
                    dto.getAllowWhatsAppAutoMessage());
            orgDb.setAllowWhatsAppAutoMessage(dto.getAllowWhatsAppAutoMessage());
        }

        if (dto.getAllowWhatsAppCampaignMessage() != null) {
            logChange("allowWhatsAppCampaignMessage",
                    orgDb.isAllowWhatsAppCampaignMessage(),
                    dto.getAllowWhatsAppCampaignMessage());
            orgDb.setAllowWhatsAppCampaignMessage(dto.getAllowWhatsAppCampaignMessage());
        }

        if (dto.getRagSet() != null) {
            logChange("ragSet", orgDb.isRagSet(), dto.getRagSet());
            orgDb.setRagSet(dto.getRagSet());
        }

        if (dto.getEnableFileUpload() != null) {
            logChange("enableFileUpload", orgDb.isEnableFileUpload(), dto.getEnableFileUpload());
            orgDb.setEnableFileUpload(dto.getEnableFileUpload());
        }

        if (dto.getEnableEmployeeCreation() != null) {
            logChange("enableEmployeeCreation", orgDb.isEnableEmployeeCreation(), dto.getEnableEmployeeCreation());
            orgDb.setEnableEmployeeCreation(dto.getEnableEmployeeCreation());
        }

        if (dto.getEnableCalling() != null) {
            logChange("enableCalling", orgDb.isEnableCalling(), dto.getEnableCalling());
            orgDb.setEnableCalling(dto.getEnableCalling());
        }

        if (dto.getEnableInternalMessaging() != null) {
            logChange("enableInternalMessaging",
                    orgDb.isEnableInternalMessaging(),
                    dto.getEnableInternalMessaging());
            orgDb.setEnableInternalMessaging(dto.getEnableInternalMessaging());
        }

        if (dto.getEnableWhatsAppMessaging() != null) {
            logChange("enableWhatsAppMessaging",
                    orgDb.isEnableWhatsAppMessaging(),
                    dto.getEnableWhatsAppMessaging());
            orgDb.setEnableWhatsAppMessaging(dto.getEnableWhatsAppMessaging());
        }

        if (dto.getActivated() != null) {
            logChange("activated", orgDb.isActivated(), dto.getActivated());
            orgDb.setActivated(dto.getActivated());
        }

        // -----------------------------
        // MENU REGEN
        // -----------------------------
        boolean shouldRegenMenu =
                (dto.getMenuDto() != null) ||
                dto.getUpdateMenu();

        if (shouldRegenMenu && dto.getMenuDto() != null) {
            JSONArray menu = new JSONArray();
            menu = menuService.generateMenu(menu, dto.getMenuDto());
            logChange("menuAccess", orgDb.getMenuAccess(), menu.toString());
            orgDb.setMenuAccess(menu.toString());
        }

        // -----------------------------
        // Recharge date
        // -----------------------------
        if (updatedTotalAmount || updatedWhatsappAmount) {
            logChange("lastRechargedOn", orgDb.getLastRechargedOn(), new Date());
            orgDb.setLastRechargedOn(new Date());
        }

        Organization saved = organizationRepository.save(orgDb);

        updateOrganizationMemoryData(saved);

        // -----------------------------
        // Notifications (unchanged)
        // -----------------------------
        if (updatedTotalAmount || updatedWhatsappAmount) {

            String rechargeSuccessfulTemplate = env.getProperty("spring.template.rechargesuccessful");

            Employee organizationAdmin = new Employee();
            organizationAdmin.setEmail(saved.getEmail());
            organizationAdmin.setFirstName(saved.getOrganization());
            organizationAdmin.setPhonenumber(saved.getPhoneNumber());
            organizationAdmin.setOrganization(saved.getOrganization());
            organizationAdmin.setLastName("");

            BulkUploadEmployeeDto bulkUploadEmployeeDto = new BulkUploadEmployeeDto();
            bulkUploadEmployeeDto.setActualPassword("");
            bulkUploadEmployeeDto.setEmployee(organizationAdmin);

            if (updatedWhatsappAmount && rechargeSuccessfulTemplate != null) {
                employeeService.sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(
                        bulkUploadEmployeeDto,
                        rechargeSuccessfulTemplate,
                        String.valueOf(dto.getTotalWhatsAppMessagesAmount()),
                        "0",
                        "url"
                );

                adminService.sendRechargeDoneEmail(
                        saved.getOrganization(),
                        saved.getEmail(),
                        String.valueOf(dto.getTotalWhatsAppMessagesAmount())
                );
            }

            if (updatedTotalAmount && rechargeSuccessfulTemplate != null) {
                employeeService.sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(
                        bulkUploadEmployeeDto,
                        rechargeSuccessfulTemplate,
                        String.valueOf(dto.getCallingTotalAmountLoaded()),
                        "0",
                        "url"
                );

                adminService.sendRechargeDoneEmail(
                        saved.getOrganization(),
                        saved.getEmail(),
                        String.valueOf(dto.getCallingTotalAmountLoaded())
                );
            }
        }

        return saved;
    }

    private void logChange(String field, Object oldVal, Object newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            System.out.println("[ORG-UPDATE] " + field + " : " + oldVal + "  ->  " + newVal);
        }
    }
    
    /**
     * Retrieve all organizations from the database.
     *
     * @return list of all organizations
     */
    public List<Organization> getAll() {
        System.out.println("Fetching all organizations from DB...");
        return organizationRepository.findAll();
    }

    
    
    /**
     * The method is to retrieve all organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */

    public Organization findByName(String organization) {
        System.out.println("Fetching organization by name: " + organization);
        return organizationRepository.findByOrganization(organization);
    }

    /**
     * The method is to retrieve all organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */

    public Organization findByBusinessId(String businessId) {
        System.out.println("Fetching organization by business ID: " + businessId);
        return organizationRepository.findByBusinessIdentificationNumber(businessId);
    }

    /**
     * The method is to retrieve all organization from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */

    public List<Organization> getOrganizationData() {
        System.out.println("Fetching all organizations...");
        return organizationRepository.findAll();
    }

    /**
     * The method is to save all organizations from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */

    public void saveAll(List<Organization> allOrganizations) {
        try {
            System.out.println("Saving all organizations: Count = " + (allOrganizations != null ? allOrganizations.size() : 0));
            organizationRepository.saveAll(allOrganizations);
            System.out.println("Organizations saved successfully.");
        } catch (Exception e) {
            System.out.println("Exception occurred while saving organizations.");
            e.printStackTrace();
        }
    }

    public static Organization setOrganizationDefault(Organization organization) {
        System.out.println("Setting default values for organization: " + organization.getOrganization());

        organization.setWhatsAppMediaFolder("WhatsApp");
        organization.setWhatsAppMediaFolderImage("src/main/resources/images/icon/whatsapp.png");
        organization.setWhatsAppMediaFolderImageType("image/png");
        organization.setWhatsAppMediaFolderImageName("whatsapp.png");
        organization.setAllowedUploadInMB(50L);
        organization.setCurrentUploadInMB(0L);
        organization.setAllowedEmbeddingConversion(1);
        organization.setConsumedEmbeddingConversion(0);
        organization.setAllowedUsers(21);
        organization.setProtocol("PJSIP//");
        organization.setSipPort(5060);
        organization.setSipPath("//");
        organization.setPriLineType(PRI_LINE_TYPE.indirectToFreePBX.name());
        organization.setCostCalculation(COST_CALCULATION.UNLIMITED.name());
        organization.setWhatsAppMessageLimit(-1);
        organization.setAllowWhatsAppAutoAIMessage(true);
        organization.setAllowWhatsAppAutoMessage(true);
        organization.setAllowWhatsAppCampaignMessage(true);
        organization.setCallLimit(-1);
        organization.setActivated(true);
        organization.setLastRechargedOn(new Date());
        organization.setEnableWhatsAppMessaging(true);
        organization.setAiCallChargeAmount(3);
        organization.setAiCallChargeType("minute");
        organization.setSimSelectorRequired(false);
        organization.setSimSelector("");
        System.out.println("Default values set for organization: " + organization.getOrganization());
        return organization;
    }

    public boolean isValidToken(String token) {
        boolean toReturn = false;
        try {
            System.out.println("Validating token...");
            String decryptedTextString = CryptoUtils.decrypt(token);
            System.out.println("Decrypted Token: " + decryptedTextString);

            String[] valueString = decryptedTextString.split(CryptoUtils.DELIMITER);

            if (valueString.length != 2) {
                System.out.println("Invalid token format: expected 2 parts, got " + valueString.length);
                return false;
            }

            String dateString = valueString[0];
            String verifyToken = valueString[1];

            System.out.println("Parsed dateString: " + dateString);
            System.out.println("Parsed verifyToken: " + verifyToken);

            if (!verifyToken.equals(CryptoUtils.VERIFYTOKEN)) {
                System.out.println("Verify token does not match expected token.");
                return false;
            }

            toReturn = isToday(Long.parseLong(dateString));
            System.out.println("isToday result: " + toReturn);

        } catch (Exception e) {
            System.out.println("Exception occurred during token validation.");
            e.printStackTrace();
            toReturn = false;
        }

        return toReturn;
    }

    public String getStringBeforeSubstring(String text, String substring) {
        System.out.println("Getting string before substring. Text: " + text + ", Substring: " + substring);
        int index = text.indexOf(substring);
        if (index == -1) {
            System.out.println("Substring not found. Returning original text.");
            return text; // Substring not found, return the original string
        } else {
            String result = text.substring(0, index);
            System.out.println("Result: " + result);
            return result;
        }
    }

    public boolean isToday(Long dateValue) {
        try {
            System.out.println("Checking if date is today. Millis: " + dateValue);
            Date date = new Date(dateValue);

            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate today = LocalDate.now();

            System.out.println("LocalDate: " + localDate + ", Today: " + today);
            boolean result = localDate.isEqual(today);
            System.out.println("Comparison result: " + result);

            return result;
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected error during date comparison.");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deduct AI call charge from in-memory OrganizationData.
     * Charge is calculated using Organization.aiCallChargeAmount + Organization.aiCallChargeType
     * and the given call duration.
     *
     * @param organizationName org key used in OrganizationData map
     * @param callDurationSeconds call duration in seconds (>= 0)
     * @return true if deducted successfully, false otherwise
     */
    public Double calculateCallAmunt(String organizationName, long callDurationSeconds, Boolean dynamicCost,Long callCost,String callCostMode) {

        try {
            if (organizationName == null || organizationName.trim().isEmpty()) {
                System.out.println("deductAiChargeForCallInMemory invalid orgName");
                return 0D;
            }
            if (callDurationSeconds < 0) {
                System.out.println("deductAiChargeForCallInMemory invalid callDurationSeconds=" + callDurationSeconds);
                return 0D;
            }

            String orgKey = organizationName.trim();

            // 1) Fetch org from memory (no DB)
            Map<String, Organization> one = OrganizationData.workWithAllOrganizationData(
                    orgKey, null, "get-one", null
            );

            if (one == null || one.get(orgKey) == null) {
                System.out.println("deductAiChargeForCallInMemory org not found in memory: " + orgKey);
                return 0D;
            }

            Organization org = one.get(orgKey);

            long chargeAmount = org.getAiCallChargeAmount();     // e.g. 10
            String chargeType = org.getAiCallChargeType();      // "call" / "minute" / etc.

            if(dynamicCost) {
            	 chargeAmount = callCost;     // e.g. 10
                 chargeType = callCostMode;      // "call" / "minute" / etc.
            }
            
            if (chargeAmount <= 0) {
                System.out.println("deductAiChargeForCallInMemory chargeAmount<=0; skipping. org=" + orgKey);
                return 0D; // nothing to deduct
            }

            // 2) Compute minutes (ceil to next minute)
            long minutes = (callDurationSeconds + 59) / 60; // 0.., ceil
            if (minutes < 1) minutes = 1; // at least 1 minute if you want billing for short calls

            double deductAmount;

            String t = (chargeType == null ? "" : chargeType.trim().toLowerCase());

            if (t.equals("minute")) {
                deductAmount = chargeAmount * (double) minutes;
            } 
            else {
                // default: per call
                deductAmount = chargeAmount;
            }


            System.out.println("calculateAmountToBeDeducted for org=" + orgKey
                    + " type=" + t
                    + " amount=" + chargeAmount
                    + " seconds=" + callDurationSeconds
                    + " minutes=" + minutes
                    + " deducted=" + deductAmount);

            return deductAmount;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    
    /**
     * Deduct AI call charge from in-memory OrganizationData.
     * Charge is calculated using Organization.aiCallChargeAmount + Organization.aiCallChargeType
     * and the given call duration.
     *
     * @param organizationName org key used in OrganizationData map
     * @param callDurationSeconds call duration in seconds (>= 0)
     * @return true if deducted successfully, false otherwise
     */
    public boolean deductChargeForCallInMemoryForOrg(String organizationName, long amount) {

        try {
            if (organizationName == null || organizationName.trim().isEmpty()) {
                System.out.println("deductAiChargeForCallInMemory invalid orgName");
                return false;
            }

            OrganizationWorkingDTO dto = OrganizationWorkingDTO.builder()
                    .deductAIAmount(amount)
                    .build();

            OrganizationData.workWithAllOrganizationData(
            		organizationName,
                    null,
                    "deductAIAmount",
                    dto
            );

            System.out.println("deductAiChargeForCallInMemory org=" + organizationName
                    + " amount=" + amount);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}