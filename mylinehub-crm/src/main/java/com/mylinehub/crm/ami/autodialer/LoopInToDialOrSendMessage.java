package com.mylinehub.crm.ami.autodialer;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.ami.TaskScheduler.DialAutomateCallReminderRunnable;
import com.mylinehub.crm.ami.TaskScheduler.DialReinitiateOnlyCustomerDialerRunnable;
import com.mylinehub.crm.ami.service.ScheduleDialAutomateCallService;
import com.mylinehub.crm.ami.service.notificaton.EmployeeCallErrorNotificationService;
import com.mylinehub.crm.ami.service.notificaton.SendDialerCallDetailToExtensionService;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.data.dto.CallToExtensionDTO;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.enums.AUTODIALER_TYPE;
import com.mylinehub.crm.mapper.CustomerMapper;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.service.SchedulerService;
import com.mylinehub.crm.utils.LoggerUtils;

/**
 * @author Anand Goel
 * @version 1.0
 *
 * Refactor goals:
 * 1) Remove duplicate code between employee dialer and only-customer dialer without changing business logic.
 * 2) Keep public method signatures intact (backward compatibility).
 * 3) Put common logic (customer picking, call-limit check, page-advance) in one place.
 * 4) Add a single common "CALL INITIATED" status hook in THIS class (no duplication).
 */
public class LoopInToDialOrSendMessage {

	// ============================================================
	// DEEP LOGS (System.out)
	// ============================================================
	private static final boolean DEEP_LOGS = true;

	private static void SYS(String msg) {
		if (DEEP_LOGS) {
			System.out.println("[LoopInToDialOrSendMessage] " + msg);
		}
	}

	private static void SYS(String key, Object val) {
		if (DEEP_LOGS) {
			System.out.println("[LoopInToDialOrSendMessage] " + key + "=" + String.valueOf(val));
		}
	}

	private static final ConcurrentMap<Long, ReentrantLock> CAMPAIGN_LOCKS = new ConcurrentHashMap<>();

	private static ReentrantLock lockForCampaign(Long campaignId) {
	    return CAMPAIGN_LOCKS.computeIfAbsent(campaignId, k -> new ReentrantLock());
	}

	/**
	 * NEW: employeeTasksRequired flag
	 * - true  => existing employee-based flow (tested)
	 * - false => only-customer flow (no employee load), uses parallelLines
	 */
	public boolean initiateDialer(Campaign campaign, ApplicationContext applicationContext) {

		SYS("ENTER initiateDialer()");
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("campaignName", (campaign == null ? null : campaign.getName()));
		SYS("autodialerType", (campaign == null ? null : campaign.getAutodialertype()));

		boolean toReturn = true;
		ErrorRepository errorRepository = applicationContext.getBean(ErrorRepository.class);
		AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);
		boolean employeeTasksRequired = autodialerReinitiateAndFunctionService.findIfWeRequireEmployeeForAutodialer(campaign.getAutodialertype());

		SYS("employeeTasksRequired", employeeTasksRequired);

