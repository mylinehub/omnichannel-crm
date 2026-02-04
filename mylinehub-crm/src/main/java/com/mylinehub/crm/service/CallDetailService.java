package com.mylinehub.crm.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.ami.TaskScheduler.RefreshBackEndConnectionRunnable;
import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.dto.CallCountByCallTypeDTO;
import com.mylinehub.crm.entity.dto.CallCountForEmployeeByTimeDTO;
import com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO;
import com.mylinehub.crm.entity.dto.CallDashboardCallDetailsDTO;
import com.mylinehub.crm.entity.dto.CallDetailDTO;
import com.mylinehub.crm.entity.dto.CallDetailPageDTO;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.enums.CALLTYPE;
import com.mylinehub.crm.enums.DASHBOARD_TIME_LINE;
import com.mylinehub.crm.exports.excel.ExportCallDetailToXLSX;
import com.mylinehub.crm.exports.pdf.ExportCallDetailToPDF;
import com.mylinehub.crm.mapper.CallDetailMapper;
import com.mylinehub.crm.repository.CallDetailRepository;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.requests.CallDashboardRequest;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.utils.ReportDateRangeService;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class CallDetailService implements CurrentTimeInterface{

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final CallDetailRepository callDetailRepository;
    private final CustomerRepository customerepository;
    private final CallDetailMapper callDetailMapper;
    private final ErrorRepository errorRepository;
    private final ApplicationContext applicationContext;
    private final ReportDateRangeService reportDateRangeService;
    private static Date lastRefreshDate = new Date();
    
    
    /**
     * Insert CDR information coming from VoiceBridge AI Call Session
     * into CDRMemoryCollection.
     *
     *  - Input: CdrDTO (flattened session info from VoiceBridge)
     *  - Creates/updates CallDetail inside the CdrDTO
     *  - Stores in CDRMemoryCollection interim map using callSessionId as key
     */
    public Boolean insertCdrFromVoiceBridge(CdrDTO cdrDTO) {
        Boolean toReturn = true;
        System.out.println("insertCdrFromVoiceBridge : START");

        try {
            if (cdrDTO == null) {
                System.out.println("insertCdrFromVoiceBridge : cdrDTO is null");
                return false;
            }

            // Use callSessionId as the unique key, if not present generate AICALL-<epoch>
            String sessionId = cdrDTO.getCallSessionId();
            if (sessionId == null || sessionId.trim().equals("")) {
                sessionId = "AICALL-" + System.currentTimeMillis();
                cdrDTO.setCallSessionId(sessionId);
                System.out.println("insertCdrFromVoiceBridge : generated callSessionId = " + sessionId);
            } else {
                System.out.println("insertCdrFromVoiceBridge : existing callSessionId = " + sessionId);
            }

            CallDetail callDetail = cdrDTO.getCallDetail();
            if (callDetail == null) {
                System.out.println("insertCdrFromVoiceBridge : callDetail was null, creating new CallDetail()");
                callDetail = new CallDetail();
            }

            // Caller id
            String callerid = cdrDTO.getCallerid();
            if (callerid == null || callerid.trim().equals("")) {
                callerid = "AICALL-" + System.currentTimeMillis();
                System.out.println("insertCdrFromVoiceBridge : callerid was null, using " + callerid);
            }
            callDetail.setCallerid(callerid);

            // Customer id: if not there, use callerid
            String customerid = cdrDTO.getCustomerid();
            if (customerid == null || customerid.trim().equals("")) {
                customerid = callerid;
                System.out.println("insertCdrFromVoiceBridge : customerid was null, using callerid " + customerid);
            }
            callDetail.setCustomerid(customerid);

            // Employee name: if empty, mark AI-Call
            String employeeName = cdrDTO.getEmployeeName();
            if (employeeName == null || employeeName.trim().equals("")) {
                employeeName = "AI-Call";
                System.out.println("insertCdrFromVoiceBridge : employeeName was null, using 'AI-Call'");
            }
            callDetail.setEmployeeName(employeeName);

            // Customer name: if empty, use phone number
            String customerName = cdrDTO.getCustomerName();
            if (customerName == null || customerName.trim().equals("")) {
                customerName = callerid;
                System.out.println("insertCdrFromVoiceBridge : customerName was null, using callerid as name");
            }
            callDetail.setCustomerName(customerName);

            // Organization
            String organization = cdrDTO.getOrganization();
            callDetail.setOrganization(organization);

            // Call flags
            callDetail.setCallonmobile(cdrDTO.isCallonmobile());
            callDetail.setCallType(cdrDTO.getCallType());
            callDetail.setPridictive(cdrDTO.isPridictive());
            callDetail.setProgressive(cdrDTO.isProgressive());
            callDetail.setIsconference(cdrDTO.isIsconference());
            callDetail.setIvr(cdrDTO.isIvr());
            callDetail.setQueue(cdrDTO.isQueue());

            // Session id into CallDetail
            callDetail.setCallSessionId(sessionId);

            // Duration: if bridgeEnterTime present, compute duration until now
            double existingDuration = callDetail.getCalldurationseconds();
            if (cdrDTO.getBridgeEnterTime() != null) {
                Date now = new Date();
                long duration = now.getTime() - cdrDTO.getBridgeEnterTime().getTime();
                long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                if (existingDuration <= 0) {
                    callDetail.setCalldurationseconds(diffInSeconds);
                } else {
                    callDetail.setCalldurationseconds(existingDuration + diffInSeconds);
                }
                System.out.println("insertCdrFromVoiceBridge : calldurationseconds = " + callDetail.getCalldurationseconds());
            }

            // Common defaults if missing
            if (callDetail.getTimezone() == null) {
                callDetail.setTimezone(TimeZone.getDefault());
            }
            if (callDetail.getStartdate() == null) {
                callDetail.setStartdate(new Date());
            }
            if (callDetail.getCountry() == null || callDetail.getCountry().trim().equals("")) {
                callDetail.setCountry("AI-Call");
            }
            callDetail.setIsactive(false);
            callDetail.setIsconnected(false);

            // Put back into CDRDTO and mark lastUpdated
            cdrDTO.setCallDetail(callDetail);
            cdrDTO.setLastUpdated(new Date());

            // Store in CDRMemoryCollection interim map
            Map<String, CdrDTO> record = new HashMap<>();
            record.put(sessionId, cdrDTO);
            CDRMemoryCollection.workWithCDRInterimData(sessionId, record, "update");

            System.out.println("insertCdrFromVoiceBridge : STORED in CDRMemoryCollection for sessionId = " + sessionId);
        }
        catch (Exception e) {
            toReturn = false;
            e.printStackTrace();
        }

        System.out.println("insertCdrFromVoiceBridge : END");
        return toReturn;
    }

	public boolean addCustomerIfRequiredAndConvert(String phoneNumber, boolean converted,String organization) {
	    	boolean toReturn = true;
	    	try {
	    		
	    		String searchPhoneNumber = "";
	    		//Sort phone number
	    		if(phoneNumber.startsWith("+91"))
				{
	    			searchPhoneNumber = phoneNumber.substring(3);
				}
				else if(phoneNumber.startsWith("91"))
				{
					searchPhoneNumber = phoneNumber.substring(2);
				}
				else
				{
					searchPhoneNumber = phoneNumber;
				}
	    	    
	    		Customers customer = customerepository.findByPhoneNumberContainingAndOrganization(searchPhoneNumber,organization);
	    		
	    		if(customer!= null && converted)
	    		{
	    			customerepository.customerGotConverted(customer.getId());
	    		}
	    		else if(customer !=null && !converted)
	    		{
	    			customerepository.customerGotDiverted(customer.getId());
	    		}
	    		else if(customer == null)
	    		{
	    			customer = new Customers();
	    			customer.setPhoneNumber(phoneNumber);
	    			customer.setFirstname("Unidentified");
	    			customer.setLastname("Customer");
	    			customer.setCoverted(converted);
	    			customerepository.save(customer);
	    		}
	    	}
	    	catch(Exception e)
	    	{
	    		toReturn = false;
	    		e.printStackTrace();
	    	}
	    	return toReturn;
	}
	
	public boolean addCustomerIfRequiredAndUpdateRemark(String phoneNumber, String remark,String organization) {
	    	boolean toReturn = true;
	    	try {
	    		
	    		String searchPhoneNumber = "";
	    		//Sort phone number
	    		if(phoneNumber.startsWith("+91"))
				{
	    			searchPhoneNumber = phoneNumber.substring(3);
				}
				else if(phoneNumber.startsWith("91"))
				{
					searchPhoneNumber = phoneNumber.substring(2);
				}
				else
				{
					searchPhoneNumber = phoneNumber;
				}
	    	    
	    		Customers customer = customerepository.findByPhoneNumberContainingAndOrganization(searchPhoneNumber,organization);
	    		
	    		if(customer!= null)
	    		{
	    			customerepository.updateCustomerDescription(remark,customer.getId());
	    		}
	    		else if(customer == null)
	    		{
	    			customer = new Customers();
	    			customer.setPhoneNumber(phoneNumber);
	    			customer.setFirstname("Unidentified");
	    			customer.setLastname("Customer");
	    			customer.setDescription(remark);
	    			customerepository.save(customer);
	    		}
	    	}
	    	catch(Exception e)
	    	{
	    		toReturn = false;
	    		e.printStackTrace();
	    	}
	    	return toReturn;
	}
	    
    public List<CallDetailDTO> addCustomerInfoToCallDetailsDTO(List<CallDetailDTO> callDetailsDTO, String organization) {
    	
    	try {
    		if(callDetailsDTO.size() > 0)
    		{
      		  List<String> allPhoneNumbers = new ArrayList<String>();
      		  callDetailsDTO.forEach((element)->{
      			  allPhoneNumbers.add(element.getCustomerid());
      		  });
      		  
      		  final List<Customers> customers = customerepository.findAllCustomersByPhoneNumberInAndOrganization(allPhoneNumbers,organization);
      		  
      		  if(customers != null)
      		  {
      			  for (int j = 0; j < callDetailsDTO.size(); j++)
      			  {
      				  for(int i=0 ; i< customers.size(); i++)
      				  {
      					  if(customers.get(i).getPhoneNumber().equals(callDetailsDTO.get(j).getCustomerid()))
      					  {
      						callDetailsDTO.get(j).setCoverted(customers.get(i).isCoverted());
      						callDetailsDTO.get(j).setDescription(customers.get(i).getDescription());
      						break;
      					  }
      				  }
	      		  }
      		  }
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
        return callDetailsDTO;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public CallDetail createCallDetailByOrganization(CallDetailDTO callDetails) {
    	CallDetail current = callDetailMapper.mapDTOToCallDetail(callDetails);
    	CallDetail toReturn = callDetailRepository.save(current);
        return toReturn;
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetail findTopByCalleridOrderByIdDesc(String callerid){
        return callDetailRepository.findTopByCalleridOrderByIdDesc(callerid);
    }
  
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     * @throws Exception 
     */
    
    public Boolean refreshConnections() throws Exception{
    	Boolean toReturn = true;
		
    		try {
				Date now = new Date();
		        long duration  = now.getTime() - lastRefreshDate.getTime();
				long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
	//			long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
	//			long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
	//			long diffInDays = TimeUnit.MILLISECONDS.toDays(duration);
					
				LoggerUtils.log.debug("now : "+ diffInMinutes);
				LoggerUtils.log.debug("lastRefreshDate : "+ lastRefreshDate);
				LoggerUtils.log.debug("diffInMinutes : "+ diffInMinutes);
				
				if(diffInMinutes>=10)
				{
					CallDetailService.lastRefreshDate = new Date();
					LoggerUtils.log.debug("Difference more than 10 minutes");
					RefreshBackEndConnectionRunnable refreshBackEndConnectionRunnable = new RefreshBackEndConnectionRunnable();
	        		refreshBackEndConnectionRunnable.setErrorRepository(errorRepository);
	        		refreshBackEndConnectionRunnable.setApplicationContext(applicationContext);
	        		refreshBackEndConnectionRunnable.execute("AMI");
				}
				else
				{
					LoggerUtils.log.debug("Difference less than 10 minutes, no refresh required");
				}
		}
		catch(Exception e)
		{
			toReturn = false;
			e.printStackTrace();
			throw e;
		}
        return toReturn;
    }

    public CallDashboardCallDetailsDTO getCallCountForDashboard(CallDashboardRequest callDashboardRequest,String organization){
		 CallDashboardCallDetailsDTO toReturn = new CallDashboardCallDetailsDTO();
	    	
    	 try
    	 {
        	 
        	 LoggerUtils.log.debug("GetCallCountForDashboard Service");
        	 Date endDate = new Date(); 
        	 Date startDate = new Date(); 
        	 
        	 try {
        		 List<Date> returnDatesList = reportDateRangeService.fetchStartAndEndDateListAsPerDateRange(callDashboardRequest.getDateRange());
        		 startDate = returnDatesList.get(0);
        		 endDate = returnDatesList.get(1);
        	 }
        	 catch(Exception e)
        	 {
        		 e.printStackTrace();
        		 throw e;
        	 }
        	 
        	 LoggerUtils.log.debug("List of Extensions : "+ callDashboardRequest.getExtensions());
        	 List<CallCountByCallTypeDTO> callCountByCallTypeDTOs = null;
        	 CallCountForEmployeeDTO callCountForEmployeeDTO = null;
        	 
        	 if(callDashboardRequest.getExtensions()==null || callDashboardRequest.getExtensions().size()==0)
        	 {
            	 callCountByCallTypeDTOs = callDetailRepository.findGroupByCallTypeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(startDate,endDate,callDashboardRequest.organization,"");
            	 callCountForEmployeeDTO = callDetailRepository.findByCallGreatorThanXSecondAndStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(startDate,endDate,callDashboardRequest.organization,"",1);
            	 toReturn.setConverted(Integer.parseInt(String.valueOf(customerepository.findCountOfAllCustomersByConvertedAndAsPerCallDetailRange(startDate,endDate,callDashboardRequest.organization,"",true))));
        	 }
        	 else
        	 {
        		 
        		 callCountByCallTypeDTOs = callDetailRepository.findGroupByCallTypeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(startDate,endDate,callDashboardRequest.organization,"",callDashboardRequest.getExtensions());
        		 callCountForEmployeeDTO = callDetailRepository.findByCallGreatorThanXSecondAndStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(startDate,endDate,callDashboardRequest.organization,"",callDashboardRequest.getExtensions(),1);
        		 toReturn.setConverted(Integer.parseInt(String.valueOf(customerepository.findCountOfAllCustomersByConvertedAndAsPerCallDetailRangeAndExtensionIn(startDate,endDate,callDashboardRequest.organization,"",callDashboardRequest.getExtensions(),true))));
        	 }
        	 
        	 toReturn.setTotalCalls(0);
        	 
        	 if(callCountByCallTypeDTOs != null && callCountByCallTypeDTOs.size() > 0)
        	 callCountByCallTypeDTOs.forEach((element)->{
        		 
        		 if(element.getCallType().equals(CALLTYPE.Inbound.name()))
        		 {
        			 toReturn.setIncomingCalls(Integer.parseInt(String.valueOf(element.getTotalCalls())));
        			 toReturn.setTotalCalls(toReturn.getIncomingCalls() + toReturn.getTotalCalls());
        		 }
        		 else if (element.getCallType().equals(CALLTYPE.Outbound.name()))
        		 {
        			 toReturn.setOutgoingCalls(Integer.parseInt(String.valueOf(element.getTotalCalls())));
        			 toReturn.setTotalCalls(toReturn.getOutgoingCalls() + toReturn.getTotalCalls());
        		 }
        		 else
        		 {
        			 toReturn.setTotalCalls(Integer.parseInt(String.valueOf(element.getTotalCalls())) + toReturn.getTotalCalls());
        		 } 
        	 });
        	 
        	 if(callCountForEmployeeDTO != null)
             toReturn.setCallsConnected(Integer.parseInt(String.valueOf(callCountForEmployeeDTO.getTotalCalls()))); 
        	 
        	 
        	 // 1) Fetch org from memory (no DB)
             Map<String, Organization> one = OrganizationData.workWithAllOrganizationData(
                     organization, null, "get-one", null
             );

             if (one == null || one.get(organization) == null) {
                 System.out.println("org not found in memory: " + organization);
                 return null;
             }

             Organization org = one.get(organization);
             
        	 toReturn.setTotalAmount(org.getCallingTotalAmountLoaded());
        	 toReturn.setTotalSpend(org.getCallingTotalAmountSpend());
        	 
        	 LoggerUtils.log.debug("totalAmount : " + toReturn.getTotalAmount());
        	 LoggerUtils.log.debug("totalSpend : " + toReturn.getTotalSpend());
        	 LoggerUtils.log.debug("totalCalls : " + toReturn.getTotalCalls());
        	 LoggerUtils.log.debug("incomingCalls : " + toReturn.getIncomingCalls());
        	 LoggerUtils.log.debug("outgoingCalls : " + toReturn.getOutgoingCalls());
        	 LoggerUtils.log.debug("convertedCalls : " + toReturn.getConverted());
        	 LoggerUtils.log.debug("connectedCalls : " + toReturn.getCallsConnected());
        	 
    	 }
    	 catch(Exception e)
    	 {
    		 e.printStackTrace();
    		 throw e;
    	 }
    	
    	 return toReturn;
    }

    
    
    public List<CallCountForEmployeeDTO> getCallCountForDashboardForEmployee(CallDashboardRequest callDashboardRequest){
    	List<CallCountForEmployeeDTO> toReturn = null;
    	List<CallCountForEmployeeDTO> actuallyReturned = new ArrayList<>();
    	
    	 try
    	 {
        	 
        	 LoggerUtils.log.debug("GetCallCountForDashboard Service");
        	 Date endDate = new Date(); 
        	 Date startDate = new Date(); 
        	 
        	 try {
        		 List<Date> returnDatesList = reportDateRangeService.fetchStartAndEndDateListAsPerDateRange(callDashboardRequest.getDateRange());
        		 startDate = returnDatesList.get(0);
        		 endDate = returnDatesList.get(1);
        	 }
        	 catch(Exception e)
        	 {
        		 e.printStackTrace();
        		 throw e;
        	 }
        	 
        	 LoggerUtils.log.debug("List of Extensions : "+ callDashboardRequest.getExtensions());
        	 
        	 LoggerUtils.log.debug("Finding values from database");
        	 if(callDashboardRequest.getExtensions()==null || callDashboardRequest.getExtensions().size()==0)
        	 {
        		 toReturn =  callDetailRepository.findGroupByEmployeeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(startDate,endDate,callDashboardRequest.organization,"");
            	
        	 }
        	 else
        	 {
        		 toReturn = callDetailRepository.findGroupByEmployeeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(startDate,endDate,callDashboardRequest.organization,"",callDashboardRequest.getExtensions());
            	
        	 }
        	 
        	 //Fill all other information about employee before sending back
        	 LoggerUtils.log.debug("Setting other values");
        	 if(toReturn != null && toReturn.size()>0)
        	 {
        		for (int i =0 ; i< toReturn.size() ; i++)
        		{
        			LoggerUtils.log.debug("toReturn.get(i).getExtension() : "+toReturn.get(i).getExtension());
        			
        			LoggerUtils.log.debug("This value is allowed");
    				Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(toReturn.get(i).getExtension(), null, "get-one");
					EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
					if(allEmployeeDataAndState != null)
					{
						employeeDataAndStateDTO = allEmployeeDataAndState.get(toReturn.get(i).getExtension());
					} 

 					Employee currentEmployee= null;
 					
 					if(employeeDataAndStateDTO !=null)
 					{
 						currentEmployee =  employeeDataAndStateDTO.getEmployee();
 					}
 					
 					if(currentEmployee!=null) {
 						if (actuallyReturned.size() < 8)
 						{
 	 						toReturn.get(i).setFirstName(currentEmployee.getFirstName());
 	 						toReturn.get(i).setLastName(currentEmployee.getLastName());
 	 						toReturn.get(i).setPhoneNumber(currentEmployee.getPhonenumber());
 	 						actuallyReturned.add(toReturn.get(i));
 						}
     					
 					}
        			
        		}
        	 }
        	 
    	 }
    	 catch(Exception e)
    	 {
    		 e.printStackTrace();
    		 throw e;
    	 }
    	return actuallyReturned;
    }
    

    public List<CallCountForEmployeeByTimeDTO> getCallCountForDashboardForEmployeeByTime(CallDashboardRequest callDashboardRequest){
    	List<CallCountForEmployeeByTimeDTO> actuallyReturned = new ArrayList<>();
    	Map<String,CallCountForEmployeeByTimeDTO> returnMap = new HashMap<>();
    	List<CallCountForEmployeeDTO> actualData = null;
    	
    	 try
    	 {
        	 
        	 LoggerUtils.log.debug("getCallCountForDashboardForEmployeeByTime Service");
        	 Date endDate = new Date(); 
        	 Date startDate = new Date(); 
        	 
        	 try {
        		 List<Date> returnDatesList = reportDateRangeService.fetchStartAndEndDateListAsPerDateRange(callDashboardRequest.getDateRange());
        		 startDate = returnDatesList.get(0);
        		 endDate = returnDatesList.get(1);
        	 }
        	 catch(Exception e)
        	 {
        		 e.printStackTrace();
        		 throw e;
        	 }
        	 
        	 LoggerUtils.log.debug("List of Extensions : "+ callDashboardRequest.getExtensions());
        	 
        	 LoggerUtils.log.debug("Finding values from database");
        	 if(callDashboardRequest.getExtensions()==null || callDashboardRequest.getExtensions().size()==0)
        	 {
        		 actualData =  callDetailRepository.findGroupByEmployeeAndTimeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(startDate,endDate,callDashboardRequest.organization,"");
            	
        	 }
        	 else
        	 {
        		 actualData = callDetailRepository.findGroupByEmployeeAndTimeByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(startDate,endDate,callDashboardRequest.organization,"",callDashboardRequest.getExtensions());
            	
        	 }
        	 
        	 //Fill all other information about employee before sending back
        	 LoggerUtils.log.debug("Setting other values");
        	 if(actualData != null && actualData.size()>0)
        	 {
        		for (int i =0 ; i< actualData.size() ; i++)
        		{
        			LoggerUtils.log.debug("This value is allowed");
    				Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(actualData.get(i).getExtension(), null, "get-one");
					EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
					if(allEmployeeDataAndState != null)
					{
						employeeDataAndStateDTO = allEmployeeDataAndState.get(actualData.get(i).getExtension());
					} 

 					Employee currentEmployee= null;
 					
 					if(employeeDataAndStateDTO !=null)
 					{
 						currentEmployee =  employeeDataAndStateDTO.getEmployee();
 					}
 					
 					if(currentEmployee!=null) {
 						
 						CallCountForEmployeeByTimeDTO callCountForEmployeeByTimeDTO = returnMap.get(currentEmployee.getExtension());
 						
 						if(callCountForEmployeeByTimeDTO != null)
 						{
 							if (returnMap.size() < 8)
 							{
 								List<CallDashboardCallDetailsDTO> callDetails = callCountForEmployeeByTimeDTO.getCallDetails();
 	 							
 	 							CallDashboardCallDetailsDTO callDashboardCallDetailsDTO = new CallDashboardCallDetailsDTO ();
 	 							callDashboardCallDetailsDTO.setTotalCalls(Integer.parseInt(String.valueOf(actualData.get(i).getTotalCalls())));;
 	 							callDashboardCallDetailsDTO.setYear(actualData.get(i).getYear());
 	 							callDashboardCallDetailsDTO.setMonth(actualData.get(i).getMonth());
 	 							
 	 							callDetails.add(callDashboardCallDetailsDTO);
 	 							
 	 							callCountForEmployeeByTimeDTO.setCallDetails(callDetails);
 	 							returnMap.put(currentEmployee.getExtension(), callCountForEmployeeByTimeDTO);
 							}
 							
 						}
 						else
 						{
 							callCountForEmployeeByTimeDTO = new CallCountForEmployeeByTimeDTO();
 							callCountForEmployeeByTimeDTO.setFirstName(currentEmployee.getFirstName());
 							callCountForEmployeeByTimeDTO.setLastName(currentEmployee.getLastName());
 							callCountForEmployeeByTimeDTO.setPhoneNumber(currentEmployee.getPhonenumber());
 							callCountForEmployeeByTimeDTO.setExtension(currentEmployee.getExtension());
 							
 							List<CallDashboardCallDetailsDTO> callDetails = new ArrayList<>();
 							
 							CallDashboardCallDetailsDTO callDashboardCallDetailsDTO = new CallDashboardCallDetailsDTO ();
 							callDashboardCallDetailsDTO.setTotalCalls(Integer.parseInt(String.valueOf(actualData.get(i).getTotalCalls())));;
 							callDashboardCallDetailsDTO.setYear(actualData.get(i).getYear());
 							callDashboardCallDetailsDTO.setMonth(actualData.get(i).getMonth());
 							
 							callDetails.add(callDashboardCallDetailsDTO);
 							
 							callCountForEmployeeByTimeDTO.setCallDetails(callDetails);
 							
 							returnMap.put(currentEmployee.getExtension(), callCountForEmployeeByTimeDTO);
 						}

 					}
        			
        		}
        	 }
        	 
    	 }
    	 catch(Exception e)
    	 {
    		 e.printStackTrace();
    		 throw e;
    	 }
    	 
    	 try {
    		 
    		 if(returnMap!= null && returnMap.size() > 0)
    		 {
        		 for (Map.Entry<String,CallCountForEmployeeByTimeDTO> entry : returnMap.entrySet()) {
     			    actuallyReturned.add(entry.getValue());
     			}
    		 }
    	 }
    	 catch(Exception e)
    	 {
    		 e.printStackTrace();
    		 throw e;
    	 }
    	 
    	return actuallyReturned;
    }
    

    
    public List<CallDetailDTO> findAllInMemoryDataByOrganization(String organization){
    	List<CallDetailDTO> toReturn = new ArrayList<>();
    	
    	try
    	{
        	Map<String,CdrDTO> allRecord = CDRMemoryCollection.workWithCDRInterimData("", null, "get");
        	for (Entry<String, CdrDTO> entry : allRecord.entrySet()) {
        		CdrDTO cdrDTO = entry.getValue();
        		if(cdrDTO.getCallDetail() != null)
        		{
        			CallDetail callDetail = cdrDTO.getCallDetail();
        			
        			if(callDetail.getOrganization().equals(organization))
        			{
            			toReturn.add(callDetailMapper.mapCallDetailToDTO(cdrDTO.getCallDetail()));
        			}
        		}
        	}
    	}
    	catch(Exception e)
    	{
    		toReturn = null;
    		e.printStackTrace();
    	}

    	return  toReturn;
    }
    
    
    
    public List<CallDetailDTO> findAllInMemoryDataByExtension(Employee employee){
    	List<CallDetailDTO> toReturn = new ArrayList<>();
    	
    	try
    	{
        	Map<String,CdrDTO> allRecord = CDRMemoryCollection.workWithCDRInterimData("", null, "get");
        	
        	if(employee != null)
        	{
        		for (Entry<String, CdrDTO> entry : allRecord.entrySet()) {
            		CdrDTO cdrDTO = entry.getValue();
            		if(cdrDTO.getCallDetail() != null)
            		{
            			CallDetail callDetail = cdrDTO.getCallDetail();
            			
            			if(callDetail.getCallerid().equals(employee.getExtension()) || callDetail.getCallerid().equals(employee.getPhonenumber()))
            			{
                			toReturn.add(callDetailMapper.mapCallDetailToDTO(cdrDTO.getCallDetail()));
            			}
            		}
            	}
        	}
        	
    	}
    	catch(Exception e)
    	{
    		toReturn = null;
    		e.printStackTrace();
    	}

    	return  toReturn;
    }
    
    
    /**
     * The method is to retrieve all employee call details from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees call details with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByOrganization(String organization,String searchText, Pageable pageable){
    	
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByOrganization(organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByOrganization(organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;
    }
    
    
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsGreaterThanEqualAndOrganization(double calldurationseconds,String organization,String searchText, Pageable pageable){
        
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsGreaterThanEqualAndOrganization(calldurationseconds,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsGreaterThanEqualAndOrganization(calldurationseconds,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsLessThanEqualAndOrganization(double calldurationseconds,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsLessThanEqualAndOrganization(calldurationseconds,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsLessThanEqualAndOrganization(calldurationseconds,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(double calldurationseconds,String customerid,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(calldurationseconds,customerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(calldurationseconds,customerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(double calldurationseconds,String callerid,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(calldurationseconds,callerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(calldurationseconds,callerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(double calldurationseconds,String customerid,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(calldurationseconds,customerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(calldurationseconds,customerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(double calldurationseconds,boolean isconference,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(calldurationseconds,isconference,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(calldurationseconds,isconference,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(double calldurationseconds,boolean isconference,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(calldurationseconds,isconference,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(calldurationseconds,isconference,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(double calldurationseconds,String callerid,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(calldurationseconds,callerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(calldurationseconds,callerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(double calldurationseconds,String timezone,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(calldurationseconds,TimeZone.getTimeZone(timezone),organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(calldurationseconds,TimeZone.getTimeZone(timezone),organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(double calldurationseconds,String timezone,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(calldurationseconds,TimeZone.getTimeZone(timezone),organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(calldurationseconds,TimeZone.getTimeZone(timezone),organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(double calldurationseconds,String phoneContext,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(calldurationseconds,phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(calldurationseconds,phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(double calldurationseconds,String phoneContext,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(calldurationseconds,phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(calldurationseconds,phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;
  
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCallonmobileAndIsconferenceAndOrganization(boolean callonmobile,boolean isconference,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCallonmobileAndIsconferenceAndOrganization(callonmobile,isconference,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCallonmobileAndIsconferenceAndOrganization(callonmobile,isconference,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCallonmobileAndOrganization(boolean callonmobile,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCallonmobileAndOrganization(callonmobile,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCallonmobileAndOrganization(callonmobile,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByTimezoneAndOrganization(String timezone,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByTimezoneAndOrganization(TimeZone.getTimeZone(timezone),organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByTimezoneAndOrganization(TimeZone.getTimeZone(timezone),organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;
        
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByIsconferenceAndOrganization(boolean isconference,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByIsconferenceAndOrganization(isconference,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByIsconferenceAndOrganization(isconference,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByProgressiveAndOrganization(boolean progressive,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByprogressiveAndOrganization(progressive,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByprogressiveAndOrganization(progressive,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByPridictiveAndOrganization(boolean pridictive,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByPridictiveAndOrganization(pridictive,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response =  callDetailRepository.getAllByPridictiveAndOrganization(pridictive,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByIsIvrAndOrganization(boolean ivr,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByIvrAndOrganization(ivr, organization, searchText, pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByIvrAndOrganization(ivr, organization, searchText, pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByQueueAndOrganization(boolean queue,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByQueueAndOrganization(queue,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByQueueAndOrganization(queue,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCountryAndOrganization(String country,String organization,String searchText, Pageable pageable){
    CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCountryAndOrganization(country,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCountryAndOrganization(country,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;
    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCustomeridAndOrganization(String customerid,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCustomeridAndOrganization(customerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCustomeridAndOrganization(customerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByPhoneContextAndOrganization(String phoneContext,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByPhoneContextAndOrganization(phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByPhoneContextAndOrganization(phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByCalleridAndOrganization(String callerid,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByCalleridAndOrganization(callerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByCalleridAndOrganization(callerid,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    public CallDetailPageDTO findAllForEmployeeHistory(String dateRange,String callerid,String organization,String searchText, Pageable pageable){
    	CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	
    	 LoggerUtils.log.debug("getCallCountForDashboardForEmployeeByTime Service");
    	 Date endDate = new Date(); 
    	 Date startDate = new Date(); 
    	 
    	 try {
    		 if(dateRange.equals(DASHBOARD_TIME_LINE.Today.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Today.name());
            	 endDate = new Date(); 
            	 Instant inst = endDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);  
    		 }
    		 else if(dateRange.equals(DASHBOARD_TIME_LINE.Yesterday.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Yesterday.name());            	 
            	 Instant instEnd = Instant.now();
            	 LocalDate localEndDate = instEnd.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayEndInst = localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 endDate = Date.from(dayEndInst);
            	 Instant inststart = instEnd.minus(1, ChronoUnit.DAYS);
            	 LocalDate localStartDate = inststart.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayStartInst = localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayStartInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate); 
    		 }
    		 else if(dateRange.equals(DASHBOARD_TIME_LINE.Week.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Week.name());
    			 Instant now = Instant.now();
    			 Instant interimInstant = now.minus(7, ChronoUnit.DAYS);
    			 Date interimtoFindStartDate = Date.from(interimInstant); 
            	 Instant inst = interimtoFindStartDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);
    		 }
    		 else if(dateRange.equals(DASHBOARD_TIME_LINE.Month.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Month.name());
    			 Instant now = Instant.now();
    			 Instant interimInstant = now.minus(30, ChronoUnit.DAYS);
    			 Date interimtoFindStartDate = Date.from(interimInstant); 
            	 Instant inst = interimtoFindStartDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);
    		 }
    		 else if(dateRange.equals(DASHBOARD_TIME_LINE.Quater.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Quater.name());
    			 Instant now = Instant.now();
    			 Instant interimInstant = now.minus(91, ChronoUnit.DAYS);
    			 Date interimtoFindStartDate = Date.from(interimInstant); 
            	 Instant inst = interimtoFindStartDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);
    		 }
    		 else if(dateRange.equals(DASHBOARD_TIME_LINE.Year.name()))
    		 {
    			 LoggerUtils.log.debug("Fetching results for : "+DASHBOARD_TIME_LINE.Year.name());
    			 Instant now = Instant.now();
    			 Instant interimInstant = now.minus(365, ChronoUnit.DAYS);
    			 Date interimtoFindStartDate = Date.from(interimInstant); 
            	 Instant inst = interimtoFindStartDate.toInstant();
            	 LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            	 Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            	 startDate = Date.from(dayInst);
            	 LoggerUtils.log.debug("endDate : " + endDate);
            	 LoggerUtils.log.debug("startDate : "+startDate);
    		 }
    	 }
    	 catch(Exception e)
    	 {
    		 e.printStackTrace();
    		 throw e;
    	 }
    	 
    	 List<String> extension =  new ArrayList<String>();
    	 extension.add(callerid);
    	 
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(startDate,endDate,organization,searchText,extension,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganizationAndExtensionIn(startDate,endDate,organization,searchText,extension,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    

    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByStartdateGreaterThanEqualAndOrganization(Date startdate,String organization,String searchText, Pageable pageable){
    CallDetailPageDTO toReturn = new CallDetailPageDTO();

    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByStartdateGreaterThanEqualAndOrganization(startdate,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByStartdateGreaterThanEqualAndOrganization(startdate,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByIsconnectedAndCustomeridAndPhoneContextAndOrganization(String isconnected,String customerid,String phoneContext,String organization,String searchText, Pageable pageable){
     CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByIsconnectedAndCustomeridAndPhoneContextAndOrganization(isconnected,customerid,phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByIsconnectedAndCustomeridAndPhoneContextAndOrganization(isconnected,customerid,phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public CallDetailPageDTO findAllByIsconnectedAndCalleridAndPhoneContext(String isconnected,String callerid,String phoneContext,String organization,String searchText, Pageable pageable){
        CallDetailPageDTO toReturn = new CallDetailPageDTO();
    	if(pageable.getPageNumber() == 0)
    	{
    		Page<CallDetail> response = callDetailRepository.findAllByIsconnectedAndCalleridAndPhoneContextAndOrganization(isconnected,callerid,phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
      		
    		toReturn.setTotalRecords(response.getTotalElements());
        	toReturn.setNumberOfPages(response.getTotalPages());
    		
    	}
    	else
    	{
    		Slice<CallDetail> response = callDetailRepository.getAllByIsconnectedAndCalleridAndPhoneContextAndOrganization(isconnected,callerid,phoneContext,organization,searchText,pageable);
    		
    		List<CallDetailDTO> returnPart = response.getContent()
		    		.stream()
		            .map(callDetailMapper::mapCallDetailToDTO)
		            .collect(Collectors.toList());
    		
    		toReturn.setData(addCustomerInfoToCallDetailsDTO(returnPart,organization));
  
    	}
    	

        return toReturn;

    }
    
    
    
    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CallDetail_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<CallDetail> CallDetailList = callDetailRepository.findAll();

        ExportCallDetailToXLSX exporter = new ExportCallDetailToXLSX(CallDetailList);
        exporter.export(response);
    }

    
    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcelOnOrganization(Date startDate,Date endDate, String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CallDetail_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<CallDetail> CallDetailList = callDetailRepository.findAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(startDate,endDate,organization);

        ExportCallDetailToXLSX exporter = new ExportCallDetailToXLSX(CallDetailList);
        exporter.export(response);
    }
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CallDetail_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<CallDetail> CallDetailList = callDetailRepository.findAll();

        ExportCallDetailToPDF exporter = new ExportCallDetailToPDF(CallDetailList);
        exporter.export(response);
    }
    
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDFOnOrganization(Date startDate,Date endDate,String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CallDetail_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);
        List<CallDetail> CallDetailList = callDetailRepository.findAllByStartdateGreaterThanEqualAndEnddateLessThanEqualAndOrganization(startDate,endDate,organization);
        ExportCallDetailToPDF exporter = new ExportCallDetailToPDF(CallDetailList);
        exporter.export(response);
    }
    
    
}
