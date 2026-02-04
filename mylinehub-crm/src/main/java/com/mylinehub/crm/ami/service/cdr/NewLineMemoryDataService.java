package com.mylinehub.crm.ami.service.cdr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Customers;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.ami.autodialer.AutodialerReinitiateAndFunctionService;
import com.mylinehub.crm.ami.service.notificaton.SendDialerCallDetailToExtensionService;
import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.DeletedCampaignData;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.dto.CallToExtensionDTO;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.enums.DEVICE_STATES;
import com.mylinehub.crm.enums.NEW_LINE_CONNECTED_EVENT;
import com.mylinehub.crm.mapper.CustomerMapper;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class NewLineMemoryDataService {
	
    private SendDialerCallDetailToExtensionService sendDialerCallDetailToExtensionService;
    private CustomerMapper customerMapper;
    private AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService;

    // Side-effects executor: prevents AMI event thread from blocking on IO.
    // Small + safe. If you don't want async, set ASYNC_SIDE_EFFECTS=false.
    private static final boolean ASYNC_SIDE_EFFECTS = true;
    private static final ExecutorService SIDE_EFFECT_EXEC =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "cdr-sidefx-newline");
                t.setDaemon(true);
                return t;
            });

    public boolean insertNewLineDataIntoCdrMemoryData(String organization, Map<String, String> mapEvent) {
        System.out.println("Inside insertNewLineDataIntoCdrMemoryData");
        boolean toReturn = true;

        try {
            if (mapEvent == null) return false;

            String linkedId = mapEvent.get(NEW_LINE_CONNECTED_EVENT.linkedid.name());
            String calleridnum = mapEvent.get(NEW_LINE_CONNECTED_EVENT.calleridnum.name());
            String exten = mapEvent.get(NEW_LINE_CONNECTED_EVENT.exten.name());

            System.out.println("linkedId : " + linkedId);
            System.out.println("calleridnum : " + calleridnum);
            System.out.println("exten : " + exten);

            boolean isExtension = false;
            boolean isPhone = false;
            boolean isCustomerPhone = false;

            if (calleridnum != null) {
                System.out.println("Calleridnum is not null");

                // ===== Optimize: do each lookup ONCE and reuse results =====
                EmployeeDataAndStateDTO employeeDataAndStateDTO = getEmployeeStateByExtensionKey(calleridnum);
                String extensionFromPhone = getExtensionForPhone(calleridnum);

                CustomerAndItsCampaignDTO customerAndItsCampaignDTO =
                        getActiveCustomerCampaign(calleridnum);

                CustomerAndItsCampaignDTO deletedCustomerAndItsCampaignDTO =
                        getDeletedCustomerCampaign(calleridnum);

                System.out.println("extension : " + extensionFromPhone);

                isPhone = (extensionFromPhone != null);
                isExtension = (employeeDataAndStateDTO != null);

                if (!isExtension) {
                    isCustomerPhone = (customerAndItsCampaignDTO != null);
                }

                System.out.println("isPhone : " + isPhone);
                System.out.println("isExtension : " + isExtension);
                System.out.println("isCustomerPhone : " + isCustomerPhone);

                if (!isExtension && !isCustomerPhone) {
                    System.out.println("Customer not found. Checking in Deleted customers");
                    if (deletedCustomerAndItsCampaignDTO != null) {
                        isCustomerPhone = true;
                    }
                    System.out.println("Is found in deleted customers : " + isCustomerPhone);
                }

                Map<String, CdrDTO> allValues = CDRMemoryCollection.workWithCDRInterimData(linkedId, null, "get-one");
                CdrDTO cdrDTO = null;
                if (allValues != null) cdrDTO = allValues.get(linkedId);

                Employee employee = null;
                Customers customer = null;

                if (!isExtension && !isPhone && !isCustomerPhone) {
                    // All three are null. Should not add value here
                    System.out.println("New line does not caller number to be any of all three(employee phone , extension or customer)");
                    if (cdrDTO != null) {
                        cdrDTO.setTrunkNumber(calleridnum);
                        // IMPORTANT: persist update (previous code set trunkNumber but never updated map)
                        Map<String, CdrDTO> values = new HashMap<>();
                        values.put(linkedId, cdrDTO);
                        CDRMemoryCollection.workWithCDRInterimData(linkedId, values, "update");
                    }
                } else {
                    System.out.println("Adding new value to insertIntoCdrMemoryData");

                    if (cdrDTO == null) {
                        System.out.println("CDR memory data was null for linked ID");
                        cdrDTO = new CdrDTO();
                        cdrDTO.setEmployee(null);
                        cdrDTO.setCustomer(null);

                        cdrDTO.setNoOfNewLine(1);

                        if ("s".equals(exten)) {
                            System.out.println("Outbound call because exten was s, which means start");

                            cdrDTO.setCallType("Outbound");

                            if (isCustomerPhone) {
                                System.out.println("It is customer call");
                                cdrDTO.setPridictive(true);
                            }

                            if (isPhone || isExtension) {
                                System.out.println("It is not customer call");
                                cdrDTO.setProgressive(true);
                            }

                            if (cdrDTO.isProgressive() && isPhone) {
                                System.out.println("First New Line Event and is progressive, hence employee in ringing state");
                                employee = fetchEmployeeViaPhone(employee, calleridnum);
                                System.out.println("Changing device state");

                                if (employee != null) {
                                    final Employee emp = employee;   // <== FIX
                                    runSideEffect(() ->
                                            this.autodialerReinitiateAndFunctionService.changeEmployeeState(DEVICE_STATES.RINGING.name(), emp));
                                }

                            }

                        } else {
                            System.out.println("Inbound call because exten was not s");
                            cdrDTO.setCallType("Inbound");

                            if (isPhone || isExtension) {
                                System.out.println("It is extension call but not the first one and CDR was null initially. Hence it is pridictive call.");
                                cdrDTO.setPridictive(true);
                            } else {
                                cdrDTO.setProgressive(true);
                            }
                        }
                    } else {
                        System.out.println("CDR memeory data was not null for this lindid");
                        cdrDTO.setNoOfNewLine(cdrDTO.getNoOfNewLine() + 1);

                        System.out.println("Fetching Employee");
                        employee = cdrDTO.getEmployee();

                        if (cdrDTO.isPridictive() && isPhone) {
                            if (employee == null) {
                                employee = fetchEmployeeViaPhone(employee, calleridnum);
                                System.out.println("Changing device state because call is predictive and is phone is true");

                                if (employee != null) {
                                    final Employee emp = employee;   // <== FIX
                                    runSideEffect(() ->
                                            this.autodialerReinitiateAndFunctionService.changeEmployeeState(DEVICE_STATES.RINGING.name(), emp));
                                }

                            }
                        }
                    }

                    if ((isPhone || isExtension) && (cdrDTO.getCallerid() == null)) {
                        System.out.println("Setting caller ID");
                        System.out.println("calleridnum : " + calleridnum);
                        cdrDTO.setCallerid(calleridnum);

                        if (isPhone) {
                            cdrDTO.setCallonmobile(true);
                            employee = fetchEmployeeViaPhone(employee, calleridnum);
                        } else {
                            employee = fetchEmployeeViaExtension(employee, calleridnum);
                        }
                    }

                    String jobID = autodialerReinitiateAndFunctionService.buildOnlyCustomerJobIdFromPhone(calleridnum);
                    if (jobID != null && cdrDTO != null && cdrDTO.getCampaignID() == null) {
                        Campaign campaign = autodialerReinitiateAndFunctionService.resolveCampaignForCallRun(jobID);
                        cdrDTO.setCampaignID(campaign.getId());
                        System.out.println("[NewLine] INITIATED recorded with campaignId=" + cdrDTO.getCampaignID());
                    } 
                    
                    if ((isCustomerPhone) && (cdrDTO.getCustomerid() == null)) {
                        System.out.println("Setting customer ID");
                        System.out.println("calleridnum : " + calleridnum);
                        cdrDTO.setCustomerid(calleridnum);
                        System.out.println("[NewLine] INITIATED attempt: resolvedCustomerPhone=" + calleridnum + " channelId=" + jobID);

                        if (jobID != null) {
                            autodialerReinitiateAndFunctionService.recordCampaignRunCallState(
                            		jobID,null,null, cdrDTO, "NEW-LINE", calleridnum, null
                            ); 
                            System.out.println("[NewLine] INITIATED recorded with channelId=" + jobID);
                        } else {
                            System.out.println("[NewLine] INITIATED SKIPPED because resolvedCustomerPhone empty OR not a customer call. linkedId=" + linkedId);
                        }
                        
                    }

                    // Fetch customer
                    System.out.println("Fetch Customer");
                    if (customerAndItsCampaignDTO != null) {
                        System.out.println("customerAndItsCampaignDTO is not null");
                        int index = customerAndItsCampaignDTO.getCampaignIds().indexOf(customerAndItsCampaignDTO.getLastRunningCampaignID());

                        if (index != -1) {
                            customer = customerAndItsCampaignDTO.getCustomers().get(index);
                            cdrDTO.setTriggerCustomerToExtentionInNewLineConnected(customerAndItsCampaignDTO.isTriggerCustomerToExtentionInNewLineConnected());
                        }
                    }

                    if (customer == null && deletedCustomerAndItsCampaignDTO != null) {
                        System.out.println("customer is null while deletedCustomerAndItsCampaignDTO is not null");
                        int index = deletedCustomerAndItsCampaignDTO.getCampaignIds().indexOf(deletedCustomerAndItsCampaignDTO.getLastRunningCampaignID());

                        if (index != -1) {
                            customer = deletedCustomerAndItsCampaignDTO.getCustomers().get(index);
                            cdrDTO.setTriggerCustomerToExtentionInNewLineConnected(deletedCustomerAndItsCampaignDTO.isTriggerCustomerToExtentionInNewLineConnected());
                        }
                    }

                    System.out.println("Setting employee");
                    System.out.println("Setting last update date");
                    if (employee != null) {
                        cdrDTO.setEmployee(employee);
                        cdrDTO.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
                    }

                    System.out.println("Setting customer");
                    if (customer != null) {
                        cdrDTO.setCustomer(customer);
                        cdrDTO.setCustomerName(customer.getFirstname() + " " + customer.getLastname());
                    }

                    System.out.println("Setting cdr organization");
                    if (cdrDTO.getEmployee() == null) {
                        if (cdrDTO.getCustomer() != null && cdrDTO.getOrganization() == null) {
                            System.out.println("Set oranization as per customer");
                            cdrDTO.setOrganization(customer.getOrganization());
                        } else {
                            if (cdrDTO.getOrganization() == null) {
                                System.out.println("Set oranization as per organization level");
                                cdrDTO.setOrganization(organization);
                            }
                        }
                    } else {
                        System.out.println("Set oranization as per employee");
                        cdrDTO.setOrganization(cdrDTO.getEmployee().getOrganization());
                    }

                    cdrDTO.setLastUpdated(new Date());

                    if (cdrDTO.getEmployee() != null && cdrDTO.getCustomer() != null && cdrDTO.isTriggerCustomerToExtentionInNewLineConnected()) {
                        System.out.println("Sending notification to employee as it isTriggerCustomerToExtentionInNewLineConnected");
                        Customers finalCustomer = cdrDTO.getCustomer();
                        Employee finalEmployee = cdrDTO.getEmployee();
                        runSideEffect(() -> this.sendNotificationToEmployeeAboutCall(finalCustomer, finalEmployee));
                    }

                    System.out.println("Putting value in CDR in New Line Memory Data change");
                    Map<String, CdrDTO> values = new HashMap<>();
                    values.put(linkedId, cdrDTO);
                    CDRMemoryCollection.workWithCDRInterimData(linkedId, values, "update");
                }
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
            // fallback synchronous if executor rejected
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

    private Employee fetchEmployeeViaExtension(Employee employee, String calleridnum) {
        try {
            if (employee == null) {
                Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                        EmployeeDataAndState.workOnAllEmployeeDataAndState(calleridnum, null, "get-one");
                EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
                if (allEmployeeDataAndState != null) {
                    employeeDataAndStateDTO = allEmployeeDataAndState.get(calleridnum);
                }

                if (employeeDataAndStateDTO != null) {
                    employee = employeeDataAndStateDTO.getEmployee();
                }
            }
        } catch (Exception e) {
            employee = null;
            e.printStackTrace();
        }

        return employee;
    }

    private Employee fetchEmployeeViaPhone(Employee employee, String calleridnum) {
        try {
            if (employee == null) {
                Map<String, String> allEmployeePhoneAndExtension =
                        EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(calleridnum, null, "get-one");
                String extension = null;
                if (allEmployeePhoneAndExtension != null) {
                    extension = allEmployeePhoneAndExtension.get(calleridnum);
                }

                if (extension != null) {
                    Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                            EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");
                    EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
                    if (allEmployeeDataAndState != null) {
                        employeeDataAndStateDTO = allEmployeeDataAndState.get(extension);
                    }

                    if (employeeDataAndStateDTO != null) {
                        employee = employeeDataAndStateDTO.getEmployee();
                    }
                }
            }
        } catch (Exception e) {
            employee = null;
            e.printStackTrace();
        }

        return employee;
    }

    private boolean sendNotificationToEmployeeAboutCall(Customers currentCustomer, Employee currentEmployee) {
        boolean toReturn = true;
        try {
            System.out.println("Send Notification To Employee from NewLineMemoryDataService");

            CallToExtensionDTO callToExtensionDTO = new CallToExtensionDTO();
            callToExtensionDTO.setCurrentDate(new Date());
            callToExtensionDTO.setAutodialertype("Not-Preview");
            callToExtensionDTO.setCampginId(1L);
            callToExtensionDTO.setCampginName("");
            callToExtensionDTO.setRemindercalling(false);
            callToExtensionDTO.setCustomer(customerMapper.mapCustomersToDto(currentCustomer));

            sendDialerCallDetailToExtensionService.sendMessageToExtension(callToExtensionDTO, currentEmployee);
        } catch (Exception e) {
            toReturn = false;
            System.out.println(e.getMessage());
        }

        return toReturn;
    }
}