		try {
			if (employeeTasksRequired) {
				SYS("FLOW employee-based dialer");
				List<String> allCampaignEmployees = autodialerReinitiateAndFunctionService.getEmployeeExtension(campaign.getId());

				SYS("allCampaignEmployees.size", (allCampaignEmployees == null ? null : allCampaignEmployees.size()));

				if (allCampaignEmployees != null && allCampaignEmployees.size() > 0) {
					for (int i = 0; i < allCampaignEmployees.size(); i++) {
						LoggerUtils.log.debug("Get Current Employee has Extension: " + allCampaignEmployees.get(i));
						SYS("Loop employee index=" + i + " ext=" + allCampaignEmployees.get(i));

						Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
								EmployeeDataAndState.workOnAllEmployeeDataAndState(allCampaignEmployees.get(i), null, "get-one");

						SYS("EmployeeDataAndState map null?", (allEmployeeDataAndState == null));

						EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
						if (allEmployeeDataAndState != null) {
							employeeDataAndStateDTO = allEmployeeDataAndState.get(allCampaignEmployees.get(i));
						}

						SYS("employeeDataAndStateDTO null?", (employeeDataAndStateDTO == null));

						Employee currentEmployee = null;
						if (employeeDataAndStateDTO != null) {
							currentEmployee = employeeDataAndStateDTO.getEmployee();
						}

						SYS("currentEmployee null?", (currentEmployee == null));
						if (currentEmployee != null) {
							SYS("currentEmployee.extension", currentEmployee.getExtension());
							SYS("currentEmployee.enabled", currentEmployee.isEnabled());
							try {
								boolean shouldWeBreak =
										this.initiateDialerForEmployee(currentEmployee, campaign, applicationContext, true, employeeTasksRequired);
								SYS("initiateDialerForEmployee returned shouldWeBreak=" + shouldWeBreak);
								if (shouldWeBreak) {
									continue;
								}
							} catch (Exception e) {
								SYS("EXCEPTION in initiateDialerForEmployee: " + e.getMessage());
								e.printStackTrace();
							}
						}
					}
				}
			} else {
				SYS("FLOW only-customer dialer");
				int totalChannels = campaign.getParallelLines();
				SYS("totalChannels", totalChannels);

				if (totalChannels != 0) {
					for (int i = 0; i < totalChannels; i++) {
						LoggerUtils.log.debug("Total channels for this dialer: " + totalChannels);
						SYS("Loop channel index=" + i + " of totalChannels=" + totalChannels);

						try {
							boolean shouldWeBreak =
									this.initiateAutomationOnlyCustomer(campaign, applicationContext, true, employeeTasksRequired);
							SYS("initiateAutomationOnlyCustomer returned shouldWeBreak=" + shouldWeBreak);
							if (shouldWeBreak) {
								continue;
							}
						} catch (Exception e) {
							SYS("EXCEPTION in initiateAutomationOnlyCustomer: " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			SYS("EXCEPTION in initiateDialer outer: " + e.getMessage());
			toReturn = false;
			if (employeeTasksRequired) {
				Report.addError(e.getMessage(), "Predictive Dialer", "Error",
						"While initialing dialing for campaign", campaign.getOrganization(), errorRepository);
			} else {
				Report.addError(e.getMessage(), "Only customer Dialer", "Error",
						"While initialing dialing for campaign", campaign.getOrganization(), errorRepository);
			}
		}

		SYS("EXIT initiateDialer() toReturn=" + toReturn);
		return toReturn;
	}

	public boolean initiateDialerForEmployee(Employee employee, Campaign campaign, ApplicationContext applicationContext,
			boolean duringStartCampaign, boolean employeeTasksRequired) {

		SYS("ENTER initiateDialerForEmployee()");
		SYS("employee.extension", (employee == null ? null : employee.getExtension()));
		SYS("duringStartCampaign", duringStartCampaign);
		SYS("employeeTasksRequired", employeeTasksRequired);
		SYS("campaignId", (campaign == null ? null : campaign.getId()));

		boolean shouldWeBreak = false;
		AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);

		try {
			Campaign forThisLoopCampaign = campaign;
			boolean requireCallInitiationAsPerDialerEngineCheck = false;

			boolean isEmployeeCronRunning = autodialerReinitiateAndFunctionService.isEmployeeCronRunning(employee);
			SYS("isEmployeeCronRunning", isEmployeeCronRunning);

			if (isEmployeeCronRunning) {
				LoggerUtils.log.debug("Campaign already running for this employee as per schedule job");
				SYS("cron is running: verifyRunningCampaignForEmployee");
				boolean isHavingRunningCampaignAssociated = autodialerReinitiateAndFunctionService.verifyRunningCampaignForEmployee(employee);
				SYS("isHavingRunningCampaignAssociated", isHavingRunningCampaignAssociated);

				if (isHavingRunningCampaignAssociated) {
					LoggerUtils.log.debug("Employee also have campaign running");
					requireCallInitiationAsPerDialerEngineCheck = true;
				} else {
					LoggerUtils.log.debug("Employee does not campaign running, finding next");
					SYS("finding next campaign for employee");
					forThisLoopCampaign = autodialerReinitiateAndFunctionService.findRunningOrElseSetCampaignForEmployee(employee);
					SYS("forThisLoopCampaign null?", (forThisLoopCampaign == null));
					if (forThisLoopCampaign != null) {
						SYS("forThisLoopCampaign.id", forThisLoopCampaign.getId());
						SYS("forThisLoopCampaign.name", forThisLoopCampaign.getName());
						requireCallInitiationAsPerDialerEngineCheck = true;
					}
				}
			} else {
				requireCallInitiationAsPerDialerEngineCheck = true;
			}

			SYS("requireCallInitiationAsPerDialerEngineCheck", requireCallInitiationAsPerDialerEngineCheck);

			if (requireCallInitiationAsPerDialerEngineCheck) {
				shouldWeBreak = this.checkCallOnMobileAndInitiate(employee, forThisLoopCampaign, applicationContext, duringStartCampaign, employeeTasksRequired);
				SYS("checkCallOnMobileAndInitiate returned shouldWeBreak=" + shouldWeBreak);
			} else {
				shouldWeBreak = true;
			}
		} catch (Exception e) {
			SYS("EXCEPTION in initiateDialerForEmployee: " + e.getMessage());
			shouldWeBreak = true;
			e.printStackTrace();
		}

		SYS("EXIT initiateDialerForEmployee() shouldWeBreak=" + shouldWeBreak);
		return shouldWeBreak;
	}

	public boolean checkCallOnMobileAndInitiate(Employee employee, Campaign campaign, ApplicationContext applicationContext,
			boolean duringStartCampaign, boolean employeeTasksRequired) throws Exception {

		SYS("ENTER checkCallOnMobileAndInitiate()");
		SYS("employee.extension", (employee == null ? null : employee.getExtension()));
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("duringStartCampaign", duringStartCampaign);
		SYS("employeeTasksRequired", employeeTasksRequired);

		boolean shouldWeBreak = false;
		InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService = applicationContext.getBean(InitiateAndLoadCampaignDataService.class);
		AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);
		EmployeeCallErrorNotificationService employeeCallErrorNotificationService = applicationContext.getBean(EmployeeCallErrorNotificationService.class);
		NotificationRepository notificationRepository = applicationContext.getBean(NotificationRepository.class);

		try {
			LoggerUtils.log.debug("requireCallInitiationAsPerDialerEngineCheck is true");
			SYS("checking callOnMobile");
			boolean callOnMobile = autodialerReinitiateAndFunctionService.checkIsCallOnMobile(employee, campaign);
			SYS("callOnMobile", callOnMobile);

			if (callOnMobile) {
				LoggerUtils.log.debug("call on mobile is true");
				SYS("callOnMobile=true -> ifEmpoyeeAvaiableCallCustomerOrElseBreak");
				shouldWeBreak = this.ifEmpoyeeAvaiableCallCustomerOrElseBreak(initiateAndLoadCampaignDataService, campaign, employee, callOnMobile, applicationContext, duringStartCampaign, employeeTasksRequired);
			} else {
				LoggerUtils.log.debug("call on mobile is false");
				SYS("callOnMobile=false -> verifyIfEmployeeIsAvailableForCallAsPerExtension");
				boolean requireCallInitiationAsPerEmployeeExtensionState =
						autodialerReinitiateAndFunctionService.verifyIfEmployeeIsAvailableForCallAsPerExtension(employee);

				SYS("requireCallInitiationAsPerEmployeeExtensionState", requireCallInitiationAsPerEmployeeExtensionState);

				if (requireCallInitiationAsPerEmployeeExtensionState) {
					LoggerUtils.log.debug("requireCallInitiationAsPerEmployeeExtensionState is true");
					SYS("extensionState ok -> ifEmpoyeeAvaiableCallCustomerOrElseBreak");
					shouldWeBreak = this.ifEmpoyeeAvaiableCallCustomerOrElseBreak(initiateAndLoadCampaignDataService, campaign, employee, callOnMobile, applicationContext, duringStartCampaign, employeeTasksRequired);
				} else {
					LoggerUtils.log.debug("requireCallInitiationAsPerEmployeeExtensionState is false");
					try {
						LoggerUtils.log.debug("Intimating user as he is not getting calls due to his extension status.");
						SYS("sending employee refresh notification due to extension status");
						employeeCallErrorNotificationService.sendEmployeeCallAutodialRefreshNotifications(
								employee.getExtension(), employee.getOrganization(), employee.getDomain(), notificationRepository);
					} catch (Exception e1) {
						SYS("EXCEPTION while sending refresh notification: " + e1.getMessage());
						e1.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			SYS("EXCEPTION in checkCallOnMobileAndInitiate: " + e.getMessage());
			shouldWeBreak = true;
			e.printStackTrace();
		}

		SYS("EXIT checkCallOnMobileAndInitiate() shouldWeBreak=" + shouldWeBreak);
		return shouldWeBreak;
	}

	public boolean ifEmpoyeeAvaiableCallCustomerOrElseBreak(InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService, Campaign campaign,
			Employee currentEmployee, boolean isCallOnMobile, ApplicationContext applicationContext,
			boolean duringStartCampaign, boolean employeeTasksRequired) throws Exception {

		SYS("ENTER ifEmpoyeeAvaiableCallCustomerOrElseBreak()");
		SYS("employee.extension", (currentEmployee == null ? null : currentEmployee.getExtension()));
		SYS("isCallOnMobile", isCallOnMobile);
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("duringStartCampaign", duringStartCampaign);
		SYS("employeeTasksRequired", employeeTasksRequired);

		boolean toReturn = false;
		EmployeeCallErrorNotificationService employeeCallErrorNotificationService = applicationContext.getBean(EmployeeCallErrorNotificationService.class);
		NotificationRepository notificationRepository = applicationContext.getBean(NotificationRepository.class);
		Employee manager = campaign.getManager();

		Organization currentOrganization = null;
		Map<String, Organization> organizationMap =
				OrganizationData.workWithAllOrganizationData(currentEmployee.getOrganization(), null, "get-one", null);
		if (organizationMap != null) {
			currentOrganization = organizationMap.get(currentEmployee.getOrganization());
		} else {
			SYS("organizationMap is null -> return true");
			return true;
		}

		SYS("currentOrganization null?", (currentOrganization == null));
		if (currentOrganization != null) {
			SYS("org", currentOrganization.getOrganization());
			SYS("org.totalCalls", currentOrganization.getTotalCalls());
			SYS("org.callLimit", currentOrganization.getCallLimit());
		}

		try {
			if (currentEmployee.isEnabled()) {
				LoggerUtils.log.debug("Employee is enabled");
				SYS("employee enabled=true");
				if (currentOrganization.getCallLimit() != -1) {
					LoggerUtils.log.debug("Call Limit is not unlimites");
					SYS("callLimit not unlimited");
					if (currentOrganization.getTotalCalls() <= currentOrganization.getCallLimit()) {
						LoggerUtils.log.debug("Call Limit is greator than total calls made");
						SYS("callLimit ok -> initiateCall");
						toReturn = this.initiateCall(initiateAndLoadCampaignDataService, campaign, currentEmployee, isCallOnMobile, applicationContext, duringStartCampaign, employeeTasksRequired);
					} else {
						LoggerUtils.log.debug("topping campaign as call limit reached");
						SYS("callLimit reached -> stop campaign and notify manager");
						employeeCallErrorNotificationService.sendEmployeeCallLimitNotifications(
								manager.getExtension(),
								manager.getOrganization(),
								manager.getDomain(),
								campaign.getId(),
								notificationRepository
						);
						initiateAndLoadCampaignDataService.triggerStopCampaign(
								campaign,
								campaign.getManager().getExtension(),
								campaign.getManager().getDomain(),
								false,
								"call-limit-reached"
						);
						return true;
					}
				} else {
					SYS("callLimit is unlimited -> initiateCall");
					toReturn = this.initiateCall(initiateAndLoadCampaignDataService, campaign, currentEmployee, isCallOnMobile, applicationContext, duringStartCampaign, employeeTasksRequired);
				}
			} else {
				SYS("employee enabled=false -> return true");
				toReturn = true;
			}
		} catch (Exception e) {
			SYS("EXCEPTION in ifEmpoyeeAvaiableCallCustomerOrElseBreak: " + e.getMessage());
			toReturn = true;
			e.printStackTrace();
		}

		SYS("EXIT ifEmpoyeeAvaiableCallCustomerOrElseBreak() toReturn=" + toReturn);
		return toReturn;
	}


	/* =========================================================================================
	 * EMPLOYEE FLOW
	 * ========================================================================================= */

	public boolean initiateCall(InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService, Campaign campaign, Employee currentEmployee,
			boolean isCallOnMobile, ApplicationContext applicationContext, boolean duringStartCampaign, boolean employeeTasksRequired) {

		SYS("ENTER initiateCall()");
		SYS("employee.extension", (currentEmployee == null ? null : currentEmployee.getExtension()));
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("isCallOnMobile", isCallOnMobile);
		SYS("duringStartCampaign", duringStartCampaign);
		SYS("employeeTasksRequired", employeeTasksRequired);

		boolean toReturn = false;

		try {
			EmployeeCallErrorNotificationService employeeCallErrorNotificationService = applicationContext.getBean(EmployeeCallErrorNotificationService.class);
			NotificationRepository notificationRepository = applicationContext.getBean(NotificationRepository.class);
			AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);

			LoggerUtils.log.debug("callCustomerOrElseBreak..");
			int breathingSeconds = campaign.getBreathingSeconds();
			SYS("breathingSeconds", breathingSeconds);

			PickCustomerResult pick = pickNextCustomerAndOrg(campaign, applicationContext, employeeTasksRequired);
			SYS("pick.outcome", (pick == null ? null : pick.outcome));

			if (pick.outcome == PickCustomerResult.Outcome.STOP_NOW) {
				SYS("pick outcome STOP_NOW -> return true");
				return true;
			}

			if (pick.outcome == PickCustomerResult.Outcome.NOT_CALLABLE_ADVANCED) {
				SYS("pick outcome NOT_CALLABLE_ADVANCED -> startCallFlowForEmployee thread then return true");
				Thread startCallFlowForEmployee = new Thread() {
					public void run() {
						try {
							autodialerReinitiateAndFunctionService.startCallFlowForEmployee(currentEmployee, campaign, duringStartCampaign);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				};
				startCallFlowForEmployee.start();
				return true;
			}

			Customers currentCustomer = pick.customer;
			CustomerAndItsCampaignDTO dto = pick.dto;

			SYS("currentCustomer null?", (currentCustomer == null));
			if (currentCustomer != null) {
				SYS("currentCustomer.phone", currentCustomer.getPhoneNumber());
				SYS("currentCustomer.firstName", currentCustomer.getFirstname());
				SYS("currentCustomer.org", currentCustomer.getOrganization());
			}

			if (currentCustomer != null && currentEmployee != null) {
				try {
					SYS("CALL INITIATED (employee flow) -> setLastCallThenNotificationFinallyScheduleCall");
					toReturn = this.setLastCallThenNotificationFinallyScheduleCall(
							dto, currentEmployee, currentCustomer, campaign, breathingSeconds,
							isCallOnMobile, applicationContext, duringStartCampaign, employeeTasksRequired
					);
					SYS("setLastCallThenNotificationFinallyScheduleCall returned toReturn=" + toReturn);
				} catch (Exception e) {
					try {
						SYS("EXCEPTION while originating scheduled call: " + e.getMessage());
						e.printStackTrace();
						LoggerUtils.log.debug("Eror while originating scheduled call, sending notification to employee who scheduled it.");
						employeeCallErrorNotificationService.sendEmployeeCallErrorNotifications(
								currentEmployee.getExtension(),
								currentEmployee.getFirstName(),
								currentEmployee.getPhonenumber(),
								currentEmployee.getOrganization(),
								currentEmployee.getDomain(),
								notificationRepository
						);
					} catch (Exception e1) {
						SYS("EXCEPTION while sending employee error notification: " + e1.getMessage());
						e1.printStackTrace();
					}
				}
			}

			if (advancePageOrStop(campaign, applicationContext)) {
				SYS("advancePageOrStop returned true -> return true");
				return true;
			}

		} catch (Exception e) {
			SYS("EXCEPTION in initiateCall: " + e.getMessage());
			toReturn = true;
			e.printStackTrace();
		}

		SYS("EXIT initiateCall() toReturn=" + toReturn);
		return toReturn;
	}

	public boolean setLastCallThenNotificationFinallyScheduleCall(CustomerAndItsCampaignDTO customerAndItsCampaignDTO, Employee currentEmployee,
			Customers currentCustomer, Campaign campaign, int breathingSeconds, boolean isCallOnMobile, ApplicationContext applicationContext,
			boolean duringStartCampaign, boolean employeeTasksRequired) {

		SYS("ENTER setLastCallThenNotificationFinallyScheduleCall()");
		SYS("employee.extension", (currentEmployee == null ? null : currentEmployee.getExtension()));
		SYS("customer.phone", (currentCustomer == null ? null : currentCustomer.getPhoneNumber()));
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("breathingSeconds", breathingSeconds);
		SYS("isCallOnMobile", isCallOnMobile);
		SYS("duringStartCampaign", duringStartCampaign);
		SYS("employeeTasksRequired", employeeTasksRequired);

		boolean toReturn = false;

		try {
			LoggerUtils.log.debug("setLastCallThenNotificationFinallyScheduleCall");

			CustomerMapper customerMapper = applicationContext.getBean(CustomerMapper.class);
			SendDialerCallDetailToExtensionService sendDialerCallDetailToExtensionService = applicationContext.getBean(SendDialerCallDetailToExtensionService.class);
			AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);
			InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService = applicationContext.getBean(InitiateAndLoadCampaignDataService.class);
			ScheduleDialAutomateCallService scheduleDialAutomateCallService = applicationContext.getBean(ScheduleDialAutomateCallService.class);

			Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
					EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
			EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
			if (allEmployeeDataAndState != null) {
				employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
			}

			SYS("employeeDataAndStateDTO null?", (employeeDataAndStateDTO == null));

			// Existing behavior: last call details update
			SYS("updateLastCallDetails (employee flow) START");
			initiateAndLoadCampaignDataService.updateLastCallDetails(
					customerAndItsCampaignDTO,
					employeeDataAndStateDTO,
					currentEmployee,
					null,
					currentCustomer,
					campaign.getId(),
					employeeTasksRequired
			);
			SYS("updateLastCallDetails (employee flow) DONE");

			boolean remindercalling = autodialerReinitiateAndFunctionService.checkReminderCalling(currentCustomer, campaign);
			SYS("remindercalling", remindercalling);

			LoggerUtils.log.debug("Send Notification");
			SYS("Send Notification -> sendMessageToExtension");
			CallToExtensionDTO callToExtensionDTO = new CallToExtensionDTO();
			callToExtensionDTO.setCurrentDate(new Date());
			callToExtensionDTO.setAutodialertype(campaign.getAutodialertype());
			callToExtensionDTO.setCampginId(campaign.getId());
			callToExtensionDTO.setCampginName(campaign.getName());
			callToExtensionDTO.setRemindercalling(remindercalling);
			callToExtensionDTO.setCustomer(customerMapper.mapCustomersToDto(currentCustomer));

			try {
				sendDialerCallDetailToExtensionService.sendMessageToExtension(callToExtensionDTO, currentEmployee);
				SYS("sendMessageToExtension DONE");
			} catch (Exception e) {
				SYS("sendMessageToExtension EXCEPTION: " + e.getMessage());
				LoggerUtils.log.debug(e.getMessage());
			}

			LoggerUtils.log.debug("Scheduling call..");
			SYS("Scheduling call.. (employee flow)");

			String callJobId = autodialerReinitiateAndFunctionService.buildOnlyCustomerJobIdFromPhone(currentCustomer.getPhoneNumber());
			String callType = "Outbound";
			SYS("callJobId", callJobId);
			SYS("callType", callType);
			SYS("dial to customer", currentCustomer.getPhoneNumber());
			SYS("from extension", currentEmployee.getExtension());
			SYS("from phone", currentEmployee.getPhonenumber());
			SYS("org", currentEmployee.getOrganization());
			SYS("domain", currentEmployee.getDomain());
			SYS("context", currentEmployee.getPhoneContext());
			SYS("protocol", currentEmployee.getProtocol());
			SYS("phoneTrunk", currentEmployee.getPhoneTrunk());
			SYS("useSecondaryLine", currentEmployee.isUseSecondaryAllotedLine());
			SYS("secondDomain", currentEmployee.getSecondDomain());
			SYS("autodialerType", campaign.getAutodialertype());

			autodialerReinitiateAndFunctionService.executeAutodialerCall(callJobId, currentCustomer.getPhoneNumber(), currentEmployee.getExtension(), currentEmployee.getPhonenumber(), isCallOnMobile, callType, currentEmployee.getOrganization(),currentEmployee.getDomain(), currentEmployee.getPhoneContext(), 1, 30000L, currentCustomer.getFirstname(), currentEmployee.getProtocol(), currentEmployee.getPhoneTrunk(), currentEmployee.isUseSecondaryAllotedLine(), currentEmployee.getSecondDomain(), breathingSeconds, campaign.getAutodialertype(),null,false);
			autodialerReinitiateAndFunctionService.recordCampaignRunCallState(callJobId, campaign, currentCustomer.getPhoneNumber(), null, "INITIATED", currentCustomer.getPhoneNumber(),null);
			StartedCampaignData.putjobIDCampaignMapping(callJobId, campaign.getId());
			
			String reminderCallJobId = TrackedSchduledJobs.dialAutomateCallReminder + currentEmployee.getExtension().toString();
			SYS("reminderCallJobId", reminderCallJobId);

			DialAutomateCallReminderRunnable dialAutomateCallReminderRunnable = new DialAutomateCallReminderRunnable();
			dialAutomateCallReminderRunnable.setJobId(reminderCallJobId);
			dialAutomateCallReminderRunnable.setPhoneNumber(currentCustomer.getPhoneNumber());
			dialAutomateCallReminderRunnable.setFromExtension(currentEmployee.getExtension());
			dialAutomateCallReminderRunnable.setCallType("Outbound");
			dialAutomateCallReminderRunnable.setOrganization(currentEmployee.getOrganization());
			dialAutomateCallReminderRunnable.setDomain(currentEmployee.getDomain());
			dialAutomateCallReminderRunnable.setContext(currentEmployee.getPhoneContext());
			dialAutomateCallReminderRunnable.setProtocol(currentEmployee.getProtocol());
			dialAutomateCallReminderRunnable.setPhoneTrunk(currentEmployee.getPhoneTrunk());
			dialAutomateCallReminderRunnable.setPriority(1);
			dialAutomateCallReminderRunnable.setTimeOut(30000L);
			dialAutomateCallReminderRunnable.setFirstName(currentEmployee.getFirstName());
			dialAutomateCallReminderRunnable.setApplicationContext(applicationContext);
			dialAutomateCallReminderRunnable.setUseSecondaryLine(currentEmployee.isUseSecondaryAllotedLine());
			dialAutomateCallReminderRunnable.setCallOnMobile(isCallOnMobile);
			dialAutomateCallReminderRunnable.setFromPhoneNumber(currentEmployee.getPhonenumber());
			dialAutomateCallReminderRunnable.setSecondDomain(currentEmployee.getSecondDomain());
			dialAutomateCallReminderRunnable.setBreathingSeconds(breathingSeconds);
			dialAutomateCallReminderRunnable.setAutodialerType(campaign.getAutodialertype());

			if (remindercalling) {
				SYS("Scheduling reminder runnable after breathingSeconds");
				scheduleDialAutomateCallService.removeAndScheduleDialAutomateCallReminder(breathingSeconds, dialAutomateCallReminderRunnable);
			} else {
				SYS("Removing reminder schedule (not reminder calling)");
				scheduleDialAutomateCallService.removeScheduleDialAutomateCall(reminderCallJobId);
			}

			LoggerUtils.log.debug("Scheduling cron for employee..");
			SYS("Scheduling cron for employee..");
			autodialerReinitiateAndFunctionService.removeAndScheduleCronForEmployee(currentEmployee, campaign);

		} catch (Exception e) {
			SYS("EXCEPTION in setLastCallThenNotificationFinallyScheduleCall: " + e.getMessage());
			toReturn = true;
			e.printStackTrace();
		}

		SYS("EXIT setLastCallThenNotificationFinallyScheduleCall() toReturn=" + toReturn);
		return toReturn;
	}

	/* =========================================================================================
	 * ONLY-CUSTOMER FLOW
	 * ========================================================================================= */

	public boolean initiateAutomationOnlyCustomer(Campaign campaign, ApplicationContext applicationContext,
			boolean duringStartCampaign) {
		SYS("ENTER initiateAutomationOnlyCustomer(3-args)");
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("duringStartCampaign", duringStartCampaign);

		AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);
		boolean employeeTasksRequired = autodialerReinitiateAndFunctionService.findIfWeRequireEmployeeForAutodialer(campaign.getAutodialertype());
		SYS("employeeTasksRequired", employeeTasksRequired);
		boolean r = initiateAutomationOnlyCustomer(campaign,applicationContext,duringStartCampaign,employeeTasksRequired);
		SYS("EXIT initiateAutomationOnlyCustomer(3-args) r=" + r);
		return r;
	}

	public boolean initiateAutomationOnlyCustomer(Campaign campaign, ApplicationContext applicationContext,
			boolean duringStartCampaign,boolean employeeTasksRequired) {

		SYS("ENTER initiateAutomationOnlyCustomer(4-args)");
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("duringStartCampaign", duringStartCampaign);
		SYS("employeeTasksRequired", employeeTasksRequired);

		boolean toReturn = false;

		try {
			EmployeeCallErrorNotificationService employeeCallErrorNotificationService = applicationContext.getBean(EmployeeCallErrorNotificationService.class);
			NotificationRepository notificationRepository = applicationContext.getBean(NotificationRepository.class);

			LoggerUtils.log.debug("callCustomerOrElseBreak..");
			int breathingSeconds = campaign.getBreathingSeconds();
			SYS("breathingSeconds", breathingSeconds);

			PickCustomerResult pick = pickNextCustomerAndOrg(campaign, applicationContext, employeeTasksRequired);
			SYS("pick.outcome", (pick == null ? null : pick.outcome));

			if (pick.outcome == PickCustomerResult.Outcome.STOP_NOW) {
				SYS("pick outcome STOP_NOW -> return true");
				return true;
			}

			if (pick.outcome == PickCustomerResult.Outcome.NOT_CALLABLE_ADVANCED) {
				SYS("pick outcome NOT_CALLABLE_ADVANCED -> return true");
				return true;
			}

			Customers currentCustomer = pick.customer;
			CustomerAndItsCampaignDTO dto = pick.dto;
			Organization currentOrganization = pick.organization;

			SYS("currentCustomer null?", (currentCustomer == null));
			if (currentCustomer != null) {
				SYS("currentCustomer.phone", currentCustomer.getPhoneNumber());
				SYS("currentCustomer.firstName", currentCustomer.getFirstname());
				SYS("currentCustomer.org", currentCustomer.getOrganization());
			}
			SYS("currentOrganization null?", (currentOrganization == null));
			if (currentOrganization != null) {
				SYS("org", currentOrganization.getOrganization());
				SYS("org.callLimit", currentOrganization.getCallLimit());
				SYS("org.totalCalls", currentOrganization.getTotalCalls());
			}

			if (currentCustomer != null) {
				boolean blockCall = enforceCallLimitOrStopCampaign(campaign, currentOrganization, applicationContext, employeeTasksRequired);
				SYS("enforceCallLimitOrStopCampaign blockCall=" + blockCall);
				if (blockCall) {
					return true;
				}

				try {
					SYS("CALL INITIATED (only-customer flow) -> setLastCallThenScheduleOnlyCustomer");
					toReturn = this.setLastCallThenScheduleOnlyCustomer(
							currentOrganization, dto, currentCustomer, campaign, breathingSeconds,
							applicationContext, duringStartCampaign, employeeTasksRequired
					);
					SYS("setLastCallThenScheduleOnlyCustomer returned toReturn=" + toReturn);
				} catch (Exception e) {
					try {
						SYS("EXCEPTION while originating scheduled call (only-customer): " + e.getMessage());
						e.printStackTrace();
						LoggerUtils.log.debug("Eror while originating scheduled call, sending notification to employee who scheduled it.");
						employeeCallErrorNotificationService.sendEmployeeCallErrorNotifications(
								campaign.getManager().getExtension(),
								campaign.getManager().getFirstName(),
								campaign.getManager().getPhonenumber(),
								campaign.getManager().getOrganization(),
								campaign.getManager().getDomain(),
								notificationRepository
						);
					} catch (Exception e1) {
						SYS("EXCEPTION while sending manager error notification: " + e1.getMessage());
						e1.printStackTrace();
					}
				}
			}

			if (advancePageOrStop(campaign, applicationContext)) {
				SYS("advancePageOrStop returned true -> return true");
				return true;
			}

		} catch (Exception e) {
			SYS("EXCEPTION in initiateAutomationOnlyCustomer: " + e.getMessage());
			toReturn = true;
			e.printStackTrace();
		}

		SYS("EXIT initiateAutomationOnlyCustomer(4-args) toReturn=" + toReturn);
		return toReturn;
	}

	public boolean setLastCallThenScheduleOnlyCustomer(Organization organization, CustomerAndItsCampaignDTO customerAndItsCampaignDTO,
			Customers currentCustomer, Campaign campaign, int breathingSeconds, ApplicationContext applicationContext,
			boolean duringStartCampaign, boolean employeeTasksRequired) {

		SYS("ENTER setLastCallThenScheduleOnlyCustomer()");
		SYS("org", (organization == null ? null : organization.getOrganization()));
		SYS("customer.phone", (currentCustomer == null ? null : currentCustomer.getPhoneNumber()));
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("breathingSeconds", breathingSeconds);
		SYS("duringStartCampaign", duringStartCampaign);
		SYS("employeeTasksRequired", employeeTasksRequired);

		boolean toReturn = false;

		try {
			LoggerUtils.log.debug("setLastCallThenNotificationFinallyScheduleCall");

			SchedulerService schedulerService = applicationContext.getBean(SchedulerService.class);
			InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService = applicationContext.getBean(InitiateAndLoadCampaignDataService.class);
			AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);

			String connectedLine = autodialerReinitiateAndFunctionService.getConnectedToLine(campaign);
			SYS("connectedLine", connectedLine);

			if (connectedLine == null) {
				SYS("connectedLine is null -> triggerStopCampaign and return true");
				initiateAndLoadCampaignDataService.triggerStopCampaign(campaign, campaign.getManager().getExtension(), campaign.getManager().getDomain(),false,"no-connected-line-found");
				return true;
			}

			// Existing behavior: last call details update
			SYS("updateLastCallDetails (only-customer flow) START");
			initiateAndLoadCampaignDataService.updateLastCallDetails(
					customerAndItsCampaignDTO,
					null,
					null,
					connectedLine,
					currentCustomer,
					campaign.getId(),
					employeeTasksRequired
			);
			SYS("updateLastCallDetails (only-customer flow) DONE");

			LoggerUtils.log.debug("Scheduling call..");
			SYS("Scheduling call.. (only-customer flow)");

			String callJobId = "";
			if(campaign.getAutodialertype().equals(AUTODIALER_TYPE.WHATSAPP_MESSAGE.name()))
				{
					callJobId = TrackedSchduledJobs.sendWhatsApp + "-ch-" + currentCustomer.getPhoneNumber();
				}
			else
				{
					callJobId = autodialerReinitiateAndFunctionService.buildOnlyCustomerJobIdFromPhone(currentCustomer.getPhoneNumber());
				}

			SYS("callJobId", callJobId);

			String callType = "Outbound";

			if(campaign.getAutodialertype().equals(AUTODIALER_TYPE.WHATSAPP_MESSAGE.name()))
				{
					autodialerReinitiateAndFunctionService.executeAutodialerWhatsAppTemplateMessage(callJobId, currentCustomer, connectedLine, campaign.getTemplate(),campaign);
				}
			else
				{
					autodialerReinitiateAndFunctionService.executeAutodialerCall(callJobId, currentCustomer.getPhoneNumber(), connectedLine, null, false, callType, organization.getOrganization(),campaign.getDomain(), organization.getPhoneContext(), 1, 30000L, currentCustomer.getFirstname(), organization.getProtocol(), organization.getPhoneTrunk(), organization.isUseSecondaryAllotedLine(), campaign.getAiApplicationDomain(), breathingSeconds, campaign.getAutodialertype(),campaign.getAiApplicationName(),false);
					autodialerReinitiateAndFunctionService.recordCampaignRunCallState(callJobId, campaign, currentCustomer.getPhoneNumber(), null, "INITIATED", currentCustomer.getPhoneNumber(),null);
					StartedCampaignData.putjobIDCampaignMapping(callJobId, campaign.getId());
				}

			LoggerUtils.log.debug("Scheduling job after x seconds for only customer dialer..");
			SYS("Scheduling job after x seconds for only customer dialer..");
			String reinitiateJobId = autodialerReinitiateAndFunctionService.buildReinitiateOnlyCustomerJobIdFromPhone(currentCustomer.getPhoneNumber());
			SYS("reinitiateJobId", reinitiateJobId);
			
			DialReinitiateOnlyCustomerDialerRunnable dialReinitiateOnlyCustomerDialerRunnable = new DialReinitiateOnlyCustomerDialerRunnable();
			dialReinitiateOnlyCustomerDialerRunnable.setCampaign(campaign);
			dialReinitiateOnlyCustomerDialerRunnable.setApplicationContext(applicationContext);
			dialReinitiateOnlyCustomerDialerRunnable.setJobId(reinitiateJobId);

			schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(reinitiateJobId, dialReinitiateOnlyCustomerDialerRunnable, breathingSeconds+600);

		} catch (Exception e) {
			SYS("EXCEPTION in setLastCallThenScheduleOnlyCustomer: " + e.getMessage());
			toReturn = true;
			e.printStackTrace();
		}

		SYS("EXIT setLastCallThenScheduleOnlyCustomer() toReturn=" + toReturn);
		return toReturn;
	}



	/* =========================================================================================
	 * COMMON: customer pick + org load + callability check (NO DUPLICATION)
	 * ========================================================================================= */

	private static class PickCustomerResult {
		private enum Outcome {
			STOP_NOW,
			NOT_CALLABLE_ADVANCED,
			FOUND
		}

		private Outcome outcome;
		private String phone;
		private CustomerAndItsCampaignDTO dto;
		private Customers customer;
		private Organization organization;
	}

	private PickCustomerResult pickNextCustomerAndOrg(Campaign campaign, ApplicationContext applicationContext, boolean employeeTasksRequired) {

	    SYS("ENTER pickNextCustomerAndOrg()");
	    SYS("campaignId", (campaign == null ? null : campaign.getId()));
	    SYS("employeeTasksRequired", employeeTasksRequired);

	    ReentrantLock lock = lockForCampaign(campaign.getId());
	    SYS("lockForCampaign acquired? (before lock)");
	    lock.lock();
	    SYS("lockForCampaign LOCKED");
	    try {
	        PickCustomerResult res = new PickCustomerResult();

	        InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService =
	                applicationContext.getBean(InitiateAndLoadCampaignDataService.class);
	        AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService =
	                applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);

	        LoggerUtils.log.debug("Get Current Customer has PhoneNumer");
	        SYS("Get Current Customer has PhoneNumer");

	        // IMPORTANT: use an atomic pick+advance here if you have it.
	        // If you don't have it, at least lock prevents duplicates within this JVM.
	        String customerPhoneNumber = initiateAndLoadCampaignDataService.getCustomerPhoneNumber(campaign, employeeTasksRequired);
	        SYS("picked customerPhoneNumber", customerPhoneNumber);

	        if (customerPhoneNumber == null) {
	            SYS("customerPhoneNumber is null -> STOP_NOW");
	            res.outcome = PickCustomerResult.Outcome.STOP_NOW;
	            return res;
	        }

	        boolean isVerifiedCustomer = autodialerReinitiateAndFunctionService.verifyIfCustomerCallable(customerPhoneNumber);
	        SYS("isVerifiedCustomer", isVerifiedCustomer);
	        if (!isVerifiedCustomer) {
	            LoggerUtils.log.debug("Customer not callable right now. Increasing campaign page variables...");
	            SYS("Customer not callable -> advanceOnlyAtomic");
	            boolean ok = initiateAndLoadCampaignDataService.advanceOnlyAtomic(campaign);
	            SYS("advanceOnlyAtomic ok", ok);
	            if (!ok) {
	                SYS("advanceOnlyAtomic returned false -> STOP_NOW");
	                res.outcome = PickCustomerResult.Outcome.STOP_NOW;
	                return res;
	            }
	            res.outcome = PickCustomerResult.Outcome.NOT_CALLABLE_ADVANCED;
	            return res;
	        }

	        LoggerUtils.log.debug("Get Current Customer");
	        SYS("Get Current Customer");

	        CustomerAndItsCampaignDTO customerAndItsCampaignDTO = null;

	        Map<String, CustomerAndItsCampaignDTO> allCustomersAndItsCampaignDTO =
	                StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(customerPhoneNumber, null, "get-one");
	        SYS("allCustomersAndItsCampaignDTO null?", (allCustomersAndItsCampaignDTO == null));
	        if (allCustomersAndItsCampaignDTO != null) {
	            customerAndItsCampaignDTO = allCustomersAndItsCampaignDTO.get(customerPhoneNumber);
	        }

	        SYS("customerAndItsCampaignDTO null?", (customerAndItsCampaignDTO == null));
	        if (customerAndItsCampaignDTO == null) {
	            res.outcome = PickCustomerResult.Outcome.STOP_NOW;
	            return res;
	        }

	        int index = customerAndItsCampaignDTO.getCampaignIds().indexOf(campaign.getId());
	        SYS("campaign index in customerAndItsCampaignDTO", index);
	        if (index == -1) {
	            res.outcome = PickCustomerResult.Outcome.STOP_NOW;
	            return res;
	        }

	        Customers currentCustomer = customerAndItsCampaignDTO.getCustomers().get(index);
	        SYS("currentCustomer null?", (currentCustomer == null));
	        if (currentCustomer == null) {
	            res.outcome = PickCustomerResult.Outcome.STOP_NOW;
	            return res;
	        }

	        Organization currentOrganization = null;
	        Map<String, Organization> organizationMap =
	                OrganizationData.workWithAllOrganizationData(currentCustomer.getOrganization(), null, "get-one", null);
	        SYS("organizationMap null?", (organizationMap == null));
	        if (organizationMap != null) {
	            currentOrganization = organizationMap.get(currentCustomer.getOrganization());
	        } else {
	            res.outcome = PickCustomerResult.Outcome.STOP_NOW;
	            return res;
	        }

	        SYS("resolved org null?", (currentOrganization == null));
	        if (currentOrganization != null) {
	        	SYS("resolved org", currentOrganization.getOrganization());
	        	SYS("resolved domain", currentOrganization.getDomain());
	        }

	        res.outcome = PickCustomerResult.Outcome.FOUND;
	        res.phone = customerPhoneNumber;
	        res.dto = customerAndItsCampaignDTO;
	        res.customer = currentCustomer;
	        res.organization = currentOrganization;

	        SYS("EXIT pickNextCustomerAndOrg() FOUND phone=" + customerPhoneNumber);
	        return res;

	    } catch (Exception e) {
	        SYS("EXCEPTION in pickNextCustomerAndOrg: " + e.getMessage());
	        e.printStackTrace();
	        PickCustomerResult res = new PickCustomerResult();
	        res.outcome = PickCustomerResult.Outcome.STOP_NOW;
	        return res;
	    } finally {
	        lock.unlock();
	        SYS("lockForCampaign UNLOCKED");
	    }
	}

	private boolean enforceCallLimitOrStopCampaign(Campaign campaign, Organization currentOrganization,
			ApplicationContext applicationContext, boolean employeeTasksRequired) {

		SYS("ENTER enforceCallLimitOrStopCampaign()");
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		SYS("org null?", (currentOrganization == null));
		SYS("employeeTasksRequired", employeeTasksRequired);

		try {
			EmployeeCallErrorNotificationService employeeCallErrorNotificationService = applicationContext.getBean(EmployeeCallErrorNotificationService.class);
			NotificationRepository notificationRepository = applicationContext.getBean(NotificationRepository.class);
			InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService = applicationContext.getBean(InitiateAndLoadCampaignDataService.class);

			if (currentOrganization == null) {
				SYS("currentOrganization null -> blockCall=true");
				return true;
			}

			if (currentOrganization.getCallLimit() != -1) {
				LoggerUtils.log.debug("Call Limit is not unlimites");
				SYS("callLimit not unlimited");
				SYS("org.totalCalls", currentOrganization.getTotalCalls());
				SYS("org.callLimit", currentOrganization.getCallLimit());

				if (currentOrganization.getTotalCalls() <= currentOrganization.getCallLimit()) {
					LoggerUtils.log.debug("Call Limit is greator than total calls made, let call go");
					SYS("callLimit ok -> blockCall=false");
					return false;
				} else {
					LoggerUtils.log.debug("topping campaign as call limit reached");
					SYS("callLimit reached -> stop campaign and notify manager");
					employeeCallErrorNotificationService.sendEmployeeCallLimitNotifications(
							campaign.getManager().getExtension(),
							campaign.getManager().getOrganization(),
							campaign.getManager().getDomain(),
							campaign.getId(),
							notificationRepository
					);
					initiateAndLoadCampaignDataService.triggerStopCampaign(
							campaign,
							campaign.getManager().getExtension(),
							campaign.getManager().getDomain(),
							false,
							"call-limit-reached"
					);
					return true;
				}
			}

			LoggerUtils.log.debug("Unlimited call Limit, let call go");
			SYS("Unlimited callLimit -> blockCall=false");
			return false;

		} catch (Exception e) {
			SYS("EXCEPTION in enforceCallLimitOrStopCampaign: " + e.getMessage());
			e.printStackTrace();
			return true;
		}
	}

	private boolean advancePageOrStop(Campaign campaign, ApplicationContext applicationContext) {
		SYS("ENTER advancePageOrStop()");
		SYS("campaignId", (campaign == null ? null : campaign.getId()));
		try {
			InitiateAndLoadCampaignDataService initiateAndLoadCampaignDataService = applicationContext.getBean(InitiateAndLoadCampaignDataService.class);
			LoggerUtils.log.debug("Increasing campage page variables...");
			SYS("Increasing campage page variables... -> pickCustomerAndAdvanceAtomic");
			boolean stop = (initiateAndLoadCampaignDataService.pickCustomerAndAdvanceAtomic(campaign) == null);
			SYS("pickCustomerAndAdvanceAtomic returned null? stop=" + stop);
			SYS("EXIT advancePageOrStop() stop=" + stop);
			return stop;
		} catch (Exception e) {
			SYS("EXCEPTION in advancePageOrStop: " + e.getMessage());
			e.printStackTrace();
			return true;
		}
	}

}
