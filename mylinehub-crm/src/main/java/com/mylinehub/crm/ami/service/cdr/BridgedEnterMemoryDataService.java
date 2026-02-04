package com.mylinehub.crm.ami.service.cdr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.ami.autodialer.AutodialerReinitiateAndFunctionService;
import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.DeletedCampaignData;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.enums.AUTODIALER_TYPE;
import com.mylinehub.crm.enums.BRIDGE_ENTER_EVENT;
import com.mylinehub.crm.enums.DEVICE_STATES;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class BridgedEnterMemoryDataService {

    final private AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService;

    private static final boolean ASYNC_SIDE_EFFECTS = true;
    private static final ExecutorService SIDE_EFFECT_EXEC =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "cdr-sidefx-bridgeenter");
                t.setDaemon(true);
                return t;
            });

    public boolean insertBridgeEnterDataIntoCdrMemoryData(String organization, Map<String, String> mapEvent) {
        System.out.println("Inside insertBridgeEnterDataIntoCdrMemoryData");
        boolean toReturn = true;

        try {
            if (mapEvent == null) return false;

            String linkedId = mapEvent.get(BRIDGE_ENTER_EVENT.linkedid.name());
            String exten = mapEvent.get(BRIDGE_ENTER_EVENT.exten.name());
            String calleridnum = mapEvent.get(BRIDGE_ENTER_EVENT.calleridnum.name());

            System.out.println("linkedId : " + linkedId);
            System.out.println("calleridnum : " + calleridnum);
            System.out.println("exten : " + exten);

            Map<String, CdrDTO> allValues = CDRMemoryCollection.workWithCDRInterimData(linkedId, null, "get-one");
            CdrDTO cdrDTO = null;
            if (allValues != null) cdrDTO = allValues.get(linkedId);

            if (cdrDTO == null) {
                System.out.println("This is Bridge enter event for CDR null initially , happenes in case of inbound Conference");
                return true;
            }

            System.out.println("CDR was not null");

            boolean isExtension = false;
            boolean isPhone = false;
            boolean isCustomerPhone = false;
            boolean isTrunkPhone = false;

            if (calleridnum != null) {
                System.out.println("calleridnum was not null");

                // ===== lookups once =====
                EmployeeDataAndStateDTO employeeDataAndStateDTO = getEmployeeStateByExtensionKey(calleridnum);
                String extensionFromPhone = getExtensionForPhone(calleridnum);

                CustomerAndItsCampaignDTO customerAndItsCampaignDTO = getActiveCustomerCampaign(calleridnum);

                isPhone = (extensionFromPhone != null);
                isExtension = (employeeDataAndStateDTO != null);

                if (!isExtension) {
                    isCustomerPhone = (customerAndItsCampaignDTO != null);
                }

                if (!isExtension && !isCustomerPhone) {
                    System.out.println("Customer not found. Checking in Deleted customers");
                    CustomerAndItsCampaignDTO deleted = getDeletedCustomerCampaign(calleridnum);
                    isCustomerPhone = (deleted != null);
                    System.out.println("Is found in deleted customers : " + isCustomerPhone);
                }

                if (cdrDTO.getTrunkNumber() != null) {
                    isTrunkPhone = cdrDTO.getTrunkNumber().equals(calleridnum);
                }

                System.out.println("Changing employee device state");
                if (cdrDTO.isPridictive() && isPhone) {
                    System.out.println("Call is pridictive and is on phone");
                    if (cdrDTO.getEmployee() != null) {
                        System.out.println("Pridictive call, employee is going into in use state");
                        final Employee emp = cdrDTO.getEmployee();   // <== FIX
                        runSideEffect(() ->
                                this.autodialerReinitiateAndFunctionService.changeEmployeeState(DEVICE_STATES.INUSE.name(),emp ));
                    }

                }
                

                System.out.println("Changing customer device state");
                if (cdrDTO.isProgressive() && isTrunkPhone) {
                    System.out.println("Call is progressive and is via trunk phone");
                    
                    if (cdrDTO.getEmployee() != null) {
                        System.out.println("Progressive call, employee is going into in use state");
                        final Employee emp = cdrDTO.getEmployee();   // <== FIX
                        runSideEffect(() ->
                                this.autodialerReinitiateAndFunctionService.changeEmployeeState(DEVICE_STATES.INUSE.name(),emp ));
                    }
                }

                System.out.println("Remove reminder call");
                if (cdrDTO.isPridictive() && (isPhone || isExtension)) {
                    System.out.println("Call is pridictable and is either on phone or extension");
                    if (cdrDTO.getEmployee() != null) {
                        System.out.println("Pridictive call,Removing reminder call");
                        final CdrDTO cdrDTOFinal = cdrDTO;
                        runSideEffect(() -> this.removeReminderCall(cdrDTOFinal));
                    }
                }

                if (cdrDTO.isProgressive() && isCustomerPhone) {
                    System.out.println("Call is progressive and is on customer phone");
                    if (cdrDTO.getEmployee() != null) {
                        System.out.println("Progressive call, and customer is in bridge enter state. Removing reminder call");
                        final CdrDTO cdrDTOFinal = cdrDTO;
                        runSideEffect(() -> this.removeReminderCall(cdrDTOFinal));
                    }
                }

//                Campaign campaign = autodialerReinitiateAndFunctionService.resolveCampaignForCdr(cdrDTO, cdrDTO.getCustomerid());
//                if (campaign != null && AUTODIALER_TYPE.AI_CALL.name().equals(campaign.getAutodialertype())) {
//                    autodialerReinitiateAndFunctionService.removeSchedulesWithoutemployee(campaign, cdrDTO.getCustomerid());
//                }

                
                System.out.println("Setting count of bridge enter event");
                cdrDTO.setNoOfBridgeEnter(cdrDTO.getNoOfBridgeEnter() + 1);

                System.out.println("Setting last update date");
                cdrDTO.setLastUpdated(new Date());
                cdrDTO.setBridgeEnterTime(new Date());
                
                if(cdrDTO !=null && cdrDTO.getCustomerid() != null) {
                	
                	 // STATUS: INITIATED (only once per jobId; only if customer phone is present)
                    String jobID = autodialerReinitiateAndFunctionService.buildOnlyCustomerJobIdFromPhone(cdrDTO.getCustomerid());

                    System.out.println("[NewLine] INITIATED attempt: resolvedCustomerPhone=" + cdrDTO.getCustomerid() + " channelId=" + jobID);

                    if (jobID != null) {
                        autodialerReinitiateAndFunctionService.recordCampaignRunCallState(
                        		jobID,null,null, cdrDTO, "IN_PROGRESS", cdrDTO.getCustomerid(),null
                        );
                        System.out.println("[NewLine] INITIATED recorded with channelId=" + jobID);
                    } else {
                        System.out.println("[NewLine] INITIATED SKIPPED because resolvedCustomerPhone empty OR not a customer call. linkedId=" + linkedId);
                    }
                    
                }
                
                
                System.out.println("Putting value in CDR");
                Map<String, CdrDTO> values = new HashMap<>();
                values.put(linkedId, cdrDTO);
                CDRMemoryCollection.workWithCDRInterimData(linkedId, values, "update");
            } else {
                System.out.println("Caller Id is null");
            }

        } catch (Exception e) {
            toReturn = false;
            e.printStackTrace();
        }

        return toReturn;
    }

    private void runSideEffect(Runnable r) {
        if (!ASYNC_SIDE_EFFECTS) {
            try { r.run(); } catch (Exception ignore) {}
            return;
        }
        try {
            SIDE_EFFECT_EXEC.submit(() -> {
                try { r.run(); } catch (Exception e) { e.printStackTrace(); }
            });
        } catch (Exception e) {
            try { r.run(); } catch (Exception ignore) {}
        }
    }

    private EmployeeDataAndStateDTO getEmployeeStateByExtensionKey(String extensionKey) {
        try {
            Map<String, EmployeeDataAndStateDTO> one =
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(extensionKey, null, "get-one");
            if (one != null) return one.get(extensionKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getExtensionForPhone(String phone) {
        try {
            Map<String, String> one =
                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(phone, null, "get-one");
            if (one != null) return one.get(phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private CustomerAndItsCampaignDTO getActiveCustomerCampaign(String phone) {
        try {
            Map<String, CustomerAndItsCampaignDTO> one =
                    StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(phone, null, "get-one");
            if (one != null) return one.get(phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private CustomerAndItsCampaignDTO getDeletedCustomerCampaign(String phone) {
        try {
            Map<String, CustomerAndItsCampaignDTO> one =
                    DeletedCampaignData.workWithAllDeletedCustomerData(phone, null, "get-one");
            if (one != null) return one.get(phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean removeReminderCall(CdrDTO cdrDTO) {
        boolean toReturn = true;

        System.out.println("Remove reminder call for customer from BrigedEnterMemoryDataService");

        try {
            Employee currentEmployee = cdrDTO.getEmployee();
            if (currentEmployee != null) {
                Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                        EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
                EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
                if (allEmployeeDataAndState != null) {
                    employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
                }

                Map<Long, Campaign> activeCampaigns = StartedCampaignData.workOnAllActiveCampaigns(null, null, "get");
                Campaign campaign = null;
                if (activeCampaigns != null && employeeDataAndStateDTO != null) {
                    campaign = activeCampaigns.get(employeeDataAndStateDTO.getRunningCamapignId());
                }

                if (campaign != null) {
                	this.autodialerReinitiateAndFunctionService.removeReminderCallScheduleJobForEmployee(campaign.getAutodialertype(), currentEmployee);
                }
            }
        } catch (Exception e) {
            toReturn = false;
            e.printStackTrace();
        }

        return toReturn;
    }
}
