package com.mylinehub.crm.ami.autodialer;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.ami.service.SaveMemoryDataToDatabaseService;
import com.mylinehub.crm.data.DeletedCampaignData;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.dto.CampaignCustomerDataDTO;
import com.mylinehub.crm.data.dto.CampaignEmployeeDataDTO;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.data.dto.EmployeeAndItsCampaignDTO;
import com.mylinehub.crm.data.dto.PageInfoDTO;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.CustomerToCampaign;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.EmployeeToCampaign;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.repository.CampaignRepository;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.CustomerToCampaignRepository;
import com.mylinehub.crm.repository.EmployeeToCampaignRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 *
 * Production-grade fixes:
 * 1) Completion vs stop handling: COMPLETED status + notification only when real completion happens.
 * 2) employeeTasksRequired=false never touches employee mappings/cleanup.
 * 3) Fixes potential NPE / null-update bug for EmployeeDataAndStateDTO in startCampaign.
 * 4) Fixes incorrect log message in stopCampaign.
 * 5) Removes duplicate code by extracting shared helpers (customer last-call + campaign page update).
 */
@Service
@AllArgsConstructor
public class InitiateAndLoadCampaignDataService {

	private final CampaignRepository campaignRepository;
	private final CustomerRepository customerRepository;
	private final NotificationRepository notificationRepository;
	private final EmployeeToCampaignRepository employeeToCampaignRepository;
	private final CustomerToCampaignRepository customerToCampaignRepository;
	private final SaveMemoryDataToDatabaseService campaignDataToDatabaseService;
	private final AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService;

	// =====================================================================
	// START / STOP
	// =====================================================================

