package com.mylinehub.crm.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.data.dto.CdrDTO;

public class CDRMemoryCollection {

    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    // Backoff on lock failure (prevents CPU spin)
    private static final long LOCK_BACKOFF_MS = 50;

    // Shared maps for interim records
    private static Map<String, CdrDTO> interimRecords = new ConcurrentHashMap<>();
    private static Map<String, CdrDTO> backupInterimRecords = new ConcurrentHashMap<>();

    // Locks for thread-safe access
    private static final ReentrantLock lockInterim = new ReentrantLock(false); // non-fair
    private static final ReentrantLock lockBackup = new ReentrantLock(false); // non-fair

    public static Map<String, CdrDTO> workWithCDRInterimData(String uniqueId, Map<String, CdrDTO> values, String action) {
        Map<String, CdrDTO> allValues = null;

        while (true) {
            try {
                long timeout = lockInterim.getQueueLength() + BASE_TIMEOUT_SECONDS;

                if (lockInterim.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        switch (action) {

                            case "get-one":
                                if (uniqueId == null) {
                                    allValues = null;
                                    break;
                                }
                                CdrDTO toAdd = interimRecords.get(uniqueId);
                                if (toAdd != null) {
                                    allValues = new HashMap<>();
                                    allValues.put(uniqueId, toAdd);
                                } else {
                                    allValues = null;
                                }
                                break;

                            case "get":
                                // IMPORTANT: return a snapshot, not the live ConcurrentHashMap
                                allValues = new HashMap<>(interimRecords);
                                break;

                            case "update":
                                updateInterimRecords(values);
                                break;

                            case "delete":
                                deleteInterimRecords(values);
                                break;

                            case "clear":
                                interimRecords.clear();
                                break;

                            default:
                                break;
                        }

                        break; // success
                    } finally {
                        lockInterim.unlock();
                    }
                } else {
                    // backoff to prevent CPU spin
                    Thread.sleep(LOCK_BACKOFF_MS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        return allValues;
    }

    public static Map<String, CdrDTO> workWithCDRBackupInterimData(String uniqueId, Map<String, CdrDTO> values, String action) {
        Map<String, CdrDTO> allValues = null;

        while (true) {
            try {
                long timeout = lockBackup.getQueueLength() + BASE_TIMEOUT_SECONDS;

                if (lockBackup.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        switch (action) {

                            case "get-one":
                                if (uniqueId == null) {
                                    allValues = null;
                                    break;
                                }
                                CdrDTO toAdd = backupInterimRecords.get(uniqueId);
                                if (toAdd != null) {
                                    allValues = new HashMap<>();
                                    allValues.put(uniqueId, toAdd);
                                } else {
                                    allValues = null;
                                }
                                break;

                            case "get":
                                // IMPORTANT: return a snapshot, not the live ConcurrentHashMap
                                allValues = new HashMap<>(backupInterimRecords);
                                break;

                            case "update":
                                updateBackupInterimRecords(values);
                                break;

                            case "delete":
                                deleteBackupInterimRecords(values);
                                break;

                            case "clear":
                                backupInterimRecords.clear();
                                break;

                            default:
                                break;
                        }

                        break; // success
                    } finally {
                        lockBackup.unlock();
                    }
                } else {
                    // backoff to prevent CPU spin
                    Thread.sleep(LOCK_BACKOFF_MS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        return allValues;
    }

    private static void updateInterimRecords(Map<String, CdrDTO> values) {
        if (values == null || values.isEmpty()) return;

        for (Map.Entry<String, CdrDTO> entry : values.entrySet()) {
            String key = entry.getKey();
            CdrDTO value = entry.getValue();
            if (key != null) {
                interimRecords.put(key, value);
            }
        }
    }

    private static void deleteInterimRecords(Map<String, CdrDTO> values) {
        if (values == null || values.isEmpty()) return;

        for (Map.Entry<String, CdrDTO> entry : values.entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                interimRecords.remove(key);
            }
        }
    }

    private static void updateBackupInterimRecords(Map<String, CdrDTO> values) {
        if (values == null || values.isEmpty()) return;

        for (Map.Entry<String, CdrDTO> entry : values.entrySet()) {
            String key = entry.getKey();
            CdrDTO value = entry.getValue();
            if (key != null) {
                backupInterimRecords.put(key, value);
            }
        }
    }

    private static void deleteBackupInterimRecords(Map<String, CdrDTO> values) {
        if (values == null || values.isEmpty()) return;

        for (Map.Entry<String, CdrDTO> entry : values.entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                backupInterimRecords.remove(key);
            }
        }
    }
}
