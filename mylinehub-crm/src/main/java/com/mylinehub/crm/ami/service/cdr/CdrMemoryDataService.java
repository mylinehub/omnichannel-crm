package com.mylinehub.crm.ami.service.cdr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.ami.autodialer.AutodialerReinitiateAndFunctionService;
import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.enums.AUTODIALER_TYPE;
import com.mylinehub.crm.enums.Cdr_Event;
import com.mylinehub.crm.service.OrganizationService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CdrMemoryDataService {

    // ============================================================
    // DEEP LOGS (System.out)
    // ============================================================
    private static final boolean DEEP_LOGS = true;

    private static void SYS(String msg) {
        if (DEEP_LOGS) {
            System.out.println("[CdrMemoryDataService] " + msg);
        }
    }

    private static void SYS(String key, Object val) {
        if (DEEP_LOGS) {
            System.out.println("[CdrMemoryDataService] " + key + "=" + String.valueOf(val));
        }
    }

    private final VerifyCrdCountToDatabaseService verifyCrdCountToDatabaseService;
    private final AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService;
    private final OrganizationService organizationService;

    public boolean insertCdrEventDataIntoCdrMemoryData(String organizationFallback, Map<String, String> mapEvent) {

        SYS("Inside insertCdrEventDataIntoCdrMemoryData");
        boolean toReturn = true;

        try {
            SYS("Verifying if CDR Data is to be inderted in database");
            verifyCrdCountToDatabaseService.checkTotalOfCallDetailsAndTrigger();

            if (mapEvent == null) {
                return false;
            }

            String linkedId = mapEvent.get(Cdr_Event.uniqueid.name());
            if (linkedId == null || linkedId.trim().isEmpty()) {
                return false;
            }

            String destinationcontext = safeStr(mapEvent.get(Cdr_Event.destinationcontext.name()));
            String lastapplication = safeStr(mapEvent.get(Cdr_Event.lastapplication.name()));
            String destination = safeStr(mapEvent.get(Cdr_Event.destination.name()));
            String src = safeStr(mapEvent.get(Cdr_Event.src.name()));

            // keep logs (your style)
            SYS("linkedId", linkedId);
            SYS("destinationcontext", destinationcontext);
            SYS("lastapplication", lastapplication);
            SYS("destination", destination);
            SYS("src", src);

            Map<String, CdrDTO> allValues = CDRMemoryCollection.workWithCDRInterimData(linkedId, null, "get-one");
            CdrDTO cdrDTO = null;
            if (allValues != null) {
                cdrDTO = allValues.get(linkedId);
            }

            if (cdrDTO == null) {
                SYS("CDR Memory Line Service : cdrDTO was null initially");
                // Keep your original behavior: do nothing here.
                return true;
            }

            Campaign campaign = null;
            String jobID = autodialerReinitiateAndFunctionService.buildOnlyCustomerJobIdFromPhone(cdrDTO.getCustomerid());
            double amount = 0L;
            long diffInSeconds = 0L;

            if (jobID != null && cdrDTO != null) {
                campaign = autodialerReinitiateAndFunctionService.resolveCampaignForCallRun(jobID);
                if (campaign != null) {
                    cdrDTO.setCampaignID(campaign.getId());
                }
                SYS("[CDR] Recorded with campaignId", cdrDTO.getCampaignID());
            }
            
            SYS("CDR was not null. Converting cdr to call detail");


            CallDetail callDetail = cdrDTO.getCallDetail();

            if (callDetail == null) {
                SYS("Call detail was null initially");
                callDetail = new CallDetail();

                // Caller ID
                if (!isBlank(cdrDTO.getCallerid())) {
                    SYS("Caller ID was not null in CDR");
                    callDetail.setCallerid(cdrDTO.getCallerid());
                } else {
                    SYS("Caller ID was null in CDR");
                    if (cdrDTO.isPridictive()) {
                        callDetail.setCallerid(destination);
                    } else {
                        callDetail.setCallerid(src);
                    }
                }

                // Customer ID
                if (!isBlank(cdrDTO.getCustomerid())) {
                    SYS("Customer ID was not null in CDR");
                    callDetail.setCustomerid(cdrDTO.getCustomerid());
                } else {
                    SYS("Customer ID was null in CDR");
                    if (cdrDTO.isPridictive()) {
                        callDetail.setCustomerid(src);
                    } else {
                        callDetail.setCustomerid(destination);
                    }
                }

                callDetail.setEmployeeName(cdrDTO.getEmployeeName());
                callDetail.setCustomerName(cdrDTO.getCustomerName());
                callDetail.setCallonmobile(cdrDTO.isCallonmobile());
                callDetail.setCallType(cdrDTO.getCallType());
                callDetail.setLinkId(linkedId);
                callDetail.setCampaignID(cdrDTO.getCampaignID());
                callDetail.setCountry("Look Timezone");
                callDetail.setTimezone(TimeZone.getDefault());
                callDetail.setStartdate(new Date());
                callDetail.setOrganization(cdrDTO.getOrganization());
                callDetail.setPhoneContext("");
                callDetail.setIsactive(false);
                callDetail.setIsconnected(false);
                callDetail.setPridictive(cdrDTO.isPridictive());
                callDetail.setProgressive(cdrDTO.isProgressive());
            }

            // Duration based on bridge enter time
            if (cdrDTO.getBridgeEnterTime() != null) {
                Date now = new Date();
                long duration = now.getTime() - cdrDTO.getBridgeEnterTime().getTime();
                diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                callDetail.setCalldurationseconds(diffInSeconds);
            } else {
                callDetail.setCalldurationseconds(0);
            }

            // Flags (null-safe)
            if (destinationcontext.contains("ivr")
                    || destinationcontext.contains("recording")
                    || destinationcontext.contains("blackhole")
                    || destinationcontext.contains("announcement")
                    || lastapplication.contains("BackGround")) {
                cdrDTO.setIvr(true);
            }

            if (destination.contains("TMEETME") || lastapplication.contains("ConfBridge")) {
                cdrDTO.setIsconference(true);
            }

            if (destinationcontext.contains("queues") || lastapplication.contains("Queue")) {
                cdrDTO.setQueue(true);
            }

            SYS("Setting up common call details value");

            callDetail.setEnddate(new Date());
            callDetail.setIsconference(cdrDTO.isIsconference());
            callDetail.setIvr(cdrDTO.isIvr());
            callDetail.setQueue(cdrDTO.isQueue());

            SYS("Call Duration : "+diffInSeconds);
            
            SYS("CostCalc.enter campaignNull=" + (campaign == null));
            SYS("CostCalc.cdr.employeeNull=" + (cdrDTO.getEmployee() == null) + " customerNull=" + (cdrDTO.getCustomer() == null));

            Long callCost = 0L;
            String mode = "";
            String organization = null;

            if (campaign == null) {

                if (cdrDTO.getEmployee() == null && cdrDTO.getCustomer() == null) {
                    // Just an incoming call
                    // Do not add any amount here
                    SYS("CostCalc: incoming call only -> no charge");
                    organization = null;

                } else if (cdrDTO.getEmployee() != null || cdrDTO.getCustomer() != null) {

                    if (cdrDTO.getEmployee() != null) {
                        organization = cdrDTO.getEmployee().getOrganization();
                        SYS("CostCalc: org from employee", organization);
                    } else {
                        organization = cdrDTO.getCustomer().getOrganization();
                        SYS("CostCalc: org from customer", organization);
                    }

                    Map<String, Organization> one = OrganizationData.workWithAllOrganizationData(
                            organization, null, "get-one", null
                    );

                    SYS("CostCalc: OrganizationData mapNull=" + (one == null));

                    if (one != null && one.get(organization) != null) {

                        Organization org = one.get(organization);
                        SYS("CostCalc: org found. callLimit=" + org.getCallLimit()
                                + " aiChargeAmount=" + org.getAiCallChargeAmount()
                                + " aiChargeType=" + org.getAiCallChargeType());

                        if (org.getCallLimit() == -1) {
                            SYS("CostCalc: callLimit=-1 -> no charge");
                            organization = null;
                        } else {
                            callCost = (long) org.getAiCallChargeAmount();
                            mode = org.getAiCallChargeType();
                            SYS("CostCalc: AI pricing selected callCost=" + callCost + " mode=" + mode);
                        }

                    } else {
                        SYS("CostCalc: org not found in memory -> no charge");
                        organization = null;
                    }
                }

            } else {
                callCost = (long) campaign.getCallCost();
                mode = campaign.getCallCostMode();
                organization = campaign.getOrganization();
                callDetail.setCampaignID(campaign.getId());
                
                Long runDetailsId = null;
                try {
                    StartedCampaignData.CampaignRunSummaryMem s = StartedCampaignData.snapshotRunSummary(campaign.getId());
                    runDetailsId = (s == null) ? null : s.runId;   // this is CampaignRunDetails.id
                } catch (Exception ignore) {}

                if (runDetailsId != null) {
                    callDetail.setCampaignRunDetailsId(runDetailsId);
                }
                
                SYS("CostCalc: campaign pricing selected organization=" + organization
                        + " callCost=" + callCost + " mode=" + mode);
            }

            SYS("CostCalc: before deduct org=" + organization + " diffInSeconds=" + diffInSeconds
                    + " callCost=" + callCost + " mode=" + mode);

            if (organization != null) {
            	
                amount = organizationService.calculateCallAmunt(
                        organization,
                        diffInSeconds,
                        true,
                        callCost,
                        mode
                );
                organizationService.deductChargeForCallInMemoryForOrg(organization, (long)amount);
                
                SYS("CostCalc: deducted amount", amount);
            } else {
                SYS("CostCalc: skipped deduction because organization is null");
            }
            
                       
            callDetail.setCallCost((long) amount);   

            if (cdrDTO != null && cdrDTO.getCustomerid() != null) {

                SYS("[CDR] INITIATED attempt: resolvedCustomerPhone=" + cdrDTO.getCustomerid() + " channelId=" + jobID);

                if (jobID != null) {

                    autodialerReinitiateAndFunctionService.recordCampaignRunCallState(
                            jobID, campaign, cdrDTO.getCustomerid(), cdrDTO, "COMPLETED", cdrDTO.getCustomerid(), (long) amount
                    );
                    SYS("[CDR] INITIATED recorded with channelId=" + jobID);
                    
                    if (campaign != null) {

                        SYS("[CDR-COMPLETE] campaignId", campaign.getId());
                        SYS("[CDR-COMPLETE] autodialerType", campaign.getAutodialertype());
                        SYS("[CDR-COMPLETE] customerPhone", (cdrDTO != null ? cdrDTO.getCustomerid() : null));

                        boolean employeeTasksRequired =
                                autodialerReinitiateAndFunctionService
                                        .findIfWeRequireEmployeeForAutodialer(campaign.getAutodialertype());

                        SYS("[CDR-COMPLETE] employeeTasksRequired", employeeTasksRequired);

                        if (!employeeTasksRequired) {

                            SYS("[CDR-COMPLETE] ONLY-CUSTOMER flow: attempting removeOnlyCustomerReinitiateJob()");
                            SYS("[CDR-COMPLETE] removeOnlyCustomerReinitiateJob.phone", (cdrDTO != null ? cdrDTO.getCustomerid() : null));

                            // 1) remove the scheduled delayed reinitiate (don’t wait)
                            boolean isRemovedSuccessful =
                                    autodialerReinitiateAndFunctionService
                                            .removeOnlyCustomerReinitiateJob(campaign, cdrDTO.getCustomerid());

                            SYS("[CDR-COMPLETE] removeOnlyCustomerReinitiateJob result", isRemovedSuccessful);

                            if (isRemovedSuccessful) {

                                final Campaign campaignFinal = campaign;

                                SYS("[CDR-COMPLETE] removal successful -> starting refill thread");
                                SYS("[CDR-COMPLETE] refill.threadName", ("cdr-refill-campaign-" + campaignFinal.getId()));

                                // 2) trigger immediately
                                // IMPORTANT: do this in a thread so CDR processing doesn’t block
                                new Thread(() -> {
                                    try {
                                        SYS("[CDR-REFILL-THREAD] START");
                                        SYS("[CDR-REFILL-THREAD] campaignId", (campaignFinal != null ? campaignFinal.getId() : null));
                                        SYS("[CDR-REFILL-THREAD] autodialerType", (campaignFinal != null ? campaignFinal.getAutodialertype() : null));
                                        SYS("[CDR-REFILL-THREAD] duringStartCampaign", false);
                                        SYS("[CDR-REFILL-THREAD] employeePassed", null);

                                        // route START_CALL_FLOW for stasis, it will call initiateAutomationOnlyCustomer(...)
                                        SYS("[CDR-REFILL-THREAD] invoking startCallFlowForEmployee(NULL_EMP, campaign, false) ...");
                                        boolean ok = autodialerReinitiateAndFunctionService
                                                .startCallFlowForEmployee(null, campaignFinal, false);

                                        SYS("[CDR-REFILL-THREAD] startCallFlowForEmployee returned", ok);
                                        SYS("[CDR-REFILL-THREAD] DONE");

                                    } catch (Exception e) {
                                        SYS("[CDR-REFILL-THREAD] EXCEPTION", e.getMessage());
                                        e.printStackTrace();
                                    }
                                }, "cdr-refill-campaign-" + campaignFinal.getId()).start();

                            } else {
                                SYS("[CDR-COMPLETE] removal NOT successful -> refill skipped");
                            }

                        } else {
                            SYS("[CDR-COMPLETE] EMPLOYEE flow -> skipping refill logic");
                        }

                    } else {
                        SYS("[CDR-COMPLETE] campaign is NULL -> skipping refill logic");
                    }
                    
                } else {
                    SYS("[NewLine] INITIATED SKIPPED because resolvedCustomerPhone empty OR not a customer call. linkedId=" + linkedId);
                }
            }
            
            // attach mapEvent now
            cdrDTO.setMapEvent(mapEvent);
            cdrDTO.setAmount((long) amount);
            cdrDTO.setCallDetail(callDetail);
            cdrDTO.setLastUpdated(new Date()); // always touch lastUpdated

            SYS("Adding value to interim Record");
            Map<String, CdrDTO> record = new HashMap<>();
            record.put(linkedId, cdrDTO);
            CDRMemoryCollection.workWithCDRInterimData(linkedId, record, "update");

        } catch (Exception e) {
            toReturn = false;
            e.printStackTrace();
        }

        return toReturn;
    }

    private static String safeStr(String v) {
        return (v == null) ? "" : v.trim();
    }

    private static boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
