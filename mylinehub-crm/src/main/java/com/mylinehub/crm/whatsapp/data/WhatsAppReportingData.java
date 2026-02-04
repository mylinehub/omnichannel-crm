package com.mylinehub.crm.whatsapp.data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.dto.OrganizationWorkingDTO;
import com.mylinehub.crm.whatsapp.dto.WhatsAppExtensionReportingDTO;
import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppReportDataParameterDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppChatHistory;
import com.mylinehub.crm.whatsapp.entity.WhatsAppNumberReport;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;
import com.mylinehub.crm.whatsapp.enums.MESSAGING_PRODUCT;
import com.mylinehub.crm.whatsapp.repository.WhatsAppNumberReportRepository;
import com.mylinehub.crm.whatsapp.service.WhatsAppNumberReportService;

public class WhatsAppReportingData {

    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    // String key = whatsAppReportDataParameterDTO.getPhoneNumberMain()+delimiter+whatsAppReportDataParameterDTO.getPhoneNumberWith();
    private static Map<String, WhatsAppNumberReport> whatsAppReportDataMap = new ConcurrentHashMap<>();
    private static Map<String, WhatsAppNumberReport> backupWhatsAppReportDataMap = new ConcurrentHashMap<>();
    public static String delimiter = "~";
    public static Date lastPutToDatabase = new Date();

    // Locks for WhatsApp report data
    private static final ReentrantLock lockWhatsAppReportDataMap = new ReentrantLock(false);
    private static final ReentrantLock lockBackupWhatsAppReportDataMap = new ReentrantLock(false);

