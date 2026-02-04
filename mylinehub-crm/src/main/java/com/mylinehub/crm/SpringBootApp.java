package com.mylinehub.crm;


import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.Map;
import java.util.stream.Collectors;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mylinehub.crm.TaskScheduler.CloseSupportTicketRunnable;
import com.mylinehub.crm.TaskScheduler.ExtensionEventRunnable;
import com.mylinehub.crm.TaskScheduler.GenericEventRunnable;
import com.mylinehub.crm.TaskScheduler.LowAmountNotificationRunnable;
import com.mylinehub.crm.TaskScheduler.SaveOrganizationDataRunnable;
import com.mylinehub.crm.TaskScheduler.ThreadDumpRunnable;
import com.mylinehub.crm.ami.TaskScheduler.StartedCampaignRunDataFlushRunnable;
import com.mylinehub.crm.ami.TaskScheduler.CustEmpCampUpdatePeriodicRunnable;
import com.mylinehub.crm.ami.TaskScheduler.DeletedPreviousChustomerPageRunnable;
import com.mylinehub.crm.ami.TaskScheduler.HardInsertCallDetailAndCostRunnable;
import com.mylinehub.crm.ami.TaskScheduler.RefreshBackEndConnectionRunnable;
import com.mylinehub.crm.ami.service.SaveMemoryDataToDatabaseService;
import com.mylinehub.crm.data.SystemConfigData;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.RunningSchedule;
import com.mylinehub.crm.entity.SystemConfig;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.enums.COST_CALCULATION;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.gst.data.GSTVerificationEngineData;
import com.mylinehub.crm.gst.data.dto.GstVerificationEngineDataParameterDto;
import com.mylinehub.crm.gst.entity.GstVerificationEngine;
import com.mylinehub.crm.gst.enums.REGISTRATIONENGINE;
import com.mylinehub.crm.gst.repository.GstVerificationEngineRepository;
import com.mylinehub.crm.rag.data.PromptAndAssistantData;
import com.mylinehub.crm.rag.data.dto.PromptAndAssistantDataDTO;
import com.mylinehub.crm.rag.model.AssistantEntity;
import com.mylinehub.crm.rag.model.SystemPrompts;
//import com.mylinehub.crm.rag.repository.AiSentimentsRepository;
import com.mylinehub.crm.rag.repository.AssistantRepository;
import com.mylinehub.crm.rag.repository.SystemPromptRepository;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.CallDetailRepository;
import com.mylinehub.crm.repository.CampaignRepository;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.OrganizationRepository;
import com.mylinehub.crm.repository.SystemConfigRepository;
import com.mylinehub.crm.service.CampaignRunTrackingService;
import com.mylinehub.crm.service.CustomerPropertyInventoryService;
import com.mylinehub.crm.service.CustomerService;
import com.mylinehub.crm.service.OrganizationService;
import com.mylinehub.crm.service.RunningScheduleService;
import com.mylinehub.crm.service.SchedulerService;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.utils.okhttp.OkHttpSourcePortPoolMemoryData;
import com.mylinehub.crm.whatsapp.repository.WhatsAppChatHistoryRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppFlattenMessageRepository;
import com.mylinehub.crm.whatsapp.repository.WhatsAppNumberReportRepository;
import com.mylinehub.crm.whatsapp.service.SetupWhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.service.WhatsAppChatHistoryService;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppCleanCustomerDataRunnable;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppHardInsertChatHistoryRunnable;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppHardInsertChatHistoryUpdatesRunnable;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppHardInsertFlattenMessageRunnable;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppHardInsertReportDataRunnable;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppSaveAllCustomerDataRunnable;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppSaveCustomerPropertyInventoryRunnable;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import springfox.documentation.swagger2.annotations.EnableSwagger2;
import com.mylinehub.crm.TaskScheduler.RecalculateOrganizationStorageRunnable;
import com.mylinehub.crm.rag.service.AssistantService;
//import com.mylinehub.crm.rag.taskscheduler.AiSentimentToDBRunnable;

@EnableSwagger2
@SpringBootApplication
@EnableScheduling
public class SpringBootApp extends SpringBootServletInitializer {

	static OrganizationRepository organizationRepository;
	static WhatsAppChatHistoryService whatsAppChatHistoryService;
	static CallDetailRepository callDetailRepository;
	static SaveMemoryDataToDatabaseService campaignDataToDatabaseService;
	static WhatsAppNumberReportRepository whatsAppNumberReportRepository;
	static WhatsAppChatHistoryRepository whatsAppChatHistoryRepository;
	static WhatsAppFlattenMessageRepository whatsAppFlattenMessageRepository;
	static GstVerificationEngineRepository gstVerificationEngineRepository;
	static SystemConfigRepository systemConfigRepository;
	static AssistantService assistantService;
	static ApplicationContext applicationContext;
	static CustomerRepository customerRepository;
	static CustomerService customerService;
	static CustomerPropertyInventoryService customerPropertyInventoryService;
	static CampaignRunTrackingService campaignRunTrackingService;
//	static AiSentimentsRepository aiSentimentsRepository;
	
