package com.mylinehub.crm.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;

public class EmployeeDataAndState {

    // Extension -> (Employee + State)
    private static Map<String, EmployeeDataAndStateDTO> allEmployeeDataAndState =
            new ConcurrentHashMap<String, EmployeeDataAndStateDTO>();

    // Phone -> Extension
    private static Map<String, String> allEmployeePhoneAndExtension =
            new ConcurrentHashMap<String, String>();

    // Phone -> Extension
    private static Map<String, String> allEmployeeEmailAndExtension =
            new ConcurrentHashMap<String, String>();
    
    // Locks
    private static final ReentrantLock lockDataAndState = new ReentrantLock(false);
    private static final ReentrantLock lockPhoneAndExtension = new ReentrantLock(false);
    private static final ReentrantLock lockEmailAndExtension = new ReentrantLock(false);
    
    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    public static Map<String, EmployeeDataAndStateDTO> workOnAllEmployeeDataAndState(
            String extension, EmployeeDataAndStateDTO details, String action) {

        Map<String, EmployeeDataAndStateDTO> toReturn = null;

        while (true) {
            int queueLength = lockDataAndState.getQueueLength();
            long timeout = queueLength + BASE_TIMEOUT_SECONDS;

            try {
                if (lockDataAndState.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        switch (action) {
                            case "get-one":
                                details = allEmployeeDataAndState.get(extension);
                                if (details != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(extension, details);
                                }
                                break;

                            case "get":
                                // IMPORTANT: snapshot (do not leak internal ref)
                                return new HashMap<>(allEmployeeDataAndState);

                            case "update":
                                allEmployeeDataAndState.put(extension, details);
                                break;

                            case "delete":
                                allEmployeeDataAndState.remove(extension);
                                break;

                            case "reset":
                                // Keep behavior (new map) but under lock
                                allEmployeeDataAndState = new ConcurrentHashMap<>();
                                break;

                            default:
                                break;
                        }
                        break;
                    } finally {
                        lockDataAndState.unlock();
                    }
                } else {
                    System.out.println("Could not acquire lock for workOnAllEmployeeDataAndState, retrying...");
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                toReturn = null;
                e.printStackTrace();
                break;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                break;
            }
        }
        return toReturn;
    }

    public static Map<String, String> workOnAllEmployeePhoneAndExtension(
            String phoneNumber, String extension, String action) {

        Map<String, String> toReturn = null;

        while (true) {
            int queueLength = lockPhoneAndExtension.getQueueLength();
            long timeout = queueLength + BASE_TIMEOUT_SECONDS;

            try {
                if (lockPhoneAndExtension.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        switch (action) {
                            case "get-one":
                                extension = allEmployeePhoneAndExtension.get(phoneNumber);
                                if (extension != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(phoneNumber, extension);
                                }
                                break;

                            case "get":
                                // IMPORTANT: snapshot (do not leak internal ref)
                                return new HashMap<>(allEmployeePhoneAndExtension);

                            case "update":
                                allEmployeePhoneAndExtension.put(phoneNumber, extension);
                                break;

                            case "delete":
                                allEmployeePhoneAndExtension.remove(phoneNumber);
                                break;

                            case "reset":
                                allEmployeePhoneAndExtension = new ConcurrentHashMap<>();
                                break;

                            default:
                                break;
                        }
                        break;
                    } finally {
                        lockPhoneAndExtension.unlock();
                    }
                } else {
                    System.out.println("Could not acquire lock for workOnAllEmployeePhoneAndExtension, retrying...");
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                toReturn = null;
                e.printStackTrace();
                break;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }
    
    public static Map<String, String> workOnAllEmployeeEmailAndExtension(
            String email, String extension, String action) {

        Map<String, String> toReturn = null;

        while (true) {
            int queueLength = lockEmailAndExtension.getQueueLength();
            long timeout = queueLength + BASE_TIMEOUT_SECONDS;

            try {
                if (lockEmailAndExtension.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        switch (action) {
                            case "get-one":
                                extension = allEmployeeEmailAndExtension.get(email);
                                if (extension != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(email, extension);
                                }
                                break;

                            case "get":
                                // snapshot
                                return new HashMap<>(allEmployeeEmailAndExtension);

                            case "update":
                                allEmployeeEmailAndExtension.put(email, extension);
                                break;

                            case "delete":
                                allEmployeeEmailAndExtension.remove(email);
                                break;

                            case "reset":
                                allEmployeeEmailAndExtension = new ConcurrentHashMap<>();
                                break;

                            default:
                                break;
                        }
                        break;
                    } finally {
                        lockEmailAndExtension.unlock();
                    }
                } else {
                    System.out.println("Could not acquire lock for workOnAllEmployeeEmailAndExtension, retrying...");
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                toReturn = null;
                e.printStackTrace();
                break;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                break;
            }
        }

        return toReturn;
    }

}