    // =======================
    // Main method to work with report map data
    // =======================
    public static Map<String, WhatsAppNumberReport> workWithWhatsAppReportMapData(WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO) {
        Map<String, WhatsAppNumberReport> toReturn = null;
        String action = whatsAppReportDataParameterDTO.getAction();
        String key = whatsAppReportDataParameterDTO.getPhoneNumberMain() + delimiter + whatsAppReportDataParameterDTO.getPhoneNumberWith();

        while (true) { // retry loop until lock is acquired
            int queueLength = lockWhatsAppReportDataMap.getQueueLength();
            long timeout = BASE_TIMEOUT_SECONDS + queueLength;

            try {
                if (lockWhatsAppReportDataMap.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        // === Original  block starts here ===
                        // System.out.println("[REPORT-MAP] Action: " + action + ", Key: " + key);

                        WhatsAppNumberReport current = null;

                        switch (action) {
                            case "get-one":
                                current = verifyAndFetchCurrentOrElseAddNewObjectForToday(whatsAppReportDataParameterDTO);
                                if (current != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(key, current);
                                    // System.out.println("[REPORT-MAP] get-one success for key: " + key);
                                } else {
                                    // System.out.println("[REPORT-MAP] get-one returned null for key: " + key);
                                }
                                break;

                            case "get-all":
                                // IMPORTANT CHANGE: do NOT return internal ConcurrentHashMap reference
                                // (no business logic change; just defensive copy at boundary)
                                toReturn = snapshotReportMap(whatsAppReportDataMap);
                                // System.out.println("[REPORT-MAP] Returned full map. Size: " + (toReturn != null ? toReturn.size() : 0));
                                break;

                            case "set-new":
                                // Resetting main map using ConcurrentHashMap for thread safety
                                whatsAppReportDataMap = new ConcurrentHashMap<>();
                                // System.out.println("[REPORT-MAP] Initialized new ConcurrentHashMap.");
                                break;

                            case "update-received-stats":
                                updateReceivedMessageStats(whatsAppReportDataParameterDTO);
                                // System.out.println("[REPORT-MAP] update-received-stats completed.");
                                break;

                            case "update-sent-stats":
                                updateSendMessageStats(whatsAppReportDataParameterDTO);
                                // System.out.println("[REPORT-MAP] update-send-stats completed.");
                                break;

                            case "update-delivered-stats":
                            	
                            	long amount = 0L;
                            	if(whatsAppReportDataParameterDTO.getAmount() != null)
                            	{
                            		amount = whatsAppReportDataParameterDTO.getAmount();
                            	}
                            	else {
                            		amount = whatsAppReportDataParameterDTO.getPhoneNumber().getCostPerOutboundMessage();
                            	}
                            	
                                updateDeliveredMessageStats(whatsAppReportDataParameterDTO,amount);
                                // System.out.println("[REPORT-MAP] update-delivered-stats completed.");
                                break;

                            case "update-failed-stats":
                                updateFailedMessageStats(whatsAppReportDataParameterDTO);
                                // System.out.println("[REPORT-MAP] update-failed-stats completed.");
                                break;

                            case "update-deleted-stats":
                                updateDeletedMessageStats(whatsAppReportDataParameterDTO);
                                // System.out.println("[REPORT-MAP] update-deleted-stats completed.");
                                break;

                            case "update-read-stats":
                                updateReadMessageStats(whatsAppReportDataParameterDTO);
                                // System.out.println("[REPORT-MAP] update-read-stats completed.");
                                break;

                            case "send-to-backup":
                                if (isOlderThan5Minutes(lastPutToDatabase)) {
                                    // lock both maps in a consistent order: main -> backup
                                    lockBackupWhatsAppReportDataMap.lock();
                                    try {
                                        // COPY main into backup (do not alias)
                                        backupWhatsAppReportDataMap = new ConcurrentHashMap<>(whatsAppReportDataMap);

                                        // reset main map for fresh accumulation
                                        whatsAppReportDataMap = new ConcurrentHashMap<>();

                                        // (optional) if you still want to clean main, do it BEFORE copying; after reset it's empty anyway
                                        // removeNonTodayReports();
                                    } finally {
                                        lockBackupWhatsAppReportDataMap.unlock();
                                    }
                                }
                                break;


                            default:
                                // System.out.println("[REPORT-MAP] Unknown action: " + action);
                                break;
                        }
                        // === Original  block ends here ===

                    } finally {
                        lockWhatsAppReportDataMap.unlock();
                    }
                    break; // exit retry loop after success
                } else {
                    // Could not acquire lock, wait a short period and retry
                    System.out.println("[INFO] Could not acquire lockWhatsAppReportDataMap, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workWithWhatsAppReportMapData");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.err.println("[ERROR] Exception in workWithWhatsAppReportMapData for action: " + action);
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

    public static boolean isOlderThan5Minutes(Date lastPutToDatabase) {
        if (lastPutToDatabase == null) {
            return true; // treat null as "too old"
        }

        long now = System.currentTimeMillis();
        long lastUpdate = lastPutToDatabase.getTime();
        long diffMillis = now - lastUpdate;

        // 15 minutes in milliseconds
        long fifteenMinutes = 5 * 60 * 1000;

        return diffMillis >= fifteenMinutes;
    }

    public static void removeNonTodayReports() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        whatsAppReportDataMap.entrySet().removeIf(entry -> {
            WhatsAppNumberReport report = entry.getValue();
            if (report == null || report.getDayUpdated() == null) return true;

            LocalDate reportDay = report.getDayUpdated().toInstant().atZone(zone).toLocalDate();
            return !reportDay.equals(today);
        });
    }


    public static Map<String, WhatsAppNumberReport> workWithBackupWhatsAppReportMapData(WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO) {
        Map<String, WhatsAppNumberReport> toReturn = null;
        String action = whatsAppReportDataParameterDTO.getAction();

        boolean acquired = false;
        while (!acquired) {
            try {
                // Calculate timeout dynamically: BASE_TIMEOUT_SECONDS + lock queue length
                long timeout = BASE_TIMEOUT_SECONDS + lockBackupWhatsAppReportDataMap.getQueueLength();
                acquired = lockBackupWhatsAppReportDataMap.tryLock(timeout, TimeUnit.SECONDS);

                if (!acquired) {
                    System.out.println("[BACKUP-REPORT-MAP] Could not acquire lock, retrying...");
                    continue; // retry loop
                }

                // Lock acquired, execute the original logic
                switch (action) {
                    case "send-backup-to-database":
                        if (isOlderThan5Minutes(lastPutToDatabase)) {
                            WhatsAppNumberReportRepository repository = whatsAppReportDataParameterDTO.getWhatsAppNumberReportRepository();
                            int BATCH_SIZE = 500;
                            if (repository == null) {
                                System.err.println("[BACKUP-REPORT-MAP] Repository is null. Cannot save backup data.");
                                break;
                            }

                            // The backup map is a shared resource; it is already a ConcurrentHashMap
                            Map<String, WhatsAppNumberReport> backup = backupWhatsAppReportDataMap;

                            if (backup != null && !backup.isEmpty()) {
                                List<WhatsAppNumberReport> reportObjects = new ArrayList<>(backup.values());

                                if (reportObjects == null || reportObjects.isEmpty()) {
                                    // no-op
                                } else {
                                    int total = reportObjects.size();
                                    int fromIndex = 0;

                                    while (fromIndex < total) {
                                        int toIndex = Math.min(fromIndex + BATCH_SIZE, total);
                                        List<WhatsAppNumberReport> batch = reportObjects.subList(fromIndex, toIndex);

                                        try {
                                            repository.saveAll(batch);
                                        } catch (Exception e) {
                                            System.err.println("[ERROR] Failed to save batch " + fromIndex + "-" + (toIndex - 1));
                                            e.printStackTrace();
                                        }

                                        fromIndex = toIndex;
                                    }
                                }

                            } else {
                                // no-op
                            }

                            lastPutToDatabase = new Date();
                            // Shared map reset: ConcurrentHashMap is maintained for thread-safety
                            backupWhatsAppReportDataMap = new ConcurrentHashMap<>();
                        } else {
                            // no-op
                        }

                        break;

                    default:
                        // no-op
                        break;
                }

            } catch (InterruptedException e) {
                System.err.println("[ERROR] Interrupted while waiting for lock for action: " + action);
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("[ERROR] Exception in workWithBackupWhatsAppReportMapData for action: " + action);
                e.printStackTrace();
            } finally {
                if (acquired) {
                    lockBackupWhatsAppReportDataMap.unlock();
                }
            }
        }

        return toReturn;
    }

    private static void updateReceivedMessageStats(WhatsAppReportDataParameterDTO dto) {
        try {
            String key = dto.getPhoneNumberMain() + delimiter + dto.getPhoneNumberWith();
            WhatsAppNumberReport current = verifyAndFetchCurrentOrElseAddNewObjectForToday(dto);
            WhatsAppPhoneNumber phoneNumber = dto.getPhoneNumber();

            String message = dto.getInputDTO().getMessageString();
            int tokens = message != null && !message.isEmpty() ? message.trim().split("\\s+").length : 0;

            if (current != null) {

                OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
                current.setTotalMessagesReceived(current.getTotalMessagesReceived() + 1);
                current.setTotalTokenReceived(current.getTotalTokenReceived() + tokens);
                current.setTotalAmountSpend(current.getTotalAmountSpend() + phoneNumber.getCostPerInboundMessage());

                organizationWorkingDTO.setAmount(phoneNumber.getCostPerInboundMessage());
                OrganizationData.workWithAllOrganizationData(dto.getOrganization(), null, "update-whatsapp-amount", organizationWorkingDTO);
                whatsAppReportDataMap.put(key, current);

            } else {
                System.err.println("[UPDATE-RECEIVED] Report object not found for key: " + key);
                throw new IllegalStateException("Report object not found in memory data. Please connect with admin");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Exception in updateReceivedMessageStats");
            e.printStackTrace();
            throw e;
        }
    }

    private static void updateSendMessageStats(WhatsAppReportDataParameterDTO dto) {
        try {
            String key = dto.getPhoneNumberMain() + delimiter + dto.getPhoneNumberWith();

            WhatsAppNumberReport current = verifyAndFetchCurrentOrElseAddNewObjectForToday(dto);

            if (current != null) {
                if (dto.isAiMessage()) {
                    current.setAiMessagesSend(current.getAiMessagesSend() + 1);
                } else if (dto.isCampaignMessage()) {
                    current.setCampaignMessageSend(current.getCampaignMessageSend() + 1);
                } else if (dto.isManualMessage()) {
                    current.setManualMessageSend(current.getManualMessageSend() + 1);

                    WhatsAppChatHistory currentChatRecord = dto.getInputDTO();

                    if (currentChatRecord != null) {
                        Map<String, WhatsAppExtensionReportingDTO> extensionReport = current.getExtensionReport();

                        if (extensionReport != null && currentChatRecord.getFromExtension() != null) {
                            WhatsAppExtensionReportingDTO whatsAppExtensionReportingDTO = extensionReport.get(currentChatRecord.getFromExtension());

                            if (whatsAppExtensionReportingDTO == null) {
                                whatsAppExtensionReportingDTO = new WhatsAppExtensionReportingDTO();
                                whatsAppExtensionReportingDTO.setTotalMessagesSend(0L);
                            }

                            whatsAppExtensionReportingDTO.setTotalMessagesSend(whatsAppExtensionReportingDTO.getTotalMessagesSend() + 1);
                            extensionReport.put(currentChatRecord.getFromExtension(), whatsAppExtensionReportingDTO);
                        }
                        current.setExtensionReport(extensionReport);
                    }
                }

                long fileSizeMB = 0;
                try {
                    String fileSizeStr = dto.getInputDTO() != null ? dto.getInputDTO().getFileSizeInMB() : null;
                    fileSizeMB = Long.parseLong(fileSizeStr);

                    current.setTotalMediaSizeSendMB(current.getTotalMediaSizeSendMB() + fileSizeMB);
                } catch (NumberFormatException nfe) {
                    System.err.println("[UPDATE-SEND] Invalid file size, defaulting to 0 MB");
                }

                whatsAppReportDataMap.put(key, current);
            } else {
                System.err.println("[UPDATE-SEND] Report object not found for key: " + key);
                throw new IllegalStateException("Report object not found in memory data. Please connect with admin");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in updateSendMessageStats");
            e.printStackTrace();
            throw e;
        }
    }

    private static void updateDeliveredMessageStats(WhatsAppReportDataParameterDTO dto,Long amount) {
        try {
            String key = dto.getPhoneNumberMain() + delimiter + dto.getPhoneNumberWith();
            WhatsAppNumberReport current = verifyAndFetchCurrentOrElseAddNewObjectForToday(dto);
            WhatsAppPhoneNumber phoneNumber = dto.getPhoneNumber();

            String message = dto.getInputDTO().getMessageString();
            int tokens = message != null && !message.isEmpty() ? message.trim().split("\\s+").length : 0;

            if (current != null) {
                OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
                if (dto.isAiMessage()) {
                    current.setAiMessagesDelivered(current.getAiMessagesDelivered() + 1);
                    current.setTotalAmountSpend(current.getTotalAmountSpend() + phoneNumber.getCostPerOutboundAIMessageToken() * tokens);
                    current.setAiTokenSend(current.getAiTokenSend() + tokens);
                    organizationWorkingDTO.setAmount(phoneNumber.getCostPerOutboundMessage());
                } else if (dto.isCampaignMessage()) {
                    current.setCampaignMessageDelivered(current.getCampaignMessageDelivered() + 1);
                    organizationWorkingDTO.setAmount(amount);
                } else if (dto.isManualMessage()) {
                    current.setManualMessageDelivered(current.getManualMessageDelivered() + 1);
                    organizationWorkingDTO.setAmount(0L);
                }

                current.setTotalAmountSpend(current.getTotalAmountSpend() + phoneNumber.getCostPerOutboundMessage());
                OrganizationData.workWithAllOrganizationData(dto.getOrganization(), null, "update-whatsapp-amount", organizationWorkingDTO);

                whatsAppReportDataMap.put(key, current);

            } else {
                System.err.println("[UPDATE-DELIVERED] Report object not found for key: " + key);
                throw new IllegalStateException("Report object not found in memory data. Please connect with admin");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in updateDeliveredMessageStats");
            e.printStackTrace();
            throw e;
        }
    }

    private static void updateFailedMessageStats(WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO) {
        try {
            String key = whatsAppReportDataParameterDTO.getPhoneNumberMain() + delimiter + whatsAppReportDataParameterDTO.getPhoneNumberWith();

            WhatsAppNumberReport current = verifyAndFetchCurrentOrElseAddNewObjectForToday(whatsAppReportDataParameterDTO);

            if (current != null) {
                if (whatsAppReportDataParameterDTO.isAiMessage()) {
                    current.setAiMessagesFailed(current.getAiMessagesFailed() + 1);
                } else if (whatsAppReportDataParameterDTO.isCampaignMessage()) {
                    current.setCampaignMessageFailed(current.getCampaignMessageFailed() + 1);
                } else if (whatsAppReportDataParameterDTO.isManualMessage()) {
                    current.setManualMessageFailed(current.getManualMessageFailed() + 1);
                }
                whatsAppReportDataMap.put(key, current);
            } else {
                String errMsg = "[updateFailedMessageStats] ERROR: Report object not found for key: " + key + ". Please connect with admin.";
                throw new IllegalStateException(errMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void updateDeletedMessageStats(WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO) {
        try {
            String key = whatsAppReportDataParameterDTO.getPhoneNumberMain() + delimiter + whatsAppReportDataParameterDTO.getPhoneNumberWith();

            WhatsAppNumberReport current = verifyAndFetchCurrentOrElseAddNewObjectForToday(whatsAppReportDataParameterDTO);

            if (current != null) {
                if (whatsAppReportDataParameterDTO.isAiMessage()) {
                    current.setAiMessagesDeleted(current.getAiMessagesDeleted() + 1);
                } else if (whatsAppReportDataParameterDTO.isCampaignMessage()) {
                    current.setCampaignMessageDeleted(current.getCampaignMessageDeleted() + 1);
                } else if (whatsAppReportDataParameterDTO.isManualMessage()) {
                    current.setManualMessageDeleted(current.getManualMessageDeleted() + 1);
                }
                whatsAppReportDataMap.put(key, current);
            } else {
                String errMsg = "[updateDeletedMessageStats] ERROR: Report object not found for key: " + key + ". Please connect with admin.";
                throw new IllegalStateException(errMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void updateReadMessageStats(WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO) {
        try {
            String key = whatsAppReportDataParameterDTO.getPhoneNumberMain() + delimiter + whatsAppReportDataParameterDTO.getPhoneNumberWith();

            WhatsAppNumberReport current = verifyAndFetchCurrentOrElseAddNewObjectForToday(whatsAppReportDataParameterDTO);

            if (current != null) {
                if (whatsAppReportDataParameterDTO.isAiMessage()) {
                    current.setAiMessagesRead(current.getAiMessagesRead() + 1);
                } else if (whatsAppReportDataParameterDTO.isCampaignMessage()) {
                    current.setCampaignMessageRead(current.getCampaignMessageRead() + 1);
                } else if (whatsAppReportDataParameterDTO.isManualMessage()) {
                    current.setManualMessageRead(current.getManualMessageRead() + 1);
                }
                whatsAppReportDataMap.put(key, current);
            } else {
                String errMsg = "[updateReadMessageStats] ERROR: Report object not found for key: " + key + ". Please connect with admin.";
                throw new IllegalStateException(errMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String getPhoneNumberMainFromKey(String key) {
        if (key == null || !key.contains(delimiter)) {
            return null;
        }
        return key.split(delimiter, 2)[0];
    }

    public static String getPhoneNumberWithFromKey(String key) {
        if (key == null || !key.contains(delimiter)) {
            return null;
        }
        String[] parts = key.split(delimiter, 2);
        return parts.length > 1 ? parts[1] : null;
    }

    private static WhatsAppNumberReport verifyAndFetchCurrentOrElseAddNewObjectForToday(WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO) {
        WhatsAppNumberReport current = null;
        WhatsAppNumberReport currentBackup = null;

        try {
            WhatsAppNumberReportRepository whatsAppNumberReportRepository = whatsAppReportDataParameterDTO.getWhatsAppNumberReportRepository();
            WhatsAppNumberReportService whatsAppNumberReportService = whatsAppReportDataParameterDTO.getWhatsAppNumberReportService();

            String phoneNumberMain = whatsAppReportDataParameterDTO.getPhoneNumberMain();
            String phoneNumberWith = whatsAppReportDataParameterDTO.getPhoneNumberWith();
            String key = phoneNumberMain + delimiter + phoneNumberWith;

            Date now = new Date();
            Instant inst = now.toInstant();
            LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
            Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Date dayStartDate = Date.from(dayInst);

            current = whatsAppReportDataMap.get(key);
            currentBackup = backupWhatsAppReportDataMap.get(key);

            if (current != null) {

                if (currentBackup != null) {
                    whatsAppNumberReportRepository.save(currentBackup);
                    backupWhatsAppReportDataMap.remove(key);
                }

                // Compare dates by LocalDate to avoid time part mismatches
                LocalDate currentDay = current.getDayUpdated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                if (currentDay.equals(localDate)) {
                    return current;
                } else {
                    whatsAppNumberReportRepository.save(current);

                    current = getNewObjectWithInitialValues(dayStartDate, whatsAppReportDataParameterDTO);
                    current = whatsAppNumberReportService.setDefaultValues(current);
                    current = whatsAppNumberReportRepository.save(current);

                    whatsAppReportDataMap.put(key, current);
                }

            } else {

                if (currentBackup != null) {
                    whatsAppNumberReportRepository.save(currentBackup);
                    backupWhatsAppReportDataMap.remove(key);
                }

                current = whatsAppNumberReportRepository.findAllByPhoneNumberMainAndPhoneNumberWithAndDayUpdated(
                        phoneNumberMain,
                        phoneNumberWith,
                        dayStartDate
                );

                if (current == null) {
                    current = getNewObjectWithInitialValues(dayStartDate, whatsAppReportDataParameterDTO);
                    current = whatsAppNumberReportService.setDefaultValues(current);
                    current = whatsAppNumberReportRepository.save(current);

                    whatsAppReportDataMap.put(key, current);
                } else {
                    whatsAppReportDataMap.put(key, current);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return current;
    }

    private static WhatsAppNumberReport getNewObjectWithInitialValues(Date dayStartDate, WhatsAppReportDataParameterDTO whatsAppReportDataParameterDTO) {
        WhatsAppNumberReport current = new WhatsAppNumberReport();

        try {
            current.setDayUpdated(dayStartDate);
            current.setPhoneNumberMain(whatsAppReportDataParameterDTO.getPhoneNumberMain());
            current.setPhoneNumberWith(whatsAppReportDataParameterDTO.getPhoneNumberWith());
            current.setExtensionReport(new HashMap<>());
            current.setOrganization(whatsAppReportDataParameterDTO.getOrganization());
            current.setTypeOfReport(MESSAGING_PRODUCT.whatsapp.name());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return current;
    }

    // ---------------------------
    // DEFENSIVE COPY HELPERS
    // ---------------------------

    // Defensive snapshot: do not leak internal map reference outside
    private static Map<String, WhatsAppNumberReport> snapshotReportMap(Map<String, WhatsAppNumberReport> src) {
        if (src == null || src.isEmpty()) return new HashMap<>();
        return new HashMap<>(src);
    }
}