    public static void main(String[] args) {
    	
    	//Spring Boot URL
    	//http://localhost:8081/swagger-ui.html
    	
    	applicationContext = SpringApplication.run(SpringBootApp.class, args);
    	organizationRepository = applicationContext.getBean(OrganizationRepository.class);
    	whatsAppChatHistoryService = applicationContext.getBean(WhatsAppChatHistoryService.class);
    	callDetailRepository=applicationContext.getBean(CallDetailRepository.class);
    	campaignDataToDatabaseService = applicationContext.getBean(SaveMemoryDataToDatabaseService.class);
    	whatsAppNumberReportRepository = applicationContext.getBean(WhatsAppNumberReportRepository.class);
    	whatsAppChatHistoryRepository = applicationContext.getBean(WhatsAppChatHistoryRepository.class);
    	whatsAppFlattenMessageRepository = applicationContext.getBean(WhatsAppFlattenMessageRepository.class);
    	gstVerificationEngineRepository = applicationContext.getBean(GstVerificationEngineRepository.class);
    	systemConfigRepository = applicationContext.getBean(SystemConfigRepository.class);
    	assistantService =  applicationContext.getBean(AssistantService.class);
    	customerRepository = applicationContext.getBean(CustomerRepository.class);
    	customerService = applicationContext.getBean(CustomerService.class);
    	customerPropertyInventoryService = applicationContext.getBean(CustomerPropertyInventoryService.class);
    	campaignRunTrackingService = applicationContext.getBean(CampaignRunTrackingService.class);
    	//    	aiSentimentsRepository  = applicationContext.getBean(AiSentimentsRepository.class);
    	
    	ErrorRepository errorRepository = applicationContext.getBean(ErrorRepository.class);
    	RunningScheduleService runningScheduleService = applicationContext.getBean(RunningScheduleService.class);
    	
    	try {
    		LoggerUtils.log.debug("Current Date / Time identified by java : "+ new Date());
    		createWebSocketsupportUsers(applicationContext);
    	}
    	catch(Exception e) {
//    		LoggerUtils.log.debug("Cannot add websocket support users. They may already be in database.");
//    		e.printStackTrace();
    	}
    	
    	try {
    		    assignApplicationContextToStompClient(applicationContext);
    		    
    		    try {
        			setupOrganizationData(applicationContext.getBean(OrganizationService.class));
    		    }
    		    catch(Exception e)
    		    {
    		    	System.out.println("Error while setting up organizational data");
    		    	throw e;
    		    }
    		    
    		    try {
    		    	loadAllActiveSystemPromptsAndAllAssistants(applicationContext.getBean(SystemPromptRepository.class),applicationContext.getBean(AssistantRepository.class));
    		    }
    		    catch(Exception e)
    		    {
    		    	System.out.println("Error while setting up system prompts and assiatnts");
    		    }
    		    
    		    try {
    		    	SetupWhatsAppMemoryData.setupWhatsAppPhoneNumberData(applicationContext);
    		    }
    		    catch(Exception e)
    		    {
    		    	System.out.println("Error while setting up Whats App Phone Number data");
    		    	throw e;
    		    }
    		    
    		    try {
    		    	SetupWhatsAppMemoryData.setupWhatsAppOpenAiAccountData(applicationContext);
    		    }
    		    catch(Exception e)
    		    {
    		    	System.out.println("Error while setting up Open AI Account data");
    		    	throw e;
    		    }
    		    
    		    try {
    		    	SetupWhatsAppMemoryData.setupWhatsAppPhoneNumberTemplates(applicationContext);
    		    }
    		    catch(Exception e)
    		    {
    		    	System.out.println("Error while setting up Whats App Phone Number Template data");
    		    	throw e;
    		    }
    		    
    		    try {
    		    	SetupWhatsAppMemoryData.setupWhatsAppPhoneNumberTemplateVariable(applicationContext);
    		    }
    		    catch(Exception e)
    		    {
    		    	System.out.println("Error while setting up Whats App Phone Number Template Variables data");
    		    	throw e;
    		    }
    		    
    		    try {
    		    	SetupWhatsAppMemoryData.setupWhatsAppPhoneNumberPrompts(applicationContext);
    		    }
    		    catch(Exception e)
    		    {
    		    	System.out.println("Error while setting up Whats App Phone Number Prompt data");
    		    	throw e;
    		    }
    		    
    		    try {
    		    	setupGstVerificationEngineData();
    		    }
    		    catch(Exception e) {
    		    	System.out.println("Error while setting up Gst Verification Engine Data");
    		    	throw e;
    		    }
    		    
    		    try {
    		    	setupSystemConfigData();
    		    }
    		    catch(Exception e) {
    		    	System.out.println("Error while setting up system config Data");
    		    	throw e;
    		    }
    		    
//    		    try {
//    		    	new Thread(() -> {
//    		    	    try {
//    		    	        assistantService.syncAssistantsWithOpenAI();
//    		    	    } catch (Exception e) {
//    		    	    	System.out.println("Error while syncing assistant data with local DB");
//    		    	        e.printStackTrace();
//    		    	    }
//    		    	}).start();
//    		    }
//    		    catch(Exception e) {
//    		    	e.printStackTrace();
//    		    }
    		    
    	    	EmployeeRepository employeeRepository = applicationContext.getBean(EmployeeRepository.class);
    	    	List<Employee> allEmployees= employeeRepository.findAll();
    	    	RefreshBackEndConnectionRunnable.setInitialExtensionStates(errorRepository,applicationContext,allEmployees);
    	    	
        		RefreshBackEndConnectionRunnable refreshBackEndConnectionRunnable = new RefreshBackEndConnectionRunnable();
        		refreshBackEndConnectionRunnable.setErrorRepository(errorRepository);
        		refreshBackEndConnectionRunnable.setApplicationContext(applicationContext);
        		refreshBackEndConnectionRunnable.run();
        		
	    		disactivateAllActiveCampaign(errorRepository,applicationContext);
//	    		setupCronAiSetimentsData(applicationContext.getBean(SchedulerService.class));
	    		setupCronCustomerUpdateForAutodialerCampaigns(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setScheduleJobAtStartOfServer(runningScheduleService);
	    		setupCronDeletedCampaignData(applicationContext.getBean(SchedulerService.class));
	    		setupCronForCdrData(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForCampaignRunFlushRunnable(applicationContext, applicationContext.getBean(SchedulerService.class));
	    		setupCronSaveOrganizationData(applicationContext.getBean(OrganizationService.class),applicationContext.getBean(SchedulerService.class));
//	    		setupRefreshBackendConnections(errorRepository,applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForWhatsAppChatHistoryData(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForWhatsAppReportData(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForWhatsAppChatUpdateData(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForWhatsAppCleanCustomerData(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForWhatsAppSaveCustomerPropertyInventoryRunnable(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForWhatsAppSaveCustomerData(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronWhatsAppHardInsertFlattenMessageData(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForLowBalanceNotificationData(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForFileStoreRecalculation(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronForThreadDump(applicationContext,applicationContext.getBean(SchedulerService.class));
	    		setupCronCloseSupportTicketData(applicationContext.getBean(SchedulerService.class));
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	try {
    		initializeOkHttpSourcePortPoolService();
    	}
    	catch(Exception e)
    	{
    		System.out.println("Exeption while initializing okhttpSourcePortPoolService in main");
    		e.printStackTrace();
    	}
    	
    	try {
    		System.out.println("Start verification SSL handshake from java  for Vue GST Engine ...");
    		verifyIfHandshakeFailsForVue();
    		System.out.println("Verification SSL handshake complete for Vue GST Engine...");
    	}
    	catch(Exception e)
    	{
    		System.out.println("Exeption while verification SSL handshake from java for Vue GST Engine");
    		e.printStackTrace();
    	}
    	
    	try {
    		System.out.println("Start verification SSL handshake from java  for Idfy GST Engine ...");
    		verifyIfHandshakeFailsForIdfy();
    		System.out.println("Verification SSL handshake complete for Idfy GST Engine...");
    	}
    	catch(Exception e)
    	{
    		System.out.println("Exeption while verification SSL handshake from java for Idfy GST Engine");
    		e.printStackTrace();
    	}
    	
//    	try {
//    		createCustomerDataforTesting(errorRepository,applicationContext);
//    	}
//    	catch(Exception e)
//    	{
//    		e.printStackTrace();
//    	}
    	
//    	try {
//        	createCallDetailDataforTesting(errorRepository,applicationContext);
//    	}
//    	catch(Exception e)
//    	{
//    		e.printStackTrace();
//    	}
    	
    	
    }

   /*// Used when deploying to a standalone servlet container
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    	
        return application.sources(SpringBootApp.class);
    }*/
    
    static void verifyIfHandshakeFailsForVue() throws UnknownHostException, IOException {
    	SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
    	SSLSocket s = (SSLSocket) ssf.createSocket(applicationContext.getEnvironment().getProperty("spring.gst.deep.vue.domain"), 443);
    	s.startHandshake(); // See if it fails
    }
    
    static void verifyIfHandshakeFailsForIdfy() throws UnknownHostException, IOException {
    	SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
    	SSLSocket s = (SSLSocket) ssf.createSocket(applicationContext.getEnvironment().getProperty("spring.gst.idfy.domain"), 443);
    	s.startHandshake(); // See if it fails
    }
    
    static void initializeOkHttpSourcePortPoolService() {
    	OkHttpSourcePortPoolMemoryData.initialize(applicationContext);
    }
    
    
    static void disactivateAllActiveCampaign(ErrorRepository errorRepository ,ApplicationContext applicationContext)
    {
    	try {
	    		CampaignRepository campaignRepository = applicationContext.getBean(CampaignRepository.class);
	        	List<Campaign> allCampigns = campaignRepository.findAllByIsactive(true);
	        	List<Campaign> allCampignsToSave = new ArrayList<Campaign>();
	        	
	        	if(allCampigns != null)
	        	{
	            	for(int i=0; i<allCampigns.size();i++)
	            	{
	            		Campaign currentCampaign = allCampigns.get(i);
	            		currentCampaign.setIsactive(false);
	            		allCampignsToSave.add(currentCampaign);
	            	}
	        	}
	        	
	        	if(allCampignsToSave.size()>0)
	        	{
	        		campaignRepository.saveAll(allCampignsToSave);
	        	}
		    }
		catch(Exception e)
			{
				e.printStackTrace();
				Report.addError(e.getMessage(), "Disable Campaign","Startup System", "cannot disable acive campaigns","N/A",errorRepository);			
			}
    }
	
    
    static void setupSystemConfigData()
   	{
       	List<SystemConfig> systemConfigAll = systemConfigRepository.findAll();
       	   
       	if(systemConfigAll.size() == 0) {
       		
       		SystemConfig current = new SystemConfig();
       		current.setJwtTokenValidationDays(1);
       		current.setOrganization(applicationContext.getEnvironment().getProperty("spring.parentorginization"));
       		current.setSmtphost(applicationContext.getEnvironment().getProperty("spring.mail.host"));
       		current.setSmtppassword(applicationContext.getEnvironment().getProperty("spring.mail.password"));
       		current.setSmtpusername(applicationContext.getEnvironment().getProperty("spring.mail.username"));
       		current.setSmtpport(Integer.parseInt(applicationContext.getEnvironment().getProperty("spring.mail.port")));
       		current.setWhatsAppNotificationNumber(applicationContext.getEnvironment().getProperty("spring.whatsapp.phone"));
       		current.setOnboardingTemplate(applicationContext.getEnvironment().getProperty("spring.template.onboarding"));
       		current.setLowBalanceTemplate(applicationContext.getEnvironment().getProperty("spring.template.rechargerequired"));
       		current.setGstEngineName(REGISTRATIONENGINE.Vue.name());
       		current.setGstEngineNameSecond(REGISTRATIONENGINE.Idfy.name());
       		current = systemConfigRepository.save(current);
       		SystemConfigData.systemConfig = current;
       	}
       	else if(systemConfigAll.size() != 1) {
       		System.out.println("System Config cannot have more than one row.");
            int exitCode = 1; // Define the desired error exit code
            System.exit(SpringApplication.exit(applicationContext, () -> exitCode));
       	}
       	else {
       		SystemConfig current = systemConfigAll.get(0);
       		SystemConfigData.systemConfig = current;
       	}
   	}
    
	
    static void setupGstVerificationEngineData()
	{
    	System.out.println("setupGstVerificationEngineData");
    	List<GstVerificationEngine> allGstVerificationEngines = gstVerificationEngineRepository.getGstVerificationEngineByActive(true);    	
    	System.out.println("allGstVerificationEngines Size: "+allGstVerificationEngines.size());
    	
    	if(allGstVerificationEngines==null || allGstVerificationEngines.size()==0) {
    		
    		System.out.println("allGstVerificationEngines is null");
    		System.out.println("Setting Vue GST Engine Data ..");
    		GstVerificationEngine gstVerificationEngineVue = new GstVerificationEngine();
    		gstVerificationEngineVue.setEngineName(REGISTRATIONENGINE.Vue.name());
    		gstVerificationEngineVue.setClientId(applicationContext.getEnvironment().getProperty("spring.vue.clientid"));
    		gstVerificationEngineVue.setCientSecret(applicationContext.getEnvironment().getProperty("spring.idfy.clientsecret"));
    		gstVerificationEngineVue.setValidityInHours(22);
    		gstVerificationEngineVue.setActive(true);
    		
    		System.out.println("saving record to database");
    		gstVerificationEngineVue = gstVerificationEngineRepository.save(gstVerificationEngineVue);
    		GstVerificationEngineDataParameterDto gstVerificationEngineDataParameterDtoForVue = new GstVerificationEngineDataParameterDto();
    		gstVerificationEngineDataParameterDtoForVue.setEngineName(gstVerificationEngineVue.getEngineName());
    		gstVerificationEngineDataParameterDtoForVue.setDetails(gstVerificationEngineVue);
    		gstVerificationEngineDataParameterDtoForVue.setAction("update");
    		
    		if(gstVerificationEngineVue.isActive())
    		{
    			System.out.println("saving record to memory");
    			GSTVerificationEngineData.workWithGstVerificationData(gstVerificationEngineDataParameterDtoForVue);
    		}
    		
    		
    		System.out.println("Setting Idfy GST Engine Data ..");
    		GstVerificationEngine gstVerificationEngineIdfy = new GstVerificationEngine();
    		gstVerificationEngineIdfy.setEngineName(REGISTRATIONENGINE.Idfy.name());
    		gstVerificationEngineIdfy.setApiKey(applicationContext.getEnvironment().getProperty("spring.idfy.apikey"));
    		gstVerificationEngineIdfy.setAccountId(applicationContext.getEnvironment().getProperty("spring.idfy.accountid"));
    		gstVerificationEngineIdfy.setValidityInHours(22);
    		gstVerificationEngineIdfy.setActive(true);
    		
    		System.out.println("saving record to database");
    		gstVerificationEngineIdfy = gstVerificationEngineRepository.save(gstVerificationEngineIdfy);
    		GstVerificationEngineDataParameterDto gstVerificationEngineDataParameterDtoForIdfy = new GstVerificationEngineDataParameterDto();
    		gstVerificationEngineDataParameterDtoForIdfy.setEngineName(gstVerificationEngineIdfy.getEngineName());
    		gstVerificationEngineDataParameterDtoForIdfy.setDetails(gstVerificationEngineIdfy);
    		gstVerificationEngineDataParameterDtoForIdfy.setAction("update");
    		
    		if(gstVerificationEngineIdfy.isActive())
    		{
    			System.out.println("saving record to memory");
    			GSTVerificationEngineData.workWithGstVerificationData(gstVerificationEngineDataParameterDtoForIdfy);
    		}
    		
    	}
    	else {
    		
    		System.out.println("allGstVerificationEngines is not null");
    		
    		allGstVerificationEngines.forEach((element)->{
    			GstVerificationEngineDataParameterDto gstVerificationEngineDataParameterDto = new GstVerificationEngineDataParameterDto();
        		gstVerificationEngineDataParameterDto.setEngineName(element.getEngineName());
        		gstVerificationEngineDataParameterDto.setDetails(element);
        		gstVerificationEngineDataParameterDto.setAction("update");
        		
        		if(element.isActive())
        		{
        			System.out.println("saving record to memory");
        			GSTVerificationEngineData.workWithGstVerificationData(gstVerificationEngineDataParameterDto);
        		}
    		});
    	}
	}
    

//Below code moved to organizatin application service.
//    static void setupPhoneNumberData()
//   	{ 	
//       	System.out.println("setupPhoneNumberData");
//
//       	List<WhatsAppPhoneNumber> allData = whatsAppPhoneNumberRepository.findAll();
//       	
//       	System.out.println("setupPhoneNumberData Size: "+allData.size());
//       	
//       	if(allData==null || allData.size()==0) {
//       		//Create Data
//       	}
//   	}

    
	static void createWebSocketsupportUsers(ApplicationContext applicationContext)
	{
		EmployeeRepository employeeRepository = applicationContext.getBean(EmployeeRepository.class);
		List<String> allWebSocketEmployeeUsers = new ArrayList<String>();
		List<Employee> allUsers = new ArrayList<Employee>();
		
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user1").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user2").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user3").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user4").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user5").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user6").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user7").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user8").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user9").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user10").replace(applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"),""));
		
		allWebSocketEmployeeUsers.forEach(
   	            (user) -> {
   	            	
   	            	Employee currentEmployee = new Employee();
   	            	currentEmployee.setFirstName(user);
   	        		currentEmployee.setLastName(user);
   	        		currentEmployee.setUserRole(USER_ROLE.ADMIN);
   	        		currentEmployee.setEmail(user+applicationContext.getEnvironment().getProperty("spring.parent.email.suffix"));
   	        		currentEmployee.setOrganization(applicationContext.getEnvironment().getProperty("spring.parentorginization"));
   	        		currentEmployee.setPhoneContext(user);
   	        		currentEmployee.setTimezone(TimeZone.getTimeZone(applicationContext.getEnvironment().getProperty("spring.parent.timezone")));
   	        		currentEmployee.setDomain(applicationContext.getEnvironment().getProperty("spring.domain"));
   	        		currentEmployee.setExtension(user);
   	        		currentEmployee.setPhonenumber(user);
   	        		allUsers.add(currentEmployee);
   	            });
 
		employeeRepository.saveAll(allUsers);
	}
	
	
