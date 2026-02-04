package com.mylinehub.crm.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;

public class DeletedCampaignData {

    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    // Customer Phone Number and Campaign it is into
    private static Map<String, CustomerAndItsCampaignDTO> allDeletedCustomersAndItsCampaign = new ConcurrentHashMap<>();

    // Lock for controlling access to shared map
    private static final ReentrantLock lock = new ReentrantLock(false); // non-fair lock

    public static Map<String, CustomerAndItsCampaignDTO> workWithAllDeletedCustomerData(
            String phoneNumber, CustomerAndItsCampaignDTO customerAndItsCampaignDTO, String action) {

        Map<String, CustomerAndItsCampaignDTO> toReturn = null;

        while (true) {
            try {
                long timeout = lock.getQueueLength() + BASE_TIMEOUT_SECONDS;

                if (lock.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        switch (action) {
                            case "get-one":
                                CustomerAndItsCampaignDTO customerAndItsCampaignDTOToReturn =
                                        allDeletedCustomersAndItsCampaign.get(phoneNumber);
                                if (customerAndItsCampaignDTOToReturn != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(phoneNumber, customerAndItsCampaignDTOToReturn);
                                }
                                break;

                            case "get":
                                // IMPORTANT: return snapshot, not internal ref
                                return new HashMap<>(allDeletedCustomersAndItsCampaign);

                            case "update":
                                allDeletedCustomersAndItsCampaign.put(phoneNumber, customerAndItsCampaignDTO);
                                break;

                            case "delete":
                                allDeletedCustomersAndItsCampaign.remove(phoneNumber);
                                break;

                            default:
                                break;
                        }
                        break; // exit retry loop after successful lock
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("Timeout acquiring lock for workWithAllDeletedCustomerData, retrying...");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt(); // restore interrupted status
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
            }
        }

        return toReturn;
    }
}
