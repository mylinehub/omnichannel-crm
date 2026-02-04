package com.mylinehub.crm.ami.autodialer;

import com.mylinehub.crm.ami.ConnectionStream;
import com.mylinehub.crm.ami.ManagerStream;
import com.mylinehub.crm.ami.TaskScheduler.DialAutomateCallCronRunnable;
import com.mylinehub.crm.ami.TaskScheduler.NoDatabaseCallRunnable;
import com.mylinehub.crm.ami.service.ScheduleDialAutomateCallService;
import com.mylinehub.crm.ami.service.notificaton.EmployeeCallErrorNotificationService;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.TrackedSchduledJobs;
import com.mylinehub.crm.data.dto.CampaignEmployeeDataDTO;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.data.dto.EmployeeAndItsCampaignDTO;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.response.ManagerResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.Customers;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.enums.AUTODIALER_TYPE;
import com.mylinehub.crm.enums.Cdr_Event;
import com.mylinehub.crm.enums.DEVICE_STATES;
import com.mylinehub.crm.enums.DEVICE_STATE_CHANGE_EVENT;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.service.SchedulerService;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.service.WhatsAppIntegrationOutboundService;

import lombok.AllArgsConstructor;
import com.mylinehub.crm.whatsapp.enums.MESSAGE_TYPE;
import com.mylinehub.crm.whatsapp.enums.SEND_MESSAGE_KEYS;
import com.mylinehub.crm.whatsapp.enums.chat.CHAT_ORIGIN;

