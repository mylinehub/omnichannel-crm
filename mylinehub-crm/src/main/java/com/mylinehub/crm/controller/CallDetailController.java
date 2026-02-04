package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.CALL_DETAIL_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.ami.autodialer.AutodialerReinitiateAndFunctionService;
import com.mylinehub.crm.ami.service.notificaton.SendDialerCallDetailToExtensionService;
import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.dto.CallToExtensionDTO;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.CallCountForEmployeeByTimeDTO;
import com.mylinehub.crm.entity.dto.CallCountForEmployeeDTO;
import com.mylinehub.crm.entity.dto.CallDashboardCallDetailsDTO;
import com.mylinehub.crm.entity.dto.CallDetailDTO;
import com.mylinehub.crm.entity.dto.CallDetailPageDTO;
import com.mylinehub.crm.entity.dto.CustomerDTO;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.CallDetailRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.LogRepository;
import com.mylinehub.crm.requests.CallDashboardRequest;
import com.mylinehub.crm.requests.CustomerDescriptionRequest;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.CallDetailService;
import com.mylinehub.crm.service.CustomerService;
import com.mylinehub.crm.service.OrganizationService;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping(produces="application/json", path = CALL_DETAIL_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class CallDetailController {

    private final EmployeeRepository employeeRepository;
    private final CallDetailService callDetailService;
    private final CustomerService customerService;
    private final SendDialerCallDetailToExtensionService sendDialerCallDetailToExtensionService;
    private final AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService;
    private final LogRepository logRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
	private Environment env;
	private final OrganizationService organizationService;
	private final CallDetailRepository callDetailRepository;

	@PostMapping("/deductAiAmountInMemory")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> deductAiAmountInMemory(
	        @RequestParam String organization,
	        @RequestParam long callDurationSeconds,
	        @RequestParam Boolean dynamicCost,
	        @RequestParam Long callCost,
	        @RequestParam String callCostMode,
	        @RequestParam String linkId,
	        @RequestParam String customerPhone,
	        @RequestParam(required = false) Boolean redirectChannel,
	        @RequestParam(required = false) Boolean ivrCall,
	        @RequestHeader(name = "Authorization") String token
	) {
	    final String TAG = "[deductAiAmountInMemory] ";
	    final long t0 = System.currentTimeMillis();

	    System.out.println(TAG + "ENTER"
	            + " org=" + organization
	            + " durSec=" + callDurationSeconds
	            + " dyn=" + dynamicCost
	            + " callCost=" + callCost
	            + " mode=" + callCostMode
	            + " linkId=" + linkId
	            + " customerPhone=" + customerPhone
	            + " redirectChannel=" + redirectChannel
	            + " ivrCall=" + ivrCall);

	    boolean redirect = Boolean.TRUE.equals(redirectChannel);
	    boolean ivr = Boolean.TRUE.equals(ivrCall);
		    
	    try {
	      
	    	  if(!ivr) {
		    	  // ----------------------------
		        // AUTH
		        // ----------------------------
		        String parentorganization = env.getProperty("spring.parentorginization");

		        if (token == null) token = "";
		        try {
		            String tokenPrefix = (jwtConfiguration != null ? jwtConfiguration.getTokenPrefix() : null);
		            if (tokenPrefix != null && !tokenPrefix.isEmpty()) {
		                token = token.replace(tokenPrefix, "");
		            }
		        } catch (Exception ignore) {}

		        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
		        String empOrg = (employee != null ? employee.getOrganization() : null);

		        System.out.println(TAG + "AUTH"
		                + " employeeNull=" + (employee == null)
		                + " empOrg=" + empOrg
		                + " parentOrg=" + parentorganization);

		        if (employee == null || empOrg == null || parentorganization == null
		                || !empOrg.trim().equals(parentorganization.trim())) {
		            System.out.println(TAG + "AUTH FAIL -> RETURN 401");
		            return status(HttpStatus.UNAUTHORIZED).body(false);
		        }

		        // ----------------------------
		        // Validate basics
		        // ----------------------------
		        String key = (linkId == null ? null : linkId.trim());
		        if (key == null || key.isEmpty()) {
		            System.out.println(TAG + "BAD REQUEST: linkId empty -> RETURN ok=true (no-op)");
		            return status(HttpStatus.OK).body(true);
		        }

		        // ----------------------------
		        // Calculate amount ONLY (do not deduct yet)
		        // ----------------------------
		        double amount;
		        try {
		            amount = organizationService.calculateCallAmunt(
		                    organization,
		                    callDurationSeconds,
		                    (dynamicCost != null ? dynamicCost : false),
		                    (callCost != null ? callCost : 0L),
		                    (callCostMode != null ? callCostMode : "")
		            );
		        } catch (Exception e) {
		            System.out.println(TAG + "calculateCallAmunt EXCEPTION=" + e.getMessage());
		            e.printStackTrace();
		            return status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
		        }

		        long amountLong = (long) Math.round(amount);
		        System.out.println(TAG + "AMOUNT"
		                + " amount=" + amount
		                + " amountLong=" + amountLong
		                + " callCostMode=" + callCostMode);

		        // ----------------------------
		        // Sure-shot campaign detection based on campaignID in records
		        // ----------------------------
		        boolean isCampaignCall = false;
		        boolean foundAnyRecord = false;

		        // ----------------------------
		        // MEMORY UPDATE (interim then backup)
		        // ----------------------------
		        boolean memUpdated = false;
		        boolean backupUpdated = false;

		        // helper: apply amount + callDetail cost fields
		        java.util.function.Consumer<CdrDTO> apply = (cdr) -> {
		            if (cdr == null) return;

		            try {
		                if (cdr.getLinkId() == null || cdr.getLinkId().trim().isEmpty()) {
		                    cdr.setLinkId(key);
		                }
		            } catch (Exception ignore) {}

		            try { cdr.setAmount(amountLong); } catch (Exception ignore) {}

		            try {
		                CallDetail cd = cdr.getCallDetail();
		                if (cd != null) {
		                    cd.setCallCost(amount);
		                    cd.setCallCostMode(callCostMode);

		                    if (cd.getLinkId() == null || cd.getLinkId().trim().isEmpty()) cd.setLinkId(key);
		                    if (cd.getOrganization() == null || cd.getOrganization().trim().isEmpty()) cd.setOrganization(organization);

		                    cdr.setCallDetail(cd);
		                }
		            } catch (Exception ignore) {}
		        };

		        // 1) interimRecords
		        try {
		            System.out.println(TAG + "MEM LOOKUP interimRecords key=" + key);

		            Map<String, CdrDTO> one = CDRMemoryCollection.workWithCDRInterimData(key, null, "get-one");
		            CdrDTO c = (one != null ? one.get(key) : null);

		            if (c != null) {
		                foundAnyRecord = true;

		                boolean camp = isCampaignCallFromCdr(c);
		                isCampaignCall = isCampaignCall || camp;

		                System.out.println(TAG + "MEM HIT interimRecords"
		                        + " cdr.campaignID=" + c.getCampaignID()
		                        + " callDetail.campaignID=" + (c.getCallDetail() != null ? c.getCallDetail().getCampaignID() : null)
		                        + " => isCampaignCall=" + isCampaignCall);

		                apply.accept(c);

		                Map<String, CdrDTO> upd = new HashMap<>();
		                upd.put(key, c);
		                CDRMemoryCollection.workWithCDRInterimData(key, upd, "update");
		                memUpdated = true;

		                System.out.println(TAG + "MEM UPDATED interimRecords key=" + key);
		            } else {
		                System.out.println(TAG + "MEM MISS interimRecords key=" + key);
		            }
		        } catch (Exception e) {
		            System.out.println(TAG + "MEM interimRecords EXCEPTION=" + e.getMessage());
		            e.printStackTrace();
		        }

		        // 2) backupInterimRecords (only if not in interim)
		        if (!memUpdated) {
		            try {
		                System.out.println(TAG + "MEM LOOKUP backupInterimRecords key=" + key);

		                Map<String, CdrDTO> oneB = CDRMemoryCollection.workWithCDRBackupInterimData(key, null, "get-one");
		                CdrDTO cB = (oneB != null ? oneB.get(key) : null);

		                if (cB != null) {
		                    foundAnyRecord = true;

		                    boolean camp = isCampaignCallFromCdr(cB);
		                    isCampaignCall = isCampaignCall || camp;

		                    System.out.println(TAG + "MEM HIT backupInterimRecords"
		                            + " cdr.campaignID=" + cB.getCampaignID()
		                            + " callDetail.campaignID=" + (cB.getCallDetail() != null ? cB.getCallDetail().getCampaignID() : null)
		                            + " => isCampaignCall=" + isCampaignCall);

		                    apply.accept(cB);

		                    Map<String, CdrDTO> updB = new HashMap<>();
		                    updB.put(key, cB);
		                    CDRMemoryCollection.workWithCDRBackupInterimData(key, updB, "update");
		                    backupUpdated = true;

		                    System.out.println(TAG + "MEM UPDATED backupInterimRecords key=" + key);
		                } else {
		                    System.out.println(TAG + "MEM MISS backupInterimRecords key=" + key);
		                }
		            } catch (Exception e) {
		                System.out.println(TAG + "MEM backupInterimRecords EXCEPTION=" + e.getMessage());
		                e.printStackTrace();
		            }
		        }

		        // ----------------------------
		        // DB CHECK + UPDATE (if not found in memory)
		        // ----------------------------
		        int dbUpdated = 0;
		        CallDetail dbRow = null;

		        if (!memUpdated && !backupUpdated) {
		            System.out.println(TAG + "DB PATH: memory not found -> check DB by linkId+org"
		                    + " linkId=" + linkId + " org=" + organization);

		            try {
		                // Requires repository method:
		                // CallDetail findTopByLinkIdAndOrganization(String linkId, String organization);
		                dbRow = callDetailRepository.findTopByLinkIdAndOrganization(linkId, organization);
		            } catch (Exception e) {
		                System.out.println(TAG + "DB fetch EXCEPTION=" + e.getMessage());
		                e.printStackTrace();
		            }

		            if (dbRow != null) {
		                foundAnyRecord = true;

		                boolean camp = isCampaignCallFromCallDetail(dbRow);
		                isCampaignCall = isCampaignCall || camp;

		                System.out.println(TAG + "DB HIT"
		                        + " callDetail.id=" + dbRow.getId()
		                        + " callDetail.campaignID=" + dbRow.getCampaignID()
		                        + " => isCampaignCall=" + isCampaignCall);
		            } else {
		                System.out.println(TAG + "DB MISS (row not created yet?) linkId=" + linkId + " org=" + organization);
		            }

		            // Update DB cost fields (safe even if campaign call; does not deduct org)
		            try {
		                dbUpdated = callDetailRepository.updateCallCostAndModeByLinkIdAndOrganization(
		                        amount,
		                        callCostMode,
		                        linkId,
		                        organization
		                );
		            } catch (Exception e) {
		                System.out.println(TAG + "DB update EXCEPTION=" + e.getMessage());
		                e.printStackTrace();
		                return status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
		            }

		            System.out.println(TAG + "DB update rows=" + dbUpdated
		                    + " (linkId=" + linkId + " org=" + organization + ")");
		            if (dbUpdated == 0) {
		                System.out.println(TAG + "DB update rows=0 (row missing OR org mismatch OR linkId mismatch)");
		            }
		        } else {
		            System.out.println(TAG + "DB SKIP (memory updated)"
		                    + " memUpdated=" + memUpdated
		                    + " backupUpdated=" + backupUpdated);
		        }

		        // ----------------------------
		        // FAIL-SAFE: if record not found anywhere, do NOT deduct
		        // ----------------------------
		        if (!foundAnyRecord) {
		            System.out.println(TAG + "FAIL-SAFE: call record not found in interim/backup/DB -> SKIP ORG DEDUCTION"
		                    + " key=" + key);
		            System.out.println(TAG + "EXIT ok=true tookMs=" + (System.currentTimeMillis() - t0));
		            return status(HttpStatus.OK).body(true);
		        }

		        // ----------------------------
		        // FINAL: Deduct org ONLY if non-campaign call
		        // ----------------------------
		        if (isCampaignCall) {
		            System.out.println(TAG + "FINAL: SKIP ORG DEDUCTION (campaign call sure-shot by campaignID)"
		                    + " memUpdated=" + memUpdated
		                    + " backupUpdated=" + backupUpdated
		                    + " dbUpdatedRows=" + dbUpdated);
		        } else {
		            System.out.println(TAG + "FINAL: DO ORG DEDUCTION (non-campaign call)"
		                    + " amountLong=" + amountLong
		                    + " memUpdated=" + memUpdated
		                    + " backupUpdated=" + backupUpdated
		                    + " dbUpdatedRows=" + dbUpdated);

		            organizationService.deductChargeForCallInMemoryForOrg(organization, amountLong);
		        }

		        System.out.println(TAG + "EXIT ok=true"
		                + " memUpdated=" + memUpdated
		                + " backupUpdated=" + backupUpdated
		                + " dbUpdatedRows=" + dbUpdated
		                + " isCampaignCall=" + isCampaignCall
		                + " tookMs=" + (System.currentTimeMillis() - t0));
		    }
	    	
	    	if(redirect) {
	    		//Do nothing. We do not want to check if autodiaer is on and if we need to speed up net dial. Because its just redirect.
	    	}
	    	else {
	    		//Check
	    	}
	    	
	        return status(HttpStatus.OK).body(true);

	    } catch (Exception e) {
	        System.out.println(TAG + "FATAL EXCEPTION=" + e.getMessage());
	        e.printStackTrace();
	        return status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
	    }
	}



	private static boolean hasCampaignId(Long cid) {
	    return cid != null && cid > 0;
	}

	private static boolean isCampaignCallFromCdr(CdrDTO cdr) {
	    if (cdr == null) return false;
	    if (hasCampaignId(cdr.getCampaignID())) return true;
	    CallDetail cd = cdr.getCallDetail();
	    return cd != null && hasCampaignId(cd.getCampaignID());
	}

	private static boolean isCampaignCallFromCallDetail(CallDetail cd) {
	    return cd != null && hasCampaignId(cd.getCampaignID());
	}

		
    // =====================================================================
    //  NEW API: insert CDR data coming from VoiceBridge AI Call Session
    // =====================================================================
    @PostMapping("/insertCdrFromVoiceBridge")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> insertCdrFromVoiceBridge(@RequestBody CdrDTO cdrDTO,
                                                            @RequestHeader(name = "Authorization") String token) {

        Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");
        Employee employee = new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

        String organizationFromCdr = (cdrDTO != null && cdrDTO.getOrganization() != null)
                ? cdrDTO.getOrganization()
                : "";

        System.out.println("insertCdrFromVoiceBridge controller : employeeOrg=" +
                employee.getOrganization() + " cdrOrg=" + organizationFromCdr);

        if (organizationFromCdr!=null) {
            toReturn = callDetailService.insertCdrFromVoiceBridge(cdrDTO);
            return status(HttpStatus.OK).body(toReturn);
        } else {
            System.out.println("insertCdrFromVoiceBridge controller : UNAUTHORIZED");
            return status(HttpStatus.UNAUTHORIZED).body(toReturn);
        }
    }

    
	@GetMapping("/refreshConnections")
	@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	public ResponseEntity<Boolean> refreshConnections(@RequestParam String organization,@RequestParam String phoneNumber,@RequestHeader (name="Authorization") String token){
	 
		Boolean toReturn = false;
		
    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
    	
    	//LoggerUtils.log.debug(token);
    	
    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
    	
    	if(employee.getOrganization().trim().equals(organization.trim()))
    	{
    		try {
    			
    			try {
    				toReturn= callDetailService.refreshConnections();
    			}
    			catch(Exception e)
    			{
    				e.printStackTrace();
    			}
				
				try {
					
					if(phoneNumber.startsWith("+91"))
					{
						phoneNumber = phoneNumber.substring(3);
					}
					else if(phoneNumber.startsWith("91"))
					{
						phoneNumber = phoneNumber.substring(2);
					}
					else
					{
						//Do nothing
					}
					
					//Send Customer info to employee
					CustomerDTO customer = customerService.findByPhoneNumberContaining(phoneNumber,employee.getOrganization());
					
					LoggerUtils.log.debug("customer : " + String.valueOf(customer));
					
					if (customer != null && employee != null)
					{
						CallToExtensionDTO callToExtensionDTO = new CallToExtensionDTO();
						callToExtensionDTO.setCustomer(customer);
						callToExtensionDTO.setCurrentDate(new Date());
						callToExtensionDTO.setRemindercalling(false);
						callToExtensionDTO.setCampginName("Manual-Call");
						callToExtensionDTO.setCampginId(0L);
						callToExtensionDTO.setAutodialertype("Manual-Call");
						
						try
						{
							LoggerUtils.log.debug("Sending customer info back to extension");
							sendDialerCallDetailToExtensionService.sendMessageToExtension(callToExtensionDTO, employee);
						}
						catch(Exception e)
						{
							LoggerUtils.log.error(e.getMessage());
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				toReturn = false;
			}
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		//LoggerUtils.log.debug("I am in else controller");
    		
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	} 	
	}
	
		@PostMapping("/getCallCountForDashboard")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDashboardCallDetailsDTO> getCallCountForDashboard(@RequestBody CallDashboardRequest callDashboardRequest,@RequestHeader (name="Authorization") String token){
			
			CallDashboardCallDetailsDTO toReturn = null;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(callDashboardRequest.getOrganization().trim()))
	    	{
	    		toReturn = callDetailService.getCallCountForDashboard(callDashboardRequest,employee.getOrganization());
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		}	
		

		@PostMapping("/getCallCountForDashboardForEmployee")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CallCountForEmployeeDTO>> getCallCountForDashboardForEmployee(@RequestBody CallDashboardRequest callDashboardRequest,@RequestHeader (name="Authorization") String token){
			
			List<CallCountForEmployeeDTO> toReturn = null;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(callDashboardRequest.getOrganization().trim()))
	    	{
	    		toReturn = callDetailService.getCallCountForDashboardForEmployee(callDashboardRequest);
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		}

		
		@PostMapping("/getCallCountForDashboardForEmployeeByTime")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CallCountForEmployeeByTimeDTO>> getCallCountForDashboardForEmployeeByTime(@RequestBody CallDashboardRequest callDashboardRequest,@RequestHeader (name="Authorization") String token){
			
			List<CallCountForEmployeeByTimeDTO> toReturn = null;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(callDashboardRequest.getOrganization().trim()))
	    	{
	    		toReturn = callDetailService.getCallCountForDashboardForEmployeeByTime(callDashboardRequest);
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		}
		
		@GetMapping("/getAllCallDetailsOnOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> getAllCallDetailsOnOrganization(@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		     
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByOrganization(organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}		
	
		
		@GetMapping("/findAllByCalldurationsecondsGreaterThanEqualAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsGreaterThanEqualAndOrganization(@RequestParam double calldurationseconds,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndOrganization(calldurationseconds,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
	
		
		@GetMapping("/findAllByCalldurationsecondsLessThanEqualAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsLessThanEqualAndOrganization(@RequestParam double calldurationseconds,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsLessThanEqualAndOrganization(calldurationseconds,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
		
		
		@GetMapping("/findAllByCallonmobileAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCallonmobileAndOrganization(@RequestParam boolean callonmobile,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCallonmobileAndOrganization(callonmobile,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
		
		@PostMapping("/addCustomerIfRequiredAndConvert")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<Boolean> addCustomerIfRequiredAndConvert(@RequestBody CustomerDescriptionRequest customerDescriptionRequest,@RequestHeader (name="Authorization") String token){
			
			Boolean toReturn = false;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(customerDescriptionRequest.getOrganization().trim()))
	    	{
	    		toReturn = callDetailService.addCustomerIfRequiredAndConvert(customerDescriptionRequest.getPhoneNumber(), customerDescriptionRequest.isConverted(),employee.getOrganization());
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		}
		
		@PostMapping("/addCustomerIfRequiredAndUpdateRemark")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<Boolean> addCustomerIfRequiredAndUpdateRemark(@RequestBody CustomerDescriptionRequest customerDescriptionRequest,@RequestHeader (name="Authorization") String token){
			
			Boolean toReturn = false;
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(customerDescriptionRequest.getOrganization().trim()))
	    	{
	    		toReturn = callDetailService.addCustomerIfRequiredAndUpdateRemark(customerDescriptionRequest.getPhoneNumber(), customerDescriptionRequest.getDescription(),employee.getOrganization());
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	} 	
		}
		
		
		@GetMapping("/findAllInMemoryDataByOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CallDetailDTO>> findAllInMemoryDataByOrganization(@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		  
			List<CallDetailDTO> toReturn = null;
			token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		toReturn= callDetailService.findAllInMemoryDataByOrganization(organization);
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	}
			
		}
		
		
		@GetMapping("/findAllInMemoryDataByOrganizationAndExtension")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<List<CallDetailDTO>> findAllInMemoryDataByOrganizationAndExtension(@RequestParam String extension,@RequestParam String organization,@RequestHeader (name="Authorization") String token){
		  
			List<CallDetailDTO> toReturn = null;
			token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		toReturn= callDetailService.findAllInMemoryDataByExtension(employee);
	    		return status(HttpStatus.OK).body(toReturn);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
	    	}
			
		}
		
		
		@GetMapping("/findAllByTimezoneAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByTimezoneAndOrganization(@RequestParam String timezone,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByTimezoneAndOrganization(timezone,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
		
		
		@GetMapping("/findAllByIsconferenceAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByIsconferenceAndOrganization(@RequestParam boolean isconference,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByIsconferenceAndOrganization(isconference,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		@GetMapping("/findAllByIsIvrAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByIsIvrAndOrganization(@RequestParam boolean ivr,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByIsIvrAndOrganization(ivr, organization, searchText, pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		@GetMapping("/findAllByIsQueueAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByIsQueueAndOrganization(@RequestParam boolean queue,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByQueueAndOrganization(queue, organization, searchText, pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		@GetMapping("/findAllByIsPridictiveAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByIsPridictiveAndOrganization(@RequestParam boolean pridictive,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByPridictiveAndOrganization(pridictive, organization, searchText, pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByIsProgressiveAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByIsProgressiveAndOrganization(@RequestParam boolean progressive,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByProgressiveAndOrganization(progressive, organization, searchText, pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		

		
		@GetMapping("/findAllByCountryAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCountryAndOrganization(@RequestParam String country,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCountryAndOrganization(country,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
		
		

		
		@GetMapping("/findAllByCustomeridAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCustomeridAndOrganization(@RequestParam String customerid,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCustomeridAndOrganization(customerid,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
		
		

		
		@GetMapping("/findAllByPhoneContextAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByPhoneContextAndOrganization(@RequestParam String phoneContext,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByPhoneContextAndOrganization(phoneContext,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
		
		

		
		@GetMapping("/findAllByCalleridAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalleridAndOrganization(@RequestParam String callerid,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
//	    	LoggerUtils.log.debug("callerid");
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalleridAndOrganization(callerid,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
		
		@GetMapping("/findAllForEmployeeHistory")
		@PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllForEmployeeHistory(@RequestParam String dateRange,@RequestParam String callerid,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
//	    	LoggerUtils.log.debug("callerid");
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllForEmployeeHistory(dateRange,callerid,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}	
		
		@GetMapping("/findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(@RequestParam double calldurationseconds,@RequestParam String callerid,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndCalleridAndOrganization(calldurationseconds,callerid,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		@GetMapping("/findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(@RequestParam double calldurationseconds,@RequestParam String customerid,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsLessThanEqualAndCustomeridAndOrganization(calldurationseconds,customerid,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(@RequestParam double calldurationseconds,@RequestParam boolean isconference,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndIsconferenceAndOrganization(calldurationseconds,isconference,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		@GetMapping("/findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(@RequestParam double calldurationseconds,@RequestParam boolean isconference,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsLessThanEqualAndIsconferenceAndOrganization(calldurationseconds,isconference,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(@RequestParam double calldurationseconds,@RequestParam String callerid,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsLessThanEqualAndCalleridAndOrganization(calldurationseconds,callerid,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(@RequestParam double calldurationseconds,@RequestParam String timezone,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndTimezoneAndOrganization(calldurationseconds,timezone,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(@RequestParam double calldurationseconds,@RequestParam String timeZone,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsLessThanEqualAndTimezoneAndOrganization(calldurationseconds,timeZone,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		
		@GetMapping("/findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(@RequestParam double calldurationseconds,@RequestParam String phoneContext,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndPhoneContextAndOrganization(calldurationseconds,phoneContext,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(@RequestParam double calldurationseconds,@RequestParam String phoneContext,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsLessThanEqualAndPhoneContextAndOrganization(calldurationseconds,phoneContext,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByCallonmobileAndIsconferenceAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCallonmobileAndIsconferenceAndOrganization(@RequestParam boolean callonmobile,@RequestParam boolean isconference,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCallonmobileAndIsconferenceAndOrganization(callonmobile,isconference,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(@RequestParam double calldurationseconds,@RequestParam String customerid,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByCalldurationsecondsGreaterThanEqualAndCustomeridAndOrganization(calldurationseconds,customerid,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
		@GetMapping("/findAllByStartdateGreaterThanEqualAndOrganization")
		@PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
		public ResponseEntity<CallDetailPageDTO> findAllByStartdateGreaterThanEqualAndOrganization(@RequestParam Date startDate,@RequestParam String organization,@RequestParam(defaultValue = "") final String searchText,@RequestParam(defaultValue = "0") final Integer pageNumber, @RequestParam(defaultValue = "10") final Integer size,@RequestHeader (name="Authorization") String token){
		        
			Pageable pageable;
			if(pageNumber <0)
			{
				pageable= PageRequest.of(0, 1000000000);
			}
			else
			{
				pageable = PageRequest.of(pageNumber, size);
			}
			
			CallDetailPageDTO calldetails= null;
			
			
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		calldetails= callDetailService.findAllByStartdateGreaterThanEqualAndOrganization(startDate,organization,searchText,pageable);
	    		return status(HttpStatus.OK).body(calldetails);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		
	    		return status(HttpStatus.UNAUTHORIZED).body(calldetails);
	    	} 	
		}
		
		
	    @GetMapping("/export/mylinehubexcel")
	    @ResponseStatus(HttpStatus.CREATED)
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public void exportToExcel(HttpServletResponse response,@RequestHeader (name="Authorization") String token) throws IOException {
	    	
	    	String parentorganization = env.getProperty("spring.parentorginization");
	    	
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	 
	    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
	    	{
	    		callDetailService.exportToExcel(response);
	    	}
	    	else
	    	{
	    		
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    	}
	        
	    }

	    @GetMapping("/export/organization/excel")
	    @ResponseStatus(HttpStatus.CREATED)
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public void exportToExcelOnOrganization(@RequestParam String organization,@RequestParam Date startDate,@RequestParam Date endDate,HttpServletResponse response, @RequestHeader (name="Authorization") String token) throws IOException {

	    	
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		callDetailService.exportToExcelOnOrganization(startDate,endDate,organization,response);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    		Report.addLog("Unauthorized", "Employee needs manager access","CallDetail", "Cannot Download Excel",organization,logRepository);	
		    	
	    	} 	
	    	
	    }
	    
	    @GetMapping("/export/mylinehubpdf")
	    @ResponseStatus(HttpStatus.CREATED)
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public void exportToPDF(HttpServletResponse response,@RequestHeader (name="Authorization") String token) throws IOException {
	    	
	    	String parentorganization = env.getProperty("spring.parentorginization");
	    	
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	 
	    	if(employee.getOrganization().trim().equals(parentorganization.trim()))
	    	{
	    		callDetailService.exportToPDF(response);
	    	}
	    	else
	    	{
	    		
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    	}
	        
	    }

	    @GetMapping("/export/organization/pdf")
	    @ResponseStatus(HttpStatus.CREATED)
	    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	    public void exportToPDFOnOrganization(@RequestParam String organization,@RequestParam Date startDate,@RequestParam Date endDate,HttpServletResponse response, @RequestHeader (name="Authorization") String token) throws IOException {

	    	
	    	token = token.replace(jwtConfiguration.getTokenPrefix(), "");
	    	
	    	//LoggerUtils.log.debug(token);
	    	
	    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);
	    	
	    	if(employee.getOrganization().trim().equals(organization.trim()))
	    	{
	    		callDetailService.exportToPDFOnOrganization(startDate,endDate,organization,response);
	    	}
	    	else
	    	{
	    		//LoggerUtils.log.debug("I am in else controller");
	    		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    		Report.addLog("Unauthorized", "Employee needs manager access","CallDetail", "Cannot Download PDF",organization,logRepository);	
	    	} 	
	    	
	    }
	
}
