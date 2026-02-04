package com.mylinehub.crm.ami.TaskScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.enums.Cdr_Event;
import com.mylinehub.crm.repository.CallDetailRepository;

import lombok.Data;

@Data
public class HardInsertCallDetailAndCostRunnable implements Runnable {

    String jobId;
    CallDetailRepository callDetailRepository;

    // Tune batch sizes based on DB + network (start with 300/500)
    private static final int BATCH_CALLDETAIL = 500;
    private static final int BATCH_COST = 500;

    @Override
    public void run() {

        System.out.println("HardInsertCallDetailAndCostRunnable");

        try {

            // Ensure transfer happens before hard insert (your existing design)
            System.out.println("Transfering to backup variable");
            TransferCdrMainMemoryToBackupRunnable transfer = new TransferCdrMainMemoryToBackupRunnable();
            transfer.run();

            // get() should return SNAPSHOT from CDRMemoryCollection
            System.out.println("Creating local variable");
            Map<String, CdrDTO> backupInterimRecords = CDRMemoryCollection.workWithCDRBackupInterimData(null, null, "get");

            if (backupInterimRecords == null || backupInterimRecords.isEmpty()) {
                System.out.println("backupInterimRecords is empty, nothing to save");
                return;
            }

            System.out.println("Starting loop to save call detail and call cost to database in batches");
            System.out.println("backupInterimRecords size : " + backupInterimRecords.size());

            // Batch buffers (avoid huge memory spikes)
            List<CallDetail> callDetailBatch = new ArrayList<>(BATCH_CALLDETAIL);

            // Local caches to avoid repeating expensive lookups
            // token -> isExtension (DTO exists)
            Map<String, Boolean> tokenIsExtensionCache = new HashMap<>();
            // phoneToken -> mappedExtension (or "" if none)
            Map<String, String> phoneToExtCache = new HashMap<>();
            // extension -> DTO (or null)
            Map<String, EmployeeDataAndStateDTO> extToEmployeeCache = new HashMap<>();

            int processed = 0;
            int savedCallDetails = 0;

            for (Map.Entry<String, CdrDTO> entry : backupInterimRecords.entrySet()) {

                CdrDTO cdrDTO = entry.getValue();
                if (cdrDTO == null) continue;

                CallDetail callDetail = cdrDTO.getCallDetail();
                Map<String, String> mapEvent = cdrDTO.getMapEvent();

                // Save call detail if present (same as your logic)
                if (callDetail != null) {
                    callDetailBatch.add(callDetail);
                    if (callDetailBatch.size() >= BATCH_CALLDETAIL) {
                        callDetailRepository.saveAll(callDetailBatch);
                        savedCallDetails += callDetailBatch.size();
                        callDetailBatch.clear();
                    }
                }

                // If no event, no cost calc (same as your logic)
                if (mapEvent == null) {
                    processed++;
                    continue;
                }

                String srcToken = safeStr(mapEvent.get(Cdr_Event.src.name().trim()));
                String dstToken = safeStr(mapEvent.get(Cdr_Event.destination.name().trim()));

                boolean isSourceExtension = resolveIsExtension(srcToken, tokenIsExtensionCache, phoneToExtCache, extToEmployeeCache);
                boolean isDestinationExtension = resolveIsExtension(dstToken, tokenIsExtensionCache, phoneToExtCache, extToEmployeeCache);

                int billingSeconds = safeParseInt(mapEvent.get(Cdr_Event.billableseconds.name().trim()), 0);

                // Same business logic:
                if (isSourceExtension && isDestinationExtension) {
                    // extension -> extension, no cost
                    processed++;
                    continue;
                }

                if (billingSeconds == 0) {
                    // no cost if bill is 0
                    processed++;
                    continue;
                }

                if (callDetail == null) {
                    // cannot compute cost without callDetail (same as your logic)
                    processed++;
                    continue;
                }


                processed++;

                // tiny progress log for long runs (optional)
                if (processed % 2000 == 0) {
                    System.out.println("Processed " + processed + " / " + backupInterimRecords.size()
                            + " | savedCallDetails=" + savedCallDetails);
                }
            }

            // Flush remaining batches
            if (!callDetailBatch.isEmpty()) {
                callDetailRepository.saveAll(callDetailBatch);
                savedCallDetails += callDetailBatch.size();
                callDetailBatch.clear();
            }

            System.out.println("Done saving. Total processed=" + processed
                    + " | totalCallDetailsSaved=" + savedCallDetails);

            // SAFE cleanup to release memory early (does not change saved data)
            // Backup is anyway cleared next run before transfer, but this frees RAM immediately.
            CDRMemoryCollection.workWithCDRBackupInterimData(null, null, "clear");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Business logic preserved:
     * Extension-first resolution, phone->extension mapping only if exists.
     * We only use "isExtension" boolean check here (DTO presence).
     *
     * Optimized:
     * - caches results so we don't call EmployeeDataAndState thousands of times
     */
    private boolean resolveIsExtension(
            String token,
            Map<String, Boolean> tokenIsExtensionCache,
            Map<String, String> phoneToExtCache,
            Map<String, EmployeeDataAndStateDTO> extToEmployeeCache
    ) {
        if (token == null || token.isEmpty()) return false;

        Boolean cached = tokenIsExtensionCache.get(token);
        if (cached != null) return cached;

        // 1) Try token as extension
        EmployeeDataAndStateDTO byExt = getEmployeeDTOByExtensionCached(token, extToEmployeeCache);
        if (byExt != null) {
            tokenIsExtensionCache.put(token, true);
            return true;
        }

        // 2) Try token as phone->extension mapping (ONLY if mapping exists)
        String mappedExt = getExtensionForPhoneCached(token, phoneToExtCache);
        if (mappedExt != null && !mappedExt.isEmpty()) {
            EmployeeDataAndStateDTO byMappedExt = getEmployeeDTOByExtensionCached(mappedExt, extToEmployeeCache);
            boolean result = (byMappedExt != null);
            tokenIsExtensionCache.put(token, result);
            return result;
        }

        tokenIsExtensionCache.put(token, false);
        return false;
    }

    private EmployeeDataAndStateDTO getEmployeeDTOByExtensionCached(String extension, Map<String, EmployeeDataAndStateDTO> extToEmployeeCache) {
        if (extension == null || extension.isEmpty()) return null;

        if (extToEmployeeCache.containsKey(extension)) {
            return extToEmployeeCache.get(extension);
        }

        EmployeeDataAndStateDTO dto = null;
        try {
            Map<String, EmployeeDataAndStateDTO> one =
                    EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");
            if (one != null) {
                dto = one.get(extension);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        extToEmployeeCache.put(extension, dto);
        return dto;
    }

    private String getExtensionForPhoneCached(String phone, Map<String, String> phoneToExtCache) {
        if (phone == null || phone.isEmpty()) return null;

        if (phoneToExtCache.containsKey(phone)) {
            String cached = phoneToExtCache.get(phone);
            return (cached == null || cached.isEmpty()) ? null : cached;
        }

        String ext = null;
        try {
            Map<String, String> one =
                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(phone, null, "get-one");
            if (one != null) {
                ext = one.get(phone);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // store "" for "no mapping" to avoid repeated calls
        phoneToExtCache.put(phone, (ext == null) ? "" : ext.trim());
        return (ext == null || ext.trim().isEmpty()) ? null : ext.trim();
    }

    private static String safeStr(String v) {
        return (v == null) ? "" : v.trim();
    }

    private int safeParseInt(String value, int defaultVal) {
        try {
            if (value == null) return defaultVal;
            String v = value.trim();
            if (v.isEmpty()) return defaultVal;
            return Integer.parseInt(v);
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
