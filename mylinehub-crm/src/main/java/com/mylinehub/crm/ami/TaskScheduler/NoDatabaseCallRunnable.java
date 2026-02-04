// ========================= NoDatabaseCallRunnable.java (REPLACEMENT) =========================
package com.mylinehub.crm.ami.TaskScheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.response.ManagerResponse;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.crm.ami.ConnectionStream;
import com.mylinehub.crm.ami.ManagerStream;
import com.mylinehub.crm.ami.service.notificaton.EmployeeCallErrorNotificationService;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import lombok.Data;

@Data
public class NoDatabaseCallRunnable implements Runnable {

	//NoDatabaseCallRunnable
    private String jobId;
    private String phoneNumber;
    private String fromExtension;
    private String fromPhoneNumber;
    boolean isCallOnMobile;

    private String callType;
    private String organization;
    private String domain;
    private ApplicationContext applicationContext;
    private NotificationRepository notificationRepository;

    private String context;
    private int priority;
    private Long timeOut;

    private String firstName;
    private String protocol;
    private String phoneTrunk;

    boolean useSecondaryLine;
    String secondDomain;

    boolean pridictive;

    // Stasis support
    String stasisAppName;
    private String simSelector;                 // e.g. "+10000001" or "10000001" (whatever your route expects)
    private boolean simSelectorRequired;        // if true -> apply selector prefix when dialing via trunk directly


    @Override
    public void run() {
        System.out.println("[NoDatabaseCallRunnable][run] START jobId=" + jobId
                + " org=" + organization
                + " domain=" + domain
                + " secondDomain=" + secondDomain
                + " useSecondaryLine=" + useSecondaryLine
                + " callType=" + callType
                + " pridictive=" + pridictive
                + " isCallOnMobile=" + isCallOnMobile
                + " fromExtension=" + fromExtension
                + " fromPhoneNumber=" + fromPhoneNumber
                + " phoneNumber=" + phoneNumber
                + " stasisAppName=" + stasisAppName
                + " simSelectorRequired=" + simSelectorRequired
                + " simSelector=" + simSelector
                + " protocol=" + protocol
                + " phoneTrunk=" + phoneTrunk
                + " context=" + context
                + " priority=" + priority
                + " timeOut=" + timeOut);

        EmployeeCallErrorNotificationService employeeCallErrorNotificationService =
                applicationContext.getBean(EmployeeCallErrorNotificationService.class);

        try {
            LoggerUtils.log.debug("Dial Now");
            System.out.println("[NoDatabaseCallRunnable][run] Dial Now -> calling dialNowWithRetry(3)");
            dialNowWithRetry(3); // you were effectively retrying up to 3 times on timeouts
            System.out.println("[NoDatabaseCallRunnable][run] SUCCESS jobId=" + jobId);
        } catch (Exception e) {
            LoggerUtils.log.debug("Final failure in NoDatabaseCallRunnable: " + safeMsg(e));
            System.out.println("[NoDatabaseCallRunnable][run] FINAL FAILURE jobId=" + jobId + " err=" + safeMsg(e));
            try {
                System.out.println("[NoDatabaseCallRunnable][run] Sending employee call error notifications...");
                employeeCallErrorNotificationService.sendEmployeeCallErrorNotifications(
                        fromExtension,
                        firstName,
                        fromPhoneNumber,
                        organization,
                        domain,
                        notificationRepository
                );
                System.out.println("[NoDatabaseCallRunnable][run] Employee call error notifications SENT");
            } catch (Exception notifyEx) {
                System.out.println("[NoDatabaseCallRunnable][run] Notification send FAILED err=" + safeMsg(notifyEx));
                notifyEx.printStackTrace();
            }
        } finally {
            System.out.println("[NoDatabaseCallRunnable][run] END jobId=" + jobId);
        }
    }

