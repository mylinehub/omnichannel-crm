package com.mylinehub.crm.whatsapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.whatsapp.dto.chat.WhatsAppFlattenMessageParameterDTO;
import com.mylinehub.crm.whatsapp.entity.WhatsAppFlattenMessage;

public class WhatsAppFlattenMessageConversation {

    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    // In-memory list of flatten messages (shared mutable state)
    private static List<WhatsAppFlattenMessage> whatsAppFlattenMessageMemoryList = new ArrayList<>();

    // Backup list for flush/save operations (shared mutable state)
    private static List<WhatsAppFlattenMessage> backupWhatsAppFlattenMessageMemoryList = new ArrayList<>();

    // Locks for flatten message memory lists (non-fair)
    private static final ReentrantLock lockFlattenMessageMemory = new ReentrantLock(false);
    private static final ReentrantLock lockBackupFlattenMessageMemory = new ReentrantLock(false);

    public static List<WhatsAppFlattenMessage> workOnWhatsAppFlattenMessageMemoryList(WhatsAppFlattenMessageParameterDTO parameter) {
        List<WhatsAppFlattenMessage> toReturn = null;

        while (true) {
            int queueLength = lockFlattenMessageMemory.getQueueLength();
            long timeout = BASE_TIMEOUT_SECONDS + queueLength;

            try {
                if (lockFlattenMessageMemory.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        String action = parameter.getAction();

                        switch (action) {

                            case "update":
                                WhatsAppFlattenMessage message = parameter.getWhatsAppFlattenMessage();
                                if (message != null) {
                                    whatsAppFlattenMessageMemoryList.add(message);
                                }
                                break;

                            case "get-all":
                                // already defensive copy in your file; keep
                                return new ArrayList<>(whatsAppFlattenMessageMemoryList);

                            case "put-all-to-backup":
                                // IMPORTANT FIX: do not assign list reference to backup; copy it
                                backupWhatsAppFlattenMessageMemoryList = new ArrayList<>(whatsAppFlattenMessageMemoryList);
                                whatsAppFlattenMessageMemoryList = new ArrayList<>();
                                // return copy so ref doesn't escape
                                return new ArrayList<>(backupWhatsAppFlattenMessageMemoryList);

                            default:
                                break;
                        }
                    } finally {
                        lockFlattenMessageMemory.unlock();
                    }
                    break;

                } else {
                    System.out.println("[INFO] Could not acquire lockFlattenMessageMemory, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workOnWhatsAppFlattenMessageMemoryList");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.out.println("[ERROR] Exception in workOnWhatsAppFlattenMessageMemoryList:");
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

    public static boolean workOnBackupWhatsAppFlattenMessageMemoryList(WhatsAppFlattenMessageParameterDTO parameter) {
        boolean toReturn = false;

        while (true) {
            int queueLength = lockBackupFlattenMessageMemory.getQueueLength();
            long timeout = BASE_TIMEOUT_SECONDS + queueLength;

            try {
                if (lockBackupFlattenMessageMemory.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        String action = parameter.getAction();

                        switch (action) {

                            case "put-all-to-database":
                                if (backupWhatsAppFlattenMessageMemoryList != null && !backupWhatsAppFlattenMessageMemoryList.isEmpty()) {
                                    parameter.getWhatsAppFlattenMessageRepository().saveAll(backupWhatsAppFlattenMessageMemoryList);
                                    backupWhatsAppFlattenMessageMemoryList = new ArrayList<>();
                                    toReturn = true;
                                }
                                break;

                            default:
                                break;
                        }
                    } finally {
                        lockBackupFlattenMessageMemory.unlock();
                    }
                    break;

                } else {
                    System.out.println("[INFO] Could not acquire lockBackupFlattenMessageMemory, retrying...");
                    Thread.sleep(500);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Interrupted while waiting for lock in workOnBackupWhatsAppFlattenMessageMemoryList");
                e.printStackTrace();
                break;

            } catch (Exception e) {
                System.out.println("[ERROR] Exception in workOnBackupWhatsAppFlattenMessageMemoryList:");
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }
}
