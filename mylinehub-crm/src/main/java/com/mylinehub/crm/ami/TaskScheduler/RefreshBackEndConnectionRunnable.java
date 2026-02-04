package com.mylinehub.crm.ami.TaskScheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.ami.ConnectionStream;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.AmiConnectionDTO;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.entity.dto.SshConnectionDTO;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.service.AMIConnectionService;
import com.mylinehub.crm.service.SshConnectionService;
import com.mylinehub.crm.utils.LoggerUtils;
import com.mylinehub.crm.ws.client.STOMPClient;
import com.mylinehub.shh.SshWrapper;

import lombok.Data;

@Data
public class RefreshBackEndConnectionRunnable implements Runnable {

    ErrorRepository errorRepository;
    ApplicationContext applicationContext;
    String jobId;

    // Non-fair lock is fine, but we never WAIT on it (skip instead)
    private static final ReentrantLock lock = new ReentrantLock(false);

    // Prevent overlapping runs
    private static final AtomicBoolean RUN_IN_PROGRESS = new AtomicBoolean(false);

    @Override
    public void run() {
        try {
            if (!RUN_IN_PROGRESS.compareAndSet(false, true)) {
                LoggerUtils.log.debug("RefreshBackEndConnectionRunnable skipped (previous run still in progress) jobId={}", jobId);
                return;
            }

            // Default: refresh everything
            execute("ALL");

        } catch (Exception e) {
            LoggerUtils.log.error("RefreshBackEndConnectionRunnable error jobId={} msg={}", jobId, String.valueOf(e.getMessage()), e);
        } finally {
            RUN_IN_PROGRESS.set(false);
        }
    }

    /**
     * Non-blocking refresh:
     * - If someone else is refreshing, this call returns immediately.
     * - Caller can pass what to refresh: "ALL", "WS", "AMI", "SSH" or comma-separated: "WS,AMI"
     */
    public void execute(String refreshWhat) {
        if (applicationContext == null) {
            LoggerUtils.log.error("RefreshBackEndConnectionRunnable applicationContext is null jobId={}", jobId);
            return;
        }

        // SKIP immediately if busy (no waiting, no sleeping)
        if (!lock.tryLock()) {
            LoggerUtils.log.debug("RefreshBackEndConnectionRunnable skipped (lock busy) jobId={} refreshWhat={}",
                    jobId, String.valueOf(refreshWhat));
            return;
        }

        try {
            RefreshFlags flags = RefreshFlags.parse(refreshWhat);

            if (flags.websocket) {
                try {
                    setWebSocketConnection(errorRepository, applicationContext);
                } catch (Exception e) {
                    LoggerUtils.log.error("RefreshBackEndConnectionRunnable websocket refresh failed jobId={} msg={}",
                            jobId, String.valueOf(e.getMessage()), e);
                }
            }

            if (flags.ami) {
                try {
                    setAmiConnections(errorRepository, applicationContext);
                } catch (Exception e) {
                    LoggerUtils.log.error("RefreshBackEndConnectionRunnable AMI refresh failed jobId={} msg={}",
                            jobId, String.valueOf(e.getMessage()), e);
                }
            }

            if (flags.ssh) {
                try {
                    setSshConnections(errorRepository, applicationContext);
                } catch (Exception e) {
                    LoggerUtils.log.error("RefreshBackEndConnectionRunnable SSH refresh failed jobId={} msg={}",
                            jobId, String.valueOf(e.getMessage()), e);
                }
            }

        } finally {
            lock.unlock();
        }
    }


