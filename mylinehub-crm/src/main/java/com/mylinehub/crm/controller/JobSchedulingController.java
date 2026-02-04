package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.JOB_SCHEDULING_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import org.springframework.web.bind.annotation.RestController;

import com.mylinehub.crm.TaskScheduler.StartCampaignRunnable;
import com.mylinehub.crm.TaskScheduler.StopCampaignRunnable;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.RunningSchedule;
import com.mylinehub.crm.entity.dto.AfterNSecondsSchedulerDefinitionDTO;
import com.mylinehub.crm.entity.dto.CronSchedulerDefinitionDTO;
import com.mylinehub.crm.entity.dto.FixedDateSchedulerDefinitionDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.JobSchedulingService;
import com.mylinehub.crm.service.RunningScheduleService;
import com.mylinehub.crm.service.SchedulerService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = JOB_SCHEDULING_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class JobSchedulingController {

	@Autowired
    private SchedulerService schedulerService;
	private final EmployeeRepository employeeRepository;
	private final JwtConfiguration jwtConfiguration;
	private final SecretKey secretKey;
	private final ApplicationContext applicationContext;
	private final RunningScheduleService runningScheduleService;
	private final JobSchedulingService jobSchedulingService;

    @PostMapping(path="/scheduleACronCampaignToStart", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> scheduleACronCampaignToStart(@RequestBody CronSchedulerDefinitionDTO cronSchedulerDefinitionDTO,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleACronCampaignToStart =========");
    	System.out.println("[DEEP] payload.campaignId=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getCampaignId() : null));
    	System.out.println("[DEEP] payload.organization=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.cron=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getCronExpression() : null));
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("=============================================================");

		Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));
    	System.out.println("[DEEP] employee.domain=" + (employee != null ? employee.getDomain() : null));

    	if(employee.getOrganization().trim().equals(cronSchedulerDefinitionDTO.getOrganization()))
    	{
    		String jobId = TrackedSchduledJobs.cron+"-"+TrackedSchduledJobs.startCampaignRunnable+cronSchedulerDefinitionDTO.getCampaignId().toString();
    		System.out.println("[DEEP] ORG MATCH OK. jobId=" + jobId);

    		StartCampaignRunnable startCampaignRunnable = new StartCampaignRunnable();
    		startCampaignRunnable.setJobId(jobId);
    		startCampaignRunnable.setCampaignID(cronSchedulerDefinitionDTO.getCampaignId());
    		startCampaignRunnable.setOrganization(cronSchedulerDefinitionDTO.getOrganization());
    		startCampaignRunnable.setFromExtension(employee.getExtension());
    		startCampaignRunnable.setDomain(employee.getDomain());
    		startCampaignRunnable.setApplicationContext(applicationContext);

    		System.out.println("[DEEP] Scheduling CRON start: jobId=" + jobId + " cron=" + cronSchedulerDefinitionDTO.getCronExpression());
    		schedulerService.removeIfExistsAndScheduleACronTask(jobId, startCampaignRunnable, cronSchedulerDefinitionDTO.getCronExpression());
    		System.out.println("[DEEP] schedulerService.removeIfExistsAndScheduleACronTask DONE for jobId=" + jobId);

        	//Schedule to database
        	RunningSchedule runningSchedule = new RunningSchedule();
        	runningSchedule.setJobId(jobId);
        	runningSchedule.setFunctionality(TrackedSchduledJobs.startCampaignRunnable);
        	runningSchedule.setScheduleType(TrackedSchduledJobs.cron);
        	runningSchedule.setCronExpression(cronSchedulerDefinitionDTO.getCronExpression());
        	runningSchedule.setDate(null);
        	runningSchedule.setCampaignId(cronSchedulerDefinitionDTO.getCampaignId());
        	runningSchedule.setActionType(cronSchedulerDefinitionDTO.getActionType());
        	runningSchedule.setData(cronSchedulerDefinitionDTO.getData());
        	runningSchedule.setOrganization(cronSchedulerDefinitionDTO.getOrganization());
        	runningSchedule.setDomain(cronSchedulerDefinitionDTO.getDomain());
        	runningSchedule.setSeconds(0);
        	runningSchedule.setPhoneNumber(cronSchedulerDefinitionDTO.getPhoneNumber());
        	runningSchedule.setCallType(cronSchedulerDefinitionDTO.getCallType());
        	runningSchedule.setFromExtension(cronSchedulerDefinitionDTO.getFromExtension());
        	runningSchedule.setContext(cronSchedulerDefinitionDTO.getContext());
        	runningSchedule.setPriority(cronSchedulerDefinitionDTO.getPriority());
        	runningSchedule.setTimeOut(cronSchedulerDefinitionDTO.getTimeOut());
        	runningSchedule.setFirstName(cronSchedulerDefinitionDTO.getFirstName());
        	runningSchedule.setProtocol(cronSchedulerDefinitionDTO.getProtocol());
        	runningSchedule.setPhoneTrunk(cronSchedulerDefinitionDTO.getPhoneTrunk());
        	runningSchedule.setFromExtension(employee.getExtension());

        	System.out.println("[DEEP] Saving RunningSchedule: jobId=" + jobId + " type=cron func=startCampaignRunnable");
        	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
        	System.out.println("[DEEP] runningScheduleService.removeIfExistsAndCreateRunningSchedules DONE jobId=" + jobId);

        	toReturn = true;
        	System.out.println("[DEEP] EXIT scheduleACronCampaignToStart OK");
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + cronSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
   }

    @PostMapping(path="/scheduleAFixedDateCampaignToStart", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> scheduleAFixedDateCampaignToStart(@RequestBody FixedDateSchedulerDefinitionDTO fixedDateSchedulerDefinitionDTO,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleAFixedDateCampaignToStart =========");
    	System.out.println("[DEEP] payload.campaignId=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getCampaignId() : null));
    	System.out.println("[DEEP] payload.organization=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.date=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getDate() : null));
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("===============================================================");

		Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));
    	System.out.println("[DEEP] employee.domain=" + (employee != null ? employee.getDomain() : null));

    	if(employee.getOrganization().trim().equals(fixedDateSchedulerDefinitionDTO.getOrganization()))
    	{
    		String jobId = TrackedSchduledJobs.fixeddate+"-"+TrackedSchduledJobs.startCampaignRunnable+fixedDateSchedulerDefinitionDTO.getCampaignId().toString();
        	System.out.println("[DEEP] ORG MATCH OK. jobId=" + jobId);

    		StartCampaignRunnable startCampaignRunnable = new StartCampaignRunnable();
    		startCampaignRunnable.setJobId(jobId);
    		startCampaignRunnable.setCampaignID(fixedDateSchedulerDefinitionDTO.getCampaignId());
    		startCampaignRunnable.setOrganization(fixedDateSchedulerDefinitionDTO.getOrganization());
    		startCampaignRunnable.setFromExtension(employee.getExtension());
    		startCampaignRunnable.setDomain(employee.getDomain());
    		startCampaignRunnable.setApplicationContext(applicationContext);

    		System.out.println("[DEEP] Scheduling FIXED DATE start: jobId=" + jobId + " date=" + fixedDateSchedulerDefinitionDTO.getDate());
    		schedulerService.removeIfExistsAndScheduleATaskOnDate(jobId,startCampaignRunnable, fixedDateSchedulerDefinitionDTO.getDate());
    		System.out.println("[DEEP] schedulerService.removeIfExistsAndScheduleATaskOnDate DONE for jobId=" + jobId);

        	//Schedule to database
        	RunningSchedule runningSchedule = new RunningSchedule();
        	runningSchedule.setJobId(jobId);
        	runningSchedule.setFunctionality(TrackedSchduledJobs.startCampaignRunnable);
        	runningSchedule.setScheduleType(TrackedSchduledJobs.fixeddate);
        	runningSchedule.setCronExpression(null);
        	runningSchedule.setDate(fixedDateSchedulerDefinitionDTO.getDate());
        	runningSchedule.setCampaignId(fixedDateSchedulerDefinitionDTO.getCampaignId());
        	runningSchedule.setActionType(fixedDateSchedulerDefinitionDTO.getActionType());
        	runningSchedule.setData(fixedDateSchedulerDefinitionDTO.getData());
        	runningSchedule.setOrganization(fixedDateSchedulerDefinitionDTO.getOrganization());
        	runningSchedule.setDomain(fixedDateSchedulerDefinitionDTO.getDomain());
        	runningSchedule.setSeconds(0);
        	runningSchedule.setPhoneNumber(fixedDateSchedulerDefinitionDTO.getPhoneNumber());
        	runningSchedule.setCallType(fixedDateSchedulerDefinitionDTO.getCallType());
        	runningSchedule.setFromExtension(fixedDateSchedulerDefinitionDTO.getFromExtension());
        	runningSchedule.setContext(fixedDateSchedulerDefinitionDTO.getContext());
        	runningSchedule.setPriority(fixedDateSchedulerDefinitionDTO.getPriority());
        	runningSchedule.setTimeOut(fixedDateSchedulerDefinitionDTO.getTimeOut());
        	runningSchedule.setFirstName(fixedDateSchedulerDefinitionDTO.getFirstName());
        	runningSchedule.setProtocol(fixedDateSchedulerDefinitionDTO.getProtocol());
        	runningSchedule.setPhoneTrunk(fixedDateSchedulerDefinitionDTO.getPhoneTrunk());
        	runningSchedule.setFromExtension(employee.getExtension());

        	System.out.println("[DEEP] Saving RunningSchedule: jobId=" + jobId + " type=fixeddate func=startCampaignRunnable date=" + fixedDateSchedulerDefinitionDTO.getDate());
        	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
        	System.out.println("[DEEP] runningScheduleService.removeIfExistsAndCreateRunningSchedules DONE jobId=" + jobId);

        	toReturn = true;
        	System.out.println("[DEEP] EXIT scheduleAFixedDateCampaignToStart OK");
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + fixedDateSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
   }

    @PostMapping(path="/scheduleAfterNSecCampaignToStart", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> scheduleAfterNSecCampaignToStart(@RequestBody AfterNSecondsSchedulerDefinitionDTO afterNSecondsSchedulerDefinitionDTO,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleAfterNSecCampaignToStart =========");
    	System.out.println("[DEEP] payload.campaignId=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getCampaignId() : null));
    	System.out.println("[DEEP] payload.organization=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.seconds=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getSeconds() : null));
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("===============================================================");

		Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));
    	System.out.println("[DEEP] employee.domain=" + (employee != null ? employee.getDomain() : null));

    	if(employee.getOrganization().trim().equals(afterNSecondsSchedulerDefinitionDTO.getOrganization()))
    	{
    		String jobId = TrackedSchduledJobs.afternseconds+"-"+TrackedSchduledJobs.startCampaignRunnable+afterNSecondsSchedulerDefinitionDTO.getCampaignId().toString();
    		System.out.println("[DEEP] ORG MATCH OK. jobId=" + jobId);

    		StartCampaignRunnable startCampaignRunnable = new StartCampaignRunnable();
    		startCampaignRunnable.setJobId(jobId);
    		startCampaignRunnable.setCampaignID(afterNSecondsSchedulerDefinitionDTO.getCampaignId());
    		startCampaignRunnable.setOrganization(afterNSecondsSchedulerDefinitionDTO.getOrganization());
    		startCampaignRunnable.setFromExtension(employee.getExtension());
    		startCampaignRunnable.setDomain(employee.getDomain());
    		startCampaignRunnable.setApplicationContext(applicationContext);

    		System.out.println("[DEEP] Scheduling AFTER N SECONDS start: jobId=" + jobId + " seconds=" + afterNSecondsSchedulerDefinitionDTO.getSeconds());
        	schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(jobId, startCampaignRunnable, afterNSecondsSchedulerDefinitionDTO.getSeconds());
        	System.out.println("[DEEP] schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds DONE for jobId=" + jobId);

        	//Schedule to database
        	RunningSchedule runningSchedule = new RunningSchedule();
        	runningSchedule.setJobId(jobId);
        	runningSchedule.setFunctionality(TrackedSchduledJobs.startCampaignRunnable);
        	runningSchedule.setScheduleType(TrackedSchduledJobs.afternseconds);
        	runningSchedule.setCronExpression(null);
        	runningSchedule.setDate(null);
        	runningSchedule.setCampaignId(afterNSecondsSchedulerDefinitionDTO.getCampaignId());
        	runningSchedule.setActionType(afterNSecondsSchedulerDefinitionDTO.getActionType());
        	runningSchedule.setData(afterNSecondsSchedulerDefinitionDTO.getData());
        	runningSchedule.setOrganization(afterNSecondsSchedulerDefinitionDTO.getOrganization());
        	runningSchedule.setDomain(afterNSecondsSchedulerDefinitionDTO.getDomain());
        	runningSchedule.setSeconds(afterNSecondsSchedulerDefinitionDTO.getSeconds());
        	runningSchedule.setPhoneNumber(afterNSecondsSchedulerDefinitionDTO.getPhoneNumber());
        	runningSchedule.setCallType(afterNSecondsSchedulerDefinitionDTO.getCallType());
        	runningSchedule.setFromExtension(afterNSecondsSchedulerDefinitionDTO.getFromExtension());
        	runningSchedule.setContext(afterNSecondsSchedulerDefinitionDTO.getContext());
        	runningSchedule.setPriority(afterNSecondsSchedulerDefinitionDTO.getPriority());
        	runningSchedule.setTimeOut(afterNSecondsSchedulerDefinitionDTO.getTimeOut());
        	runningSchedule.setFirstName(afterNSecondsSchedulerDefinitionDTO.getFirstName());
        	runningSchedule.setProtocol(afterNSecondsSchedulerDefinitionDTO.getProtocol());
        	runningSchedule.setPhoneTrunk(afterNSecondsSchedulerDefinitionDTO.getPhoneTrunk());
        	runningSchedule.setFromExtension(employee.getExtension());

        	System.out.println("[DEEP] Saving RunningSchedule: jobId=" + jobId + " type=afternseconds func=startCampaignRunnable seconds=" + afterNSecondsSchedulerDefinitionDTO.getSeconds());
        	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
        	System.out.println("[DEEP] runningScheduleService.removeIfExistsAndCreateRunningSchedules DONE jobId=" + jobId);

        	toReturn = true;
        	System.out.println("[DEEP] EXIT scheduleAfterNSecCampaignToStart OK");
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + afterNSecondsSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
   }

    @GetMapping(path="/removeStartedScheduledCampgin")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> removeStartedScheduledCampgin(@RequestParam String scheduleType,
    		@RequestParam String campaignId,
    		@RequestParam String organization,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER removeStartedScheduledCampgin =========");
    	System.out.println("[DEEP] scheduleType=" + scheduleType + " campaignId=" + campaignId + " organization=" + organization);
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("=============================================================");

        Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));

    	if(employee.getOrganization().trim().equals(organization))
    	{
    		String jobId = scheduleType+"-"+TrackedSchduledJobs.startCampaignRunnable+campaignId.toString();
    		System.out.println("[DEEP] Removing scheduled START jobId=" + jobId);

    		schedulerService.removeScheduledTask(jobId);
    		System.out.println("[DEEP] schedulerService.removeScheduledTask DONE jobId=" + jobId);

        	runningScheduleService.deleteAllRunningSchedulesByJobId(jobId);
        	System.out.println("[DEEP] runningScheduleService.deleteAllRunningSchedulesByJobId DONE jobId=" + jobId);

    		toReturn = true;
    		System.out.println("[DEEP] EXIT removeStartedScheduledCampgin OK");
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() + " req.org=" + organization);
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
    }

    @GetMapping(path="/findIfScheduledStartedCampaignJob")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> findIfScheduledStartedCampaignJob(@RequestParam String scheduleType,
    		@RequestParam String campaignId,
    		@RequestParam String organization,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER findIfScheduledStartedCampaignJob =========");
    	System.out.println("[DEEP] scheduleType=" + scheduleType + " campaignId=" + campaignId + " organization=" + organization);
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("===============================================================");

    	boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));

    	if(employee.getOrganization().trim().equals(organization))
    	{
    		String jobId = scheduleType+"-"+TrackedSchduledJobs.startCampaignRunnable+campaignId.toString();
    		System.out.println("[DEEP] Checking scheduled START jobId=" + jobId);

    		toReturn = schedulerService.findIfScheduledTask(jobId);
    		System.out.println("[DEEP] schedulerService.findIfScheduledTask result=" + toReturn + " jobId=" + jobId);

    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() + " req.org=" + organization);
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
    }

    @PostMapping(path="/scheduleACronCampaignToStop", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> scheduleACronCampaignToStop(@RequestBody CronSchedulerDefinitionDTO cronSchedulerDefinitionDTO,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleACronCampaignToStop =========");
    	System.out.println("[DEEP] payload.campaignId=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getCampaignId() : null));
    	System.out.println("[DEEP] payload.organization=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.cron=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getCronExpression() : null));
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("===========================================================");

		Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));
    	System.out.println("[DEEP] employee.domain=" + (employee != null ? employee.getDomain() : null));

    	if(employee.getOrganization().trim().equals(cronSchedulerDefinitionDTO.getOrganization()))
    	{
    		String jobId = TrackedSchduledJobs.cron+"-"+TrackedSchduledJobs.stopCampaignRunnable+cronSchedulerDefinitionDTO.getCampaignId().toString();
    		System.out.println("[DEEP] ORG MATCH OK. jobId=" + jobId);

    		StopCampaignRunnable stopCampaignRunnable = new StopCampaignRunnable();
    		stopCampaignRunnable.setJobId(jobId);
    		stopCampaignRunnable.setCampaignID(cronSchedulerDefinitionDTO.getCampaignId());
    		stopCampaignRunnable.setOrganization(cronSchedulerDefinitionDTO.getOrganization());
    		stopCampaignRunnable.setFromExtension(employee.getExtension());
    		stopCampaignRunnable.setDomain(employee.getDomain());
    		stopCampaignRunnable.setApplicationContext(applicationContext);

    		System.out.println("[DEEP] Scheduling CRON stop: jobId=" + jobId + " cron=" + cronSchedulerDefinitionDTO.getCronExpression());
    		schedulerService.removeIfExistsAndScheduleACronTask(jobId, stopCampaignRunnable, cronSchedulerDefinitionDTO.getCronExpression());
    		System.out.println("[DEEP] schedulerService.removeIfExistsAndScheduleACronTask DONE for jobId=" + jobId);

        	//Schedule to database
        	RunningSchedule runningSchedule = new RunningSchedule();
        	runningSchedule.setJobId(jobId);
        	runningSchedule.setFunctionality(TrackedSchduledJobs.stopCampaignRunnable);
        	runningSchedule.setScheduleType(TrackedSchduledJobs.cron);
        	runningSchedule.setCronExpression(cronSchedulerDefinitionDTO.getCronExpression());
        	runningSchedule.setDate(null);
        	runningSchedule.setCampaignId(cronSchedulerDefinitionDTO.getCampaignId());
        	runningSchedule.setActionType(cronSchedulerDefinitionDTO.getActionType());
        	runningSchedule.setData(cronSchedulerDefinitionDTO.getData());
        	runningSchedule.setOrganization(cronSchedulerDefinitionDTO.getOrganization());
        	runningSchedule.setDomain(cronSchedulerDefinitionDTO.getDomain());
        	runningSchedule.setSeconds(0);
        	runningSchedule.setPhoneNumber(cronSchedulerDefinitionDTO.getPhoneNumber());
        	runningSchedule.setCallType(cronSchedulerDefinitionDTO.getCallType());
        	runningSchedule.setFromExtension(cronSchedulerDefinitionDTO.getFromExtension());
        	runningSchedule.setContext(cronSchedulerDefinitionDTO.getContext());
        	runningSchedule.setPriority(cronSchedulerDefinitionDTO.getPriority());
        	runningSchedule.setTimeOut(cronSchedulerDefinitionDTO.getTimeOut());
        	runningSchedule.setFirstName(cronSchedulerDefinitionDTO.getFirstName());
        	runningSchedule.setProtocol(cronSchedulerDefinitionDTO.getProtocol());
        	runningSchedule.setPhoneTrunk(cronSchedulerDefinitionDTO.getPhoneTrunk());
        	runningSchedule.setFromExtension(employee.getExtension());

        	System.out.println("[DEEP] Saving RunningSchedule: jobId=" + jobId + " type=cron func=stopCampaignRunnable");
        	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
        	System.out.println("[DEEP] runningScheduleService.removeIfExistsAndCreateRunningSchedules DONE jobId=" + jobId);

        	toReturn = true;
        	System.out.println("[DEEP] EXIT scheduleACronCampaignToStop OK");
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + cronSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
   }

    @PostMapping(path="/scheduleAFixedDateCampaignToStop", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> scheduleAFixedDateCampaignToStop(@RequestBody FixedDateSchedulerDefinitionDTO fixedDateSchedulerDefinitionDTO,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleAFixedDateCampaignToStop =========");
    	System.out.println("[DEEP] payload.campaignId=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getCampaignId() : null));
    	System.out.println("[DEEP] payload.organization=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.date=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getDate() : null));
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("===============================================================");

		Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));
    	System.out.println("[DEEP] employee.domain=" + (employee != null ? employee.getDomain() : null));

    	if(employee.getOrganization().trim().equals(fixedDateSchedulerDefinitionDTO.getOrganization()))
    	{
    		String jobId = TrackedSchduledJobs.fixeddate+"-"+TrackedSchduledJobs.stopCampaignRunnable+fixedDateSchedulerDefinitionDTO.getCampaignId().toString();
        	System.out.println("[DEEP] ORG MATCH OK. jobId=" + jobId);

    		StopCampaignRunnable stopCampaignRunnable = new StopCampaignRunnable();
    		stopCampaignRunnable.setJobId(jobId);
    		stopCampaignRunnable.setCampaignID(fixedDateSchedulerDefinitionDTO.getCampaignId());
    		stopCampaignRunnable.setOrganization(fixedDateSchedulerDefinitionDTO.getOrganization());
    		stopCampaignRunnable.setFromExtension(employee.getExtension());
    		stopCampaignRunnable.setDomain(employee.getDomain());
    		stopCampaignRunnable.setApplicationContext(applicationContext);

    		System.out.println("[DEEP] Scheduling FIXED DATE stop: jobId=" + jobId + " date=" + fixedDateSchedulerDefinitionDTO.getDate());
    		schedulerService.removeIfExistsAndScheduleATaskOnDate(jobId,stopCampaignRunnable, fixedDateSchedulerDefinitionDTO.getDate());
    		System.out.println("[DEEP] schedulerService.removeIfExistsAndScheduleATaskOnDate DONE for jobId=" + jobId);

        	//Schedule to database
        	RunningSchedule runningSchedule = new RunningSchedule();
        	runningSchedule.setJobId(jobId);
        	runningSchedule.setFunctionality(TrackedSchduledJobs.stopCampaignRunnable);
        	runningSchedule.setScheduleType(TrackedSchduledJobs.fixeddate);
        	runningSchedule.setCronExpression(null);
        	runningSchedule.setDate(fixedDateSchedulerDefinitionDTO.getDate());
        	runningSchedule.setCampaignId(fixedDateSchedulerDefinitionDTO.getCampaignId());
        	runningSchedule.setActionType(fixedDateSchedulerDefinitionDTO.getActionType());
        	runningSchedule.setData(fixedDateSchedulerDefinitionDTO.getData());
        	runningSchedule.setOrganization(fixedDateSchedulerDefinitionDTO.getOrganization());
        	runningSchedule.setDomain(fixedDateSchedulerDefinitionDTO.getDomain());
        	runningSchedule.setSeconds(0);
        	runningSchedule.setPhoneNumber(fixedDateSchedulerDefinitionDTO.getPhoneNumber());
        	runningSchedule.setCallType(fixedDateSchedulerDefinitionDTO.getCallType());
        	runningSchedule.setFromExtension(fixedDateSchedulerDefinitionDTO.getFromExtension());
        	runningSchedule.setContext(fixedDateSchedulerDefinitionDTO.getContext());
        	runningSchedule.setPriority(fixedDateSchedulerDefinitionDTO.getPriority());
        	runningSchedule.setTimeOut(fixedDateSchedulerDefinitionDTO.getTimeOut());
        	runningSchedule.setFirstName(fixedDateSchedulerDefinitionDTO.getFirstName());
        	runningSchedule.setProtocol(fixedDateSchedulerDefinitionDTO.getProtocol());
        	runningSchedule.setPhoneTrunk(fixedDateSchedulerDefinitionDTO.getPhoneTrunk());
        	runningSchedule.setFromExtension(employee.getExtension());

        	System.out.println("[DEEP] Saving RunningSchedule: jobId=" + jobId + " type=fixeddate func=stopCampaignRunnable date=" + fixedDateSchedulerDefinitionDTO.getDate());
        	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
        	System.out.println("[DEEP] runningScheduleService.removeIfExistsAndCreateRunningSchedules DONE jobId=" + jobId);

        	toReturn = true;
        	System.out.println("[DEEP] EXIT scheduleAFixedDateCampaignToStop OK");
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + fixedDateSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
   }

    @PostMapping(path="/scheduleAfterNSecCampaignToStop", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> scheduleAfterNSecCampaignToStop(@RequestBody AfterNSecondsSchedulerDefinitionDTO afterNSecondsSchedulerDefinitionDTO,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleAfterNSecCampaignToStop =========");
    	System.out.println("[DEEP] payload.campaignId=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getCampaignId() : null));
    	System.out.println("[DEEP] payload.organization=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.seconds=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getSeconds() : null));
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("===============================================================");

		Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));
    	System.out.println("[DEEP] employee.domain=" + (employee != null ? employee.getDomain() : null));

    	if(employee.getOrganization().trim().equals(afterNSecondsSchedulerDefinitionDTO.getOrganization()))
    	{
    		String jobId = TrackedSchduledJobs.afternseconds+"-"+TrackedSchduledJobs.stopCampaignRunnable+afterNSecondsSchedulerDefinitionDTO.getCampaignId().toString();
    		System.out.println("[DEEP] ORG MATCH OK. jobId=" + jobId);

    		StopCampaignRunnable stopCampaignRunnable = new StopCampaignRunnable();
    		stopCampaignRunnable.setJobId(jobId);
    		stopCampaignRunnable.setCampaignID(afterNSecondsSchedulerDefinitionDTO.getCampaignId());
    		stopCampaignRunnable.setOrganization(afterNSecondsSchedulerDefinitionDTO.getOrganization());
    		stopCampaignRunnable.setFromExtension(employee.getExtension());
    		stopCampaignRunnable.setDomain(employee.getDomain());
    		stopCampaignRunnable.setApplicationContext(applicationContext);

    		System.out.println("[DEEP] Scheduling AFTER N SECONDS stop: jobId=" + jobId + " seconds=" + afterNSecondsSchedulerDefinitionDTO.getSeconds());
        	schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(jobId, stopCampaignRunnable, afterNSecondsSchedulerDefinitionDTO.getSeconds());
        	System.out.println("[DEEP] schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds DONE for jobId=" + jobId);

        	//Schedule to database
        	RunningSchedule runningSchedule = new RunningSchedule();
        	runningSchedule.setJobId(jobId);
        	runningSchedule.setFunctionality(TrackedSchduledJobs.stopCampaignRunnable);
        	runningSchedule.setScheduleType(TrackedSchduledJobs.afternseconds);
        	runningSchedule.setCronExpression(null);
        	runningSchedule.setDate(null);
        	runningSchedule.setCampaignId(afterNSecondsSchedulerDefinitionDTO.getCampaignId());
        	runningSchedule.setActionType(afterNSecondsSchedulerDefinitionDTO.getActionType());
        	runningSchedule.setData(afterNSecondsSchedulerDefinitionDTO.getData());
        	runningSchedule.setOrganization(afterNSecondsSchedulerDefinitionDTO.getOrganization());
        	runningSchedule.setDomain(afterNSecondsSchedulerDefinitionDTO.getDomain());
        	runningSchedule.setSeconds(afterNSecondsSchedulerDefinitionDTO.getSeconds());
        	runningSchedule.setPhoneNumber(afterNSecondsSchedulerDefinitionDTO.getPhoneNumber());
        	runningSchedule.setCallType(afterNSecondsSchedulerDefinitionDTO.getCallType());
        	runningSchedule.setFromExtension(afterNSecondsSchedulerDefinitionDTO.getFromExtension());
        	runningSchedule.setContext(afterNSecondsSchedulerDefinitionDTO.getContext());
        	runningSchedule.setPriority(afterNSecondsSchedulerDefinitionDTO.getPriority());
        	runningSchedule.setTimeOut(afterNSecondsSchedulerDefinitionDTO.getTimeOut());
        	runningSchedule.setFirstName(afterNSecondsSchedulerDefinitionDTO.getFirstName());
        	runningSchedule.setProtocol(afterNSecondsSchedulerDefinitionDTO.getProtocol());
        	runningSchedule.setPhoneTrunk(afterNSecondsSchedulerDefinitionDTO.getPhoneTrunk());
        	runningSchedule.setFromExtension(employee.getExtension());

        	System.out.println("[DEEP] Saving RunningSchedule: jobId=" + jobId + " type=afternseconds func=stopCampaignRunnable seconds=" + afterNSecondsSchedulerDefinitionDTO.getSeconds());
        	runningScheduleService.removeIfExistsAndCreateRunningSchedules(runningSchedule);
        	System.out.println("[DEEP] runningScheduleService.removeIfExistsAndCreateRunningSchedules DONE jobId=" + jobId);

        	toReturn = true;
        	System.out.println("[DEEP] EXIT scheduleAfterNSecCampaignToStop OK");
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + afterNSecondsSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
   }

    @GetMapping(path="/removeScheduledStopedCampgin")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> removeScheduledStopedCampgin(@RequestParam String scheduleType,
    		@RequestParam String campaignId,
    		@RequestParam String organization,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER removeScheduledStopedCampgin =========");
    	System.out.println("[DEEP] scheduleType=" + scheduleType + " campaignId=" + campaignId + " organization=" + organization);
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("===========================================================");

        Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));

    	if(employee.getOrganization().trim().equals(organization))
    	{
    		String jobId = scheduleType+"-"+TrackedSchduledJobs.stopCampaignRunnable+campaignId.toString();
    		System.out.println("[DEEP] Removing scheduled STOP jobId=" + jobId);

    		schedulerService.removeScheduledTask(jobId);
    		System.out.println("[DEEP] schedulerService.removeScheduledTask DONE jobId=" + jobId);

        	runningScheduleService.deleteAllRunningSchedulesByJobId(jobId);
        	System.out.println("[DEEP] runningScheduleService.deleteAllRunningSchedulesByJobId DONE jobId=" + jobId);

    		toReturn = true;
    		System.out.println("[DEEP] EXIT removeScheduledStopedCampgin OK");
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() + " req.org=" + organization);
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
    }

    @GetMapping(path="/findIfScheduledStoppedCampaignJob")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> findIfScheduledStoppedCampaignJob(@RequestParam String scheduleType,
    		@RequestParam String campaignId,
    		@RequestParam String organization,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER findIfScheduledStoppedCampaignJob =========");
    	System.out.println("[DEEP] scheduleType=" + scheduleType + " campaignId=" + campaignId + " organization=" + organization);
    	System.out.println("[DEEP] token.present=" + (token != null));
    	System.out.println("===============================================================");

    	boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));

    	if(employee.getOrganization().trim().equals(organization))
    	{
    		String jobId = scheduleType+"-"+TrackedSchduledJobs.stopCampaignRunnable+campaignId.toString();
    		System.out.println("[DEEP] Checking scheduled STOP jobId=" + jobId);

    		toReturn = schedulerService.findIfScheduledTask(jobId);
    		System.out.println("[DEEP] schedulerService.findIfScheduledTask result=" + toReturn + " jobId=" + jobId);

    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() + " req.org=" + organization);
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
    }

    @PostMapping(path="/scheduleACronCallToCustomer", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
	 public ResponseEntity<Boolean> scheduleACronCallToCustomer(@RequestBody CronSchedulerDefinitionDTO cronSchedulerDefinitionDTO,
			 @RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleACronCallToCustomer =========");
    	System.out.println("[DEEP] payload.organization=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.cron=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getCronExpression() : null));
    	System.out.println("[DEEP] payload.phone=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getPhoneNumber() : null));
    	System.out.println("[DEEP] payload.fromExt=" + (cronSchedulerDefinitionDTO != null ? cronSchedulerDefinitionDTO.getFromExtension() : null));
    	System.out.println("===========================================================");

		Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));

    	if(employee.getOrganization().trim().equals(cronSchedulerDefinitionDTO.getOrganization()))
    	{
    		System.out.println("[DEEP] ORG MATCH OK -> delegating to jobSchedulingService.scheduleACronCallToCustomer");
    		toReturn = jobSchedulingService.scheduleACronCallToCustomer(cronSchedulerDefinitionDTO, employee, applicationContext);
    		System.out.println("[DEEP] jobSchedulingService.scheduleACronCallToCustomer result=" + toReturn);
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + cronSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
   }

    @PostMapping(path="/scheduleAFixedDateCallToCustomer", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> scheduleAFixedDateCallToCustomer(@RequestBody FixedDateSchedulerDefinitionDTO fixedDateSchedulerDefinitionDTO,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleAFixedDateCallToCustomer =========");
    	System.out.println("[DEEP] payload.organization=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.date=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getDate() : null));
    	System.out.println("[DEEP] payload.phone=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getPhoneNumber() : null));
    	System.out.println("[DEEP] payload.fromExt=" + (fixedDateSchedulerDefinitionDTO != null ? fixedDateSchedulerDefinitionDTO.getFromExtension() : null));
    	System.out.println("===============================================================");

    	boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));

    	if(employee.getOrganization().trim().equals(fixedDateSchedulerDefinitionDTO.getOrganization()))
    	{
    		System.out.println("[DEEP] ORG MATCH OK -> delegating to jobSchedulingService.scheduleAFixedDateCallToCustomer");
    		toReturn = jobSchedulingService.scheduleAFixedDateCallToCustomer(fixedDateSchedulerDefinitionDTO, employee, applicationContext);
    		System.out.println("[DEEP] jobSchedulingService.scheduleAFixedDateCallToCustomer result=" + toReturn);
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + fixedDateSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(false);
    	}
   }

    @PostMapping(path="/scheduleAfterNSecCallToCustomer", consumes = "application/json", produces="application/json")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> scheduleAfterNSecCallToCustomer(@RequestBody AfterNSecondsSchedulerDefinitionDTO afterNSecondsSchedulerDefinitionDTO,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER scheduleAfterNSecCallToCustomer =========");
    	System.out.println("[DEEP] payload.organization=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getOrganization() : null));
    	System.out.println("[DEEP] payload.seconds=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getSeconds() : null));
    	System.out.println("[DEEP] payload.phone=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getPhoneNumber() : null));
    	System.out.println("[DEEP] payload.fromExt=" + (afterNSecondsSchedulerDefinitionDTO != null ? afterNSecondsSchedulerDefinitionDTO.getFromExtension() : null));
    	System.out.println("===============================================================");

		Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));
    	System.out.println("[DEEP] employee.ext=" + (employee != null ? employee.getExtension() : null));

    	if(employee.getOrganization().trim().equals(afterNSecondsSchedulerDefinitionDTO.getOrganization()))
    	{
    		System.out.println("[DEEP] ORG MATCH OK -> delegating to jobSchedulingService.scheduleAfterNSecCallToCustomer");
    		toReturn = jobSchedulingService.scheduleAfterNSecCallToCustomer(afterNSecondsSchedulerDefinitionDTO, employee, applicationContext);
        	System.out.println("[DEEP] jobSchedulingService.scheduleAfterNSecCallToCustomer result=" + toReturn);
        	System.out.println("[DEEP] Call is scheduled after : "+afterNSecondsSchedulerDefinitionDTO.getSeconds()+ " seconds");
        	return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() +
    				" dto.org=" + afterNSecondsSchedulerDefinitionDTO.getOrganization());
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
   }

    @GetMapping(path="/removeScheduledCallToCustomer")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> removeScheduledCallToCustomer(@RequestParam String scheduleType,
    		@RequestParam String phoneNumber,
    		@RequestParam String fromExtension,
    		@RequestParam String organization,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER removeScheduledCallToCustomer =========");
    	System.out.println("[DEEP] scheduleType=" + scheduleType + " phoneNumber=" + phoneNumber + " fromExtension=" + fromExtension + " organization=" + organization);
    	System.out.println("=============================================================");

        Boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));

    	if(employee.getOrganization().trim().equals(organization))
    	{
    		System.out.println("[DEEP] ORG MATCH OK -> delegating to jobSchedulingService.removeScheduledCallToCustomer");
    		toReturn = jobSchedulingService.removeScheduledCallToCustomer(scheduleType, phoneNumber, fromExtension, organization);
    		System.out.println("[DEEP] jobSchedulingService.removeScheduledCallToCustomer result=" + toReturn);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() + " req.org=" + organization);
    		return status(HttpStatus.UNAUTHORIZED).body(toReturn);
    	}
    }

    @GetMapping(path="/findIfScheduledCallJobToCustomer")
    @PreAuthorize("hasAuthority('EMPLOYEE') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<Boolean> findIfScheduledCallJobToCustomer(@RequestParam String scheduleType,
    		@RequestParam String phoneNumber,
    		@RequestParam String fromExtension,
    		@RequestParam String organization,
    		@RequestHeader (name="Authorization") String token) {

    	System.out.println("========= [DEEP] ENTER findIfScheduledCallJobToCustomer =========");
    	System.out.println("[DEEP] scheduleType=" + scheduleType + " phoneNumber=" + phoneNumber + " fromExtension=" + fromExtension + " organization=" + organization);
    	System.out.println("===============================================================");

    	boolean toReturn = false;

        token = token.replace(jwtConfiguration.getTokenPrefix(), "");

    	Employee employee= new JwtVerify().verifyTokenOrThrowError(token, secretKey, employeeRepository);

    	System.out.println("[DEEP] employee.org=" + (employee != null ? employee.getOrganization() : null));

    	if(employee.getOrganization().trim().equals(organization))
    	{
    		System.out.println("[DEEP] ORG MATCH OK -> delegating to jobSchedulingService.findIfScheduledCallJobToCustomer");
    		toReturn = jobSchedulingService.findIfScheduledCallJobToCustomer(scheduleType, phoneNumber, fromExtension, organization);
    		System.out.println("[DEEP] jobSchedulingService.findIfScheduledCallJobToCustomer result=" + toReturn);
    		return status(HttpStatus.OK).body(toReturn);
    	}
    	else
    	{
    		System.out.println("[DEEP] ORG MISMATCH -> UNAUTHORIZED. employee.org=" + employee.getOrganization() + " req.org=" + organization);
    		return status(HttpStatus.UNAUTHORIZED).body(false);
    	}
    }
}