//   static void setupCronAiSetimentsData(SchedulerService schedulerService) throws IOException {
//		
//		String jobId = TrackedSchduledJobs.aiSentimentToDBRunnable;
//		AiSentimentToDBRunnable aiSentimentToDBRunnable = new AiSentimentToDBRunnable();
//		aiSentimentToDBRunnable.setJobId(jobId);
//		aiSentimentToDBRunnable.setAiSentimentsRepository(aiSentimentsRepository);
//		schedulerService.removeIfExistsAndScheduleACronTask(jobId, aiSentimentToDBRunnable, "0 */55 * * * ?");
//	}
	
	static void setupCronCustomerUpdateForAutodialerCampaigns(ApplicationContext applicationContext,SchedulerService schedulerService) throws IOException {
		
		String jobId = TrackedSchduledJobs.custEmpCampUpdatePeriodicRunnable;
		CustEmpCampUpdatePeriodicRunnable custEmpCampUpdatePeriodicRunnable = new CustEmpCampUpdatePeriodicRunnable();
		custEmpCampUpdatePeriodicRunnable.setCampaignDataToDatabaseService(applicationContext.getBean(SaveMemoryDataToDatabaseService.class));
		custEmpCampUpdatePeriodicRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, custEmpCampUpdatePeriodicRunnable, "0 */59 * * * ?");
	}
	
	
	static void setupCronDeletedCampaignData(SchedulerService schedulerService) throws IOException {
		
		String jobId = TrackedSchduledJobs.deletedPreviousChustomerPageRunnable;
		DeletedPreviousChustomerPageRunnable deletedPreviousChustomerPageRunnable = new DeletedPreviousChustomerPageRunnable();
		deletedPreviousChustomerPageRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, deletedPreviousChustomerPageRunnable, "0 */40 * * * ?");
	}
	
	
	static void setupOrganizationData(OrganizationService organizationService) throws IOException {	
		List<Organization> allOrganization = organizationService.getAll();
		
		System.out.println("setupOrganizationData");
		
		if(allOrganization != null)
		{
			System.out.println("allOrganization is not null");
			System.out.println("Size : "+allOrganization.size());
			for(int i=0; i< allOrganization.size();i++)
			{
				organizationService.loadNewOrganizationData(allOrganization.get(i));
			}
		}
	}
	
	

	static void loadAllActiveSystemPromptsAndAllAssistants(SystemPromptRepository systemPromptRepository,
	                                                                    AssistantRepository assistantRepository) {

	        System.out.println("Loading active system prompts and all assistants into memory");

	        // 1 Load all active system prompts and group by organization
	        Map<String, List<SystemPrompts>> promptsByOrg = systemPromptRepository.findAll()
	                .stream()
	                .filter(SystemPrompts::isActive)
	                .collect(Collectors.groupingBy(SystemPrompts::getOrganization));

	        // Update memory per organization in one batch
	        promptsByOrg.forEach((org, prompts) -> {
	            PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
	            dto.setAction("updateCompleteBatch");
	            dto.setOrganization(org);
	            dto.setBatchPrompt(prompts);  // batch field in DTO

	            PromptAndAssistantData.workWithAllSystemPrompts(dto);
	        });

	        System.out.println("Loaded all active system prompts into memory (batch per org)");

	        // 2 Load all assistants and group by organization
	        Map<String, List<AssistantEntity>> assistantsByOrg = assistantRepository.findAll()
	                .stream()
	                .collect(Collectors.groupingBy(AssistantEntity::getOrganization));

	        // Update memory per organization in one batch
	        assistantsByOrg.forEach((org, assistants) -> {
	            PromptAndAssistantDataDTO dto = new PromptAndAssistantDataDTO();
	            dto.setAction("updateCompleteBatch");
	            dto.setOrganization(org);
	            dto.setBatchAssistantEntity(assistants);  // batch field in DTO

	            PromptAndAssistantData.workWithAllAssistants(dto);
	        });

	        System.out.println("Loaded all assistants into memory (batch per org)");
	  }

	
	static void setupCronCloseSupportTicketData(SchedulerService schedulerService) throws IOException {
		
		String jobId=TrackedSchduledJobs.closeSupportTicketRunnable;
		CloseSupportTicketRunnable closeSupportTicketRunnable = new CloseSupportTicketRunnable();
		closeSupportTicketRunnable.setApplicationContext(applicationContext);
		closeSupportTicketRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, closeSupportTicketRunnable, "0 0 2 * * ?");
	}
	
	static void setupCronSaveOrganizationData(OrganizationService organizationService,SchedulerService schedulerService) throws IOException {
		
		String jobId=TrackedSchduledJobs.saveOrganizationDataRunnable;
		SaveOrganizationDataRunnable saveOrganizationDataRunnable = new SaveOrganizationDataRunnable();
		saveOrganizationDataRunnable.setOrganizationRepository(organizationRepository);
		saveOrganizationDataRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, saveOrganizationDataRunnable, "0 */60 * * * ?");
	}
	
	static void setupRefreshBackendConnections(ErrorRepository errorRepository, ApplicationContext applicationContext,SchedulerService schedulerService) throws IOException {
		String jobId=TrackedSchduledJobs.refreshBackEndConnectionRunnableCron;
		RefreshBackEndConnectionRunnable refreshBackEndConnectionRunnable = new RefreshBackEndConnectionRunnable();
		refreshBackEndConnectionRunnable.setErrorRepository(errorRepository);
		refreshBackEndConnectionRunnable.setApplicationContext(applicationContext);
		refreshBackEndConnectionRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, refreshBackEndConnectionRunnable, "0 */60 * * * ?");
	}
	
	static void setupCronForCdrData(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException {
		String jobId=TrackedSchduledJobs.hardInsertCallDetailAndCostRunnableCron;
		HardInsertCallDetailAndCostRunnable hardInsertCallDetailAndCostRunnable = new HardInsertCallDetailAndCostRunnable();
		hardInsertCallDetailAndCostRunnable.setCallDetailRepository(callDetailRepository);
		hardInsertCallDetailAndCostRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, hardInsertCallDetailAndCostRunnable, "0 */5 * * * ?");
	}
	
	static void setupCronForCampaignRunFlushRunnable(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException {

	    String jobId = TrackedSchduledJobs.startedCampaignRunDataFlushRunnable;

	    StartedCampaignRunDataFlushRunnable campaignRunFlushRunnable = new StartedCampaignRunDataFlushRunnable();
	    campaignRunFlushRunnable.setJobId(jobId);
	    campaignRunFlushRunnable.setTrackingService(applicationContext.getBean(com.mylinehub.crm.service.CampaignRunTrackingService.class));
	    campaignRunFlushRunnable.setForceFlush(false);

	    // Every 60 minutes
	    schedulerService.removeIfExistsAndScheduleACronTask(jobId, campaignRunFlushRunnable, "0 */60 * * * ?");
	}

	static void setupCronForWhatsAppChatHistoryData(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
		String jobId = TrackedSchduledJobs.whatsAppHardInsertChatHistoryRunnable;
		
//    	System.out.println("Scheduling hard insert");
    	WhatsAppHardInsertChatHistoryRunnable hardInsertChatHistoryRunnable = new WhatsAppHardInsertChatHistoryRunnable();
    	hardInsertChatHistoryRunnable.setWhatsAppChatHistoryService(whatsAppChatHistoryService);
    	hardInsertChatHistoryRunnable.setWhatsAppChatHistoryRepository(whatsAppChatHistoryRepository);
    	hardInsertChatHistoryRunnable.setJobId(jobId);
    	schedulerService.removeIfExistsAndScheduleACronTask(jobId, hardInsertChatHistoryRunnable, "0 */30 * * * ?");
	}
	
	
	static void setupCronForWhatsAppReportData(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
		String jobId = TrackedSchduledJobs.whatsAppHardInsertReportDataRunnable;
		
//    	System.out.println("Scheduling hard insert");
		WhatsAppHardInsertReportDataRunnable whatsAppHardInsertReportDataRunnable = new WhatsAppHardInsertReportDataRunnable();
		whatsAppHardInsertReportDataRunnable.setWhatsAppNumberReportRepository(whatsAppNumberReportRepository);
		whatsAppHardInsertReportDataRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, whatsAppHardInsertReportDataRunnable, "0 */50 * * * ?");
	}
	
	
	static void setupCronForWhatsAppChatUpdateData(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
		String jobId = TrackedSchduledJobs.whatsAppHardInsertChatHistoryUpdatesRunnable;
		
//    	System.out.println("Scheduling hard insert");
		WhatsAppHardInsertChatHistoryUpdatesRunnable whatsAppHardInsertChatHistoryUpdatesRunnable = new WhatsAppHardInsertChatHistoryUpdatesRunnable();
		whatsAppHardInsertChatHistoryUpdatesRunnable.setWhatsAppChatHistoryRepository(whatsAppChatHistoryRepository);
		whatsAppHardInsertChatHistoryUpdatesRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, whatsAppHardInsertChatHistoryUpdatesRunnable, "0 */20 * * * ?");
	}
	
	
	static void setupCronForWhatsAppCleanCustomerData(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
		String jobId = TrackedSchduledJobs.whatsAppCleanCustomerDataRunnable;
		
//    	System.out.println("Scheduling hard insert");
		WhatsAppCleanCustomerDataRunnable whatsAppCleanCustomerDataRunnable = new WhatsAppCleanCustomerDataRunnable();
		whatsAppCleanCustomerDataRunnable.setJobId(jobId);
		whatsAppCleanCustomerDataRunnable.setCustomerService(customerService);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, whatsAppCleanCustomerDataRunnable, "0 1 * * * ?");
	}
	
	static void setupCronForWhatsAppSaveCustomerPropertyInventoryRunnable(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
		String jobId = TrackedSchduledJobs.whatsAppSaveCustomerPropertyInventoryRunnable;
		CustomerPropertyInventoryService customerPropertyInventoryService = applicationContext.getBean(CustomerPropertyInventoryService.class);
		WhatsAppSaveCustomerPropertyInventoryRunnable whatsAppSaveCustomerPropertyInventoryRunnable = new WhatsAppSaveCustomerPropertyInventoryRunnable();
		whatsAppSaveCustomerPropertyInventoryRunnable.setJobId(jobId);
		whatsAppSaveCustomerPropertyInventoryRunnable.setCustomerPropertyInventoryService(customerPropertyInventoryService);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, whatsAppSaveCustomerPropertyInventoryRunnable, "0 0 0/2 * * ?");
	}
	
	
	static void setupCronForWhatsAppSaveCustomerData(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
		String jobId = TrackedSchduledJobs.whatsAppSaveAllCustomerDataRunnable;
		WhatsAppSaveAllCustomerDataRunnable whatsAppSaveAllCustomerDataRunnable = new WhatsAppSaveAllCustomerDataRunnable();
		whatsAppSaveAllCustomerDataRunnable.setJobId(jobId);
		whatsAppSaveAllCustomerDataRunnable.setCustomerService(null);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, whatsAppSaveAllCustomerDataRunnable, "0 0 0/6 * * ?");
	}
	
	static void setupCronWhatsAppHardInsertFlattenMessageData(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
		String jobId = TrackedSchduledJobs.whatsAppHardInsertFlattenMessageRunnable;
		
//    	System.out.println("Scheduling hard insert");
		WhatsAppHardInsertFlattenMessageRunnable whatsAppHardInsertFlattenMessageRunnable = new WhatsAppHardInsertFlattenMessageRunnable();
		whatsAppHardInsertFlattenMessageRunnable.setJobId(jobId);
		whatsAppHardInsertFlattenMessageRunnable.setWhatsAppFlattenMessageRepository(whatsAppFlattenMessageRepository);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, whatsAppHardInsertFlattenMessageRunnable, "0 */20 * * * ?");
	}
	
	static void setupCronForLowBalanceNotificationData(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
		String jobId = TrackedSchduledJobs.lowAmountNotificationRunnable;
		
//    	System.out.println("Scheduling hard insert");
		LowAmountNotificationRunnable lowAmountNotificationRunnable = new LowAmountNotificationRunnable();
		lowAmountNotificationRunnable.setApplicationContext(applicationContext);
		lowAmountNotificationRunnable.setJobId(jobId);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, lowAmountNotificationRunnable, "0 0 */3 * * ?");
	}
	
	static void setupCronForFileStoreRecalculation(ApplicationContext applicationContext, SchedulerService schedulerService) throws IOException
	{
    	try {
    		String jobId = TrackedSchduledJobs.recalculateFileStorage;
    		System.out.println("Saving cron having job Id : "+jobId);
    		RecalculateOrganizationStorageRunnable recalculateOrganizationStorageRunnable = new RecalculateOrganizationStorageRunnable();
    		recalculateOrganizationStorageRunnable.setApplicationContext(applicationContext);
    		recalculateOrganizationStorageRunnable.setJobId(jobId);
    		schedulerService.removeIfExistsAndScheduleACronTask(jobId, recalculateOrganizationStorageRunnable, "0 0 0 ? * SUN");
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	static void setupCronForThreadDump(ApplicationContext applicationContext,SchedulerService schedulerService) throws IOException {
		String jobId = TrackedSchduledJobs.threadDump;
		ThreadDumpRunnable threadDumpRunnable = new ThreadDumpRunnable();
		threadDumpRunnable.setJobId(jobId);
		threadDumpRunnable.setApplicationContext(applicationContext);
		schedulerService.removeIfExistsAndScheduleACronTask(jobId, threadDumpRunnable, "0 9 1 * * ?");
	}
	
	
	static void setScheduleJobAtStartOfServer(RunningScheduleService runningScheduleService)
	{
		try {
			List<RunningSchedule> runningSchedules = runningScheduleService.getAllRunningSchedules();
			runningScheduleService.runningSchedulesToJobsAfterRestart(runningSchedules);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	static void assignApplicationContextToStompClient(ApplicationContext applicationContext)
	{
		LoggerUtils.log.debug("assignApplicationContextToStompClient");
		MyStompSessionHandler.applicationContext = applicationContext;
	}
		

	
	// **************************************************************************************************
	// Testing Functions - Below are not called anywhere in code. They were used to create test data
	// **************************************************************************************************
	
	   
    static void sendPeriodicMessagesForTest(ErrorRepository errorRepository ,SchedulerService schedulerService) throws IOException {
//	    LoggerUtils.log.info("Starting scheduling service for generic stomp messages for test");
//	    LoggerUtils.log.info("Scheduled Service value : " + schedulerService);
    	GenericEventRunnable genericEventRunnable = new GenericEventRunnable();
    	ExtensionEventRunnable extensionEventRunnable = new ExtensionEventRunnable();
    	
    	try
        {    
    		BotInputDTO msg = new BotInputDTO();
    	    msg.setDomain("app.mylinehub.com");
    	    msg.setExtension(null);
    	    msg.setFormat("Test Format");
    	    msg.setMessage("Test Message");
    	    msg.setMessagetype("Test Message Type");
    	    msg.setOrganization("MYLINEHUB");
    	    genericEventRunnable.setMsg(msg);
    		schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds("GenericSTOMPEventTestScheduler", genericEventRunnable, 1000);
        }
        catch(Exception e)
        {
        	//LoggerUtils.log.debug(e.getMessage());
        	Report.addError(e.getMessage(), "Web Socket Message","GenericSTOMPEventTestScheduler", "cannot send message to web socket","N/A",errorRepository);
        }
    	
    	try
        {    
    		BotInputDTO msg = new BotInputDTO();
    	    msg.setDomain("app.mylinehub.com");
    	    msg.setExtension("201");
    	    msg.setFormat("Test Format");
    	    msg.setMessage("Test Message");
    	    msg.setMessagetype("Test Message Type");
    	    msg.setOrganization("MYLINEHUB");
    	    extensionEventRunnable.setMsg(msg);
    		schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds("ExtensionSTOMPEventTestScheduler", extensionEventRunnable, 1000);
        }
        catch(Exception e)
        {
        	//LoggerUtils.log.debug(e.getMessage());
        	e.printStackTrace();
        	Report.addError(e.getMessage(), "Web Socket Message","ExtensionSTOMPEventTestScheduler", "cannot send message to web socket","N/A",errorRepository);
        }
    	
    }

    
	static void createCustomerDataforTesting(ErrorRepository errorRepository ,ApplicationContext applicationContext) throws ParseException {
	    	
	    	CustomerRepository customerRepository = applicationContext.getBean(CustomerRepository.class);
	    	List<Customers> customers = new ArrayList<Customers>();
			
	    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    	Date startDate = formatter.parse("1490-0-29");
	    	Date endDate = formatter.parse("1699-5-01");
	    	int i =90000;
	    	
	    	Calendar start = Calendar.getInstance();
	    	start.setTime(startDate);
	    	Calendar end = Calendar.getInstance();
	    	end.setTime(endDate);
	    	LoggerUtils.log.debug("start creating customer test data");
	    	for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
	    		LoggerUtils.log.debug(date+" : "+ i);
	    		Customers currentCustomer = new Customers();
	    		
	    		currentCustomer.setBusiness("business "+date);
	    		currentCustomer.setDatatype("datatype "+date);
	    		currentCustomer.setDescription("description "+date);
	    		currentCustomer.setOrganization("MYLINEHUB");
	    		currentCustomer.setCountry("country "+date);
	    		currentCustomer.setCity("city "+date);
	    		currentCustomer.setZipCode("110" + i);
	    		currentCustomer.setEmail("email"+i+"@gmail.com");
	    		currentCustomer.setFirstname("firstname "+i);
	    		currentCustomer.setLastname("lastname "+i);
	    		currentCustomer.setDomain("app.mylinehub.com");
	    		currentCustomer.setPesel("pesel"+i);
	    		currentCustomer.setPhoneContext("from-internal");
	    		currentCustomer.setPhoneNumber("+919711761"+i);
	    		customers.add(currentCustomer);
	    		
	    		i = i+1;
	    	}
	    	LoggerUtils.log.debug("saving all customers to database now");
	    	customerRepository.saveAll(customers);
	}
	
	
    static void createCallDetailDataforTesting(ErrorRepository errorRepository ,ApplicationContext applicationContext) throws ParseException {
    	
    	CallDetailRepository callDetailRepository = applicationContext.getBean(CallDetailRepository.class);

    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	Date startDate = formatter.parse("2022-4-29");
    	Date endDate = formatter.parse("2024-5-01");
    	
    	
    	Calendar start = Calendar.getInstance();
    	start.setTime(startDate);
    	Calendar end = Calendar.getInstance();
    	end.setTime(endDate);
    	LoggerUtils.log.debug("start creating call detail test data");
    	for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
    	    // Do your job here with `date`.
    	    LoggerUtils.log.debug(String.valueOf(date));
    		
    		Random rand = new Random();
            int max=5000,min=2000;
            int number = rand.nextInt(max - min + 1) + min;
            LoggerUtils.log.debug(String.valueOf(number));
            
          for (int j = 1; j <number ; j++)
          {
          	CallDetail toReturn = new CallDetail();
      		//destination - callType
      		//src - Employee ID
      		//billableseconds
      		//destinationcontext
      		//starttime
      		//endtime
      		//lastapplicatio
          	
          	if((number%5) ==0)
      		{
          		toReturn.setCallerid("201");
      		}
      		else
      		{
      			if((number%7) ==0)
      			{
      				toReturn.setCallerid("203");
      			}
      			else
      			{
      				toReturn.setCallerid("202");
      			}
      			
      			if((number%83) ==0)
      			{
      				toReturn.setCallerid("204");
      			}
      			
      		}
      		
      		toReturn.setCalldurationseconds(20);
      		
      		if((number%3) ==0)
      		{
          		toReturn.setCallType("Inbound");
      		}
      		else
      		{
      			toReturn.setCallType("Outbound");
      		}
      		toReturn.setCountry("Look Timezone");
      		toReturn.setCallonmobile(false);
      		toReturn.setCustomerid(number+"1176115"+(int)(number/10));
      		toReturn.setEnddate(date);
      		toReturn.setTimezone(TimeZone.getTimeZone("IST"));
      		toReturn.setStartdate(date);
      		toReturn.setOrganization("MYLINEHUB");
      		toReturn.setPhoneContext("from-internal");
      		toReturn.setIsactive(false);
      		toReturn.setIsconnected(false);
      		LoggerUtils.log.debug("Saving to database");
      		toReturn = callDetailRepository.save(toReturn);

          }  
    	}
 	}

}




