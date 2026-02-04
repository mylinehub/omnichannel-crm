package com.mylinehub.crm.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.entity.dto.StatementReconciliationBatchDTO;
import com.mylinehub.crm.entity.dto.StatementReconciliationLineDTO;

public class StatementReconciliationData {

    // Base timeout in seconds for lock acquisition
    private static final int BASE_TIMEOUT_SECONDS = 2;

    // Non-fair lock for statementReconciliationRawData
    private static final ReentrantLock statementReconciliationRawDataLock = new ReentrantLock(false);

    // Batch ID / Reconciliation ID and associated live data
    // Changed from HashMap to ConcurrentHashMap because it's shared across threads
    private static Map<Long, StatementReconciliationBatchDTO> statementReconciliationRawData =
            new ConcurrentHashMap<Long, StatementReconciliationBatchDTO>();

    public static Map<Long, StatementReconciliationBatchDTO> workOnStatementReconciliationRawData(
            Long batchId,
            StatementReconciliationBatchDTO details,
            StatementReconciliationLineDTO statementReconciliationLineDTO,
            String action) {

        Map<Long, StatementReconciliationBatchDTO> toReturn = null;
        boolean acquired = false;

        while (!acquired) {
            try {
                // Timeout = current queue length + base seconds
                int timeoutSeconds = statementReconciliationRawDataLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = statementReconciliationRawDataLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) continue; // retry if lock not acquired

                switch (action) {
                    case "get-one":
                        details = statementReconciliationRawData.get(batchId);
                        if (details != null) {
                            toReturn = new HashMap<>(); // local map remains HashMap
                            toReturn.put(batchId, details);
                        }
                        break;

                    case "add-utr":
                        details = statementReconciliationRawData.get(batchId);
                        if (details != null) {
                            Map<String, StatementReconciliationLineDTO> allObservations = details.getAllUtrs();
                            if (allObservations.get(statementReconciliationLineDTO.getUtr()) == null) {
                                allObservations.put(statementReconciliationLineDTO.getUtr(), statementReconciliationLineDTO);
                                details.setAllUtrs(allObservations);
                                statementReconciliationRawData.put(batchId, details);
                            }
                        }
                        break;

                    case "get":
                        return  new HashMap<>(statementReconciliationRawData);

                    case "update":
                        statementReconciliationRawData.put(batchId, details);
                        break;

                    case "delete":
                        statementReconciliationRawData.remove(batchId);
                        break;

                    default:
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
            } finally {
                if (acquired) statementReconciliationRawDataLock.unlock();
            }
        }

        return toReturn;
    }
}
