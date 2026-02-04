package com.mylinehub.crm.whatsapp.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.utils.ReportDateRangeService;
import com.mylinehub.crm.whatsapp.dto.WhatsAppMessageCountForNumberDTO;
import com.mylinehub.crm.whatsapp.dto.WhatsAppNumberReportDto;
import com.mylinehub.crm.whatsapp.dto.WhatsAppReportNumberResponseDTO;
import com.mylinehub.crm.whatsapp.dto.WhatsAppReportVariableDto;
import com.mylinehub.crm.whatsapp.entity.WhatsAppNumberReport;
import com.mylinehub.crm.whatsapp.mapper.WhatsAppNumberReportMapper;
import com.mylinehub.crm.whatsapp.repository.WhatsAppNumberReportRepository;
import com.mylinehub.crm.whatsapp.requests.WhatsAppReportRequest;
import com.mylinehub.crm.whatsapp.taskscheduler.WhatsAppHardInsertReportDataRunnable;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class WhatsAppNumberReportService {
	
	 private final WhatsAppNumberReportRepository whatsAppNumberReportRepository;
	 private final WhatsAppNumberReportMapper whatsAppNumberReportMapper;
	 private final ReportDateRangeService reportDateRangeService;
	 private final ObjectMapper objectMapper = new ObjectMapper();
	 
	    public boolean create(WhatsAppNumberReportDto whatsAppNumberReportDto) {
		    boolean toReturn = true;
		    try {
		        System.out.println("Creating WhatsAppNumberReport for DTO: " + whatsAppNumberReportDto);
		        whatsAppNumberReportRepository.save(
		            whatsAppNumberReportMapper.mapDTOToWhatsAppNumberReport(whatsAppNumberReportDto));
		        System.out.println("Create operation successful.");
		    } catch (Exception e) {
		        toReturn = false;
		        System.out.println("Exception during create operation:");
		        e.printStackTrace();
		    }
		    return toReturn;
		}

		public boolean delete(Long id) {
		    boolean toReturn = true;
		    try {
		        System.out.println("Deleting WhatsAppNumberReport with id: " + id);
		        Optional<WhatsAppNumberReport> whatsAppNumberReport = whatsAppNumberReportRepository.findById(id);
		        if (whatsAppNumberReport.isPresent()) {
		            whatsAppNumberReportRepository.delete(whatsAppNumberReport.get());
		            System.out.println("Delete operation successful.");
		        } else {
		            System.out.println("No WhatsAppNumberReport found with id: " + id);
		            toReturn = false;
		        }
		    } catch (Exception e) {
		        toReturn = false;
		        System.out.println("Exception during delete operation:");
		        e.printStackTrace();
		    }
		    return toReturn;
		}

		
		public void saveDataFromMemoryToDatabase() {
			
	        System.out.println("Trigger report data to get into DB");
	        String jobId = TrackedSchduledJobs.whatsAppHardInsertReportDataRunnable;
			WhatsAppHardInsertReportDataRunnable whatsAppHardInsertReportDataRunnable = new WhatsAppHardInsertReportDataRunnable();
			whatsAppHardInsertReportDataRunnable.setWhatsAppNumberReportRepository(whatsAppNumberReportRepository);
			whatsAppHardInsertReportDataRunnable.setJobId(jobId);
			whatsAppHardInsertReportDataRunnable.run();
			
		}
		
		
		/**
		 * Retrieves all WhatsApp number reports for a given organization.
		 * 
		 * Steps:
		 * 1. Fetches all records from DB for the given organization.
		 * 2. Fetches all in-memory report entries using WhatsAppReportingData.
		 * 3. Merges memory data into DB results — updates existing or adds new entries.
		 * 4. Updates DB with any new/modified memory records.
		 * 5. Maps and returns final list as DTOs.
		 */
		public List<WhatsAppNumberReportDto> findAllByOrganization(String organization) {
		    System.out.println("[START] Fetching all WhatsAppNumberReports for organization: " + organization);
		    saveDataFromMemoryToDatabase();
		    // 1. Fetch all reports from DB
		    List<WhatsAppNumberReport> dbReports = whatsAppNumberReportRepository.findAllByOrganizationOrderByDayUpdatedAsc(organization);
		    System.out.println("[INFO] Database report size: " + (dbReports != null ? dbReports.size() : 0));


		    // 2. Convert to DTOs
		    List<WhatsAppNumberReportDto> result = dbReports
		        .stream()
		        .map(whatsAppNumberReportMapper::mapWhatsAppNumberReportToDTO)
		        .collect(Collectors.toList());

		    System.out.println("[END] Returning report DTOs. Total count: " + result.size());
		    return result;
		}

	
		
		/**
		 * Retrieves all WhatsApp number reports for a specific phoneNumberMain and organization.
		 *
		 * Steps:
		 * 1. Fetches reports from DB using phoneNumberMain and organization.
		 * 2. Loads in-memory WhatsApp report data.
		 * 3. Merges memory data into DB data (update if exists, or add if new).
		 * 4. Converts to DTO and returns.
		 */
		public List<WhatsAppNumberReportDto> findAllByPhoneNumberMainAndOrganization(String phoneNumberMain, String organization) {
		    System.out.println("[START] Fetching WhatsAppNumberReports for phoneNumberMain: " + phoneNumberMain + " and organization: " + organization);
		    saveDataFromMemoryToDatabase();
		    // 3. Fetch DB data
		    List<WhatsAppNumberReport> dbReports = whatsAppNumberReportRepository.findAllByPhoneNumberMainAndOrganization(phoneNumberMain, organization);
		    System.out.println("[INFO] DB report size: " + (dbReports != null ? dbReports.size() : 0));

		 		    // 6. Convert to DTOs
		    List<WhatsAppNumberReportDto> result = dbReports
		        .stream()
		        .map(whatsAppNumberReportMapper::mapWhatsAppNumberReportToDTO)
		        .collect(Collectors.toList());

		    System.out.println("[END] Returning report DTOs. Total count: " + result.size());
		    return result;
		}


		
		/**
		 * Retrieves WhatsApp number reports after a specific day for a given phone number and organization.
		 *
		 * Steps:
		 * 1. Fetch reports from DB with dayUpdated >= given date.
		 * 2. Fetch all in-memory report data.
		 * 3. Merge memory reports:
		 *    - If key matches and memory date is newer, update or add.
		 * 4. Return final merged list as DTOs.
		 */
		public List<WhatsAppNumberReportDto> findAllByDayUpdatedGreaterThanEqualAndPhoneNumberMainAndOrganization(
		        Date dayUpdated, String phoneNumberMain, String organization) {

		    System.out.println("[START] Fetching WhatsAppNumberReports for phoneNumberMain: " + phoneNumberMain +
		            ", organization: " + organization + ", after date: " + dayUpdated);
		    saveDataFromMemoryToDatabase();
		    // 1. Fetch DB data
		    List<WhatsAppNumberReport> dbReports =
		            whatsAppNumberReportRepository.findAllByDayUpdatedGreaterThanEqualAndPhoneNumberMainAndOrganization(
		                    dayUpdated, phoneNumberMain, organization);
		    System.out.println("[INFO] DB report count: " + (dbReports != null ? dbReports.size() : 0));


		    // 2. Convert to DTOs and return
		    List<WhatsAppNumberReportDto> result = dbReports
		        .stream()
		        .map(whatsAppNumberReportMapper::mapWhatsAppNumberReportToDTO)
		        .collect(Collectors.toList());

		    System.out.println("[END] Returning DTOs. Count: " + result.size());
		    return result;
		}


		
		/**
		 * Retrieves WhatsApp number reports filtered by dayUpdated >= given date,
		 * phoneNumberMain, typeOfReport, and organization.
		 *
		 * Steps:
		 * 1. Fetch DB reports matching all filters.
		 * 2. Fetch all in-memory reports.
		 * 3. Merge in-memory reports that match filters and are newer.
		 * 4. Return merged results mapped to DTO.
		 */
		public List<WhatsAppNumberReportDto> findAllByDayUpdatedGreaterThanEqualAndPhoneNumberMainAndTypeOfReportAndOrganization(
		        Date dayUpdated, String phoneNumberMain, String typeOfReport, String organization) {

		    System.out.println("[START] Fetching WhatsAppNumberReports for phoneNumberMain: " + phoneNumberMain
		            + ", typeOfReport: " + typeOfReport + ", organization: " + organization + ", after date: " + dayUpdated);

		    saveDataFromMemoryToDatabase();
		    // Fetch DB reports with full filter
		    List<WhatsAppNumberReport> dbReports =
		            whatsAppNumberReportRepository.findAllByDayUpdatedGreaterThanEqualAndPhoneNumberMainAndTypeOfReportAndOrganization(
		                    dayUpdated, phoneNumberMain, typeOfReport, organization);
		    System.out.println("[INFO] DB report count: " + (dbReports != null ? dbReports.size() : 0));


		   
		    // Convert merged map values to DTO list and return
		    List<WhatsAppNumberReportDto> result = dbReports
		        .stream()
		        .map(whatsAppNumberReportMapper::mapWhatsAppNumberReportToDTO)
		        .collect(Collectors.toList());

		    System.out.println("[END] Returning DTOs. Count: " + result.size());
		    return result;
		}

    
		
		public WhatsAppReportVariableDto getReportCountForDashboard(WhatsAppReportRequest whatsAppReportRequest) {
		    WhatsAppReportVariableDto toReturn = new WhatsAppReportVariableDto();
		    toReturn = setReportDefaultValues(toReturn);

		    try {
		        System.out.println("[START] getReportCountForDashboard - Request: " + whatsAppReportRequest);
		        saveDataFromMemoryToDatabase();
		        // 1. Get date range
		        Date startDate = new Date();
		        Date endDate = new Date();
		        try {
		            System.out.println("Fetching start and end date for date range: " + whatsAppReportRequest.getDateRange());
		            List<Date> returnDatesList = reportDateRangeService.fetchStartAndEndDateListAsPerDateRange(whatsAppReportRequest.getDateRange());
		            if (returnDatesList != null && returnDatesList.size() >= 2) {
		                startDate = returnDatesList.get(0);
		                endDate = returnDatesList.get(1);
		            } else {
		                System.out.println("Warning: Invalid or null date range received");
		            }
		            System.out.println("Start date: " + startDate + ", End date: " + endDate);
		        } catch (Exception e) {
		            System.out.println("Error while fetching date range:");
		            e.printStackTrace();
		            throw e;
		        }

		        // 2. Fetch DB records
		        List<WhatsAppNumberReport> dbReports = null;
		        if (whatsAppReportRequest.getWhatsAppPhoneNumbers() == null || whatsAppReportRequest.getWhatsAppPhoneNumbers().isEmpty()) {
		            System.out.println("Fetching all records without filtering by phone numbers");
		            dbReports = whatsAppNumberReportRepository.findAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganization(
		                    startDate, endDate, whatsAppReportRequest.getOrganization());
		        } else {
		            System.out.println("Fetching records filtered by phone numbers");
		            dbReports = whatsAppNumberReportRepository.findAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganizationAndPhoneNumberIn(
		                    startDate, endDate, whatsAppReportRequest.getOrganization(), whatsAppReportRequest.getWhatsAppPhoneNumbers());
		        }

		        System.out.println("DB records fetched: " + (dbReports != null ? dbReports.size() : 0));

	
		        // 3. Aggregate metrics from merged reports
		        if(dbReports != null)
		        for (WhatsAppNumberReport report : dbReports) {
		            toReturn.setTotalMessagesReceived(toReturn.getTotalMessagesReceived() + safeLong(report.getTotalMessagesReceived()));
		            toReturn.setTotalMediaSizeSendMB(toReturn.getTotalMediaSizeSendMB() + safeLong(report.getTotalMediaSizeSendMB()));
		            toReturn.setTotalAmountSpend(toReturn.getTotalAmountSpend() + safeLong(report.getTotalAmountSpend()));
		            toReturn.setTotalTokenReceived(toReturn.getTotalTokenReceived() + safeLong(report.getTotalTokenReceived()));
		            toReturn.setTotalTokenSend(toReturn.getTotalTokenSend() + safeLong(report.getAiTokenSend()));

		            toReturn.setTotalMessagesSend(toReturn.getTotalMessagesSend()
		                    + safeLong(report.getManualMessageSend())
		                    + safeLong(report.getCampaignMessageSend())
		                    + safeLong(report.getAiMessagesSend()));

		            toReturn.setManualMessagesSend(toReturn.getManualMessagesSend() + safeLong(report.getManualMessageSend()));
		            toReturn.setCampaignMessagesSend(toReturn.getCampaignMessagesSend() + safeLong(report.getCampaignMessageSend()));
		            toReturn.setAiMessagesSend(toReturn.getAiMessagesSend() + safeLong(report.getAiMessagesSend()));

		            toReturn.setTotalMessagesDelivered(toReturn.getTotalMessagesDelivered()
		                    + safeLong(report.getManualMessageDelivered())
		                    + safeLong(report.getCampaignMessageDelivered())
		                    + safeLong(report.getAiMessagesDelivered()));

		            toReturn.setManualMessagesDelivered(toReturn.getManualMessagesDelivered() + safeLong(report.getManualMessageDelivered()));
		            toReturn.setCampaignMessagesDelivered(toReturn.getCampaignMessagesDelivered() + safeLong(report.getCampaignMessageDelivered()));
		            toReturn.setAiMessagesDelivered(toReturn.getAiMessagesDelivered() + safeLong(report.getAiMessagesDelivered()));

		            toReturn.setTotalMessagesRead(toReturn.getTotalMessagesRead()
		                    + safeLong(report.getManualMessageRead())
		                    + safeLong(report.getCampaignMessageRead())
		                    + safeLong(report.getAiMessagesRead()));

		            toReturn.setManualMessagesRead(toReturn.getManualMessagesRead() + safeLong(report.getManualMessageRead()));
		            toReturn.setCampaignMessagesRead(toReturn.getCampaignMessagesRead() + safeLong(report.getCampaignMessageRead()));
		            toReturn.setAiMessagesRead(toReturn.getAiMessagesRead() + safeLong(report.getAiMessagesRead()));

		            toReturn.setTotalMessagesFailed(toReturn.getTotalMessagesFailed()
		                    + safeLong(report.getManualMessageFailed())
		                    + safeLong(report.getCampaignMessageFailed())
		                    + safeLong(report.getAiMessagesFailed()));

		            toReturn.setManualMessagesFailed(toReturn.getManualMessagesFailed() + safeLong(report.getManualMessageFailed()));
		            toReturn.setCampaignMessagesFailed(toReturn.getCampaignMessagesFailed() + safeLong(report.getCampaignMessageFailed()));
		            toReturn.setAiMessagesFailed(toReturn.getAiMessagesFailed() + safeLong(report.getAiMessagesFailed()));

		            toReturn.setTotalMessagesDeleted(toReturn.getTotalMessagesDeleted()
		                    + safeLong(report.getManualMessageDeleted())
		                    + safeLong(report.getCampaignMessageDeleted())
		                    + safeLong(report.getAiMessagesDeleted()));

		            toReturn.setManualMessagesDeleted(toReturn.getManualMessagesDeleted() + safeLong(report.getManualMessageDeleted()));
		            toReturn.setCampaignMessagesDeleted(toReturn.getCampaignMessagesDeleted() + safeLong(report.getCampaignMessageDeleted()));
		            toReturn.setAiMessagesDeleted(toReturn.getAiMessagesDeleted() + safeLong(report.getAiMessagesDeleted()));
		        }

		        // 7. Log the aggregated result concisely
		        System.out.println("[RESULT] Aggregated WhatsAppReportVariableDto: " + toReturn);

		    } catch (Exception e) {
		        System.out.println("Exception in getReportCountForDashboard:");
		        e.printStackTrace();
		        throw e;
		    }

		    return toReturn;
		}


		private long safeLong(Long val) {
		    return val != null ? val : 0L;
		}
		

		public List<WhatsAppMessageCountForNumberDTO> getReportCountForDashboardForNumber(WhatsAppReportRequest whatsAppReportRequest) {
		    List<WhatsAppMessageCountForNumberDTO> toReturn = null;

		    try {
		    		    	
		        System.out.println("Entered getReportCountForDashboardForNumber Service");
		        saveDataFromMemoryToDatabase();
				System.out.println("Fetching report data");
		        Date endDate = new Date();
		        Date startDate = new Date();

		        try {
		            System.out.println("Fetching start and end date for date range: " + whatsAppReportRequest.getDateRange());
		            List<Date> returnDatesList = reportDateRangeService.fetchStartAndEndDateListAsPerDateRange(whatsAppReportRequest.getDateRange());
		            if (returnDatesList != null && returnDatesList.size() >= 2) {
		                startDate = returnDatesList.get(0);
		                endDate = returnDatesList.get(1);
		            } else {
		                System.out.println("Warning: Invalid returnDatesList size or null");
		            }
		            System.out.println("Start date: " + startDate + ", End date: " + endDate);
		        } catch (Exception e) {
		            System.out.println("Error while fetching date range:");
		            e.printStackTrace();
		            throw e;
		        }

		        System.out.println("List of Phone Numbers: " + whatsAppReportRequest.getWhatsAppPhoneNumbers());

		        System.out.println("Fetching grouped data from database");

		        if (whatsAppReportRequest.getWhatsAppPhoneNumbers() == null || whatsAppReportRequest.getWhatsAppPhoneNumbers().isEmpty()) {
		            toReturn = whatsAppNumberReportRepository.findGroupByNumberAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganization(
		                    startDate, endDate, whatsAppReportRequest.getOrganization());
		        } else {
		            toReturn = whatsAppNumberReportRepository.findGroupByNumberAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganizationAndPhoneNumberIn(
		                    startDate, endDate, whatsAppReportRequest.getOrganization(), whatsAppReportRequest.getWhatsAppPhoneNumbers());
		        }

		        System.out.println("Number of grouped records fetched from DB: " + (toReturn != null ? toReturn.size() : 0));

		        // Pretty print final DTO list
		        try {
		            String jsonOutput = objectMapper.writeValueAsString(toReturn);
		            System.out.println("[FINAL RESULT] WhatsAppMessageCountForNumberDTO list: \n" + jsonOutput);
		        } catch (Exception e) {
		            System.out.println("[WARN] Failed to serialize final DTO list for logging.");
		            e.printStackTrace();
		        }

		    } catch (Exception e) {
		        System.out.println("Exception in getReportCountForDashboardForNumber:");
		        e.printStackTrace();
		        throw e;
		    }

		    return toReturn;
		}

	
		
		public List<WhatsAppReportNumberResponseDTO> getReportCountForDashboardForNumberByTime(WhatsAppReportRequest whatsAppReportRequest){
		    List<WhatsAppReportNumberResponseDTO> toReturn = new ArrayList<>();
		    List<WhatsAppMessageCountForNumberDTO> actualData = null;
		    Map<String, WhatsAppReportNumberResponseDTO> returnMap = new HashMap<>();

		    try
		    {
		        System.out.println("getReportCountForDashboardForNumberByTime Service started");
		        saveDataFromMemoryToDatabase();
		
				System.out.println("Fetching report data");
		        Date endDate = new Date(); 
		        Date startDate = new Date(); 

		        try {
		            List<Date> returnDatesList = reportDateRangeService.fetchStartAndEndDateListAsPerDateRange(whatsAppReportRequest.getDateRange());
		            startDate = returnDatesList.get(0);
		            endDate = returnDatesList.get(1);
		            System.out.println("Date Range - startDate: " + startDate + ", endDate: " + endDate);
		        }
		        catch(Exception e)
		        {
		            System.out.println("Exception while fetching date range:");
		            e.printStackTrace();
		            throw e;
		        }

		        System.out.println("List of Phone Numbers : " + whatsAppReportRequest.getWhatsAppPhoneNumbers());

		        System.out.println("Fetching data from repository");
		        if(whatsAppReportRequest.getWhatsAppPhoneNumbers() == null || whatsAppReportRequest.getWhatsAppPhoneNumbers().isEmpty())
		        {
		            actualData =  whatsAppNumberReportRepository.findGroupByNumberAndTimeAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganization(
		                startDate, endDate, whatsAppReportRequest.getOrganization());    
		            System.out.println("Fetched data without phone number filter. Count: " + (actualData != null ? actualData.size() : 0));
		        }
		        else
		        {
		            actualData =  whatsAppNumberReportRepository.findGroupByNumberAndTimeAllByDayUpdatedGreaterThanEqualAndDayUpdatedLessThanAndOrganizationAndPhoneNumberIn(
		                startDate, endDate, whatsAppReportRequest.getOrganization(), whatsAppReportRequest.getWhatsAppPhoneNumbers());
		            System.out.println("Fetched data with phone number filter. Count: " + (actualData != null ? actualData.size() : 0));
		        }

		        
		        System.out.println("Processing fetched DB data for sending response");
		        if(actualData != null && !actualData.isEmpty())
		        {
		            for (int i = 0; i < actualData.size(); i++)
		            {
		                WhatsAppMessageCountForNumberDTO currentData = actualData.get(i);
		                WhatsAppReportNumberResponseDTO whatsAppReportNumberResponseDTO = returnMap.get(currentData.getPhoneNumberMain());

		                if(whatsAppReportNumberResponseDTO != null)
		                {
		                        List<WhatsAppMessageCountForNumberDTO> data = whatsAppReportNumberResponseDTO.getMessageDetails();
		                        if(data == null) {
		                            data = new ArrayList<>();
		                        }
		                        data.add(currentData);  // Added missing add of current data here!
		                        whatsAppReportNumberResponseDTO.setMessageDetails(data);
		                        returnMap.put(currentData.getPhoneNumberMain(), whatsAppReportNumberResponseDTO);
		                        System.out.println("Added message detail to existing phone number: " + currentData.getPhoneNumberMain());
		           
		                }
		                else
		                {
		                	whatsAppReportNumberResponseDTO = new WhatsAppReportNumberResponseDTO();
		                    whatsAppReportNumberResponseDTO.setAdminFirstName(currentData.getAdminFirstName());
		                    whatsAppReportNumberResponseDTO.setAdminLastName(currentData.getAdminLastName());
		                    whatsAppReportNumberResponseDTO.setAdminPhoneNumber(currentData.getAdminPhoneNumber());
		                    whatsAppReportNumberResponseDTO.setAdminExtension(currentData.getAdminExtension());
		                    whatsAppReportNumberResponseDTO.setAdminEmail(currentData.getAdminEmail());
		                    whatsAppReportNumberResponseDTO.setPhoneNumberMain(currentData.getPhoneNumberMain());  // intentional as original
		                    List<WhatsAppMessageCountForNumberDTO> data = new ArrayList<>();
		                    data.add(currentData);
		                    whatsAppReportNumberResponseDTO.setMessageDetails(data);
		                    returnMap.put(currentData.getPhoneNumberMain(), whatsAppReportNumberResponseDTO);
		                    System.out.println("Created new entry for phone number: " + currentData.getPhoneNumberMain());
		               
		                 }
		            }
		        }
		        else
		        {
		            System.out.println("No data found for given criteria.");
		        }

		    }
		    catch(Exception e)
		    {
		        System.out.println("Exception in getReportCountForDashboardForNumberByTime:");
		        e.printStackTrace();
		        throw e;
		    }

		    try {
		        if(returnMap != null && !returnMap.isEmpty())
		        {
		            System.out.println("Converting map values to list");
		            for (Map.Entry<String, WhatsAppReportNumberResponseDTO> entry : returnMap.entrySet()) {
		                toReturn.add(entry.getValue());
		            }
		            System.out.println("Total number of entries to return: " + toReturn.size());
		        }
		        else
		        {
		            System.out.println("Return map is empty, nothing to add to return list.");
		        }
		    }
		    catch(Exception e)
		    {
		        System.out.println("Exception while converting map to list:");
		        e.printStackTrace();
		        throw e;
		    }

		    System.out.println("getReportCountForDashboardForNumberByTime Service ended");
		    return toReturn;
		}

		
		
		public static void updateReportFromMemory(WhatsAppNumberReport dbReport, WhatsAppNumberReport memoryReport) {
		    if (dbReport == null || memoryReport == null) {
		        System.out.println("[WARN] Either dbReport or memoryReport is null. Skipping update.");
		        return;
		    }

		    dbReport.setPhoneNumberMain(memoryReport.getPhoneNumberMain());
		    dbReport.setPhoneNumberWith(memoryReport.getPhoneNumberWith());

		    dbReport.setManualMessageSend(memoryReport.getManualMessageSend());
		    dbReport.setCampaignMessageSend(memoryReport.getCampaignMessageSend());
		    dbReport.setAiMessagesSend(memoryReport.getAiMessagesSend());
		    dbReport.setTotalMessagesReceived(memoryReport.getTotalMessagesReceived());

		    dbReport.setManualMessageDelivered(memoryReport.getManualMessageDelivered());
		    dbReport.setCampaignMessageDelivered(memoryReport.getCampaignMessageDelivered());
		    dbReport.setAiMessagesDelivered(memoryReport.getAiMessagesDelivered());

		    dbReport.setManualMessageRead(memoryReport.getManualMessageRead());
		    dbReport.setCampaignMessageRead(memoryReport.getCampaignMessageRead());
		    dbReport.setAiMessagesRead(memoryReport.getAiMessagesRead());

		    dbReport.setManualMessageFailed(memoryReport.getManualMessageFailed());
		    dbReport.setCampaignMessageFailed(memoryReport.getCampaignMessageFailed());
		    dbReport.setAiMessagesFailed(memoryReport.getAiMessagesFailed());

		    dbReport.setManualMessageDeleted(memoryReport.getManualMessageDeleted());
		    dbReport.setCampaignMessageDeleted(memoryReport.getCampaignMessageDeleted());
		    dbReport.setAiMessagesDeleted(memoryReport.getAiMessagesDeleted());

		    dbReport.setAiTokenSend(memoryReport.getAiTokenSend());
		    dbReport.setTotalTokenReceived(memoryReport.getTotalTokenReceived());
		    dbReport.setTotalAmountSpend(memoryReport.getTotalAmountSpend());
		    dbReport.setTotalMediaSizeSendMB(memoryReport.getTotalMediaSizeSendMB());

		    dbReport.setTypeOfReport(memoryReport.getTypeOfReport());
		    dbReport.setExtensionReport(memoryReport.getExtensionReport());
		    dbReport.setDayUpdated(memoryReport.getDayUpdated());
		    dbReport.setOrganization(memoryReport.getOrganization());
		    dbReport.setLastUpdatedOn(memoryReport.getLastUpdatedOn());

		    System.out.println("[INFO] dbReport updated from memoryReport for key: " +
		        memoryReport.getPhoneNumberMain() + " :: " + memoryReport.getPhoneNumberWith());
		}


		public WhatsAppNumberReport setDefaultValues(WhatsAppNumberReport input){
		    System.out.println("Setting default values for WhatsAppNumberReport");
		    input.setManualMessageSend(0L);
		    input.setCampaignMessageSend(0L);
		    input.setAiMessagesSend(0L);
		    input.setTotalMessagesReceived(0L);
		    input.setManualMessageDelivered(0L);
		    input.setCampaignMessageDelivered(0L);
		    input.setAiMessagesDelivered(0L);
		    input.setManualMessageRead(0L);
		    input.setCampaignMessageRead(0L);
		    input.setAiMessagesRead(0L);
		    input.setManualMessageFailed(0L);
		    input.setCampaignMessageFailed(0L);
		    input.setAiMessagesFailed(0L);
		    input.setManualMessageDeleted(0L);
		    input.setCampaignMessageDeleted(0L);
		    input.setAiMessagesDeleted(0L);
		    input.setAiTokenSend(0L);
		    input.setTotalTokenReceived(0L);
		    input.setTotalAmountSpend(0L);
		    input.setTotalMediaSizeSendMB(0L);
		    System.out.println("Default values set");
		    return input;
		}

		public WhatsAppReportVariableDto setReportDefaultValues(WhatsAppReportVariableDto input){
		    System.out.println("Setting default values for WhatsAppReportVariableDto");
		    input.setTotalMessagesReceived(0L);
		    input.setTotalMediaSizeSendMB(0L);
		    input.setTotalAmountSpend(0L);
		    input.setTotalTokenReceived(0L);
		    input.setTotalTokenSend(0L);

		    input.setTotalMessagesSend(0L);
		    input.setManualMessagesSend(0L);
		    input.setCampaignMessagesSend(0L);
		    input.setAiMessagesSend(0L);

		    input.setTotalMessagesDelivered(0L);
		    input.setManualMessagesDelivered(0L);
		    input.setCampaignMessagesDelivered(0L);
		    input.setAiMessagesDelivered(0L);

		    input.setTotalMessagesRead(0L);
		    input.setManualMessagesRead(0L);
		    input.setCampaignMessagesRead(0L);
		    input.setAiMessagesRead(0L);

		    input.setTotalMessagesFailed(0L);
		    input.setManualMessagesFailed(0L);
		    input.setCampaignMessagesFailed(0L);
		    input.setAiMessagesFailed(0L);

		    input.setTotalMessagesDeleted(0L);
		    input.setManualMessagesDeleted(0L);
		    input.setCampaignMessagesDeleted(0L);
		    input.setAiMessagesDeleted(0L);

		    System.out.println("Default values set");
		    return input;
		}

}