    // ---------------------------- Retry handling ----------------------------
    private void dialNowWithRetry(int maxAttempts) throws Exception {
        System.out.println("[NoDatabaseCallRunnable][dialNowWithRetry] START maxAttempts=" + maxAttempts);
        Exception last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                LoggerUtils.log.debug("Dial attempt " + attempt + "/" + maxAttempts);
                System.out.println("[NoDatabaseCallRunnable][dialNowWithRetry] Attempt " + attempt + "/" + maxAttempts
                        + " jobId=" + jobId);
                dialNow(); // does the actual originate
                System.out.println("[NoDatabaseCallRunnable][dialNowWithRetry] SUCCESS on attempt=" + attempt
                        + " jobId=" + jobId);
                return;
            } catch (Exception e) {
                last = e;

                System.out.println("[NoDatabaseCallRunnable][dialNowWithRetry] FAILED attempt=" + attempt
                        + " jobId=" + jobId
                        + " err=" + safeMsg(e));

                if (isTimeoutExceptionMessage(e) && attempt < maxAttempts) {
                    LoggerUtils.log.debug("Timeout detected, retrying. Error: " + safeMsg(e));
                    System.out.println("[NoDatabaseCallRunnable][dialNowWithRetry] Timeout detected -> RETRYING...");
                    continue;
                }

                // Not a timeout OR no attempts left
                System.out.println("[NoDatabaseCallRunnable][dialNowWithRetry] Not retrying further. Throwing err.");
                throw e;
            }
        }

        if (last != null) {
            System.out.println("[NoDatabaseCallRunnable][dialNowWithRetry] Exhausted attempts. Throwing last err="
                    + safeMsg(last));
            throw last;
        }

        System.out.println("[NoDatabaseCallRunnable][dialNowWithRetry] END (no exception?) jobId=" + jobId);
    }

    private boolean isTimeoutExceptionMessage(Exception e) {
        String m = safeMsg(e).toLowerCase();
        return m.contains("timeout");
    }

    private String safeMsg(Exception e) {
        return (e == null || e.getMessage() == null) ? "" : e.getMessage();
    }

    // ---------------------------- Core logic ----------------------------

    void dialNow() throws Exception {
        LoggerUtils.log.debug("NoDatabaseCallRunnable");
        System.out.println("[NoDatabaseCallRunnable][dialNow] START jobId=" + jobId);

        // Determine the originate target and notification fields the same way as your existing logic
        final CallPlan plan = buildCallPlan();

        System.out.println("[NoDatabaseCallRunnable][dialNow] CallPlan built:"
                + " channelToCall=" + plan.channelToCall
                + " callerId=" + plan.callerId
                + " extensionToCall=" + plan.extensionToCall
                + " notifFromExt=" + plan.fromExtensionForNotification
                + " notifToPhone=" + plan.toPhoneNumberForNotification
                + " useStasis=" + useStasis()
                + " isCallOnMobile=" + isCallOnMobile
                + " pridictive=" + pridictive);

        // If Stasis app requested -> originate into Stasis instead of dialplan context/exten
        if (useStasis()) {
            LoggerUtils.log.debug("Stasis enabled. Originating into Stasis app: " + stasisAppName);
            System.out.println("[NoDatabaseCallRunnable][dialNow] PATH=STASIS stasisAppName=" + stasisAppName);

            // Build stasis args (CSV). Keep it simple; you can add/remove args as needed.
            // Example: jobId,org,fromExtension,toPhoneNumber,callType
            String stasisArgsCsv = buildStasisArgsCsv(plan.fromExtensionForNotification, plan.toPhoneNumberForNotification);
            System.out.println("[NoDatabaseCallRunnable][dialNow] stasisArgsCsv=" + stasisArgsCsv);

            System.out.println("[NoDatabaseCallRunnable][dialNow] Calling originateToStasis(...)");
            originateToStasis(
                    organization,
                    plan.channelToCall,
                    plan.callerId,
                    stasisAppName,
                    stasisArgsCsv,
                    timeOut,
                    true,
                    domain,
                    secondDomain,
                    useSecondaryLine,
                    plan.fromExtensionForNotification,
                    plan.toPhoneNumberForNotification
            );

            System.out.println("[NoDatabaseCallRunnable][dialNow] END PATH=STASIS jobId=" + jobId);
            return;
        }

        // If you are on mobile, you want SECOND LEG direct too => use AMI Application=Dial
        if (isCallOnMobile) {
            LoggerUtils.log.debug("Mobile mode -> using originateToDialDirect (both legs direct)");
            System.out.println("[NoDatabaseCallRunnable][dialNow] PATH=MOBILE_DIRECT (originateDialDirect)");

            System.out.println("[NoDatabaseCallRunnable][dialNow] Calling originateDialDirect(...)"
                    + " firstLeg(channelToCall)=" + plan.channelToCall
                    + " secondLeg(dialString)=" + plan.extensionToCall
                    + " dialTimeoutSeconds=60");

            originateDialDirect(
                    organization,
                    plan.channelToCall,       // first leg dialstring (already prepared)
                    plan.callerId,
                    plan.extensionToCall,     // second leg dialstring (already prepared)
                    60,                       // dial timeout seconds (change if you want)
                    timeOut,
                    true,
                    domain,
                    secondDomain,
                    useSecondaryLine,
                    plan.fromExtensionForNotification,
                    plan.toPhoneNumberForNotification
            );
            System.out.println("[NoDatabaseCallRunnable][dialNow] END PATH=MOBILE_DIRECT jobId=" + jobId);
            return;
        }

        // Otherwise keep old dialplan route behavior (FreePBX / from-internal / outbound routes)
        System.out.println("[NoDatabaseCallRunnable][dialNow] PATH=DIALPLAN (originateDialplan)"
                + " context=" + context
                + " exten=" + plan.extensionToCall
                + " priority=" + priority);

        originateDialplan(
                organization,
                plan.channelToCall,
                plan.callerId,
                context,
                plan.extensionToCall,
                priority,
                timeOut,
                true,
                domain,
                secondDomain,
                useSecondaryLine,
                plan.fromExtensionForNotification,
                plan.toPhoneNumberForNotification
        );

        System.out.println("[NoDatabaseCallRunnable][dialNow] END PATH=DIALPLAN jobId=" + jobId);
    }

    private boolean useStasis() {
        return stasisAppName != null && !stasisAppName.isBlank();
    }

    private CallPlan buildCallPlan() {
        System.out.println("[NoDatabaseCallRunnable][buildCallPlan] START"
                + " jobId=" + jobId
                + " pridictive=" + pridictive
                + " isCallOnMobile=" + isCallOnMobile
                + " phoneNumber=" + phoneNumber
                + " fromExtension=" + fromExtension
                + " fromPhoneNumber=" + fromPhoneNumber
                + " protocol=" + protocol
                + " phoneTrunk=" + phoneTrunk
                + " simSelectorRequired=" + simSelectorRequired
                + " simSelector=" + simSelector);

        final String callerId = firstName + " <" + phoneNumber + ">";

        // selector-applied numbers (only if simSelectorRequired=true)
        final String customerNum = applySimSelectorToNumber(phoneNumber);
        final String agentMobileNum = applySimSelectorToNumber(fromPhoneNumber);

        System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Numbers after selector:"
                + " customerNum=" + customerNum
                + " agentMobileNum=" + agentMobileNum
                + " shouldApplySimSelector=" + shouldApplySimSelector());

        if (pridictive) {
            // predictive: call customer first, then connect to agent
            if (!isCallOnMobile) {
                LoggerUtils.log.debug("Predictive -> Running Scheduled Call on Extension");
                System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Predictive + Extension mode");
                CallPlan p = new CallPlan(
                        trunkDial(customerNum),  // channelToCall (customer) via trunk
                        callerId,
                        fromExtension,           // extensionToCall (agent extension) via dialplan
                        fromExtension,
                        phoneNumber
                );
                System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Built plan:"
                        + " channelToCall=" + p.channelToCall
                        + " extensionToCall=" + p.extensionToCall);
                return p;
            } else {
                LoggerUtils.log.debug("Predictive -> Running Scheduled Call on Phone");
                System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Predictive + Mobile mode");
                CallPlan p = new CallPlan(
                        trunkDial(customerNum),      // channelToCall (customer) via trunk
                        callerId,
                        trunkDial(agentMobileNum),   // <-- IMPORTANT: 2nd leg must be direct dialstring
                        fromExtension,
                        phoneNumber
                );
                System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Built plan:"
                        + " channelToCall=" + p.channelToCall
                        + " extensionToCall=" + p.extensionToCall);
                return p;
            }
        } else {
            // normal: ring agent first, then dial customer
            if (!isCallOnMobile) {
                LoggerUtils.log.debug("Normal -> Running Scheduled Call on Extension");
                System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Normal + Extension mode");
                CallPlan p = new CallPlan(
                        protocol + fromExtension, // channelToCall (agent extension) internal
                        callerId,
                        phoneNumber,              // extensionToCall (customer) via dialplan/outbound routes
                        fromExtension,
                        phoneNumber
                );
                System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Built plan:"
                        + " channelToCall=" + p.channelToCall
                        + " extensionToCall=" + p.extensionToCall);
                return p;
            } else {
                LoggerUtils.log.debug("Normal -> Running Scheduled Call on Phone");
                System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Normal + Mobile mode");
                CallPlan p = new CallPlan(
                        trunkDial(agentMobileNum),   // channelToCall (agent mobile) via trunk
                        callerId,
                        trunkDial(customerNum),      // <-- IMPORTANT: 2nd leg must be direct dialstring
                        fromExtension,
                        phoneNumber
                );
                System.out.println("[NoDatabaseCallRunnable][buildCallPlan] Built plan:"
                        + " channelToCall=" + p.channelToCall
                        + " extensionToCall=" + p.extensionToCall);
                return p;
            }
        }
    }

    private String buildStasisArgsCsv(String fromExt, String toNumber) {
        // CSV values must not contain commas; if they might, sanitize/replace.
        // Keep it minimal but useful for ARI.
        List<String> parts = new ArrayList<>();
        parts.add(nullToEmpty(jobId));
        parts.add(nullToEmpty(organization));
        parts.add(nullToEmpty(fromExt));
        parts.add(nullToEmpty(toNumber));
        parts.add(nullToEmpty(callType));
        String out = String.join(",", parts);
        System.out.println("[NoDatabaseCallRunnable][buildStasisArgsCsv] out=" + out);
        return out;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    // ---------------------------- Originate (Dialplan) ----------------------------

    public ManagerResponse originateDialDirect(
            String organization,
            String channelToCall,
            String callerID,
            String dialString,
            int dialTimeoutSeconds,
            Long timeOut,
            boolean async,
            String domain,
            String secondDomain,
            boolean useSecondaryLine,
            String fromExtension,
            String toPhoneNumber
    ) throws Exception {

        LoggerUtils.log.debug("originateDialDirect");
        System.out.println("[NoDatabaseCallRunnable][originateDialDirect] START"
                + " org=" + organization
                + " domain=" + domain
                + " secondDomain=" + secondDomain
                + " useSecondaryLine=" + useSecondaryLine
                + " channelToCall=" + channelToCall
                + " callerID=" + callerID
                + " dialString=" + dialString
                + " dialTimeoutSeconds=" + dialTimeoutSeconds
                + " timeOut=" + timeOut
                + " async=" + async);

        ManagerResponse response;

        ConnectionStream connectionStream = getConnectionStream();
        ManagerStream managerStream = getManagerStream();

        ManagerConnection managerConnection = connectionStream.getConnection(domain, secondDomain, useSecondaryLine);

        if (managerConnection == null) {
            System.out.println("[NoDatabaseCallRunnable][originateDialDirect] AMI Connection null -> refreshing backend");
            refreshAmiBackend();
            managerConnection = connectionStream.getConnection(domain, secondDomain, useSecondaryLine);
            if (managerConnection == null) {
                System.out.println("[NoDatabaseCallRunnable][originateDialDirect] AMI Connection still null after refresh");
                throw new Exception("AMI Connection not found.");
            }
            System.out.println("[NoDatabaseCallRunnable][originateDialDirect] Logging in to AMI after refresh");
            connectionStream.login(managerConnection);
        }

        System.out.println("[NoDatabaseCallRunnable][originateDialDirect] Calling managerStream.originateToDialDirect(...)");
        response = managerStream.originateToDialDirect(
                organization,
                channelToCall,
                callerID,
                dialString,
                dialTimeoutSeconds,
                timeOut,
                async,
                managerConnection
        );
        System.out.println("[NoDatabaseCallRunnable][originateDialDirect] Originate response=" + response);

        trySendCallNotification(domain, organization, fromExtension, toPhoneNumber);
        System.out.println("[NoDatabaseCallRunnable][originateDialDirect] END");
        return response;
    }

    public ManagerResponse originateDialplan(
            String organization,
            String channelToCall,
            String callerID,
            String context,
            String extensionToCall,
            int priority,
            Long timeOut,
            boolean async,
            String domain,
            String secondDomain,
            boolean useSecondaryLine,
            String fromExtension,
            String toPhoneNumber
    ) throws Exception {

        LoggerUtils.log.debug("originateDialplan");
        System.out.println("[NoDatabaseCallRunnable][originateDialplan] START"
                + " org=" + organization
                + " domain=" + domain
                + " secondDomain=" + secondDomain
                + " useSecondaryLine=" + useSecondaryLine
                + " channelToCall=" + channelToCall
                + " callerID=" + callerID
                + " context=" + context
                + " extensionToCall=" + extensionToCall
                + " priority=" + priority
                + " timeOut=" + timeOut
                + " async=" + async);

        ManagerResponse response = null;

        // Prefer Spring beans when possible; if not beans, keep old behavior but via applicationContext if available.
        ConnectionStream connectionStream = getConnectionStream();
        ManagerStream managerStream = getManagerStream();

        ManagerConnection managerConnection = connectionStream.getConnection(domain, secondDomain, useSecondaryLine);

        if (managerConnection == null) {
            System.out.println("[NoDatabaseCallRunnable][originateDialplan] AMI Connection null -> reinitiateAfterRefreshConnectionDialplan");
            response = reinitiateAfterRefreshConnectionDialplan(
                    organization, channelToCall, callerID, context, extensionToCall, priority, timeOut, async,
                    domain, secondDomain, useSecondaryLine, fromExtension, toPhoneNumber
            );
            System.out.println("[NoDatabaseCallRunnable][originateDialplan] END (via refresh path) response=" + response);
            return response;
        }

        try {
            LoggerUtils.log.debug("Originating dialplan call now");
            System.out.println("[NoDatabaseCallRunnable][originateDialplan] Calling managerStream.originateCall(...)");
            response = managerStream.originateCall(
                    organization, channelToCall, callerID, context, extensionToCall, priority, timeOut, async, managerConnection
            );
            System.out.println("[NoDatabaseCallRunnable][originateDialplan] Originate response=" + response);

            trySendCallNotification(domain, organization, fromExtension, toPhoneNumber);
            System.out.println("[NoDatabaseCallRunnable][originateDialplan] END (normal path)");
            return response;

        } catch (Exception e) {
            System.out.println("[NoDatabaseCallRunnable][originateDialplan] Exception -> " + safeMsg(e));
            e.printStackTrace();
            response = reinitiateAfterRefreshConnectionDialplan(
                    organization, channelToCall, callerID, context, extensionToCall, priority, timeOut, async,
                    domain, secondDomain, useSecondaryLine, fromExtension, toPhoneNumber
            );
            System.out.println("[NoDatabaseCallRunnable][originateDialplan] END (after refresh retry) response=" + response);
            return response;
        }
    }

    // ---------------------------- Originate (Stasis) ----------------------------

    public ManagerResponse originateToStasis(
            String organization,
            String channelToCall,
            String callerID,
            String stasisAppName,
            String stasisArgsCsv,
            Long timeOut,
            boolean async,
            String domain,
            String secondDomain,
            boolean useSecondaryLine,
            String fromExtension,
            String toPhoneNumber
    ) throws Exception {

        LoggerUtils.log.debug("originateToStasis");
        System.out.println("[NoDatabaseCallRunnable][originateToStasis] START"
                + " org=" + organization
                + " domain=" + domain
                + " secondDomain=" + secondDomain
                + " useSecondaryLine=" + useSecondaryLine
                + " channelToCall=" + channelToCall
                + " callerID=" + callerID
                + " stasisAppName=" + stasisAppName
                + " stasisArgsCsv=" + stasisArgsCsv
                + " timeOut=" + timeOut
                + " async=" + async);

        ManagerResponse response = null;

        ConnectionStream connectionStream = getConnectionStream();
        ManagerStream managerStream = getManagerStream();

        ManagerConnection managerConnection = connectionStream.getConnection(domain, secondDomain, useSecondaryLine);

        if (managerConnection == null) {
            System.out.println("[NoDatabaseCallRunnable][originateToStasis] AMI Connection null -> reinitiateAfterRefreshConnectionStasis");
            response = reinitiateAfterRefreshConnectionStasis(
                    organization, channelToCall, callerID, stasisAppName, stasisArgsCsv, timeOut, async,
                    domain, secondDomain, useSecondaryLine, fromExtension, toPhoneNumber
            );
            System.out.println("[NoDatabaseCallRunnable][originateToStasis] END (via refresh path) response=" + response);
            return response;
        }

        try {
            LoggerUtils.log.debug("Originating stasis call now");
            System.out.println("[NoDatabaseCallRunnable][originateToStasis] Calling managerStream.originateToStasis(...)");
            response = managerStream.originateToStasis(
                    organization,
                    channelToCall,
                    callerID,
                    stasisAppName,
                    stasisArgsCsv,
                    timeOut,
                    async,
                    managerConnection
            );
            System.out.println("[NoDatabaseCallRunnable][originateToStasis] Originate response=" + response);

            trySendCallNotification(domain, organization, fromExtension, toPhoneNumber);
            System.out.println("[NoDatabaseCallRunnable][originateToStasis] END (normal path)");
            return response;

        } catch (Exception e) {
            System.out.println("[NoDatabaseCallRunnable][originateToStasis] Exception -> " + safeMsg(e));
            e.printStackTrace();
            response = reinitiateAfterRefreshConnectionStasis(
                    organization, channelToCall, callerID, stasisAppName, stasisArgsCsv, timeOut, async,
                    domain, secondDomain, useSecondaryLine, fromExtension, toPhoneNumber
            );
            System.out.println("[NoDatabaseCallRunnable][originateToStasis] END (after refresh retry) response=" + response);
            return response;
        }
    }

    // ---------------------------- Refresh + retry (Dialplan) ----------------------------

    private ManagerResponse reinitiateAfterRefreshConnectionDialplan(
            String organization,
            String channelToCall,
            String callerID,
            String context,
            String extensionToCall,
            int priority,
            Long timeOut,
            boolean async,
            String domain,
            String secondDomain,
            boolean useSecondaryLine,
            String fromExtension,
            String toPhoneNumber
    ) throws Exception {

        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionDialplan] START"
                + " org=" + organization
                + " domain=" + domain
                + " secondDomain=" + secondDomain
                + " useSecondaryLine=" + useSecondaryLine
                + " channelToCall=" + channelToCall
                + " callerID=" + callerID
                + " context=" + context
                + " extensionToCall=" + extensionToCall
                + " priority=" + priority
                + " timeOut=" + timeOut
                + " async=" + async);

        ManagerResponse response;

        refreshAmiBackend();

        ConnectionStream connectionStream = getConnectionStream();
        ManagerStream managerStream = getManagerStream();

        ManagerConnection managerConnection = connectionStream.getConnection(domain, secondDomain, useSecondaryLine);
        if (managerConnection == null) {
            System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionDialplan] AMI Connection still null after refresh");
            throw new Exception("AMI Connection not found.");
        }

        // Ensure login after refresh
        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionDialplan] Logging in to AMI after refresh");
        connectionStream.login(managerConnection);

        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionDialplan] Calling managerStream.originateCall(...)");
        response = managerStream.originateCall(
                organization, channelToCall, callerID, context, extensionToCall, priority, timeOut, async, managerConnection
        );
        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionDialplan] Originate response=" + response);

        trySendCallNotification(domain, organization, fromExtension, toPhoneNumber);
        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionDialplan] END");
        return response;
    }

    // ---------------------------- Refresh + retry (Stasis) ----------------------------

    private ManagerResponse reinitiateAfterRefreshConnectionStasis(
            String organization,
            String channelToCall,
            String callerID,
            String stasisAppName,
            String stasisArgsCsv,
            Long timeOut,
            boolean async,
            String domain,
            String secondDomain,
            boolean useSecondaryLine,
            String fromExtension,
            String toPhoneNumber
    ) throws Exception {

        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionStasis] START"
                + " org=" + organization
                + " domain=" + domain
                + " secondDomain=" + secondDomain
                + " useSecondaryLine=" + useSecondaryLine
                + " channelToCall=" + channelToCall
                + " callerID=" + callerID
                + " stasisAppName=" + stasisAppName
                + " stasisArgsCsv=" + stasisArgsCsv
                + " timeOut=" + timeOut
                + " async=" + async);

        ManagerResponse response;

        refreshAmiBackend();

        ConnectionStream connectionStream = getConnectionStream();
        ManagerStream managerStream = getManagerStream();

        ManagerConnection managerConnection = connectionStream.getConnection(domain, secondDomain, useSecondaryLine);
        if (managerConnection == null) {
            System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionStasis] AMI Connection still null after refresh");
            throw new Exception("AMI Connection not found.");
        }

        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionStasis] Logging in to AMI after refresh");
        connectionStream.login(managerConnection);

        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionStasis] Calling managerStream.originateToStasis(...)");
        response = managerStream.originateToStasis(
                organization,
                channelToCall,
                callerID,
                stasisAppName,
                stasisArgsCsv,
                timeOut,
                async,
                managerConnection
        );
        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionStasis] Originate response=" + response);

        trySendCallNotification(domain, organization, fromExtension, toPhoneNumber);
        System.out.println("[NoDatabaseCallRunnable][reinitiateAfterRefreshConnectionStasis] END");
        return response;
    }

    private void refreshAmiBackend() throws Exception {
        System.out.println("[NoDatabaseCallRunnable][refreshAmiBackend] START");
        ErrorRepository errorRepository = applicationContext.getBean(ErrorRepository.class);

        RefreshBackEndConnectionRunnable refreshBackEndConnectionRunnable = new RefreshBackEndConnectionRunnable();
        refreshBackEndConnectionRunnable.setErrorRepository(errorRepository);
        refreshBackEndConnectionRunnable.setApplicationContext(applicationContext);
        refreshBackEndConnectionRunnable.execute("AMI");
        System.out.println("[NoDatabaseCallRunnable][refreshAmiBackend] END");
    }

    // ---------------------------- Notifications ----------------------------

    private void trySendCallNotification(String domain, String organization, String fromExtension, String toPhoneNumber) {
        System.out.println("[NoDatabaseCallRunnable][trySendCallNotification] START domain=" + domain
                + " org=" + organization
                + " fromExt=" + fromExtension
                + " toPhone=" + toPhoneNumber);
        try {
            sendCallNotifications("call-notification", domain, organization, fromExtension, toPhoneNumber);
            System.out.println("[NoDatabaseCallRunnable][trySendCallNotification] SENT");
        } catch (Exception e) {
            System.out.println("[NoDatabaseCallRunnable][trySendCallNotification] FAILED err=" + safeMsg(e));
            e.printStackTrace();
        }
    }

    /**
     * The purpose of the method is to send notifications
     */
    public void sendCallNotifications(String type, String domain, String organization, String fromExtension, String toPhoneNumber) throws Exception {

        System.out.println("[NoDatabaseCallRunnable][sendCallNotifications] START type=" + type
                + " domain=" + domain
                + " org=" + organization
                + " fromExt=" + fromExtension
                + " toPhone=" + toPhoneNumber);

        if (fromExtension == null || fromExtension.isBlank()) {
            System.out.println("[NoDatabaseCallRunnable][sendCallNotifications] SKIP save: fromExtension is null");
            return; // or set a safe value like "SYSTEM"
        }

        ObjectMapper mapper = new ObjectMapper();
        BotInputDTO msg = new BotInputDTO();
        Notification msgNotification = new Notification();
        List<Notification> allNotifications = new ArrayList<>();

        switch (type) {

            case "call-notification":

                msgNotification.setCreationDate(new Date());
                msgNotification.setAlertType("alert-info");
                msgNotification.setForExtension(fromExtension);

                msgNotification.setNotificationType("call");
                msgNotification.setOrganization(organization);
                msgNotification.setTitle("Triggered!");
                allNotifications.add(msgNotification);

                msg.setDomain(domain);
                msg.setExtension(fromExtension);
                msg.setFormat("json");
                try {
                    msg.setMessage(mapper.writeValueAsString(msgNotification));
                } catch (JsonProcessingException e) {
                    System.out.println("[NoDatabaseCallRunnable][sendCallNotifications] JSON serialize FAILED err=" + safeMsg(e));
                    e.printStackTrace();
                }

                msg.setMessagetype("notification");
                msg.setOrganization(organization);

                try {
                    System.out.println("[NoDatabaseCallRunnable][sendCallNotifications] STOMP send -> /mylinehub/sendcalldetails");
                    MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
                    System.out.println("[NoDatabaseCallRunnable][sendCallNotifications] STOMP sent");
                } catch (Exception e) {
                    System.out.println("[NoDatabaseCallRunnable][sendCallNotifications] STOMP send FAILED err=" + safeMsg(e));
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }

        System.out.println("[NoDatabaseCallRunnable][sendCallNotifications] Saving notificationRepository.saveAll size=" + allNotifications.size());
        notificationRepository.saveAll(allNotifications);
        System.out.println("[NoDatabaseCallRunnable][sendCallNotifications] END");
    }

    // ---------------------------- Small helpers ----------------------------

    /**
     * Prefer beans if available. If ConnectionStream is not a bean in your project,
     * this still works by falling back to `new`.
     */
    private ConnectionStream getConnectionStream() {
        try {
            ConnectionStream cs = applicationContext.getBean(ConnectionStream.class);
            System.out.println("[NoDatabaseCallRunnable][getConnectionStream] Using Spring bean");
            return cs;
        } catch (Exception ignored) {
            System.out.println("[NoDatabaseCallRunnable][getConnectionStream] Using new ConnectionStream()");
            return new ConnectionStream();
        }
    }

    /**
     * Prefer beans if available. If ManagerStream is not a bean in your project,
     * this still works by falling back to `new`.
     */
    private ManagerStream getManagerStream() {
        try {
            ManagerStream ms = applicationContext.getBean(ManagerStream.class);
            System.out.println("[NoDatabaseCallRunnable][getManagerStream] Using Spring bean");
            return ms;
        } catch (Exception ignored) {
            System.out.println("[NoDatabaseCallRunnable][getManagerStream] Using new ManagerStream()");
            return new ManagerStream();
        }
    }

    private static class CallPlan {
        final String channelToCall;
        final String callerId;
        final String extensionToCall;

        // for notifications
        final String fromExtensionForNotification;
        final String toPhoneNumberForNotification;

        CallPlan(String channelToCall, String callerId, String extensionToCall,
                 String fromExtensionForNotification, String toPhoneNumberForNotification) {
            this.channelToCall = channelToCall;
            this.callerId = callerId;
            this.extensionToCall = extensionToCall;
            this.fromExtensionForNotification = fromExtensionForNotification;
            this.toPhoneNumberForNotification = toPhoneNumberForNotification;
        }
    }

    private boolean shouldApplySimSelector() {
        boolean ok = simSelectorRequired && simSelector != null && !simSelector.trim().isEmpty();
        System.out.println("[NoDatabaseCallRunnable][shouldApplySimSelector] simSelectorRequired=" + simSelectorRequired
                + " simSelector=" + simSelector + " -> " + ok);
        return ok;
    }

    /**
     * Applies selector ONLY to the NUMBER part, NOT to protocol/trunk.
     * Example:
     *   simSelector="10000001", phone="9711761156" => "100000019711761156"
     *
     * No '+' is added. Whatever user set in simSelector is used as-is.
     */
    private String applySimSelectorToNumber(String number) {
        if (number == null) return null;
        if (!shouldApplySimSelector()) return number;

        String sel = simSelector.trim();
        String n = number.trim();

        if (n.startsWith(sel)) return n; // avoid double-prefix
        String out = sel + n;
        System.out.println("[NoDatabaseCallRunnable][applySimSelectorToNumber] sel=" + sel + " number=" + n + " -> " + out);
        return out;
    }

    private String trunkDial(String number) {
        // builds: protocol + number + "@" + phoneTrunk
        String out = protocol + number + "@" + phoneTrunk;
        System.out.println("[NoDatabaseCallRunnable][trunkDial] number=" + number + " -> " + out);
        return out;
    }

}