/**
 * AutodialerReinitiateAndFunctionService
 *
 * Goals of refactor:
 * 1) Remove repeated autodialer-type if/else ladders (single router)
 * 2) Keep Campaign.autodialertype as String (NO DB/entity change)
 * 3) Combine schedule removal methods (ALL / REMINDER / CRON)
 * 4) Deduplicate device-state -> UI state mapping logic
 *
 * Business behavior preserved:
 * - Only PRIDICTIVE_DIALER executes actual dialer logic (same as current file)
 * - Other dialer types remain no-op and return true (as earlier empty blocks)
 * - Unknown autodialerType returns false in most actions (same spirit as earlier "else toReturn=false")
 * - findIfWeRequireEmployeeForAutodialer keeps default=true for unknown (same as earlier)
 *
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class AutodialerReinitiateAndFunctionService {

    // ============================================================
    // DEEP LOGS (System.out)
    // ============================================================
    private static final boolean DEEP_LOGS = true;

    private static void SYS(String msg) {
        if (DEEP_LOGS) {
            System.out.println("[AutodialerReinitiateAndFunctionService] " + msg);
        }
    }

    private static void SYS(String key, Object val) {
        if (DEEP_LOGS) {
            System.out.println("[AutodialerReinitiateAndFunctionService] " + key + "=" + String.valueOf(val));
        }
    }

    private final ApplicationContext applicationContext;
    private final ScheduleDialAutomateCallService scheduleDialAutomateCallService;
    private final NotificationRepository notificationRepository;
    private final EmployeeCallErrorNotificationService employeeCallErrorNotificationService;
    private final SchedulerService schedulerService;
    private final WhatsAppIntegrationOutboundService whatsAppIntegrationOutboundService;

    // =========================================================
    // Router Types (single place to branch by autodialerType)
    // =========================================================

    /**
     * What the caller wants to do. The router uses this + autodialerType to decide the action.
     */
    private enum DialerAction {
        START_CALL_FLOW,
        DIAL_FROM_CRON,
        EXECUTE_AUTODIALER_CALL,
        REMOVE_SCHEDULES,
        REQUIRES_EMPLOYEE
    }

    /**
     * Schedule removal mode.
     * ALL         -> remove reminder + cron
     * REMINDER_ONLY -> remove reminder
     * CRON_ONLY   -> remove cron
     */
    public enum RemoveMode {
        ALL,
        REMINDER_ONLY,
        CRON_ONLY
    }

    /**
     * Request context passed into router.
     * Only some fields are used depending on DialerAction.
     */
    private static final class DialerRouteContext {
        Campaign campaign;
        Employee employee;
        String customerPhone;
        boolean duringStartCampaign;
        SchedulerService schedulerService;
        RemoveMode removeMode;
        ExecuteCallArgs executeArgs;
    }

    /**
     * Arguments for executeAutodialerCall() routed execution.
     * We keep existing external signature but internally use this holder.
     */
    private static final class ExecuteCallArgs {
        String jobId;
        String phoneNumber;
        String fromExtension;
        String fromPhoneNumber;
        boolean isCallOnMobile;
        String callType;
        String organization;
        String domain;
        String context;
        int priority;
        Long timeOut;
        String firstName;
        String protocol;
        String phoneTrunk;
        boolean useSecondaryLine;
        String secondDomain;
        int breathingSeconds; // kept for compatibility even if not used in predictive
        String autodialerType; // String; parsed inside router
        String stasisName;
        boolean callNow;
    }

    /**
     * Convert campaign autodialer type string -> enum constant.
     * IMPORTANT: Campaign stores String; we only parse at runtime.
     */
    private AUTODIALER_TYPE safeType(String raw) {
        SYS("ENTER safeType()");
        SYS("raw", raw);

        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return null;

        try {
            AUTODIALER_TYPE t = AUTODIALER_TYPE.valueOf(v);
            SYS("safeType.resolved", t);
            return t;
        } catch (Exception ex) {
            // Unknown / invalid type
            if (DEEP_LOGS) LoggerUtils.log.warn("safeType: Unknown autodialerType={}", raw);
            SYS("safeType.unknown", raw);
            return null;
        }
    }

    /**
     * The single router method:
     * - Does ALL autodialerType branching in one place
     * - Calls predictive logic only when type is PRIDICTIVE_DIALER
     * - Other known types are treated as NO-OP (same as earlier empty blocks)
     */
    private boolean routeByAutodialerType(String autodialerType, DialerAction action, DialerRouteContext ctx) throws Exception {
        SYS("ENTER routeByAutodialerType()");
        SYS("autodialerType", autodialerType);
        SYS("action", (action == null ? null : action.name()));
        SYS("ctx.campaignId", (ctx != null && ctx.campaign != null ? ctx.campaign.getId() : null));
        SYS("ctx.empExt", (ctx != null && ctx.employee != null ? ctx.employee.getExtension() : null));
        SYS("ctx.customerPhone", (ctx != null ? ctx.customerPhone : null));
        SYS("ctx.removeMode", (ctx != null && ctx.removeMode != null ? ctx.removeMode.name() : null));

        AUTODIALER_TYPE type = safeType(autodialerType);

        // For "requires employee", old behavior defaulted to true for unknown strings.
        if (action == DialerAction.REQUIRES_EMPLOYEE) {
            boolean r = requiresEmployeeForType(type, autodialerType);
            SYS("EXIT routeByAutodialerType REQUIRES_EMPLOYEE=" + r);
            return r;
        }

        // For other actions, unknown should behave like old "else toReturn=false"
        if (type == null) {
            SYS("routeByAutodialerType: type is null -> return false");
            return false;
        }

        switch (type) {
            case PRIDICTIVE_DIALER:
                SYS("route -> handlePredictive");
                return handlePredictive(action, ctx);

            case AI_CALL:
                SYS("route AI Call-> handleStasisCall");
                return handleStasisCall(action, ctx);
                
            case IVR_DIALER:
            	SYS("route IVR DIALER-> handleStasisCall");
                return handleStasisCall(action, ctx);
                
            // Known types exist but currently not implemented (same as earlier empty if blocks).
            // Keep them as NO-OP and return true.
            case PROGRESSIVE_DIALER:
            case PREVIEW_DIALER:
            case QUEUE_DIALER:
            case YOUTUBE_DIALER:
            case CONFERENCE_DIALING:
            case RATIO_DIALING:
            case HARD_STICKY_DIALING:
            case SOFT_STICKY_DIALING:
            case WHATSAPP_MESSAGE:
                SYS("route -> NO-OP true for type=" + type);
                return true;

            default:
                SYS("route -> default false for type=" + type);
                return false;
        }
    }

    /**
     * Preserve old "default=true" behavior for unknown autodialer type in requires-employee function.
     * Also matches your existing switch mapping.
     */
    private boolean requiresEmployeeForType(AUTODIALER_TYPE type, String raw) {
        SYS("ENTER requiresEmployeeForType()");
        SYS("type", (type == null ? null : type.name()));
        SYS("raw", raw);

        if (type == null) {
            // old default: return true
            SYS("requiresEmployeeForType: type null -> true");
            return true;
        }

        final EnumSet<AUTODIALER_TYPE> REQUIRES_EMP = EnumSet.of(
                AUTODIALER_TYPE.PRIDICTIVE_DIALER,
                AUTODIALER_TYPE.PROGRESSIVE_DIALER,
                AUTODIALER_TYPE.RATIO_DIALING,
                AUTODIALER_TYPE.PREVIEW_DIALER,
                AUTODIALER_TYPE.HARD_STICKY_DIALING,
                AUTODIALER_TYPE.SOFT_STICKY_DIALING
        );

        boolean r = REQUIRES_EMP.contains(type);
        SYS("EXIT requiresEmployeeForType", r);
        return r;
    }

    /**
     * Predictive dialer action handler.
     * Contains all predictive-specific logic that was earlier spread across multiple methods.
     */
    private boolean handlePredictive(DialerAction action, DialerRouteContext ctx) throws Exception {
        SYS("ENTER handlePredictive()");
        SYS("action", (action == null ? null : action.name()));
        SYS("ctx.campaignId", (ctx != null && ctx.campaign != null ? ctx.campaign.getId() : null));
        SYS("ctx.empExt", (ctx != null && ctx.employee != null ? ctx.employee.getExtension() : null));
        SYS("ctx.duringStartCampaign", (ctx != null ? ctx.duringStartCampaign : null));

        switch (action) {

            case START_CALL_FLOW:
                SYS("handlePredictive START_CALL_FLOW -> initiateDialerForEmployee");
                return new LoopInToDialOrSendMessage().initiateDialerForEmployee(
                        ctx.employee,
                        ctx.campaign,
                        applicationContext,
                        ctx.duringStartCampaign,
                        true
                );

            case DIAL_FROM_CRON: {
                SYS("handlePredictive DIAL_FROM_CRON");
                String jobId1 = TrackedSchduledJobs.dialAutomateCall + ctx.employee.getExtension().toString();
                SYS("jobId1", jobId1);

                if (ctx.schedulerService == null) {
                    SYS("ctx.schedulerService is null -> return true");
                    return true;
                }

                if (!ctx.schedulerService.findIfScheduledTask(jobId1)) {
                    if (DEEP_LOGS) LoggerUtils.log.debug("No schedule job found for employee");
                    SYS("No schedule job found for employee -> verifyIfEmployeeIsAvailableForCallAsPerExtension");

                    boolean canDial = verifyIfEmployeeIsAvailableForCallAsPerExtension(ctx.employee);
                    SYS("canDial", canDial);

                    if (canDial) {
                        if (DEEP_LOGS) LoggerUtils.log.debug("Initialting Dialer");
                        SYS("Initialting Dialer -> initiateDialerForEmployee");
                        new LoopInToDialOrSendMessage().initiateDialerForEmployee(
                                ctx.employee,
                                ctx.campaign,
                                applicationContext,
                                false,
                                true
                        );
                    }
                } else {
                    if (DEEP_LOGS) LoggerUtils.log.debug("Dial schedule job found for employee");
                    SYS("Dial schedule job found for employee");
                }
                return true;
            }

            case EXECUTE_AUTODIALER_CALL: {
                SYS("handlePredictive EXECUTE_AUTODIALER_CALL");
                ExecuteCallArgs a = ctx.executeArgs;
                if (a == null) {
                    SYS("executeArgs is null -> return false");
                    return false;
                }

                NoDatabaseCallRunnable runnable = buildNoDatabaseRunnable(a, true, null);
                return runNowOrSchedule(a, runnable);
            }

            case REMOVE_SCHEDULES: {
                SYS("handlePredictive REMOVE_SCHEDULES");
                if (ctx.employee == null || ctx.employee.getExtension() == null) {
                    SYS("employee or extension null -> return true");
                    return true;
                }

                String reminderJobId = buildReminderJobId(ctx.employee);
                String cronJobId = buildCronJobId(ctx.employee);
                SYS("reminderJobId", reminderJobId);
                SYS("cronJobId", cronJobId);

                RemoveMode mode = (ctx.removeMode != null) ? ctx.removeMode : RemoveMode.ALL;
                SYS("removeMode", mode.name());

                if (mode == RemoveMode.ALL || mode == RemoveMode.REMINDER_ONLY) {
                    SYS("removeScheduleDialAutomateCall(reminder)");
                    scheduleDialAutomateCallService.removeScheduleDialAutomateCall(reminderJobId);
                }
                if (mode == RemoveMode.ALL || mode == RemoveMode.CRON_ONLY) {
                    SYS("removeScheduleDialAutomateCall(cron)");
                    scheduleDialAutomateCallService.removeScheduleDialAutomateCall(cronJobId);
                }
                return true;
            }

            default:
                SYS("handlePredictive default -> true");
                return true;
        }
    }

    private boolean handleStasisCall(DialerAction action, DialerRouteContext ctx) throws Exception {
        SYS("ENTER handleStasisCall()");
        SYS("action", (action == null ? null : action.name()));
        SYS("ctx.campaignId", (ctx != null && ctx.campaign != null ? ctx.campaign.getId() : null));
        SYS("ctx.customerPhone", (ctx != null ? ctx.customerPhone : null));
        SYS("ctx.empExt", (ctx != null && ctx.employee != null ? ctx.employee.getExtension() : null));

        switch (action) {

            case START_CALL_FLOW:
                SYS("handleAiCall START_CALL_FLOW -> initiateAutomationOnlyCustomer");
                // AI_CALL should run WITHOUT customer dial loop
                return new LoopInToDialOrSendMessage().initiateAutomationOnlyCustomer(
                        ctx.campaign,
                        applicationContext,
                        ctx.duringStartCampaign,
                        true
                );

            case DIAL_FROM_CRON:
                // do nothing, just log
                LoggerUtils.log.debug("AI_CALL DIAL_FROM_CRON: no-op. campaignId={} empExt={}",
                        (ctx.campaign != null ? ctx.campaign.getId() : null),
                        (ctx.employee != null ? ctx.employee.getExtension() : null)
                );
                SYS("AI_CALL DIAL_FROM_CRON no-op");
                return true;

            case EXECUTE_AUTODIALER_CALL: {
                SYS("handleAiCall EXECUTE_AUTODIALER_CALL");
                ExecuteCallArgs a = ctx.executeArgs;
                if (a == null) {
                    SYS("executeArgs is null -> return false");
                    return false;
                }

                // reuse same builder; only difference: predictive=false and stasisName used
                NoDatabaseCallRunnable runnable = buildNoDatabaseRunnable(a, true, a.stasisName);
                return runNowOrSchedule(a, runnable);
            }

            case REMOVE_SCHEDULES:
                SYS("handleAiCall REMOVE_SCHEDULES");
                if (ctx.campaign == null || ctx.customerPhone == null) {
                    SYS("campaign or customerPhone null -> return true");
                    return true;
                }

                RemoveMode mode = (ctx.removeMode != null) ? ctx.removeMode : RemoveMode.ALL;
                SYS("removeMode", mode.name());

                if (mode == RemoveMode.ALL || mode == RemoveMode.REMINDER_ONLY) {
                    // Noreminder for CRON Job
                    SYS("AI_CALL reminder removal no-op");
                }
                if (mode == RemoveMode.ALL || mode == RemoveMode.CRON_ONLY) {
                    SYS("removeOnlyCustomerReinitiateJob START");
                    removeOnlyCustomerReinitiateJob(ctx.campaign, ctx.customerPhone);
                    SYS("removeOnlyCustomerReinitiateJob DONE");
                }

                return true;

            default:
                SYS("handleAiCall default -> true");
                return true;
        }
    }

    private NoDatabaseCallRunnable buildNoDatabaseRunnable(ExecuteCallArgs a, boolean isPredictive, String stasisAppName) throws Exception {
        SYS("ENTER buildNoDatabaseRunnable()");
        SYS("jobId", (a == null ? null : a.jobId));
        SYS("phoneNumber", (a == null ? null : a.phoneNumber));
        SYS("fromExtension", (a == null ? null : a.fromExtension));
        SYS("organization", (a == null ? null : a.organization));
        SYS("domain", (a == null ? null : a.domain));
        SYS("context", (a == null ? null : a.context));
        SYS("protocol", (a == null ? null : a.protocol));
        SYS("phoneTrunk", (a == null ? null : a.phoneTrunk));
        SYS("isPredictive", isPredictive);
        SYS("stasisAppName", stasisAppName);

        Organization organization = null;
        Map<String, Organization> organizationMap =
                OrganizationData.workWithAllOrganizationData(a.organization, null, "get-one", null);
        SYS("organizationMap null?", (organizationMap == null));
        if (organizationMap != null) {
            organization = organizationMap.get(a.organization);
        } else {
           throw new Exception("Organization now found for emp");
        }
        SYS("resolvedOrganization null?", (organization == null));
        if (organization != null) {
            SYS("resolvedOrganization.simSelector", organization.getSimSelector());
            SYS("resolvedOrganization.simSelectorRequired", organization.isSimSelectorRequired());
        }

        NoDatabaseCallRunnable runnable = new NoDatabaseCallRunnable();

        runnable.setJobId(a.jobId);
        runnable.setPhoneNumber(a.phoneNumber);
        runnable.setFromExtension(a.fromExtension);

        runnable.setCallType(a.callType != null ? a.callType : "Outbound");
        runnable.setOrganization(a.organization);
        runnable.setDomain(a.domain);
        runnable.setContext(a.context);
        runnable.setProtocol(a.protocol);
        runnable.setPhoneTrunk(a.phoneTrunk);

        // take from args (no magic defaults except safety)
        runnable.setPriority(a.priority > 0 ? a.priority : 1);
        runnable.setTimeOut(a.timeOut != null ? a.timeOut : 30000L);

        runnable.setFirstName(a.firstName);
        runnable.setApplicationContext(applicationContext);
        runnable.setNotificationRepository(notificationRepository);

        runnable.setUseSecondaryLine(a.useSecondaryLine);
        runnable.setCallOnMobile(a.isCallOnMobile);

        runnable.setFromPhoneNumber(a.fromPhoneNumber != null ? a.fromPhoneNumber : a.phoneNumber);
        runnable.setSecondDomain(a.secondDomain);

        // only two real differences between Predictive vs AI
        runnable.setPridictive(isPredictive);
        runnable.setStasisAppName(stasisAppName);
        runnable.setSimSelector(organization.getSimSelector());
        runnable.setSimSelectorRequired(organization.isSimSelectorRequired());

        SYS("EXIT buildNoDatabaseRunnable()");
        return runnable;
    }

    private boolean runNowOrSchedule(ExecuteCallArgs a, Runnable runnable) {
        SYS("ENTER runNowOrSchedule()");
        SYS("jobId", (a == null ? null : a.jobId));
        SYS("callNow", (a == null ? null : a.callNow));
        SYS("breathingSecond", (a == null ? null : a.breathingSeconds));
        
        if (a.callNow) {
            SYS("callNow=true -> runnable.run()");
            runnable.run();
            SYS("EXIT runNowOrSchedule true");
            return true;
        }
        LoggerUtils.log.debug("after setting runnable jobId={}", a.jobId);
        SYS("schedule removeIfExistsAndScheduleATaskAfterXSeconds");
        schedulerService.removeIfExistsAndScheduleATaskAfterXSeconds(a.jobId, runnable, a.breathingSeconds);
        SYS("EXIT runNowOrSchedule true");
        return true;
    }

    public boolean removeOnlyCustomerReinitiateJob(Campaign campaign, String customerPhone) {
        SYS("ENTER removeOnlyCustomerReinitiateJob()");
        SYS("campaignId", (campaign == null ? null : campaign.getId()));
        SYS("customerPhone", customerPhone);
        boolean toReturn = false;
        try {
            String jobId = buildReinitiateOnlyCustomerJobIdFromPhone(customerPhone);
            SYS("jobId", jobId);

            if (jobId == null) return true;
            if (schedulerService == null) return false;

            toReturn = schedulerService.findIfScheduledTask(jobId);
            schedulerService.removeScheduledTask(jobId);
            SYS("EXIT removeOnlyCustomerReinitiateJob true");
            return toReturn;

        } catch (Exception e) {
            SYS("EXCEPTION removeOnlyCustomerReinitiateJob: " + e.getMessage());
            e.printStackTrace();
            return toReturn;
        }
    }


    private String buildReminderJobId(Employee emp) {
        String v = TrackedSchduledJobs.dialAutomateCallReminder + emp.getExtension().toString();
        SYS("buildReminderJobId", v);
        return v;
    }

    private String buildCronJobId(Employee emp) {
        String v = TrackedSchduledJobs.dialAutomateCallCron + emp.getExtension().toString();
        SYS("buildCronJobId", v);
        return v;
    }

    // =========================================================
    // Manager Event parsing
    // =========================================================

    public Map<String, String> eventToMap(ManagerEvent e) {

        SYS("ENTER eventToMap()");
        SYS("event null?", (e == null));

        Map<String, String> toReturn = new HashMap<String, String>();

        if (e == null) {
            return toReturn;
        }

        String eventType = e.getClass().getSimpleName(); // stable, does not depend on toString()
        toReturn.put("eventType", eventType);

        String raw = String.valueOf(e);
        if (DEEP_LOGS) {
            LoggerUtils.log.debug("eventToMap: eventType=" + eventType + " raw=" + raw);
        }

        SYS("eventType", eventType);

        try {
            int lb = raw.indexOf('[');
            int rb = raw.lastIndexOf(']'); // use LAST ']' to avoid cut issues

            SYS("lb", lb);
            SYS("rb", rb);

            if (lb < 0 || rb < 0 || rb <= lb) {
                // can’t parse key-values safely, return only eventType
                if (DEEP_LOGS) LoggerUtils.log.debug("eventToMap: no bracket body. raw=" + raw);
                SYS("eventToMap: no bracket body");
                return toReturn;
            }

            String body = raw.substring(lb + 1, rb).trim();
            SYS("body.len", (body == null ? null : body.length()));

            // split by commas safely (ignores commas inside single quotes)
            List<String> parts = splitOutsideQuotes(body, ',');
            SYS("parts.size", (parts == null ? null : parts.size()));

            String lastKey = "";

            for (String element : parts) {
                if (element == null) continue;
                element = element.trim();
                if (element.isEmpty()) continue;

                if (element.contains("=")) {
                    // split only on FIRST '='
                    String[] pair = element.split("=", 2);
                    if (pair.length < 2) continue;

                    String key = pair[0] != null ? pair[0].trim() : "";
                    String val = pair[1] != null ? pair[1].trim() : "";

                    if (!key.isEmpty()) {
                        val = stripWrappingSingleQuotes(val);
                        toReturn.put(key, val);
                        lastKey = key;
                    }
                } else {
                    // continuation (legacy behavior)
                    if (!lastKey.isEmpty()) {
                        String interimValue = toReturn.get(lastKey);
                        if (interimValue == null) interimValue = "";
                        interimValue = interimValue + "," + element;
                        toReturn.put(lastKey, stripWrappingSingleQuotes(interimValue.trim()));
                    }
                }
            }

        } catch (Exception ex) {
            LoggerUtils.log.debug("eventToMap exception: " + ex.getMessage());
            SYS("eventToMap exception", ex.getMessage());
            ex.printStackTrace();
        }

        SYS("EXIT eventToMap() size=" + toReturn.size());
        return toReturn;
    }

    private static String stripWrappingSingleQuotes(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.length() >= 2 && v.charAt(0) == '\'' && v.charAt(v.length() - 1) == '\'') {
            return v.substring(1, v.length() - 1).trim();
        }
        return v;
    }

    // Split string by delimiter, but ignore delimiters inside single quotes.
    private static List<String> splitOutsideQuotes(String input, char delimiter) {
        List<String> out = new ArrayList<String>();
        if (input == null || input.isEmpty()) return out;

        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'') {
                inQuote = !inQuote;
                cur.append(c);
                continue;
            }

            if (c == delimiter && !inQuote) {
                out.add(cur.toString());
                cur.setLength(0);
                continue;
            }

            cur.append(c);
        }

        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    // =========================================================
    // CDR / Device state filtering
    // =========================================================

    public boolean verifyIfCDRIsAllowed(Map<String, String> mapEvent, String organization) {

        SYS("ENTER verifyIfCDRIsAllowed()");
        SYS("org", organization);
        SYS("mapEvent null?", (mapEvent == null));

        boolean allowed = true;

        try {
            String lastApp = (mapEvent == null) ? null : mapEvent.get(Cdr_Event.lastapplication.name().trim());
            if (DEEP_LOGS) LoggerUtils.log.debug("Last Application : " + lastApp);
            SYS("lastapplication", lastApp);

            if (lastApp == null) return true;

            if (lastApp.equals("Queue")) {
            } else if (lastApp.contains("ConfBridge")) {
            } else if (lastApp.contains("BackGround")) {
            } else if (lastApp.contains("Read")) {
            } else if (lastApp.contains("VoiceMail")) {
            } else if (lastApp.contains("Dial")) {
                String value = mapEvent.get(Cdr_Event.channel.name().trim());
                SYS("cdr.channel", value);
                if (value != null && value.contains("queue")) {
                    allowed = false;
                }
            } else {
                // allowed remains true by design
            }

        } catch (Exception e) {
            LoggerUtils.log.debug("verifyIfCDRIsAllowed exception: " + e.getMessage());
            SYS("verifyIfCDRIsAllowed exception", e.getMessage());
            e.printStackTrace();
            allowed = true; // fail-open
        }

        SYS("EXIT verifyIfCDRIsAllowed", allowed);
        return allowed;
    }

    public boolean verifyIfDeviceStateChangeIsAllowed(String device) {
        SYS("ENTER verifyIfDeviceStateChangeIsAllowed()");
        SYS("device", device);

        if (device == null) {
            SYS("EXIT verifyIfDeviceStateChangeIsAllowed false (device null)");
            return false;
        }

        String d = device.trim();

        // Block these first (highest priority)
        if (d.contains("confbridge") || d.contains("ConfBridge")) {
            SYS("blocked confbridge");
            return false;
        }
        if (d.contains("CBAnn")) {
            SYS("blocked CBAnn");
            return false;
        }
        if (d.contains("queue") || d.contains("Queue")) {
            SYS("blocked queue");
            return false;
        }

        // Allow only PJSIP after exclusions
        if (d.contains("PJSIP")) {
            SYS("allowed PJSIP");
            return true;
        }

        SYS("EXIT verifyIfDeviceStateChangeIsAllowed false (not PJSIP)");
        return false;
    }

    // =========================================================
    // Device state -> UI mapping (deduplicated)
    // =========================================================

    private static final class PresenceState {
        final String sipState;
        final String presence;
        final String dotClass;

        PresenceState(String sipState, String presence, String dotClass) {
            this.sipState = sipState;
            this.presence = presence;
            this.dotClass = dotClass;
        }

        List<String> toList() {
            ArrayList<String> list = new ArrayList<String>(3);
            list.add(sipState);
            list.add(presence);
            list.add(dotClass);
            return list;
        }
    }

    private PresenceState mapPresenceState(String initialState) {
        SYS("ENTER mapPresenceState()");
        SYS("initialState", initialState);

        String state = "terminated";
        String presence = "danger";
        String dotClass = "dotOffline";

        if (initialState != null && initialState.contains(DEVICE_STATES.NOT_INUSE.name())) {
            state = "terminated";
            presence = "success";
            dotClass = "dotOnline";
        } else if (initialState != null && initialState.contains(DEVICE_STATES.RINGING.name())) {
            state = "trying";
            presence = "success";
            dotClass = "dotInUse";
        } else if (initialState != null && initialState.contains(DEVICE_STATES.INUSE.name())) {
            state = "confirmed";
            presence = "success";
            dotClass = "dotInUse";
        } else if (initialState != null && initialState.contains(DEVICE_STATES.RINGINUSE.name())) {
            state = "early";
            presence = "success";
            dotClass = "dotInUse";
        } else if (initialState != null && initialState.contains(DEVICE_STATES.ONHOLD.name())) {
            state = "on-hold";
            presence = "success";
            dotClass = "dotOnHold";
        } else if (initialState != null && initialState.contains(DEVICE_STATES.UNAVAILABLE.name())) {
            state = "terminated";
            presence = "danger";
            dotClass = "dotOffline";
        } else if (initialState != null && initialState.contains(DEVICE_STATES.UNKNOWN.name())) {
            state = "terminated";
            presence = "danger";
            dotClass = "dotOffline";
        } else if (initialState != null && initialState.contains(DEVICE_STATES.INVALID.name())) {
            state = "terminated";
            presence = "danger";
            dotClass = "dotOffline";
        } else if (initialState != null && initialState.contains(DEVICE_STATES.BUSY.name())) {
            state = "proceeding";
            presence = "success";
            dotClass = "dotInUse";
        } else {
            state = "terminated";
            presence = "danger";
            dotClass = "dotOffline";
        }

        PresenceState ps = new PresenceState(state, presence, dotClass);
        SYS("EXIT mapPresenceState sipState=" + ps.sipState + " presence=" + ps.presence + " dotClass=" + ps.dotClass);
        return ps;
    }

    // =========================================================
    // Employee state changes + call-flow trigger
    // =========================================================

    public boolean changeStateAndTriggerAutodialer(String initialState, String extension) throws Exception {

        SYS("ENTER changeStateAndTriggerAutodialer()");
        SYS("initialState", initialState);
        SYS("extension", extension);

        if (DEEP_LOGS) LoggerUtils.log.debug("changeStateAndTriggerAutodialer");

        boolean toReturn = true;

        try {
            boolean setAtEnd = true;

            Employee currentEmployee = null;
            Campaign campaign = null;

            Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");

            EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
            if (allEmployeeDataAndState != null) {
                employeeDataAndStateDTO = allEmployeeDataAndState.get(extension);
            }

            if (employeeDataAndStateDTO != null) {
                currentEmployee = employeeDataAndStateDTO.getEmployee();
            }

            SYS("employeeDataAndStateDTO null?", (employeeDataAndStateDTO == null));
            SYS("currentEmployee null?", (currentEmployee == null));

            if (currentEmployee != null && employeeDataAndStateDTO != null) {

                campaign = findRunningOrElseSetCampaignForEmployee(currentEmployee);

                LoggerUtils.log.debug("Device initial state : " + initialState);
                SYS("campaign null?", (campaign == null));

                if (initialState != null && initialState.contains(DEVICE_STATES.NOT_INUSE.name())) {

                    LoggerUtils.log.debug("Entered NotInUse");
                    SYS("Entered NOT_INUSE -> update memberState + trigger call flow");
                    setAtEnd = false;

                    PresenceState ps = mapPresenceState(initialState);

                    employeeDataAndStateDTO.setMemberState(ps.toList());
                    LoggerUtils.log.debug("synching values now");
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");

                    LoggerUtils.log.debug("Checking if we can start call flow");

                    if (campaign != null && currentEmployee != null) {
                        SYS("startCallFlowForEmployee() about to call");
                        startCallFlowForEmployee(currentEmployee, campaign, false);
                    }

                } else {
                    PresenceState ps = mapPresenceState(initialState);

                    if (setAtEnd) {
                        employeeDataAndStateDTO.setMemberState(ps.toList());
                        EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
                    }
                }
            }

        } catch (Exception e) {
            LoggerUtils.log.debug("I am in changeStateAndTriggerAutodialer Exception");
            SYS("EXCEPTION changeStateAndTriggerAutodialer: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
            throw e;
        }

        SYS("EXIT changeStateAndTriggerAutodialer", toReturn);
        return toReturn;
    }

    public boolean setExtensionState(String initialState, String extension) {

        SYS("ENTER setExtensionState()");
        SYS("initialState", initialState);
        SYS("extension", extension);

        boolean toReturn = true;

        try {

            Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");

            EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
            if (allEmployeeDataAndState != null) {
                employeeDataAndStateDTO = allEmployeeDataAndState.get(extension);
            }

            SYS("employeeDataAndStateDTO null?", (employeeDataAndStateDTO == null));

            if (employeeDataAndStateDTO != null) {

                PresenceState ps = mapPresenceState(initialState);

                employeeDataAndStateDTO.setExtensionState(ps.toList());
                EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, employeeDataAndStateDTO, "update");
            }

        } catch (Exception e) {
            SYS("EXCEPTION setExtensionState: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
            throw e;
        }

        SYS("EXIT setExtensionState", toReturn);
        return toReturn;
    }

    public List<String> extractNumberFromDerivedString(String value) {
        SYS("ENTER extractNumberFromDerivedString()");
        SYS("value", value);

        ArrayList<String> toReturn = new ArrayList<String>();
        String isCallOnMobile = "false";

        if (value == null) {
            toReturn.add("");
            toReturn.add(isCallOnMobile);
            SYS("EXIT extractNumberFromDerivedString => ['',false]");
            return toReturn;
        }

        value = value.replace("confbridge:", "");
        value = value.replace("@from-queue", "");
        value = value.trim();

        if (value.contains("/")) {
            value = value.substring(value.indexOf("/") + 1);
        }

        if (value.contains("/")) {
            value = value.substring(value.indexOf("/") + 1);
            isCallOnMobile = "true";
        }

        if (value.contains("-")) {
            value = value.substring(0, value.indexOf("-"));
        }

        if (value.contains("@")) {
            value = value.substring(0, value.indexOf("@"));
        }

        value = value.trim();

        toReturn.add(value);
        toReturn.add(isCallOnMobile);

        SYS("EXIT extractNumberFromDerivedString number=" + value + " isCallOnMobile=" + isCallOnMobile);
        return toReturn;
    }

    public void recordCampaignRunWhatsAppMessageState(String whatsAppId, Long campaignId,String campaignName, String messageState,
                                           String phoneMain, String phoneWith,Long messageCost) {
        SYS("ENTER recordCampaignRunWhatsAppMessageState()");
        SYS("whatsAppId", whatsAppId);
        SYS("campaignId", campaignId);
        SYS("campaignName", campaignName);
        SYS("messageState", messageState);
        SYS("phoneMain", phoneMain);
        SYS("phoneWith", phoneWith);

        if (whatsAppId == null || whatsAppId.trim().isEmpty() || messageState == null) return;

        try {
            if (campaignId == null) return;

            String org = null;

            String fromNumber = null;
            String toNumber = null;

            if (phoneMain != null && !phoneMain.trim().isEmpty()) {
                fromNumber = phoneMain.trim();
            }
            if (phoneWith != null && !phoneWith.trim().isEmpty()) {
                toNumber = phoneWith.trim();
            }

            Long durationMs = 0L;
            String extraJson = null;

            StartedCampaignData.recordCallEvent(
                    campaignId,
                    org,
                    campaignName,
                    fromNumber,
                    toNumber,
                    null,
                    whatsAppId,      // channelId
                    messageState,     // INITIATED / IN_PROGRESS / COMPLETED
                    messageCost,
                    durationMs,
                    extraJson
            );

            SYS("EXIT recordCampaignRunWhatsAppMessageState done");

        } catch (Exception e) {
            SYS("EXCEPTION recordCampaignRunWhatsAppMessageState: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void recordCampaignRunCallState(String jobID, Campaign campaign,String toNumber, CdrDTO cdrDTO,String callState,
                                           String fallbackPhoneMaybe,Long callCost) {
        SYS("ENTER recordCampaignRunCallState()");
        SYS("jobID", jobID);
        SYS("callState", callState);
        SYS("fallbackPhoneMaybe", fallbackPhoneMaybe);

        if (jobID == null || jobID.trim().isEmpty() || callState == null) return;

        try {
        	
        	if(campaign == null) {
	            campaign = resolveCampaignForCallRun(jobID);
	            SYS("resolved campaign null?", (campaign == null));
        	}
            if (campaign == null) {
            	System.out.println("Returning as we donot have any campaign associated submitted to recordCampaignRunCallState ");
            	return;
            }

            String org = null;
            String campaignName = null;
            try { org = campaign.getOrganization(); } catch (Exception ignore) {}
            try { campaignName = campaign.getName(); } catch (Exception ignore) {}

            String employeeExtension = null;
            String fromNumber = null;
            if (cdrDTO != null) {
                if (cdrDTO.getEmployee() != null && cdrDTO.getEmployee().getExtension() != null) {
                    employeeExtension = String.valueOf(cdrDTO.getEmployee().getExtension());
                }

                if (cdrDTO.getCallerid() != null && !cdrDTO.getCallerid().trim().isEmpty()) {
                    fromNumber = cdrDTO.getCallerid().trim();
                }
                if (toNumber == null && cdrDTO.getCustomerid() != null && !cdrDTO.getCustomerid().trim().isEmpty()) {
                    toNumber = cdrDTO.getCustomerid().trim();
                }
            }

            if ((toNumber == null || toNumber.isEmpty()) && fallbackPhoneMaybe != null) {
                toNumber = fallbackPhoneMaybe.trim();
            }

            Long durationMs = 0L;
            if (cdrDTO != null && cdrDTO.getBridgeEnterTime() != null) {
                durationMs = System.currentTimeMillis() - cdrDTO.getBridgeEnterTime().getTime();
                if (durationMs < 0) durationMs = 0L;
            }

            String extraJson = null;

            StartedCampaignData.recordCallEvent(
                    campaign.getId(),
                    org,
                    campaignName,
                    fromNumber,
                    toNumber,
                    employeeExtension,
                    jobID,      // channelId
                    callState,     // INITIATED / IN_PROGRESS / COMPLETED
                    callCost,
                    durationMs,
                    extraJson
            );

            if ("COMPLETED".equalsIgnoreCase(callState)) {
            	SYS("REMOVING call job ID as status is completed");
                StartedCampaignData.removejobIDMapping(jobID);
            }
            
            SYS("EXIT recordCampaignRunCallState done");

        } catch (Exception e) {
            SYS("EXCEPTION recordCampaignRunCallState: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Campaign resolveCampaignForCustomerByPhoneNumber(String phone) {
        SYS("ENTER resolveCampaignForCustomerByPhoneNumber()");
        SYS("phone", phone);
        try {
            Map<String, CustomerAndItsCampaignDTO> one =
                    StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(phone, null, "get-one");
            CustomerAndItsCampaignDTO dto = (one != null) ? one.get(phone) : null;
            SYS("dto null?", (dto == null));
            if (dto == null || dto.getLastRunningCampaignID() == null) return null;

            Long campaignId = dto.getLastRunningCampaignID();
            SYS("campaignId", campaignId);

            Map<Long, Campaign> cm =
                    StartedCampaignData.workOnAllActiveCampaigns(campaignId, null, "get-one");
            Campaign c = (cm != null) ? cm.get(campaignId) : null;
            SYS("EXIT resolveCampaignForCustomerByPhoneNumber campaign null?", (c == null));
            return c;

        } catch (Exception e) {
            SYS("EXCEPTION resolveCampaignForCustomerByPhoneNumber: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Campaign resolveCampaignForCdr(CdrDTO cdrDTO, String fallbackPhoneMaybe) {
        SYS("ENTER resolveCampaignForCdr()");
        SYS("fallbackPhoneMaybe", fallbackPhoneMaybe);

        try {
            Employee emp = (cdrDTO != null) ? cdrDTO.getEmployee() : null;
            SYS("cdrDTO null?", (cdrDTO == null));
            SYS("emp null?", (emp == null));

            Campaign c = resolveRunningCampaignForEmployee(emp);
            if (c != null) {
                SYS("resolved via employee running campaign", c.getId());
                return c;
            }

            String phone = null;

            if (cdrDTO != null) {
                if (cdrDTO.getCustomerid() != null && !cdrDTO.getCustomerid().trim().isEmpty()) {
                    phone = cdrDTO.getCustomerid().trim();
                } else if (cdrDTO.getCallerid() != null && !cdrDTO.getCallerid().trim().isEmpty()) {
                    phone = cdrDTO.getCallerid().trim();
                }
            }

            if ((phone == null || phone.isEmpty()) && fallbackPhoneMaybe != null) {
                phone = fallbackPhoneMaybe.trim();
            }

            SYS("resolved phone", phone);

            if (phone == null || phone.isEmpty()) return null;

            Map<String, CustomerAndItsCampaignDTO> one =
                    StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(phone, null, "get-one");
            CustomerAndItsCampaignDTO dto = (one != null) ? one.get(phone) : null;
            SYS("dto null?", (dto == null));
            if (dto == null || dto.getLastRunningCampaignID() == null) return null;

            Long campaignId = dto.getLastRunningCampaignID();
            SYS("campaignId", campaignId);

            Map<Long, Campaign> cm =
                    StartedCampaignData.workOnAllActiveCampaigns(campaignId, null, "get-one");
            Campaign cc = (cm != null) ? cm.get(campaignId) : null;

            SYS("EXIT resolveCampaignForCdr campaign null?", (cc == null));
            return cc;

        } catch (Exception e) {
            SYS("EXCEPTION resolveCampaignForCdr: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public Campaign resolveCampaignForCallRun(String jobId) {
        SYS("ENTER resolveCampaignForCallRun()");
        try {

        	Long campaignId = StartedCampaignData.getCampaignIdByjobID(jobId);
            Map<Long, Campaign> cm =
                    StartedCampaignData.workOnAllActiveCampaigns(campaignId, null, "get-one");
            Campaign cc = (cm != null) ? cm.get(campaignId) : null;

            SYS("EXIT resolveCampaignForCdr campaign null?", (cc == null));
            
            return cc;

        } catch (Exception e) {
            SYS("EXCEPTION resolveCampaignForCdr: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Campaign resolveRunningCampaignForEmployee(Employee emp) {
        SYS("ENTER resolveRunningCampaignForEmployee()");
        SYS("emp null?", (emp == null));
        SYS("emp.ext", (emp != null ? emp.getExtension() : null));

        try {
            if (emp == null || emp.getExtension() == null) return null;

            String ext = String.valueOf(emp.getExtension());
            Map<String, EmployeeDataAndStateDTO> one =
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(ext, null, "get-one");
            EmployeeDataAndStateDTO st = (one != null) ? one.get(ext) : null;
            SYS("state dto null?", (st == null));
            if (st == null || st.getRunningCamapignId() == null) return null;

            Long campaignId = st.getRunningCamapignId();
            SYS("runningCampaignId", campaignId);

            Map<Long, Campaign> cm =
                    StartedCampaignData.workOnAllActiveCampaigns(campaignId, null, "get-one");
            Campaign c = (cm != null) ? cm.get(campaignId) : null;

            SYS("EXIT resolveRunningCampaignForEmployee campaign null?", (c == null));
            return c;

        } catch (Exception e) {
            SYS("EXCEPTION resolveRunningCampaignForEmployee: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean startCallFlowForEmployee(Employee currentEmployee, Campaign campaign, boolean duringStartCampaign) throws Exception {
        LoggerUtils.log.debug("startCallFlowForEmployee");

        SYS("ENTER startCallFlowForEmployee()");
        SYS("empExt", (currentEmployee != null ? currentEmployee.getExtension() : null));
        SYS("campaignId", (campaign != null ? campaign.getId() : null));
        SYS("duringStartCampaign", duringStartCampaign);

        boolean toReturn = true;
        try {
            DialerRouteContext ctx = new DialerRouteContext();
            ctx.employee = currentEmployee;
            ctx.campaign = campaign;
            ctx.duringStartCampaign = duringStartCampaign;

            String autodialerType = (campaign != null) ? campaign.getAutodialertype() : null;
            toReturn = routeByAutodialerType(autodialerType, DialerAction.START_CALL_FLOW, ctx);

        } catch (Exception e) {
            SYS("EXCEPTION startCallFlowForEmployee: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
            throw e;
        }

        SYS("EXIT startCallFlowForEmployee", toReturn);
        return toReturn;
    }

    public boolean removeAndScheduleCronForEmployee(Employee employee, Campaign campaign) {
        SYS("ENTER removeAndScheduleCronForEmployee()");
        SYS("empExt", (employee != null ? employee.getExtension() : null));
        SYS("campaignId", (campaign != null ? campaign.getId() : null));

        boolean toReturn = true;

        LoggerUtils.log.debug("scheduleCronForEmployee");

        try {
            String jobId = TrackedSchduledJobs.dialAutomateCallCron + employee.getExtension().toString();
            SYS("cronJobId", jobId);

            DialAutomateCallCronRunnable dialAutomateCallCronRunnable = new DialAutomateCallCronRunnable();
            dialAutomateCallCronRunnable.setJobId(jobId);
            dialAutomateCallCronRunnable.setEmployee(employee);
            dialAutomateCallCronRunnable.setCampaign(campaign);
            dialAutomateCallCronRunnable.setApplicationContext(applicationContext);

            scheduleDialAutomateCallService.removeAndScheduleDialAutomateCallCron(dialAutomateCallCronRunnable);
            SYS("removeAndScheduleDialAutomateCallCron DONE");
        } catch (Exception e) {
            SYS("EXCEPTION removeAndScheduleCronForEmployee: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
            throw e;
        }

        SYS("EXIT removeAndScheduleCronForEmployee", toReturn);
        return toReturn;
    }

    public boolean removeSchedules(String autodialerType, Employee currentEmployee, RemoveMode mode) throws Exception {
        SYS("ENTER removeSchedules()");
        SYS("autodialerType", autodialerType);
        SYS("empExt", (currentEmployee != null ? currentEmployee.getExtension() : null));
        SYS("mode", (mode != null ? mode.name() : null));

        boolean toReturn = true;

        LoggerUtils.log.debug("removeSchedules mode=" + mode);

        try {
            DialerRouteContext ctx = new DialerRouteContext();
            ctx.employee = currentEmployee;
            ctx.removeMode = (mode != null) ? mode : RemoveMode.ALL;

            toReturn = routeByAutodialerType(autodialerType, DialerAction.REMOVE_SCHEDULES, ctx);

        } catch (Exception e) {
            SYS("EXCEPTION removeSchedules: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
            throw e;
        }

        SYS("EXIT removeSchedules", toReturn);
        return toReturn;
    }

    public boolean removeSchedulesWithoutemployee(Campaign campaign,String customerPhone, RemoveMode mode) throws Exception {
        SYS("ENTER removeSchedulesWithoutemployee()");
        SYS("campaignId", (campaign != null ? campaign.getId() : null));
        SYS("customerPhone", customerPhone);
        SYS("mode", (mode != null ? mode.name() : null));

        boolean toReturn = true;
        try {
            DialerRouteContext ctx = new DialerRouteContext();
            ctx.customerPhone = customerPhone;
            ctx.campaign = campaign;
            ctx.removeMode = (mode != null) ? mode : RemoveMode.ALL;
            toReturn = routeByAutodialerType(campaign.getAutodialertype(), DialerAction.REMOVE_SCHEDULES, ctx);

        } catch (Exception e) {
            SYS("EXCEPTION removeSchedulesWithoutemployee: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
            throw e;
        }

        SYS("EXIT removeSchedulesWithoutemployee", toReturn);
        return toReturn;
    }

    public boolean removeAllScheduleJobForEmployee(String autodialerType, Employee currentEmployee) throws Exception {
        return removeSchedules(autodialerType, currentEmployee, RemoveMode.ALL);
    }

    public boolean removeReminderCallScheduleJobForEmployee(String autodialerType, Employee currentEmployee) throws Exception {
        return removeSchedules(autodialerType, currentEmployee, RemoveMode.REMINDER_ONLY);
    }

    public boolean removeCronCallScheduleJobForEmployee(String autodialerType, Employee currentEmployee) throws Exception {
        return removeSchedules(autodialerType, currentEmployee, RemoveMode.CRON_ONLY);
    }

    public boolean checkIfExtensionIsInIdealState(String extension, String domain, String secondDomain, boolean useSecondaryLine) throws Exception {
        LoggerUtils.log.debug("checkIfExtensionIsInIdealState");

        SYS("ENTER checkIfExtensionIsInIdealState()");
        SYS("extension", extension);
        SYS("domain", domain);
        SYS("secondDomain", secondDomain);
        SYS("useSecondaryLine", useSecondaryLine);

        boolean toReturn = true;

        ConnectionStream connectionStream = new ConnectionStream();
        ManagerConnection managerConnection = connectionStream.getConnection(domain, secondDomain, useSecondaryLine);

        ManagerStream managerStream = new ManagerStream();
        ManagerResponse response = null;

        if (managerConnection != null) {
            try {
                LoggerUtils.log.debug("Getting response");
                SYS("commandAction core show hint " + extension);
                response = managerStream.commandAction("host-tune-perform", "core show hint " + extension, 3000L, managerConnection);
                String output = response.getOutput();
                SYS("amiOutput", output);

                if (output.contains("State:Idle") && output.contains("Presence:available")) {
                    toReturn = true;
                } else {
                    toReturn = false;
                }

            } catch (IOException | AuthenticationFailedException | TimeoutException e) {
                SYS("EXCEPTION checkIfExtensionIsInIdealState: " + e.getMessage());
                toReturn = false;
                e.printStackTrace();
            }

        } else {
            SYS("managerConnection null -> throw");
            throw new Exception("Cannot find asterisk manager");
        }

        SYS("EXIT checkIfExtensionIsInIdealState", toReturn);
        return toReturn;
    }

    public boolean checkReminderCalling(Customers currentCustomer, Campaign campaign) {
        SYS("ENTER checkReminderCalling()");
        SYS("customer.phone", (currentCustomer != null ? currentCustomer.getPhoneNumber() : null));
        SYS("campaignId", (campaign != null ? campaign.getId() : null));

        try {
            boolean remindercalling = campaign.isRemindercalling();

            if (remindercalling) {
                // Do nothing
            } else {
                if (currentCustomer != null && currentCustomer.isRemindercalling()) {
                    remindercalling = true;
                } else {
                    remindercalling = false;
                }
            }
            LoggerUtils.log.debug("remindercalling for Customer: " + currentCustomer.getPhoneNumber() + "is :" + remindercalling);
            SYS("EXIT checkReminderCalling", remindercalling);
            return remindercalling;
        } catch (Exception e) {
            SYS("EXCEPTION checkReminderCalling: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean checkIsCallOnMobile(Employee currentEmployee, Campaign campaign) {
        SYS("ENTER checkIsCallOnMobile()");
        SYS("empExt", (currentEmployee != null ? currentEmployee.getExtension() : null));
        SYS("campaignId", (campaign != null ? campaign.getId() : null));

        try {
            boolean isCallOnMobile = campaign.isIsonmobile();

            if (isCallOnMobile) {
                //Do nothing
            } else {
                if (currentEmployee != null && currentEmployee.isCallonnumber()) {
                    isCallOnMobile = true;
                } else {
                    isCallOnMobile = false;
                }
            }
            LoggerUtils.log.debug("checkIsCallOnMobile for Extension: " + currentEmployee.getExtension() + "is :" + isCallOnMobile);
            SYS("EXIT checkIsCallOnMobile", isCallOnMobile);
            return isCallOnMobile;
        } catch (Exception e) {
            SYS("EXCEPTION checkIsCallOnMobile: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean verifyRunningCampaignForEmployee(Employee employee) {
        SYS("ENTER verifyRunningCampaignForEmployee()");
        SYS("empExt", (employee != null ? employee.getExtension() : null));

        boolean toReturn = false;

        try {
            LoggerUtils.log.debug("verifyRunningCampaignForEmployee for Extension : " + employee.getExtension());

            Map<String, EmployeeAndItsCampaignDTO> allEmployeeAndItsCampaignDTO =
                    StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(employee.getExtension(), null, "get-one");
            EmployeeAndItsCampaignDTO employeeAndItsCampaignDTO = null;
            if (allEmployeeAndItsCampaignDTO != null) {
                employeeAndItsCampaignDTO = allEmployeeAndItsCampaignDTO.get(employee.getExtension());
            }

            Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), null, "get-one");
            EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
            if (allEmployeeDataAndState != null) {
                employeeDataAndStateDTO = allEmployeeDataAndState.get(employee.getExtension());
            }

            SYS("employeeAndItsCampaignDTO null?", (employeeAndItsCampaignDTO == null));
            SYS("employeeDataAndStateDTO null?", (employeeDataAndStateDTO == null));

            if (employeeAndItsCampaignDTO != null && employeeDataAndStateDTO != null) {
                LoggerUtils.log.debug("employeeAndItsCampaignDTO is not null");
                if (employeeDataAndStateDTO.getRunningCamapignId() != -1) {
                    LoggerUtils.log.debug("Employee have running campaign ID");
                    Map<Long, Campaign> activeCampaigns = StartedCampaignData.workOnAllActiveCampaigns(null, null, "get");
                    if (activeCampaigns.get(employeeDataAndStateDTO.getRunningCamapignId()) != null) {
                        toReturn = true;
                    }
                }
            } else {
                toReturn = false;
            }
        } catch (Exception e) {
            SYS("EXCEPTION verifyRunningCampaignForEmployee: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
        }

        SYS("EXIT verifyRunningCampaignForEmployee", toReturn);
        return toReturn;
    }

    public Campaign findRunningOrElseSetCampaignForEmployee(Employee employee) {
        SYS("ENTER findRunningOrElseSetCampaignForEmployee()");
        SYS("empExt", (employee != null ? employee.getExtension() : null));

        LoggerUtils.log.debug("findRunningOrElseNextCampaignForEmployee for Extension : " + employee.getExtension());

        Map<String, EmployeeAndItsCampaignDTO> allEmployeeAndItsCampaignDTO =
                StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(employee.getExtension(), null, "get-one");
        Map<Long, Campaign> activeCampaigns = StartedCampaignData.workOnAllActiveCampaigns(null, null, "get");

        EmployeeAndItsCampaignDTO employeeAndItsCampaignDTO = null;
        if (allEmployeeAndItsCampaignDTO != null) {
            employeeAndItsCampaignDTO = allEmployeeAndItsCampaignDTO.get(employee.getExtension());
        }

        Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), null, "get-one");
        EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
        if (allEmployeeDataAndState != null) {
            employeeDataAndStateDTO = allEmployeeDataAndState.get(employee.getExtension());
        }

        Campaign campaign = null;

        SYS("employeeAndItsCampaignDTO null?", (employeeAndItsCampaignDTO == null));
        SYS("employeeDataAndStateDTO null?", (employeeDataAndStateDTO == null));
        SYS("activeCampaigns null?", (activeCampaigns == null));

        if (employeeAndItsCampaignDTO != null && employeeDataAndStateDTO != null) {
            LoggerUtils.log.debug("employeeAndItsCampaignDTO is not null");
            if (employeeDataAndStateDTO.getRunningCamapignId() != -1) {
                LoggerUtils.log.debug("Employee have running campaign ID");
                if (activeCampaigns != null) {
                    campaign = activeCampaigns.get(employeeDataAndStateDTO.getRunningCamapignId());
                } else {
                    return null;
                }
            }

            if (campaign == null) {
                int index = employeeAndItsCampaignDTO.getCampaignIds().indexOf(employeeDataAndStateDTO.getRunningCamapignId());
                if (index != -1) {
                    employeeAndItsCampaignDTO.getCampaignIds().remove(index);
                    StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(employee.getExtension(), employeeAndItsCampaignDTO, "update");
                }
            }

            for (int i = 0; i < 500; i++) {
                if (campaign == null && i != 0) {
                    int index = employeeAndItsCampaignDTO.getCampaignIds().indexOf(employeeDataAndStateDTO.getRunningCamapignId());
                    if (index != -1) {
                        employeeAndItsCampaignDTO.getCampaignIds().remove(index);
                        StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(employee.getExtension(), employeeAndItsCampaignDTO, "update");
                    }
                }

                if (campaign == null && employeeAndItsCampaignDTO.getCampaignIds().size() > 0) {
                    Long currentCampaignId = employeeAndItsCampaignDTO.getCampaignIds().get(0);
                    LoggerUtils.log.debug("Employee does not have running campaign, Next campaign id is : " + currentCampaignId);
                    if (activeCampaigns != null) {
                        campaign = activeCampaigns.get(currentCampaignId);
                    } else {
                        campaign = null;
                        break;
                    }

                    LoggerUtils.log.debug("setting this new campaign as current Running campaign for user having Extension : " + employee.getExtension());
                    employeeDataAndStateDTO.setRunningCamapignId(currentCampaignId);
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), employeeDataAndStateDTO, "update");
                }

                if (campaign == null && employeeAndItsCampaignDTO.getCampaignIds().size() == 0) {
                    employeeDataAndStateDTO.setRunningCamapignId(-1L);
                    StartedCampaignData.workOnAllActiveExtensionsAndTheirCampaign(employee.getExtension(), null, "delete");
                    StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(employee.getPhonenumber(), null, "delete");
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), employeeDataAndStateDTO, "update");
                    break;
                } else if (campaign != null) {
                    break;
                }
            }
        }

        SYS("EXIT findRunningOrElseSetCampaignForEmployee campaignId", (campaign != null ? campaign.getId() : null));
        return campaign;
    }

    public boolean verifyIfEmployeeIsAvailableForCallAsPerExtension(Employee employee) {
        SYS("ENTER verifyIfEmployeeIsAvailableForCallAsPerExtension()");
        SYS("empExt", (employee != null ? employee.getExtension() : null));

        boolean toReturn = false;

        try {

            Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), null, "get-one");
            EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
            if (allEmployeeDataAndState != null) {
                employeeDataAndStateDTO = allEmployeeDataAndState.get(employee.getExtension());
            }

            List<String> employeeStates = employeeDataAndStateDTO.getExtensionState();

            LoggerUtils.log.debug(employeeStates.get(0));
            LoggerUtils.log.debug(employeeStates.get(1));
            LoggerUtils.log.debug(employeeStates.get(2));

            SYS("state0", employeeStates.get(0));
            SYS("state1", employeeStates.get(1));
            SYS("state2", employeeStates.get(2));

            if ((employeeStates.get(0).equals("terminated")) && (employeeStates.get(1).equals("success")) && (employeeStates.get(2).equals("dotOnline"))) {
                toReturn = true;
            } else if ((employeeStates.get(0).equals("trying")) && (employeeStates.get(1).equals("success")) && (employeeStates.get(2).equals("dotInUse"))) {
            } else if ((employeeStates.get(0).equals("confirmed")) && (employeeStates.get(1).equals("success")) && (employeeStates.get(2).equals("dotInUse"))) {
            } else if ((employeeStates.get(0).equals("early")) && (employeeStates.get(1).equals("success")) && (employeeStates.get(2).equals("dotInUse"))) {
            } else if ((employeeStates.get(0).equals("on-hold")) && (employeeStates.get(1).equals("success")) && (employeeStates.get(2).equals("dotOnHold"))) {
                toReturn = true;
            } else if ((employeeStates.get(0).equals("terminated")) && (employeeStates.get(1).equals("danger")) && (employeeStates.get(2).equals("dotOffline"))) {
            } else if ((employeeStates.get(0).equals("proceeding")) && (employeeStates.get(1).equals("success")) && (employeeStates.get(2).equals("dotInUse"))) {
            }

        } catch (Exception e) {
            SYS("EXCEPTION verifyIfEmployeeIsAvailableForCallAsPerExtension: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
        }

        SYS("EXIT verifyIfEmployeeIsAvailableForCallAsPerExtension", toReturn);
        return toReturn;
    }

    public boolean changeDeviceState(Map<String, String> mapEvent) {

        SYS("ENTER changeDeviceState()");
        SYS("mapEvent null?", (mapEvent == null));

        boolean toReturn = true;

        try {
            if (DEEP_LOGS) LoggerUtils.log.debug("changeDeviceState");

            if (mapEvent == null) {
                if (DEEP_LOGS) LoggerUtils.log.debug("changeDeviceState: mapEvent is null");
                return true;
            }

            String state = mapEvent.get(DEVICE_STATE_CHANGE_EVENT.state.name());
            String device = mapEvent.get(DEVICE_STATE_CHANGE_EVENT.device.name());

            if (state == null) state = "";
            if (device == null) device = "";

            boolean allowed = verifyIfDeviceStateChangeIsAllowed(device);

            List<String> phoneNumberData = extractNumberFromDerivedString(device);
            String identifier = (phoneNumberData != null && phoneNumberData.size() > 0) ? phoneNumberData.get(0) : "";

            if (DEEP_LOGS) {
                LoggerUtils.log.debug("changeDeviceState: state=" + state + " device=" + device + " identifier=" + identifier + " allowed=" + allowed);
            }

            SYS("state", state);
            SYS("device", device);
            SYS("identifier", identifier);
            SYS("allowed", allowed);

            if (state.trim().equals("null")) {
                return true;
            }

            if (!allowed) {
                return true;
            }

            String resolvedExtension = resolveExtensionFromIdentifier(identifier);
            SYS("resolvedExtension", resolvedExtension);

            if (resolvedExtension == null || resolvedExtension.trim().isEmpty()) {
                if (DEEP_LOGS) LoggerUtils.log.debug("changeDeviceState: could not resolve extension from identifier=" + identifier);
                return true;
            }

            Employee employee = null;

            try {
                Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
                        EmployeeDataAndState.workOnAllEmployeeDataAndState(resolvedExtension, null, "get-one");

                EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
                if (allEmployeeDataAndState != null) {
                    employeeDataAndStateDTO = allEmployeeDataAndState.get(resolvedExtension);
                }

                if (employeeDataAndStateDTO != null) {
                    employee = employeeDataAndStateDTO.getEmployee();
                }

                SYS("employee null?", (employee == null));

                if (employee != null) {

                    if (DEEP_LOGS) LoggerUtils.log.debug("state : " + state);

                    setExtensionState(state, employee.getExtension());

                    Campaign campaign = null;
                    campaign = findRunningOrElseSetCampaignForEmployee(employee);

                    if (DEEP_LOGS) LoggerUtils.log.debug("After finding running campaign for employee");
                    SYS("campaign null?", (campaign == null));

                    if (campaign != null) {
                        if (DEEP_LOGS) LoggerUtils.log.debug("Campaign is not null");

                        boolean isCallOnMobile = checkIsCallOnMobile(employee, campaign);

                        if (DEEP_LOGS) LoggerUtils.log.debug("isCallOnMobile : " + isCallOnMobile);
                        SYS("isCallOnMobile", isCallOnMobile);

                        if (!isCallOnMobile) {
                            this.changeEmployeeState(state, employee);
                        }
                    }
                }

            } catch (Exception e) {
                SYS("EXCEPTION inner changeDeviceState: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            LoggerUtils.log.debug("changeDeviceState Exception");
            SYS("EXCEPTION changeDeviceState: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
        }

        SYS("EXIT changeDeviceState", toReturn);
        return toReturn;
    }

    private String resolveExtensionFromIdentifier(String identifier) {

        SYS("ENTER resolveExtensionFromIdentifier()");
        SYS("identifier", identifier);

        if (identifier == null) return null;
        String id = identifier.trim();
        if (id.isEmpty()) return null;

        try {
            Map<String, EmployeeDataAndStateDTO> byExtension =
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(id, null, "get-one");

            if (byExtension != null && byExtension.containsKey(id)) {
                SYS("resolved as extension", id);
                return id;
            }
        } catch (Exception e) {
            if (DEEP_LOGS)
                LoggerUtils.log.debug("resolveExtensionFromIdentifier: extension lookup failed id=" + id + " msg=" + e.getMessage());
            SYS("extension lookup failed", e.getMessage());
        }

        try {
            Map<String, String> phoneMap =
                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(id, null, "get-one");

            if (phoneMap != null) {
                String ext = phoneMap.get(id);
                if (ext != null && !ext.trim().isEmpty()) {
                    SYS("resolved via phone->ext", ext.trim());
                    return ext.trim();
                }
            }
        } catch (Exception e) {
            if (DEEP_LOGS)
                LoggerUtils.log.debug("resolveExtensionFromIdentifier: phone lookup failed id=" + id + " msg=" + e.getMessage());
            SYS("phone lookup failed", e.getMessage());
        }

        SYS("EXIT resolveExtensionFromIdentifier null");
        return null;
    }

    public void changeEmployeeState(String state, Employee employee) {
        SYS("ENTER changeEmployeeState()");
        SYS("state", state);
        SYS("empExt", (employee != null ? employee.getExtension() : null));

        try {
            changeStateAndTriggerAutodialer(state, employee.getExtension());
        } catch (Exception e) {
            LoggerUtils.log.debug("changeEmployeeState Exception");
            SYS("EXCEPTION changeEmployeeState: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean dialFromCron(Campaign campaign, Employee employee, SchedulerService schedulerService) {
        SYS("ENTER dialFromCron()");
        SYS("campaignId", (campaign != null ? campaign.getId() : null));
        SYS("empExt", (employee != null ? employee.getExtension() : null));

        boolean toReturn = true;

        try {
            LoggerUtils.log.debug("dialFromCron");

            DialerRouteContext ctx = new DialerRouteContext();
            ctx.campaign = campaign;
            ctx.employee = employee;
            ctx.schedulerService = schedulerService;

            toReturn = routeByAutodialerType(
                    (campaign != null) ? campaign.getAutodialertype() : null,
                    DialerAction.DIAL_FROM_CRON,
                    ctx
            );

        } catch (Exception e) {
            SYS("EXCEPTION dialFromCron: " + e.getMessage());
            toReturn = false;
            e.printStackTrace();
        }

        SYS("EXIT dialFromCron", toReturn);
        return toReturn;
    }

    public void executeAutodialerWhatsAppTemplateMessage(String jobId,Customers customer,String connectedLine,String template,Campaign campaign) {
        SYS("ENTER executeAutodialerWhatsAppTemplateMessage()");
        SYS("jobId", jobId);
        SYS("customer.phone", (customer != null ? customer.getPhoneNumber() : null));
        SYS("connectedLine", connectedLine);
        SYS("template", template);
        SYS("campaignId", (campaign != null ? campaign.getId() : null));

        try {

            WhatsAppChatHistory inputDTO = new WhatsAppChatHistory();
            inputDTO.setMessageType(MESSAGE_TYPE.text.name());
            inputDTO.setMessageString("Message initiation via template :"+template+" was send to this customer.");
            inputDTO.setPhoneNumberWith(customer.getPhoneNumber());
            inputDTO.setPhoneNumberMain(connectedLine);
            inputDTO.setOrganization(customer.getOrganization());
            inputDTO.setFromName(customer.getFirstname()+" "+customer.getLastname());
            inputDTO.setFromExtension(connectedLine);
            inputDTO.setOutbound(true);
            inputDTO.setCreatedOn(new Date());
            inputDTO.setLastUpdateTime(new Date());
            inputDTO.setMessageOrigin(CHAT_ORIGIN.template.name());
            inputDTO.setDeleteSelf(true);

            JSONObject jsonObject= whatsAppIntegrationOutboundService.sendWhatsAppMessageAsPerTemplateName(customer, template, connectedLine);
            SYS("whatsapp response null?", (jsonObject == null));

            if (jsonObject == null) {
                LoggerUtils.log.error("WhatsApp API returned null jsonObject. template=" + template + ", line=" + connectedLine);
                recordCampaignRunWhatsAppMessageState(jobId, campaign.getId(),campaign.getName(), "UNACCEPTED", connectedLine, customer.getPhoneNumber(),null);
                return;
            }

            if (!jsonObject.has(SEND_MESSAGE_KEYS.messages.name())) {
                LoggerUtils.log.error("WhatsApp response missing 'messages'. Response=" + jsonObject);
                recordCampaignRunWhatsAppMessageState(jobId,campaign.getId(),campaign.getName(), "UNACCEPTED", connectedLine, customer.getPhoneNumber(),null);
                return;
            }

            JSONArray array = (JSONArray) jsonObject.get(SEND_MESSAGE_KEYS.messages.name());
            SYS("messages array length", array.length());

            if(array.length() > 0)
            {
                JSONObject object = array.getJSONObject(0);
                String waId = object.get(SEND_MESSAGE_KEYS.id.name()).toString();
                SYS("whatsapp messageId", waId);

                inputDTO.setWhatsAppMessageId(waId);
                recordCampaignRunWhatsAppMessageState(waId, campaign.getId(),campaign.getName(),"INITIATED",connectedLine,customer.getPhoneNumber(),null);
                StartedCampaignData.putWaMsgCampaignMapping(waId, campaign.getId());
                whatsAppIntegrationOutboundService.digestOutboundMessageToChatHistory(inputDTO);
            }

        }
        catch(Exception e) {
            SYS("EXCEPTION executeAutodialerWhatsAppTemplateMessage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void executeAutodialerCall(
            String jobId,
            String phoneNumber,
            String fromExtension,
            String fromPhoneNumber,
            boolean isCallOnMobile,
            String callType,
            String organization,
            String domain,
            String context,
            int priority,
            Long timeOut,
            String firstName,
            String protocol,
            String phoneTrunk,
            boolean useSecondaryLine,
            String secondDomain,
            int breathingSeconds,
            String autodialerType,
            String stasisName,
            boolean callNow
    ) {
        SYS("ENTER executeAutodialerCall()");
        SYS("jobId", jobId);
        SYS("phoneNumber", phoneNumber);
        SYS("fromExtension", fromExtension);
        SYS("fromPhoneNumber", fromPhoneNumber);
        SYS("isCallOnMobile", isCallOnMobile);
        SYS("callType", callType);
        SYS("organization", organization);
        SYS("domain", domain);
        SYS("context", context);
        SYS("priority", priority);
        SYS("timeOut", timeOut);
        SYS("firstName", firstName);
        SYS("protocol", protocol);
        SYS("phoneTrunk", phoneTrunk);
        SYS("useSecondaryLine", useSecondaryLine);
        SYS("secondDomain", secondDomain);
        SYS("breathingSeconds", breathingSeconds);
        SYS("autodialerType", autodialerType);
        SYS("stasisName", stasisName);
        SYS("callNow", callNow);

        try {

            if (autodialerType == null) {
                LoggerUtils.log.warn("autodialerType is null for jobId={}", jobId);
                SYS("autodialerType null -> return");
                return;
            }

            ExecuteCallArgs args = new ExecuteCallArgs();
            args.jobId = jobId;
            args.phoneNumber = phoneNumber;
            args.fromExtension = fromExtension;
            args.fromPhoneNumber = fromPhoneNumber;
            args.isCallOnMobile = isCallOnMobile;
            args.callType = callType;
            args.organization = organization;
            args.domain = domain;
            args.context = context;
            args.priority = priority;
            args.timeOut = timeOut;
            args.firstName = firstName;
            args.protocol = protocol;
            args.phoneTrunk = phoneTrunk;
            args.useSecondaryLine = useSecondaryLine;
            args.secondDomain = secondDomain;
            args.breathingSeconds = breathingSeconds;
            args.autodialerType = autodialerType;
            args.stasisName = stasisName;
            args.callNow = callNow;

            DialerRouteContext ctx = new DialerRouteContext();
            ctx.executeArgs = args;

            boolean ok = routeByAutodialerType(autodialerType, DialerAction.EXECUTE_AUTODIALER_CALL, ctx);
            SYS("route EXECUTE_AUTODIALER_CALL ok", ok);

            if (!ok) {
                LoggerUtils.log.warn("Unknown autodialerType={} jobId={}", autodialerType, jobId);
                SYS("Unknown autodialerType", autodialerType);
            }

        } catch (Exception e) {
            try {
                SYS("EXCEPTION executeAutodialerCall: " + e.getMessage());
                e.printStackTrace();
                LoggerUtils.log.error(
                        "Eror while originating scheduled call, sending notification to employee who scheduled it. jobId={} msg={}",
                        jobId, String.valueOf(e.getMessage()), e
                );

                if (employeeCallErrorNotificationService != null) {
                    employeeCallErrorNotificationService.sendEmployeeCallErrorNotifications(
                            fromExtension,
                            firstName,
                            fromPhoneNumber,
                            organization,
                            domain,
                            notificationRepository
                    );
                } else {
                    LoggerUtils.log.error("employeeCallErrorNotificationService is null; cannot send notification. jobId={}", jobId);
                }
            } catch (Exception e1) {
                SYS("EXCEPTION executeAutodialerCall notify: " + e1.getMessage());
                e1.printStackTrace();
            }
        }
    }

    public boolean findIfWeRequireEmployeeForAutodialer(String autoDialerType) {
        SYS("ENTER findIfWeRequireEmployeeForAutodialer()");
        SYS("autoDialerType", autoDialerType);

        DialerRouteContext ctx = new DialerRouteContext();
        try {
            boolean r = routeByAutodialerType(autoDialerType, DialerAction.REQUIRES_EMPLOYEE, ctx);
            SYS("EXIT findIfWeRequireEmployeeForAutodialer", r);
            return r;
        } catch (Exception e) {
            SYS("EXCEPTION findIfWeRequireEmployeeForAutodialer -> default true: " + e.getMessage());
            return true;
        }
    }

    public List<String> getEmployeeExtension(Long campaignId) {

        SYS("ENTER getEmployeeExtension()");
        SYS("campaignId", campaignId);

        List<String> employeeExtensions = new ArrayList<String>();

        try {
            LoggerUtils.log.debug("getEmployeeExtension");
            Map<String, CampaignEmployeeDataDTO> mapOfCampaignEmployeeDataDTO =
                    StartedCampaignData.workOnActiveCampaignAndAllEmployeeData(campaignId, null, null, null, "get");

            Set<String> allExtensions = (mapOfCampaignEmployeeDataDTO == null) ? null : mapOfCampaignEmployeeDataDTO.keySet();
            SYS("allExtensions null?", (allExtensions == null));
            SYS("allExtensions.size", (allExtensions == null ? null : allExtensions.size()));

            if (allExtensions != null && !allExtensions.isEmpty()) {
                employeeExtensions.addAll(allExtensions);
            }
        } catch (Exception e) {
            SYS("EXCEPTION getEmployeeExtension: " + e.getMessage());
            employeeExtensions = null;
            e.printStackTrace();
            throw e;
        }

        SYS("EXIT getEmployeeExtension size", (employeeExtensions == null ? null : employeeExtensions.size()));
        return employeeExtensions;
    }

    public boolean isEmployeeCronRunning(Employee employee) {
        SYS("ENTER isEmployeeCronRunning()");
        SYS("empExt", (employee != null ? employee.getExtension() : null));

        LoggerUtils.log.debug("getCurrentAutodialerTypeForEmployee");

        boolean foundRunningCron = false;
        if (!foundRunningCron) {
            String jobID = TrackedSchduledJobs.dialAutomateCall + employee.getExtension().toString();
            SYS("check scheduleDialAutomateCallService.findIfScheduleDialAutomateCall jobID", jobID);
            foundRunningCron = scheduleDialAutomateCallService.findIfScheduleDialAutomateCall(jobID);
        }
        if (!foundRunningCron) {
            // keep as-is
        }

        SYS("EXIT isEmployeeCronRunning", foundRunningCron);
        return foundRunningCron;
    }

    public boolean verifyIfCustomerCallable(String phoneNumber) {
        SYS("ENTER verifyIfCustomerCallable()");
        SYS("phoneNumber", phoneNumber);

        boolean toReturn = true;
        try {
            Map<String, CustomerAndItsCampaignDTO> allCustomersAndItsCampaignDTO =
                    StartedCampaignData.workOnAllActiveCustomersAndItsCampaign(phoneNumber, null, "get-one");
            CustomerAndItsCampaignDTO customerAndItsCampaignDTO = (allCustomersAndItsCampaignDTO == null) ? null : allCustomersAndItsCampaignDTO.get(phoneNumber);

            SYS("dto null?", (customerAndItsCampaignDTO == null));

            if (customerAndItsCampaignDTO != null) {
                SYS("lastRunningCampaignID", customerAndItsCampaignDTO.getLastRunningCampaignID());
                SYS("assignedDate", customerAndItsCampaignDTO.getAssignedDate());

                if (customerAndItsCampaignDTO.getLastRunningCampaignID() != -1) {
                    boolean ok = isAtleastTenMinutesAgo(customerAndItsCampaignDTO.getAssignedDate());
                    SYS("isAtleastTenMinutesAgo", ok);
                    if (!ok) {
                        toReturn = false;
                    }
                }
            }
        } catch (Exception e) {
            SYS("EXCEPTION verifyIfCustomerCallable: " + e.getMessage());
            e.printStackTrace();
        }

        SYS("EXIT verifyIfCustomerCallable", toReturn);
        return toReturn;
    }

    private static boolean isAtleastTenMinutesAgo(Date date) {
        if (date == null) {
            return true;
        }
        Instant instant = Instant.ofEpochMilli(date.getTime());
        Instant tenMinutesAgo = Instant.now().minus(Duration.ofMinutes(10));
        return instant.isBefore(tenMinutesAgo);
    }

    public String getConnectedToLine(Campaign campaign) {
        SYS("ENTER getConnectedToLine()");
        SYS("campaignId", (campaign != null ? campaign.getId() : null));
        SYS("autodialerType", (campaign != null ? campaign.getAutodialertype() : null));

        String toReturn = null;

        try {
            if (campaign.getAutodialertype().equals(AUTODIALER_TYPE.QUEUE_DIALER.name())) {
                toReturn = campaign.getQueueExtension();
            } else if (campaign.getAutodialertype().equals(AUTODIALER_TYPE.CONFERENCE_DIALING.name())) {
                toReturn = campaign.getConfExtension();
            } else if (campaign.getAutodialertype().equals(AUTODIALER_TYPE.YOUTUBE_DIALER.name())) {
                toReturn = campaign.getConfExtension();
            } else if (campaign.getAutodialertype().equals(AUTODIALER_TYPE.AI_CALL.name())) {
                toReturn = " ";
            } else if (campaign.getAutodialertype().equals(AUTODIALER_TYPE.IVR_DIALER.name())) {
//                toReturn = campaign.getIvrExtension();
                toReturn = " ";
            }
            else if (campaign.getAutodialertype().equals(AUTODIALER_TYPE.WHATSAPP_MESSAGE.name())) {
                toReturn = campaign.getWhatsAppNumber();
            }
        } catch (Exception e) {
            SYS("EXCEPTION getConnectedToLine: " + e.getMessage());
            System.out.println(e.getMessage());
        }

        SYS("EXIT getConnectedToLine", toReturn);
        return toReturn;
    }
    
    public String buildOnlyCustomerJobIdFromPhone(String customerPhone) {
        if (customerPhone == null) return null;
        String p = customerPhone.trim();
        if (p.isEmpty()) return null;
        return TrackedSchduledJobs.dialAutomateCall + "-ch-" + p;
    }
    
    public String buildReinitiateOnlyCustomerJobIdFromPhone(String customerPhone) {
        if (customerPhone == null) return null;
        String p = customerPhone.trim();
        if (p.isEmpty()) return null;
        return TrackedSchduledJobs.reinitiateOnlyCustomerDialer + "-ch-" + p;
    }
    
}