	public String triggerStartCampaign(Campaign campaign, String fromExtension, String domain) {
		String toReturn = "Campaign data is not loaded.";
		System.out.println("triggerStartCampaign.");

		if (campaign == null) {
			System.out.println("triggerStartCampaign: campaign was null");
			return toReturn;
		}

		boolean employeeTasksRequired = autodialerReinitiateAndFunctionService.findIfWeRequireEmployeeForAutodialer(campaign.getAutodialertype());
		
		int lastCustomerNumber = campaign.getLastCustomerNumber();
		if (lastCustomerNumber <= 0) {
			lastCustomerNumber = 1;
		}

		int pageNumber = (lastCustomerNumber / StartedCampaignData.getPageSize()) + 1;
		int recordNumber = (lastCustomerNumber % StartedCampaignData.getPageSize());
		if (recordNumber == 0) {
			recordNumber = 1;
		}

		System.out.println("pageNumber : " + pageNumber);
		System.out.println("recordNumber : " + recordNumber);

		Pageable pageable = PageRequest.of(
			    (pageNumber - 1),
			    StartedCampaignData.getPageSize(),
			    Sort.by(Sort.Direction.DESC, "id")
			);

		// Customers are always required
		Page<CustomerToCampaign> customersToCampaignPage =
				customerToCampaignRepository.findAllByCampaignAndOrganization(campaign, campaign.getOrganization(), "", pageable);
		List<CustomerToCampaign> customersToCampaign = customersToCampaignPage.getContent();

		int totalPages = customersToCampaignPage.getTotalPages();
		int elementsInCurrentPage = customersToCampaignPage.getNumberOfElements();

		System.out.println("customersToCampaignPage.getTotalPages() : " + totalPages);
		System.out.println("customersToCampaign.size() : " + ((customersToCampaign == null) ? 0 : customersToCampaign.size()));

		List<EmployeeToCampaign> employeesToCampaign = null;
		if (employeeTasksRequired) {
			employeesToCampaign = employeeToCampaignRepository.findAllByCampaignAndOrganization(campaign, campaign.getOrganization());
			System.out.println("employeesToCampaign.size() : " + ((employeesToCampaign == null) ? 0 : employeesToCampaign.size()));
		} else {
			System.out.println("employeeTasksRequired=false (only-customer dialer). Skipping employee association check.");
		}

		if (customersToCampaign != null && customersToCampaign.size() > 0) {

			// Employee required only in employeeTasksRequired=true mode
			if (employeeTasksRequired) {
				if (employeesToCampaign == null || employeesToCampaign.size() == 0) {
					System.out.println("No employee associated to campaign");
					try {
						System.out.println("Sending notification");
						sendNoChangeNotifications(campaign.getId(),"no-employee-for-campaign", fromExtension, domain, campaign.getName(), campaign.getOrganization());
					} catch (Exception e) {
						e.printStackTrace();
					}

					System.out.println("Stopping campaign (no-employee-for-campaign).");
					stopCampaign(campaign, campaign.getManager(), null);

					try {
						StartedCampaignData.markRunStatus(campaign.getId(), StartedCampaignData.RUN_STATUS_STOPPED);
					} catch (Exception e) {
						e.printStackTrace();
					}
					toReturn = "Stopping campaign (no-employee-for-campaign).";
					return toReturn;
				}
			}

			if (pageNumber < totalPages) {
				if (recordNumber > elementsInCurrentPage) {
					pageNumber = pageNumber + 1;
					recordNumber = 1;
				}

				System.out.println("Starting campaign from another function");
				this.startCampaign(customersToCampaignPage, pageNumber, recordNumber, campaign, employeesToCampaign, employeeTasksRequired);
			} else {
				if (pageNumber == totalPages) {
					if (recordNumber <= elementsInCurrentPage) {
						this.startCampaign(customersToCampaignPage, pageNumber, recordNumber, campaign, employeesToCampaign, employeeTasksRequired);
					} else {
						toReturn = "Campaign already complete. Reset required";
						System.out.println("Campaign already complete (recordNumber>elementsInCurrentPage). Reset required.");
						try {
							sendNoChangeNotifications(campaign.getId(),"reset-campaign", fromExtension, domain, campaign.getName(), campaign.getOrganization());
							stopCampaign(campaign, campaign.getManager(), null);
							StartedCampaignData.markRunStatus(campaign.getId(), StartedCampaignData.RUN_STATUS_STOPPED);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return toReturn;
					}
				} else {
					toReturn = "Campaign already complete. Reset required";
					System.out.println("Campaign already complete (pageNumber>totalPages). Reset required.");
					try {
						sendNoChangeNotifications(campaign.getId(),"reset-campaign", fromExtension, domain, campaign.getName(), campaign.getOrganization());
						stopCampaign(campaign, campaign.getManager(), null);
						StartedCampaignData.markRunStatus(campaign.getId(), StartedCampaignData.RUN_STATUS_STOPPED);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return toReturn;
				}
			}
		} else {
			System.out.println("No customer associated with campaign");
			toReturn = "No customer associated with campaign";

			try {
				sendNoChangeNotifications(campaign.getId(),"no-customer-for-campaign", fromExtension, domain, campaign.getName(), campaign.getOrganization());
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("Stopping campaign (no-customer-for-campaign).");
			stopCampaign(campaign, campaign.getManager(), null);

			try {
				StartedCampaignData.markRunStatus(campaign.getId(), StartedCampaignData.RUN_STATUS_STOPPED);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return toReturn;
		}

		toReturn = "true";
		return toReturn;
	}

	public boolean startCampaign(Page<CustomerToCampaign> customersToCampaignPage,
			int pageNumber,
			int recordNumber,
			Campaign campaign,
			List<EmployeeToCampaign> employeesToCampaign,
			boolean employeeTasksRequired) {

		boolean ok = loadCustomers(customersToCampaignPage, pageNumber, recordNumber, campaign, employeeTasksRequired);
		if (!ok) {
			return false;
		}
		if (!employeeTasksRequired) {
			return true;
		}

		boolean toReturn = true;

		try {
			System.out.println("startCampaignCustomerAndEmployees");

			if (employeesToCampaign == null || employeesToCampaign.size() == 0) {
				System.out.println("startCampaignCustomerAndEmployees: employeesToCampaign was null/empty while employeeTasksRequired=true");
				return false;
			}

			Map<String, CampaignEmployeeDataDTO> campaignEmployeeData = new HashMap<String, CampaignEmployeeDataDTO>();

			employeesToCampaign.forEach((employeetocampaign) -> {

				if (employeetocampaign == null || employeetocampaign.getEmployee() == null) {
					System.out.println("startCampaignCustomerAndEmployees: employeetocampaign/employee was null. skipping");
					return;
				}

				String extension = employeetocampaign.getEmployee().getExtension();
				if (extension == null) {
					System.out.println("startCampaignCustomerAndEmployees: employee extension was null. skipping");
					return;
				}

				// NEW DTO per extension
				CampaignEmployeeDataDTO campaignEmployeeDataDTO = new CampaignEmployeeDataDTO();
				campaignEmployeeData.put(extension, campaignEmployeeDataDTO);

				Map<String, EmployeeAndItsCampaignDTO> allEmployeeAndItsCampaignDTO =
						StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(extension, null, "get-one");
				EmployeeAndItsCampaignDTO employeeAndItsCampaignDTO = (allEmployeeAndItsCampaignDTO == null) ? null : allEmployeeAndItsCampaignDTO.get(extension);

				Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
						EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");
				EmployeeDataAndStateDTO employeeDataAndStateDTO = (allEmployeeDataAndState == null) ? null : allEmployeeDataAndState.get(extension);

				if (employeeDataAndStateDTO == null) {
					System.out.println("startCampaignCustomerAndEmployees: employeeDataAndStateDTO was null. Creating new state for extension: " + extension);
					employeeDataAndStateDTO = new EmployeeDataAndStateDTO();
					employeeDataAndStateDTO.setEmployee(employeetocampaign.getEmployee());
					employeeDataAndStateDTO.setRunningCamapignId(-1L);
					employeeDataAndStateDTO.setEmployeeLastCall(null);
					employeeDataAndStateDTO.setLastCalledTime(null);
				}

				if (employeeAndItsCampaignDTO == null) {
					employeeAndItsCampaignDTO = new EmployeeAndItsCampaignDTO();
					employeeAndItsCampaignDTO.setCampaignIds(new ArrayList<>());
				}

				int idx = employeeAndItsCampaignDTO.getCampaignIds().indexOf(campaign.getId());
				if (idx == -1) {
					Long runningCampaignId = employeeDataAndStateDTO.getRunningCamapignId();
					if (runningCampaignId == null || runningCampaignId == -1) {
						employeeDataAndStateDTO.setRunningCamapignId(campaign.getId());
					}
					employeeAndItsCampaignDTO.getCampaignIds().add(campaign.getId());
				}

				StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(extension, employeeAndItsCampaignDTO, "update");
				StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(employeetocampaign.getEmployee().getPhonenumber(), extension, "update");

				EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, employeeDataAndStateDTO, "update");
			});

			StartedCampaignData.workOnActiveCampaignAndAllEmployeeData(campaign.getId(), null, null, campaignEmployeeData, "update-all");
		} catch (Exception e) {
			toReturn = false;
			e.printStackTrace();
		}

		return toReturn;
	}

	public boolean loadCustomers(Page<CustomerToCampaign> customersToCampaignPage,
			int pageNumber,
			int recordNumber,
			Campaign campaign,
			boolean employeeTasksRequired) {

		boolean toReturn = true;

		try {
			if (campaign == null) {
				System.out.println("startCampaignCustomers: campaign was null");
				return false;
			}

			StartedCampaignData.workOnAllActiveCampaigns(campaign.getId(), campaign, "update");

			StartedCampaignData.ensureRunInitialized(campaign.getId(), campaign.getOrganization(), campaign.getName());
			StartedCampaignData.markRunStatus(campaign.getId(), StartedCampaignData.RUN_STATUS_RUNNING);

			System.out.println("startCampaignCustomers");
			System.out.println("Adding new customers page to memory");
			this.addNewCustomersPage(customersToCampaignPage.getTotalPages(), pageNumber, recordNumber, campaign, true);

		} catch (Exception e) {
			toReturn = false;
			e.printStackTrace();
		}

		return toReturn;
	}

	public boolean triggerStopCampaign(Campaign campaign,
			String fromExtension,
			String domain,
			boolean completed,
			String notification) {

		boolean toReturn = true;
		
		if (campaign == null) {
			System.out.println("triggerStopCampaign: campaign was null");
			return false;
		}

		try {
			
			boolean employeeTasksRequired = autodialerReinitiateAndFunctionService.findIfWeRequireEmployeeForAutodialer(campaign.getAutodialertype());
			System.out.println("triggerStopCampaign (completed=" + completed + ", employeeTasksRequired=" + employeeTasksRequired + ")");
			
			PageInfoDTO campaignPageInfoDTO = StartedCampaignData.updateOrGetCampaignCommonPageInfo(campaign.getId(), null, "get");
			System.out.println("After reading campaign page info");

			if (campaignPageInfoDTO != null) {
				campaignDataToDatabaseService.saveLastCommonRecordForCampaign(campaign.getId());
			}

			if (employeeTasksRequired) {
				campaignDataToDatabaseService.saveAllEmployeeDataInMemoryToDatabase();
				campaignDataToDatabaseService.saveEmployeeRelatedToCampaignLastNum(campaign.getId());
			}

			campaignDataToDatabaseService.saveAllCustomerDataInMemoryToDatabase();

			this.removePreviousCustomersPage(campaign);

			if (employeeTasksRequired) {
				cleanupEmployeesForCampaignStop(campaign);
			} else {
				System.out.println("employeeTasksRequired=false: skipping employee cleanup logic");
			}

			StartedCampaignData.workOnAllActiveCampaigns(campaign.getId(), null, "delete");
			StartedCampaignData.updateOrGetCampaignCommonPageInfo(campaign.getId(), null, "delete");

			if (employeeTasksRequired) {
				StartedCampaignData.workOnActiveCampaignAndAllEmployeeData(campaign.getId(), null, null, null, "delete");
			}

			if (completed) {
				this.stopCampaign(campaign, campaign.getManager(), "campaign-complete");
			} else {
				this.stopCampaign(campaign, campaign.getManager(), notification);
			}

			try {
				StartedCampaignData.markRunStatus(
						campaign.getId(),
						completed ? StartedCampaignData.RUN_STATUS_COMPLETED : StartedCampaignData.RUN_STATUS_STOPPED
				);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			toReturn = false;
			e.printStackTrace();
		}

		return toReturn;
	}

	public boolean triggerResetCampaign(Campaign campaign, String fromExtension, String domain) {
		boolean toReturn = true;

		try {
			System.out.println("triggerResetCampaign For Customer to Campaign");
			customerToCampaignRepository.resetCampaignCustomers(campaign, campaign.getOrganization());

			System.out.println("triggerResetCampaign  For Employee to Campaign");
			employeeToCampaignRepository.resetCampaignEmployees(campaign, campaign.getOrganization());

			System.out.println("Setting LastCustomerNumber to zero");
			campaign.setTotalCallsMade(0);
			campaign.setLastCustomerNumber(0);
			campaignRepository.save(campaign);

		} catch (Exception e) {
			toReturn = false;
			e.printStackTrace();
			throw e;
		}
		return toReturn;
	}

	// =====================================================================
	// EMPLOYEE HELPERS
	// =====================================================================

	private void cleanupEmployeesForCampaignStop(Campaign campaign) throws Exception {
		System.out.println("cleanupEmployeesForCampaignStop for campaign: " + campaign.getId());

		List<String> employeeExtensions = autodialerReinitiateAndFunctionService.getEmployeeExtension(campaign.getId());
		if (employeeExtensions == null || employeeExtensions.isEmpty()) {
			System.out.println("cleanupEmployeesForCampaignStop: no employee extensions found for campaign: " + campaign.getId());
			return;
		}

		for (String currentExtension : employeeExtensions) {
			if (currentExtension == null) {
				continue;
			}

			Map<String, EmployeeAndItsCampaignDTO> allEmployeeAndItsCampaignDTO =
					StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(currentExtension, null, "get-one");
			EmployeeAndItsCampaignDTO employeeAndItsCampaignDTO = (allEmployeeAndItsCampaignDTO == null) ? null : allEmployeeAndItsCampaignDTO.get(currentExtension);

			Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
					EmployeeDataAndState.workOnAllEmployeeDataAndState(currentExtension, null, "get-one");
			EmployeeDataAndStateDTO employeeDataAndStateDTO = (allEmployeeDataAndState == null) ? null : allEmployeeDataAndState.get(currentExtension);

			Employee employee = (employeeDataAndStateDTO == null) ? null : employeeDataAndStateDTO.getEmployee();

			if (employee == null || employeeDataAndStateDTO == null || employeeAndItsCampaignDTO == null) {
				System.out.println("cleanupEmployeesForCampaignStop: skipping extension=" + currentExtension +
						" (employee/state/campaignMapping missing)");
				continue;
			}

			String currentPhoneNumber = employee.getPhonenumber();

			int index = employeeAndItsCampaignDTO.getCampaignIds().indexOf(campaign.getId());
			if (index != -1) {
				employeeAndItsCampaignDTO.getCampaignIds().remove(index);
				StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(employee.getExtension(), employeeAndItsCampaignDTO, "update");

				Long runningId = employeeDataAndStateDTO.getRunningCamapignId();
				if (runningId != null && runningId.longValue() == campaign.getId().longValue()) {
					employeeDataAndStateDTO.setRunningCamapignId(-1L);
					EmployeeDataAndState.workOnAllEmployeeDataAndState(currentExtension, employeeDataAndStateDTO, "update");
				}
			}

			if (employeeAndItsCampaignDTO.getCampaignIds().isEmpty()) {
				StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(currentExtension, null, "delete");
				StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(currentPhoneNumber, null, "delete");
				autodialerReinitiateAndFunctionService.removeCronCallScheduleJobForEmployee(campaign.getAutodialertype(), employee);
			} else {
				Long runningId = employeeDataAndStateDTO.getRunningCamapignId();
				if (runningId == null || runningId == -1) {
					Campaign nextCampaign = autodialerReinitiateAndFunctionService.findRunningOrElseSetCampaignForEmployee(employee);
					if (nextCampaign != null) {
						employeeDataAndStateDTO.setRunningCamapignId(nextCampaign.getId());
						EmployeeDataAndState.workOnAllEmployeeDataAndState(currentExtension, employeeDataAndStateDTO, "update");
						autodialerReinitiateAndFunctionService.startCallFlowForEmployee(employee, nextCampaign, false);
					}
				}
			}
		}
	}

	// =====================================================================
	// LAST CALL UPDATES (NO DUPLICATE CODE)
	// =====================================================================
	public boolean updateLastCallDetails(
			CustomerAndItsCampaignDTO customerAndItsCampaignDTO,
			EmployeeDataAndStateDTO employeeDataAndStateDTO,
			Employee currentEmployee,
			String connectedLine,
			Customers currentCustomer,
			Long campaignId,
			boolean employeeTasksRequired
	) {
		System.out.println("updateLastCallDetails");

		if (customerAndItsCampaignDTO == null || currentCustomer == null || campaignId == null) {
			System.out.println("updateLastCallDetails: required input was null");
			return false;
		}

		if(employeeTasksRequired) {
			if (employeeDataAndStateDTO == null || currentEmployee == null) {
				System.out.println("updateLastCallDetails: required input was null");
				return false;
			}
		}
		
		if (connectedLine == null || connectedLine.isBlank()) {
		    connectedLine = employeeTasksRequired && currentEmployee != null
		            ? currentEmployee.getExtension()
		            : "";
		}
		
		try {
			// 1) Update customer record in CustomerAndItsCampaignDTO (shared)
			if (!updateCustomerInCustomerAndItsCampaign(customerAndItsCampaignDTO, currentCustomer, campaignId, connectedLine)) {
				return false;
			}

			// 2) Update campaign page map for this customer (shared)
			updateCampaignCustomerLastCall(campaignId, currentCustomer.getPhoneNumber(), connectedLine, customerAndItsCampaignDTO);

			if(!employeeTasksRequired) {
				return true;
			}
			
			// 3) Update employee state (employee-only)
			updateEmployeeStateForLastCall(employeeDataAndStateDTO, currentEmployee, currentCustomer.getPhoneNumber());

			// 4) Update employee-to-campaign map (employee-only)
			updateCampaignEmployeeLastCall(campaignId, currentEmployee.getExtension(), currentCustomer.getPhoneNumber());

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Shared: updates customer object inside CustomerAndItsCampaignDTO for a campaignId
	 * and persists it into StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(...)
	 */
	private boolean updateCustomerInCustomerAndItsCampaign(
			CustomerAndItsCampaignDTO customerAndItsCampaignDTO,
			Customers currentCustomer,
			Long campaignId,
			String lastConnectedExtensionOrLine
	) {
		try {
			if (customerAndItsCampaignDTO == null || currentCustomer == null || campaignId == null) {
				return false;
			}
			if (lastConnectedExtensionOrLine == null) {
				lastConnectedExtensionOrLine = "";
			}

			currentCustomer.setLastConnectedExtension(lastConnectedExtensionOrLine);

			List<Long> campaignIds = customerAndItsCampaignDTO.getCampaignIds();
			List<Customers> customers = customerAndItsCampaignDTO.getCustomers();
			if (campaignIds == null || customers == null) {
				System.out.println("updateCustomerInCustomerAndItsCampaign: campaignIds/customers list was null");
				return false;
			}

			int index = campaignIds.indexOf(campaignId);
			if (index == -1) {
				System.out.println("updateCustomerInCustomerAndItsCampaign: campaignId not present in customerAndItsCampaignDTO");
				return false;
			}

			customers.set(index, currentCustomer);
			customerAndItsCampaignDTO.setCustomers(customers);

			customerAndItsCampaignDTO.setAssignedDate(new Date());
			customerAndItsCampaignDTO.setTriggerCustomerToExtentionInNewLineConnected(false);

			StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(currentCustomer.getPhoneNumber(), customerAndItsCampaignDTO, "update");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Shared: updates campaign page map (StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage)
	 * for a given customer phone number.
	 */
	private void updateCampaignCustomerLastCall(
			Long campaignId,
			String customerPhoneNumber,
			String lastCallValue,
			CustomerAndItsCampaignDTO customerAndItsCampaignDTO
	) {
		if (campaignId == null || customerPhoneNumber == null) {
			return;
		}
		if (lastCallValue == null) {
			lastCallValue = "";
		}

		Map<String, CampaignCustomerDataDTO> customersCalled =
				StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaignId, null, null, null, "get");

		CampaignCustomerDataDTO campaignCustomerDataDTO = null;

		if (customersCalled != null) {
			campaignCustomerDataDTO = customersCalled.get(customerPhoneNumber);
			if (campaignCustomerDataDTO == null) {
				campaignCustomerDataDTO = new CampaignCustomerDataDTO();
			}
			campaignCustomerDataDTO.setCustomerLastCall(lastCallValue);
			customersCalled.put(customerPhoneNumber, campaignCustomerDataDTO);

			StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaignId, customerPhoneNumber, campaignCustomerDataDTO, null, "update");
		} else {
			customersCalled = new HashMap<>();
			campaignCustomerDataDTO = new CampaignCustomerDataDTO();
			campaignCustomerDataDTO.setCustomerLastCall(lastCallValue);
			customersCalled.put(customerPhoneNumber, campaignCustomerDataDTO);

			StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaignId, customerPhoneNumber, campaignCustomerDataDTO, null, "update");
		}

		// keep any DTO flags consistent (some callers depend on this)
		if (customerAndItsCampaignDTO != null) {
			customerAndItsCampaignDTO.setAssignedDate(new Date());
			customerAndItsCampaignDTO.setTriggerCustomerToExtentionInNewLineConnected(false);
		}
	}

	/**
	 * Employee-only: updates employee state DTO + persists into EmployeeDataAndState.
	 */
	private void updateEmployeeStateForLastCall(EmployeeDataAndStateDTO employeeDataAndStateDTO, Employee employee, String customerPhoneNumber) {
		if (employeeDataAndStateDTO == null || employee == null) {
			return;
		}

		employee.setLastConnectedCustomerPhone(customerPhoneNumber);

		employeeDataAndStateDTO.setEmployee(employee);
		employeeDataAndStateDTO.setEmployeeLastCall(customerPhoneNumber);
		employeeDataAndStateDTO.setLastCalledTime(new Date());

		EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), employeeDataAndStateDTO, "update");
	}

	/**
	 * Employee-only: updates StartedCampaignData employee map with employee last call number.
	 * NOTE: CampaignEmployeeDataDTO doesn't currently store last call; keeping behavior consistent by ensuring
	 * the extension key exists + updating employeeDataAndStateDTO separately.
	 */
	private void updateCampaignEmployeeLastCall(Long campaignId, String employeeExtension, String customerPhoneNumber) {
		if (campaignId == null || employeeExtension == null) {
			return;
		}

		Map<String, CampaignEmployeeDataDTO> employeesToCampaign =
				StartedCampaignData.workOnActiveCampaignAndAllEmployeeData(campaignId, null, null, null, "get");

		CampaignEmployeeDataDTO campaignEmployeeDataDTO;

		if (employeesToCampaign != null) {
			campaignEmployeeDataDTO = employeesToCampaign.get(employeeExtension);
			if (campaignEmployeeDataDTO == null) {
				campaignEmployeeDataDTO = new CampaignEmployeeDataDTO();
			}
			employeesToCampaign.put(employeeExtension, campaignEmployeeDataDTO);
			StartedCampaignData.workOnActiveCampaignAndAllEmployeeData(campaignId, employeeExtension, campaignEmployeeDataDTO, null, "update");
		} else {
			employeesToCampaign = new HashMap<String, CampaignEmployeeDataDTO>();
			campaignEmployeeDataDTO = new CampaignEmployeeDataDTO();
			employeesToCampaign.put(employeeExtension, campaignEmployeeDataDTO);
			StartedCampaignData.workOnActiveCampaignAndAllEmployeeData(campaignId, employeeExtension, campaignEmployeeDataDTO, null, "update");
		}
	}

	// =====================================================================
	// PAGE INCREMENT / PAGE SWAP / CUSOMER ENGAGEMENT
	// =====================================================================


	public String getCustomerPhoneNumber(Campaign campaign, boolean employeeTasksRequired) {
		System.out.println("getCustomerPhoneNumber for campaign : " + ((campaign == null) ? "null" : campaign.getId()));

		if (campaign == null) {
			return null;
		}

		Map<String, CampaignCustomerDataDTO> allPageCustomersMap =
				StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaign.getId(), null, null, null, "get");

		if (allPageCustomersMap == null || allPageCustomersMap.isEmpty()) {
			System.out.println("getCustomerPhoneNumber: active campaign customers page map was null/empty. stopping campaign: " + campaign.getId());
			this.triggerStopCampaign(campaign, campaign.getManager().getExtension(), campaign.getManager().getDomain(), false,"customer-not-found");
			return null;
		}

		List<String> campaignCurrentPageCustomers = new ArrayList<>(allPageCustomersMap.keySet());

		PageInfoDTO campaignPageInfoDTO = StartedCampaignData.updateOrGetCampaignCommonPageInfo(campaign.getId(), null, "get");
		if (campaignPageInfoDTO == null) {
			System.out.println("getCustomerPhoneNumber: campaignPageInfoDTO was null. stopping campaign: " + campaign.getId());
			this.triggerStopCampaign(campaign, campaign.getManager().getExtension(), campaign.getManager().getDomain(), false,"customer-not-found");
			return null;
		}

		int currentRecord = campaignPageInfoDTO.getRecordOfPage();
		if (currentRecord <= 0) {
			currentRecord = 1;
		}

		if (currentRecord <= campaignCurrentPageCustomers.size()) {
			return campaignCurrentPageCustomers.get(currentRecord - 1);
		}

		System.out.println("getCustomerPhoneNumber: out of bound (currentRecord=" + currentRecord +
				", size=" + campaignCurrentPageCustomers.size() + "). stopping campaign: " + campaign.getId());
		this.triggerStopCampaign(campaign, campaign.getManager().getExtension(), campaign.getManager().getDomain(), false,"customer-not-found");
		return null;
	}
	
	public boolean advanceOnlyAtomic(Campaign campaign) {
	    if (campaign == null) return false;

	    ReentrantLock lock = StartedCampaignData.lockForCampaign(campaign.getId());
	    lock.lock();
	    try {
	        // just advance (do NOT call getCustomerPhoneNumber again)
	        List<String> next = increaseCampaignPageRecordNumberOrFetchNew(campaign);
	        return next != null; // null means campaign stopped/completed
	    } finally {
	        lock.unlock();
	    }
	}

	
	public String pickCustomerAndAdvanceAtomic(Campaign campaign) {
	    if (campaign == null) return null;

	    ReentrantLock lock = StartedCampaignData.lockForCampaign(campaign.getId());
	    lock.lock();
	    try {
	        String phone = getCustomerPhoneNumber(campaign, false);
	        if (phone == null) return null;

	        // advance immediately so next thread can't pick same
	        increaseCampaignPageRecordNumberOrFetchNew(campaign);

	        return phone;
	    } finally {
	        lock.unlock();
	    }
	}


	public List<String> increaseCampaignPageRecordNumberOrFetchNew(Campaign campaign) {

		if (campaign == null) {
			return null;
		}

		Map<String, CampaignCustomerDataDTO> allPageCustomersMap =
				StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaign.getId(), null, null, null, "get");

		if (allPageCustomersMap == null || allPageCustomersMap.isEmpty()) {
			System.out.println("increaseCampaignPageOrFetchNew: page customers map was null/empty. stopping campaign : " + campaign.getId());
			this.triggerStopCampaign(campaign, campaign.getManager().getExtension(), campaign.getManager().getDomain(), false,"customer-not-found");
			return null;
		}

		List<String> campaignCurrentPageCustomers = new ArrayList<>(allPageCustomersMap.keySet());

		PageInfoDTO campaignPageInfoDTO = StartedCampaignData.updateOrGetCampaignCommonPageInfo(campaign.getId(), null, "get");
		if (campaignPageInfoDTO == null) {
			System.out.println("increaseCampaignPageOrFetchNew: campaignPageInfoDTO was null. stopping campaign : " + campaign.getId());
			this.triggerStopCampaign(campaign, campaign.getManager().getExtension(), campaign.getManager().getDomain(), false,"customer-not-found");
			return null;
		}

		int currentRecord = campaignPageInfoDTO.getRecordOfPage();
		int totalPages = campaignPageInfoDTO.getTotalPages();
		int currentPage = campaignPageInfoDTO.getCurrentPage();

		System.out.println("increaseCampaignPageOrFetchNew for campaign Id : " + campaign.getId());

		try {
			if (currentRecord < campaignCurrentPageCustomers.size()) {

				currentRecord = currentRecord + 1;
				campaignPageInfoDTO.setRecordOfPage(currentRecord);
				StartedCampaignData.updateOrGetCampaignCommonPageInfo(campaign.getId(), campaignPageInfoDTO, "update");

			} else {

				if (currentPage < totalPages) {
					removePreviousCustomersPage(campaign);
					addNewCustomersPage(totalPages, currentPage, 1, campaign, false);

					Map<String, CampaignCustomerDataDTO> allCampaignCustomer =
							StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaign.getId(), null, null, null, "get");

					if (allCampaignCustomer != null) {
						campaignCurrentPageCustomers = new ArrayList<>(allCampaignCustomer.keySet());
					} else {
						System.out.println("increaseCampaignPageOrFetchNew: new page customer map was null. stopping campaign : " + campaign.getId());
						campaignCurrentPageCustomers = null;
						this.triggerStopCampaign(campaign, campaign.getManager().getExtension(), campaign.getManager().getDomain(), false,"customer-not-found");
					}

				} else {
					System.out.println("Campaign completed. currentPage=" + currentPage + ", totalPages=" + totalPages);

					campaignPageInfoDTO.setRecordOfPage(currentRecord + 1);
					StartedCampaignData.updateOrGetCampaignCommonPageInfo(campaign.getId(), campaignPageInfoDTO, "update");

					campaignCurrentPageCustomers = null;

					// completed=true (only here)
					this.triggerStopCampaign(campaign, campaign.getManager().getExtension(), campaign.getManager().getDomain(), true,"customer-not-found");
				}
			}
		} catch (Exception e) {
			campaignCurrentPageCustomers = null;
			e.printStackTrace();
			throw e;
		}

		try {
			System.out.println("Logs : activeCampaigns size : " + StartedCampaignData.workOnAllActiveCampaigns(null, null, "get").size());
			System.out.println("Logs : allActiveCustomersPhoneNumbersAndItsCampaign size : " + StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(null, null, "get").size());
			System.out.println("Logs : allActiveExtensionsAndTheirCampaign size : " + StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(null, null, "get").size());
			System.out.println("Logs : allActivePhoneAndTheirExtensions size : " + StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(null, null, "get").size());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return campaignCurrentPageCustomers;
	}

	// =====================================================================
	// PAGE REMOVE / ADD
	// =====================================================================

	public boolean removePreviousCustomersPage(Campaign campaign) {
		boolean toReturn = true;
		System.out.println("removePreviousCustomersPage");

		try {
			campaignDataToDatabaseService.saveCustomerRelatedToCampaignLastNum(campaign.getId());

			List<Customers> allCampaignCustomer = new ArrayList<Customers>();

			Map<String, CampaignCustomerDataDTO> campaignCustomersPhoneNumberPage =
					StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaign.getId(), null, null, null, "get");

			if (campaignCustomersPhoneNumberPage != null && !campaignCustomersPhoneNumberPage.isEmpty()) {

				for (Map.Entry<String, CampaignCustomerDataDTO> entry : campaignCustomersPhoneNumberPage.entrySet()) {
					String currentPhoneNumber = entry.getKey();

					Map<String, CustomerAndItsCampaignDTO> allCustomersAndItsCampaignDTO =
							StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(currentPhoneNumber, null, "get-one");
					CustomerAndItsCampaignDTO customerAndItsCampaignDTO = (allCustomersAndItsCampaignDTO == null) ? null : allCustomersAndItsCampaignDTO.get(currentPhoneNumber);

					Map<String, CustomerAndItsCampaignDTO> allDeletedCustomersAndItsCampaignDTO =
							DeletedCampaignData.workWithAllDeletedCustomerData(currentPhoneNumber, null, "get-one");
					CustomerAndItsCampaignDTO deletedCustomerAndItsCampaignDTO = (allDeletedCustomersAndItsCampaignDTO == null) ? null : allDeletedCustomersAndItsCampaignDTO.get(currentPhoneNumber);

					if (customerAndItsCampaignDTO != null) {

						int index = customerAndItsCampaignDTO.getCampaignIds().indexOf(campaign.getId());

						if (index != -1) {

							allCampaignCustomer.add(customerAndItsCampaignDTO.getCustomers().get(index));

							if (deletedCustomerAndItsCampaignDTO != null) {
								List<Long> campaignIds = deletedCustomerAndItsCampaignDTO.getCampaignIds();
								List<Customers> customers = deletedCustomerAndItsCampaignDTO.getCustomers();
								campaignIds.add(customerAndItsCampaignDTO.getCampaignIds().get(index));
								customers.add(customerAndItsCampaignDTO.getCustomers().get(index));
								deletedCustomerAndItsCampaignDTO.setCustomers(customers);
								deletedCustomerAndItsCampaignDTO.setCampaignIds(campaignIds);

							} else {
								deletedCustomerAndItsCampaignDTO = new CustomerAndItsCampaignDTO();
								List<Long> campaignIds = new ArrayList<>();
								List<Customers> customers = new ArrayList<>();
								campaignIds.add(customerAndItsCampaignDTO.getCampaignIds().get(index));
								customers.add(customerAndItsCampaignDTO.getCustomers().get(index));
								deletedCustomerAndItsCampaignDTO.setCustomers(customers);
								deletedCustomerAndItsCampaignDTO.setCampaignIds(campaignIds);
							}

							customerAndItsCampaignDTO.getCampaignIds().remove(index);
							customerAndItsCampaignDTO.getCustomers().remove(index);

							deletedCustomerAndItsCampaignDTO.setDeletedDate(new Date());
							deletedCustomerAndItsCampaignDTO.setAssignedDate(customerAndItsCampaignDTO.getAssignedDate());
							deletedCustomerAndItsCampaignDTO.setCalledOnce(customerAndItsCampaignDTO.isCalledOnce());
							deletedCustomerAndItsCampaignDTO.setTriggerCustomerToExtentionInNewLineConnected(customerAndItsCampaignDTO.isTriggerCustomerToExtentionInNewLineConnected());
							deletedCustomerAndItsCampaignDTO.setLastRunningCampaignID(customerAndItsCampaignDTO.getLastRunningCampaignID());

							DeletedCampaignData.workWithAllDeletedCustomerData(currentPhoneNumber, deletedCustomerAndItsCampaignDTO, "update");
							StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(currentPhoneNumber, customerAndItsCampaignDTO, "update");
						}

						if (customerAndItsCampaignDTO.getCampaignIds().isEmpty()) {
							StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(currentPhoneNumber, null, "delete");
						}
					} else {
						toReturn = false;
					}
				}

				customerRepository.saveAll(allCampaignCustomer);
			} else {
				System.out.println("removePreviousCustomersPage: campaignCustomersPhoneNumberPage was null/empty");
			}

			StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaign.getId(), null, null, null, "delete");
		} catch (Exception e) {
			toReturn = false;
			e.printStackTrace();
			throw e;
		}
		return toReturn;
	}

	public boolean addNewCustomersPage(int totalPages, int pageNumber, int recordNumber, Campaign campaign, boolean fromStart) {
		boolean toReturn = true;
		try {

			System.out.println("addNewCustomersPage");
			System.out.println("pageNumber : " + pageNumber);
			System.out.println("recordNumber : " + recordNumber);

			if (!fromStart) {
				campaignDataToDatabaseService.saveCustomerRelatedToCampaignLastNum(campaign.getId());
				pageNumber = pageNumber + 1;
				recordNumber = 1;
			}

			PageInfoDTO pageInfoDTO = new PageInfoDTO();
			pageInfoDTO.setCurrentPage(pageNumber);
			pageInfoDTO.setRecordOfPage(recordNumber);
			pageInfoDTO.setTotalPages(totalPages);

			StartedCampaignData.updateOrGetCampaignCommonPageInfo(campaign.getId(), pageInfoDTO, "update");

			Pageable pageable = PageRequest.of(
				    (pageNumber - 1),
				    StartedCampaignData.getPageSize(),
				    Sort.by(Sort.Direction.DESC, "id")
				);
			
			Page<CustomerToCampaign> customersToCampaignPage =
					customerToCampaignRepository.findAllByCampaignAndOrganization(campaign, campaign.getOrganization(), "", pageable);
			List<CustomerToCampaign> customersToCampaign = customersToCampaignPage.getContent();

			Map<String, CampaignCustomerDataDTO> campaignCustomersPhoneNumberPage = new LinkedHashMap<>();

			if (customersToCampaign != null && !customersToCampaign.isEmpty()) {
				customersToCampaign.forEach((customertocampaign) -> {

					if (customertocampaign == null || customertocampaign.getCustomer() == null) {
						return;
					}

					CampaignCustomerDataDTO campaignCustomerDataDTO = new CampaignCustomerDataDTO();
					campaignCustomerDataDTO.setCalledOnce(false);
					campaignCustomerDataDTO.setCustomerLastCall("");

					String phone = (customertocampaign.getCustomer() == null) ? null : customertocampaign.getCustomer().getPhoneNumber();
					if (phone == null || phone.isBlank()) {
					    System.out.println("Skipping customerToCampaign id=" + customertocampaign.getId() + " due to null/blank phone");
					    return;
					}

					campaignCustomersPhoneNumberPage.put(customertocampaign.getCustomer().getPhoneNumber(), campaignCustomerDataDTO);

					Map<String, CustomerAndItsCampaignDTO> allCustomersAndItsCampaignDTO =
							StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(customertocampaign.getCustomer().getPhoneNumber(), null, "get-one");
					CustomerAndItsCampaignDTO customerAndItsCampaignDTO =
							(allCustomersAndItsCampaignDTO == null) ? null : allCustomersAndItsCampaignDTO.get(customertocampaign.getCustomer().getPhoneNumber());

					if (customerAndItsCampaignDTO != null) {

						List<Long> campaignIds = customerAndItsCampaignDTO.getCampaignIds();
						List<Customers> customers = customerAndItsCampaignDTO.getCustomers();

						if (campaignIds != null && customers != null) {
							int index = campaignIds.indexOf(campaign.getId());

							if (index != -1) {
								customers.set(index, customertocampaign.getCustomer());
							} else {
								campaignIds.add(campaign.getId());
								customers.add(customertocampaign.getCustomer());
							}
							customerAndItsCampaignDTO.setCustomers(customers);
							customerAndItsCampaignDTO.setCampaignIds(campaignIds);
						} else {
							List<Long> newCampaignIds = new ArrayList<>();
							newCampaignIds.add(campaign.getId());
							List<Customers> newCustomers = new ArrayList<>();
							newCustomers.add(customertocampaign.getCustomer());
							customerAndItsCampaignDTO.setCampaignIds(newCampaignIds);
							customerAndItsCampaignDTO.setCustomers(newCustomers);
						}

					} else {
						customerAndItsCampaignDTO = new CustomerAndItsCampaignDTO();
						List<Long> campaignIds = new ArrayList<>();
						List<Customers> customers = new ArrayList<>();
						campaignIds.add(campaign.getId());
						customers.add(customertocampaign.getCustomer());
						customerAndItsCampaignDTO.setCampaignIds(campaignIds);
						customerAndItsCampaignDTO.setCustomers(customers);
						customerAndItsCampaignDTO.setLastRunningCampaignID(-1L);
					}

					StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(customertocampaign.getCustomer().getPhoneNumber(), customerAndItsCampaignDTO, "update");
				});

				StartedCampaignData.workOnActiveCampaignAndCommonCustomersPage(campaign.getId(), null, null, campaignCustomersPhoneNumberPage, "update-all");
			} else {
				System.out.println("addNewCustomersPage: customersToCampaign was null/empty for pageNumber=" + pageNumber);
			}
		} catch (Exception e) {
			toReturn = false;
			e.printStackTrace();
			throw e;
		}

		return toReturn;
	}

	// =====================================================================
	// CAMPAIGN DB STOP + NOTIFICATIONS
	// =====================================================================

	public boolean stopCampaign(Campaign campaign, Employee campaignManager, String notificationType) {
		boolean toReturn = true;

		try {
			System.out.println("stopCampaign");
			campaign.setIsactive(false);
			int isDeactive = campaignRepository.deactivateCampaignByOrganization(campaign.getId(), campaign.getOrganization());

			if (isDeactive == 1) {
				System.out.println("Campaign deactivated/stopped successfully (campaignId=" + campaign.getId() + ")");

				if (notificationType != null) {
					try {
						System.out.println("sending notification type=" + notificationType);
						sendNoChangeNotifications(campaign.getId(),notificationType,
								campaignManager.getExtension(),
								campaignManager.getDomain(),
								campaign.getName(),
								campaign.getOrganization());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				System.out.println("Campaign deactivation failed (campaignId=" + campaign.getId() + ", result=" + isDeactive + ")");
				toReturn = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return toReturn;
	}

	public void sendNoChangeNotifications(Long campaignId,String type, String extension, String domain, String campaignName, String organization) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();

		BotInputDTO msg;
		Notification notification;

		switch (type) {

			case "stopped":
			notification = buildNotification("alert-danger", extension, campaignName + "' is stopped.", "Campaign!", organization);
			sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
			allNotifications.add(notification);
			break;
			
			case "customer-not-found":
				//notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' may not have customers.", "Customer!", organization);
				//sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				//allNotifications.add(notification);
				//We are not ding anything here .. As if its not there it will automatically shoot stop campaign.
				System.out.println("customer-not-found while running this campaign. Hence will will stop now.");
				break;
			case "call-limit-reached":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' exceeded automation limit.", "Limit!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "no-connected-line-found":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' does not have connected line.", "Connection!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;	
		
			case "campaign-complete":
				notification = buildNotification("alert-success", extension, "Campaign '" + campaignName + "' is complete.", "Done!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "cannot-reset":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' is running. Cannot reset.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "no-employee-for-campaign":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' cannot be started. No employee added.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "no-customer-for-campaign":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' is cannot be started. No customer added.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "reset-campaign":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' is already complete. Reset to start.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "campaign-not-found":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' not found. Contact admin.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "pause/unpause":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' is started. Cannot pause/unpause.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "activate/deactivate":
				notification = buildNotification("alert-danger", extension, "Campaign '" + campaignName + "' is disabled. Cannot start/stop.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "cannotupdate":
				notification = buildNotification("alert-danger", extension, "'" + campaignName + "' is started. Can't update.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			case "cannotdelete":
				notification = buildNotification("alert-danger", extension, "'" + campaignName + "' is started. Can't delete.", "Can't!", organization);
				sendNotification(mapper, msg = buildBotInput(domain, extension, organization), notification);
				allNotifications.add(notification);
				break;

			default:
				break;
		}

		notificationRepository.saveAll(allNotifications);
	}

	private Notification buildNotification(String alertType, String extension, String message, String title, String organization) {
		Notification notification = new Notification();
		notification.setCreationDate(new Date());
		notification.setAlertType(alertType);
		notification.setForExtension(extension);
		notification.setMessage(message);
		notification.setNotificationType("campign");
		notification.setOrganization(organization);
		notification.setTitle(title);
		return notification;
	}

	private BotInputDTO buildBotInput(String domain, String extension, String organization) {
		BotInputDTO msg = new BotInputDTO();
		msg.setDomain(domain);
		msg.setExtension(extension);
		msg.setFormat("json");
		msg.setMessagetype("notification");
		msg.setOrganization(organization);
		return msg;
	}

	private void sendNotification(ObjectMapper mapper, BotInputDTO msg, Notification notification) {
		try {
			msg.setMessage(mapper.writeValueAsString(notification));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		try {
			MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
