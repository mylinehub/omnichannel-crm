package com.mylinehub.crm.data;


public class TrackedSchduledJobs {

	//Backend connection refresh
	public final static String refreshBackEndConnectionRunnableCron = "refreshBackEndConnectionRunnableCron";
	
	//Main Class Jobs
	public final static String hardInsertCallDetailAndCostRunnableCron = "hardInsertCallDetailAndCostRunnableCron";
	public final static String saveOrganizationDataRunnable = "saveOrganizationDataRunnable";
	public final static String custEmpCampUpdatePeriodicRunnable = "custEmpCampUpdatePeriodicRunnable";
	public final static String deletedPreviousChustomerPageRunnable = "deletedPreviousChustomerPageRunnable";
	
	
	//Types of runnable
	public final static String startCampaignRunnable = "startCampaignRunnable";
	public final static String stopCampaignRunnable = "stopCampaignRunnable";
	public final static String customerCallRunnable = "customerCallRunnable";
	
	
	//Types of schedule prefix
	public final static String cron = "cron";
	public final static String fixeddate = "fixeddate";
	public final static String afternseconds = "afternseconds";

	//CommonDialer
	public final static String sendWhatsApp = "sendWhatsApp";
	public final static String dialAutomateCall = "dialAutomateCall";
	public final static String dialAutomateCallCron = "dialAutomateCallCron";
	public final static String dialAutomateCallReminder = "dialAutomateCallReminder";
	public final static String reinitiateOnlyCustomerDialer = "reinitiateOnlyCustomerDialer";
//	public final static String scheduleEmployeeCustomerReminderCallService = "scheduleEmployeeCustomerReminderCallService";
//	public final static String scheduleEmployeeCustomerCallRunnableService ="scheduleEmployeeCustomerCallRunnableService";
//	public final static String scheduleEmployeeCustomerCronCallRunnableService = "scheduleEmployeeCustomerCronCallRunnableService";

	//FileStorageRecalculator
	public final static String recalculateFileStorage = "recalculateFileStorage";
	
	//ThreadDump
    public final static String threadDump = "threadDump";
    
    //ChatHistory
    public final static String chatHistory = "CC";
    
    //RunCampaign
    public final static String startedCampaignRunDataFlushRunnable = "startedCampaignRunDataFlushRunnable";
    
    
    //WhatsApp Types of runnable
  	public final static String whatsAppHardInsertChatHistoryRunnable = "whatsAppHardInsertChatHistoryRunnable";
  	public final static String whatsAppHardInsertReportDataRunnable = "whatsAppHardInsertReportDataRunnable";
  	public final static String whatsAppHardInsertChatHistoryUpdatesRunnable = "whatsAppHardInsertChatHistoryUpdatesRunnable";
  	public final static String whatsAppCleanCustomerDataRunnable = "whatsAppCleanCustomerDataRunnable";
  	public final static String whatsAppSaveAllCustomerDataRunnable = "whatsAppSaveAllCustomerDataRunnable";
  	public final static String whatsAppHardInsertFlattenMessageRunnable = "whatsAppHardInsertFlattenMessageRunnable";
  	public final static String lowAmountNotificationRunnable = "lowAmountNotificationRunnable";
  	public final static String whatAppAIMessageRunnable = "whatAppAIMessageRunnable";
  	public final static String whatAppAIMessageRunnableViaAssistant = "whatAppAIMessageRunnableViaAssistant";
  	public final static String whatsAppSaveCustomerPropertyInventoryRunnable = "whatsAppSaveCustomerPropertyInventoryRunnable";
  	
  	//Rag
  	public final static String aiSentimentToDBRunnable = "aiSentimentToDBRunnable";
  	
  	//Support Tickets
  	public final static String closeSupportTicketRunnable = "closeSupportTicketRunnable";
  	
}