    public static void setInitialExtensionStates(ErrorRepository errorRepository, ApplicationContext applicationContext, List<Employee> allEmployees) {
        String state = "terminated";
        String presence = "danger";
        String dotClass = "dotOffline";

        // Each employee gets its own list instance (no shared mutable list reference)
        allEmployees.forEach(employee -> {
            List<String> combinedValue = new ArrayList<>();
            combinedValue.add(state);
            combinedValue.add(presence);
            combinedValue.add(dotClass);

            EmployeeDataAndStateDTO current = new EmployeeDataAndStateDTO();
            current.setEmployee(employee);
            current.setMemberState(combinedValue);
            current.setExtensionState(combinedValue);
            current.setRunningCamapignId(-1L);

            EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), current, "update");

            if ((employee.getPhonenumber() != null) && (!employee.getPhonenumber().isEmpty())) {
                EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(employee.getPhonenumber(), employee.getExtension(), "update");
            }
            
            if ((employee.getEmail() != null) && (!employee.getEmail().isEmpty())) {
                EmployeeDataAndState.workOnAllEmployeeEmailAndExtension(
                        employee.getEmail().trim().toLowerCase(),
                        employee.getExtension(),
                        "update"
                );
            }

        });
    }

    /**
     * IMPORTANT: no locking here. execute(...) holds the lock.
     */
    static void setWebSocketConnection(ErrorRepository errorRepository, ApplicationContext applicationContext) throws Exception {
        if (applicationContext == null) {
            LoggerUtils.log.error("setWebSocketConnection applicationContext is null");
            return;
        }

        String socketURL = applicationContext.getEnvironment().getProperty("spring.websocket.connect");
        STOMPClient client = new STOMPClient();
        LoggerUtils.log.info("Initializing all Stomp Session");
        client.createConnection(applicationContext, socketURL);
    }

    /**
     * IMPORTANT: no locking here. execute(...) holds the lock.
     */
    static void setAmiConnections(ErrorRepository errorRepository, ApplicationContext applicationContext) {
        if (applicationContext == null) {
            LoggerUtils.log.error("setAmiConnections applicationContext is null");
            return;
        }

        AMIConnectionService amiConnectionService = applicationContext.getBean(AMIConnectionService.class);
        LoggerUtils.log.debug("setAmiConnections");

        List<AmiConnectionDTO> amiConnections = amiConnectionService.getAllAmiConnectionsOnIsEnabled(true);
        ConnectionStream connectionStream = new ConnectionStream();

        amiConnections.forEach((amiConnection) -> {
            try {
                LoggerUtils.log.debug("Creating AMI Connection and adding listner org={} domain={}",
                        amiConnection.getOrganization(), amiConnection.getDomain());

                ManagerConnection managerConnection = connectionStream.createConnection(
                        amiConnection.getOrganization(),
                        amiConnection.getDomain(),
                        amiConnection.getAmiuser(),
                        amiConnection.getPassword(),
                        applicationContext
                );

                LoggerUtils.log.debug("Login AMI org={} domain={}", amiConnection.getOrganization(), amiConnection.getDomain());
//                connectionStream.login(managerConnection);
//                LoggerUtils.log.debug("Login AMI successful org={} domain={}", amiConnection.getOrganization(), amiConnection.getDomain());

            } catch (Exception e) {
                LoggerUtils.log.error("Error while connecting AMI org={} domain={} msg={}",
                        amiConnection.getOrganization(),
                        amiConnection.getDomain(),
                        String.valueOf(e.getMessage()),
                        e);
            }
        });
    }

    /**
     * IMPORTANT: no locking here. execute(...) holds the lock.
     */
    static void setSshConnections(ErrorRepository errorRepository, ApplicationContext applicationContext) {
        if (applicationContext == null) {
            LoggerUtils.log.error("setSshConnections applicationContext is null");
            return;
        }

        SshConnectionService sshConnectionService = applicationContext.getBean(SshConnectionService.class);
        List<SshConnectionDTO> sshConnections = sshConnectionService.getAllsshConnectionsOnIsEnabled(true);
        SshWrapper sshWrapper = new SshWrapper();

        LoggerUtils.log.debug("Setting all ssh connections");

        sshConnections.forEach((sshConnection) -> {
            SshConnectionDTO current = sshConnection;

            if (current.getPassword() != null) {
                LoggerUtils.log.debug("SSH connect using password org={} domain={}", current.getOrganization(), current.getDomain());
                try {
                    sshWrapper.configureOrGetChannelUsingPassword(
                            current.getOrganization(),
                            current.getPassword(),
                            current.getSshUser(),
                            current.getDomain(),
                            applicationContext
                    );
                } catch (IllegalArgumentException | IllegalStateException | IOException | TimeoutException e) {
                    LoggerUtils.log.error("SSH password connect error org={} domain={} msg={}",
                            current.getOrganization(), current.getDomain(), String.valueOf(e.getMessage()), e);
                }
            } else {
                LoggerUtils.log.debug("SSH connect using pem org={} domain={} pem={}",
                        current.getOrganization(), current.getDomain(), current.getPemFileName());
                try {
                    sshWrapper.configureOrGetChannelUsingPem(
                            current.getOrganization(),
                            current.getPemFileName(),
                            current.getSshUser(),
                            current.getDomain(),
                            applicationContext
                    );
                } catch (IllegalArgumentException | IllegalStateException | IOException | TimeoutException e) {
                    LoggerUtils.log.error("SSH pem connect error org={} domain={} msg={}",
                            current.getOrganization(), current.getDomain(), String.valueOf(e.getMessage()), e);
                }
            }
        });
    }

    /**
     * Small helper: parse what to refresh.
     * Supported values: ALL, WS/WEBSOCKET, AMI, SSH; comma-separated.
     * Anything unknown => defaults to ALL.
     */
    private static final class RefreshFlags {
        final boolean websocket;
        final boolean ami;
        final boolean ssh;

        private RefreshFlags(boolean websocket, boolean ami, boolean ssh) {
            this.websocket = websocket;
            this.ami = ami;
            this.ssh = ssh;
        }

        static RefreshFlags parse(String refreshWhat) {
            if (refreshWhat == null || refreshWhat.trim().isEmpty()) {
                return new RefreshFlags(true, true, true);
            }

            String v = refreshWhat.trim().toUpperCase(Locale.ROOT);
            if ("ALL".equals(v) || "*".equals(v)) {
                return new RefreshFlags(true, true, true);
            }

            boolean ws = false, ami = false, ssh = false;

            String[] parts = v.split(",");
            for (String p : parts) {
                String token = p.trim();
                if (token.isEmpty()) continue;

                if ("WS".equals(token) || "WEBSOCKET".equals(token) || "SOCKET".equals(token) || "STOMP".equals(token)) {
                    ws = true;
                } else if ("AMI".equals(token)) {
                    ami = true;
                } else if ("SSH".equals(token) || "SFTP".equals(token)) {
                    ssh = true;
                } else if ("ALL".equals(token) || "*".equals(token)) {
                    ws = true; ami = true; ssh = true;
                } else {
                    // Unknown token: safest is refresh all (keeps behavior predictable)
                    ws = true; ami = true; ssh = true;
                }
            }

            // If user passed something like "WS," ensure at least one flag is true
            if (!ws && !ami && !ssh) {
                ws = true; ami = true; ssh = true;
            }

            return new RefreshFlags(ws, ami, ssh);
        }
    }
}
