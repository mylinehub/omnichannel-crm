package com.mylinehub.crm.gst.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.gst.data.dto.GstVerificationEngineDataParameterDto;
import com.mylinehub.crm.gst.entity.GstVerificationEngine;

public class GSTVerificationEngineData {

    // Base timeout in seconds for lock acquisition
    private static final int BASE_TIMEOUT_SECONDS = 2;

    // Non-fair lock for gstEngineData
    private static final ReentrantLock gstEngineDataLock = new ReentrantLock(false);

    // Shared map for GST engine data
    // Using ConcurrentHashMap since it may be read concurrently
    private static Map<String, GstVerificationEngine> gstEngineData = new ConcurrentHashMap<>();

    public static Map<String, GstVerificationEngine> workWithGstVerificationData(
            GstVerificationEngineDataParameterDto gstVerificationEngineDataParameterDto) {

        Map<String, GstVerificationEngine> toReturn = null;
        boolean acquired = false;

        while (!acquired) {
            try {
                // Timeout = current queue length + base timeout
                int timeoutSeconds = gstEngineDataLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = gstEngineDataLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) continue; // retry if lock not acquired

                System.out.println("workWithGstVerificationData");

                GstVerificationEngine current = null;

                switch (gstVerificationEngineDataParameterDto.getAction()) {
                    case "get-one":
                        current = gstEngineData.get(gstVerificationEngineDataParameterDto.getEngineName());
                        if (current != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(gstVerificationEngineDataParameterDto.getEngineName(), current);
                        }
                        break;

                    case "get":
                        return new HashMap<>(gstEngineData);

                    case "update":
                        gstEngineData.put(gstVerificationEngineDataParameterDto.getEngineName(),
                                gstVerificationEngineDataParameterDto.getDetails());
                        break;

                    case "delete":
                        gstEngineData.remove(gstVerificationEngineDataParameterDto.getEngineName());
                        break;

                    default:
                        break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (acquired) gstEngineDataLock.unlock();
            }
        }

        return toReturn;
    }
}
