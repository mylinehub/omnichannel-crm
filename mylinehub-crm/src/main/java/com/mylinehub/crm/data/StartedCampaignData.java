package com.mylinehub.crm.data;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.mylinehub.crm.data.dto.CampaignCustomerDataDTO;
import com.mylinehub.crm.data.dto.CampaignEmployeeDataDTO;
import com.mylinehub.crm.data.dto.CustomerAndItsCampaignDTO;
import com.mylinehub.crm.data.dto.EmployeeAndItsCampaignDTO;
import com.mylinehub.crm.data.dto.PageInfoDTO;
import com.mylinehub.crm.entity.Campaign;

/**
 * Author : Anand Goel
 */
public class StartedCampaignData {

    private static final int pageSize = 3000;

    // Base timeout constant (2 seconds)
    private static final int BASE_TIMEOUT_SECONDS = 2;

    // Backoff on lock failure (prevents CPU spin)
    private static final long LOCK_BACKOFF_MS = 50;

    // Campaign ID and Campaign
    private static Map<Long, Campaign> activeCampaigns = new ConcurrentHashMap<>();
    // Campaign ID and Current Page Number
    private static Map<Long, PageInfoDTO> activeCampaignAndPageDetail = new ConcurrentHashMap<>();

    // Campaign Id and Customer for current Page (ORDER MUST BE STABLE)
    private static Map<Long, Map<String, CampaignCustomerDataDTO>> activeCampaignAndCommonCustomersPage =
            new ConcurrentHashMap<>();

    // Campaign ID and Map Of Employee Extension & Last PhoneNumber
    private static Map<Long, Map<String, CampaignEmployeeDataDTO>> activeCampaignAndAllEmployeeData =
            new ConcurrentHashMap<>();

    // Customer Phone Number and Campaign it is into
    private static Map<String, CustomerAndItsCampaignDTO> allActiveCustomersAndItsCampaign =
            new ConcurrentHashMap<>();
    // Extension , Campaign ID's
    private static Map<String, EmployeeAndItsCampaignDTO> allActiveExtensionsAndTheirCampaign =
            new ConcurrentHashMap<>();
    // Employee Phone , Extension
    private static Map<String, String> allActivePhoneAndTheirExtensions =
            new ConcurrentHashMap<>();

    // Customer Phone Number and Campaign ID
    private static List<String> allMissedCustomersPhoneNumbers = new ArrayList<>();

    private static final ConcurrentHashMap<Long, ReentrantLock> campaignPickLocks = new ConcurrentHashMap<>();

    public static ReentrantLock lockForCampaign(Long campaignId) {
        return campaignPickLocks.computeIfAbsent(campaignId, k -> new ReentrantLock(false));
    }
    
    // Locks for different shared maps (non-fair like existing code)
    private static final ReentrantLock activeCampaignsLock = new ReentrantLock(false);
    private static final ReentrantLock activeCampaignPageLock = new ReentrantLock(false);
    private static final ReentrantLock activeCampaignCommonCustomersLock = new ReentrantLock(false);
    private static final ReentrantLock activeCampaignEmployeeDataLock = new ReentrantLock(false);
    private static final ReentrantLock allActiveCustomersLock = new ReentrantLock(false);
    private static final ReentrantLock allActiveExtensionsLock = new ReentrantLock(false);
    private static final ReentrantLock allActivePhoneLock = new ReentrantLock(false);
    private static final ReentrantLock allMissedCustomersLock = new ReentrantLock(false);

    // -----------------------------
    // RUN TRACKING (MEMORY ONLY)
    // -----------------------------

    public static final String RUN_STATUS_RUNNING = "RUNNING";
    public static final String RUN_STATUS_STOPPED = "STOPPED";
    public static final String RUN_STATUS_COMPLETED = "COMPLETED";

    // Summary per campaign
    private static final Map<Long, CampaignRunSummaryMem> runSummaryByCampaign =
            new ConcurrentHashMap<>();


	// Pending call logs per campaign (latest by channelId; flushed by Spring service)
	// campaignId -> (channelId -> latestRow)
	private static final Map<Long, ConcurrentHashMap<String, CampaignRunCallLogMem>> pendingCallLogsByCampaign =
	         new ConcurrentHashMap<>();

 
	 // --------------------------------------------------
	 // WhatsApp message -> Campaign mapping (MEMORY ONLY)
	 // wamid -> campaignId
	 // --------------------------------------------------
	 private static final Map<String, Long> waMsgIdToCampaignId = new ConcurrentHashMap<>();
	
	// --------------------------------------------------
		 // WhatsApp message -> Campaign mapping (MEMORY ONLY)
		 // wamid -> campaignId
		 // --------------------------------------------------
    private static final Map<String, Long> callRunIDToCampaignId = new ConcurrentHashMap<>();

	 
    // Flush hints
    private static final Map<Long, AtomicLong> pendingCountByCampaign =
            new ConcurrentHashMap<>();
    private static final Map<Long, Instant> lastFlushAtByCampaign =
            new ConcurrentHashMap<>();

    private static final ReentrantLock runTrackingLock = new ReentrantLock(false);

    // Policy constants (service uses these too)
    public static final int FLUSH_EVERY_N_RECORDS = 200;
    public static final Duration FLUSH_EVERY_DURATION = Duration.ofMinutes(60);

    public static int getPageSize() {
        return pageSize;
    }

    // -----------------------------
    // LOCK BACKOFF HELPER
    // -----------------------------
    private static void backoff() {
        try {
            Thread.sleep(LOCK_BACKOFF_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    // -----------------------------
    // ORDERED MAP HELPERS
    // -----------------------------
    private static Map<String, CampaignCustomerDataDTO> toOrderedCustomerMap(Map<String, CampaignCustomerDataDTO> src) {
        if (src == null) return null;
        return new LinkedHashMap<>(src);
    }

    private static Map<String, CampaignEmployeeDataDTO> toOrderedEmployeeMap(Map<String, CampaignEmployeeDataDTO> src) {
        if (src == null) return null;
        return new LinkedHashMap<>(src);
    }
    
    public static void putWaMsgCampaignMapping(String wamid, Long campaignId) {
        if (wamid == null || wamid.trim().isEmpty()) return;
        if (campaignId == null) return;
        waMsgIdToCampaignId.put(wamid, campaignId);
    }

    public static Long getCampaignIdByWaMsgId(String wamid) {
        if (wamid == null || wamid.trim().isEmpty()) return null;
        return waMsgIdToCampaignId.get(wamid);
    }

    public static void clearWaMsgMappingsForCampaign(Long campaignId) {
        if (campaignId == null) return;
        for (Map.Entry<String, Long> e : new ArrayList<>(waMsgIdToCampaignId.entrySet())) {
            if (campaignId.equals(e.getValue())) {
                waMsgIdToCampaignId.remove(e.getKey());
            }
        }
    }

    public static void removeWaMsgMapping(String wamid) {
        if (wamid == null || wamid.trim().isEmpty()) return;
        waMsgIdToCampaignId.remove(wamid);
    }
    
    
    public static void putjobIDCampaignMapping(String jobID, Long campaignId) {
        if (jobID == null || jobID.trim().isEmpty()) return;
        if (campaignId == null) return;
        callRunIDToCampaignId.put(jobID, campaignId);
    }

    public static Long getCampaignIdByjobID(String jobID) {
        if (jobID == null || jobID.trim().isEmpty()) return null;
        return callRunIDToCampaignId.get(jobID);
    }

    public static void clearjobIDMappingsForCampaign(Long campaignId) {
        if (campaignId == null) return;
        for (Map.Entry<String, Long> e : new ArrayList<>(callRunIDToCampaignId.entrySet())) {
            if (campaignId.equals(e.getValue())) {
            	callRunIDToCampaignId.remove(e.getKey());
            }
        }
    }

    public static void removejobIDMapping(String jobID) {
        if (jobID == null || jobID.trim().isEmpty()) return;

        final String id = jobID.trim();

        System.out.println("[removejobIDMapping] scheduled removal after 3 min jobID=" + id);
        try {
            callRunIDToCampaignId.remove(id);
            System.out.println("[removejobIDMapping] REMOVED after 3 min jobID=" + id);
        } catch (Exception e) {
            System.out.println("[removejobIDMapping] EXCEPTION jobID=" + id + " msg=" + e.getMessage());
            e.printStackTrace();
        }
    }


    // -----------------------------
    // EXISTING APIs (with ordering fix)   UNCHANGED
    // -----------------------------

    public static Map<Long, Campaign> workOnAllActiveCampaigns(Long id, Campaign campaign, String action) {
        Map<Long, Campaign> toReturn = null;

        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = activeCampaignsLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = activeCampaignsLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                switch (action) {
                    case "get-one":
                        campaign = activeCampaigns.get(id);
                        if (campaign != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(id, campaign);
                        }
                        break;

                    case "get":
                        return new HashMap<>(activeCampaigns);

                    case "update":
                        activeCampaigns.put(id, campaign);
                        break;

                    case "delete":
                        activeCampaigns.remove(id);
                        break;

                    default:
                        break;
                }
                return toReturn;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return toReturn;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                return toReturn;
            } finally {
                if (acquired) activeCampaignsLock.unlock();
            }
        }
    }

    public static PageInfoDTO updateOrGetCampaignCommonPageInfo(Long campaignId, PageInfoDTO campaignPageInfoDTO, String type) {
        PageInfoDTO result = null;

        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = activeCampaignPageLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = activeCampaignPageLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                try {
                    if ("get".equals(type)) {
                        result = activeCampaignAndPageDetail.get(campaignId);
                    } else if ("update".equals(type)) {
                        activeCampaignAndPageDetail.put(campaignId, campaignPageInfoDTO);
                        result = campaignPageInfoDTO;
                    } else if ("delete".equals(type)) {
                        activeCampaignAndPageDetail.remove(campaignId);
                        result = campaignPageInfoDTO;
                    } else {
                        result = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = null;
                }

                return result;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return result;
            } finally {
                if (acquired) activeCampaignPageLock.unlock();
            }
        }
    }

    public static Map<String, CampaignCustomerDataDTO> workOnActiveCampaignAndCommonCustomersPage(
            Long id, String phoneNumber, CampaignCustomerDataDTO details,
            Map<String, CampaignCustomerDataDTO> allData, String action) {

        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = activeCampaignCommonCustomersLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = activeCampaignCommonCustomersLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                switch (action) {
                    case "get": {
                        Map<String, CampaignCustomerDataDTO> m = activeCampaignAndCommonCustomersPage.get(id);
                        if (m == null) return null;
                        return toOrderedCustomerMap(m);
                    }

                    case "update-all": {
                        Map<String, CampaignCustomerDataDTO> ordered = toOrderedCustomerMap(allData);
                        activeCampaignAndCommonCustomersPage.put(id, ordered);
                        break;
                    }

                    case "update": {
                        Map<String, CampaignCustomerDataDTO> interim = activeCampaignAndCommonCustomersPage.get(id);
                        if (interim == null) {
                            interim = new LinkedHashMap<>();
                            activeCampaignAndCommonCustomersPage.put(id, interim);
                        } else if (!(interim instanceof LinkedHashMap)) {
                            interim = new LinkedHashMap<>(interim);
                            activeCampaignAndCommonCustomersPage.put(id, interim);
                        }
                        interim.put(phoneNumber, details);
                        break;
                    }

                    case "remove": {
                        Map<String, CampaignCustomerDataDTO> existing = activeCampaignAndCommonCustomersPage.get(id);
                        if (existing != null) existing.remove(phoneNumber);
                        break;
                    }

                    case "delete":
                        activeCampaignAndCommonCustomersPage.remove(id);
                        break;

                    default:
                        break;
                }

                return null;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (acquired) activeCampaignCommonCustomersLock.unlock();
            }
        }
    }

    public static Map<String, CampaignEmployeeDataDTO> workOnActiveCampaignAndAllEmployeeData(
            Long id, String extension, CampaignEmployeeDataDTO details,
            Map<String, CampaignEmployeeDataDTO> allData, String action) {

        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = activeCampaignEmployeeDataLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = activeCampaignEmployeeDataLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                switch (action) {
                    case "get": {
                        Map<String, CampaignEmployeeDataDTO> m = activeCampaignAndAllEmployeeData.get(id);
                        if (m == null) return null;
                        return toOrderedEmployeeMap(m);
                    }

                    case "update-all": {
                        activeCampaignAndAllEmployeeData.put(id, toOrderedEmployeeMap(allData));
                        break;
                    }

                    case "update": {
                        Map<String, CampaignEmployeeDataDTO> interim = activeCampaignAndAllEmployeeData.get(id);
                        if (interim == null) {
                            interim = new LinkedHashMap<>();
                            activeCampaignAndAllEmployeeData.put(id, interim);
                        } else if (!(interim instanceof LinkedHashMap)) {
                            interim = new LinkedHashMap<>(interim);
                            activeCampaignAndAllEmployeeData.put(id, interim);
                        }
                        interim.put(extension, details);
                        break;
                    }

                    case "remove": {
                        Map<String, CampaignEmployeeDataDTO> existing = activeCampaignAndAllEmployeeData.get(id);
                        if (existing != null) existing.remove(extension);
                        break;
                    }

                    case "delete":
                        activeCampaignAndAllEmployeeData.remove(id);
                        break;

                    default:
                        break;
                }

                return null;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (acquired) activeCampaignEmployeeDataLock.unlock();
            }
        }
    }

    public static Map<String, CustomerAndItsCampaignDTO> workOnAllActiveCustomersAndItsCampaign(
            String phoneNumber, CustomerAndItsCampaignDTO customerAndItsCampaignDTO, String action) {

        Map<String, CustomerAndItsCampaignDTO> toReturn = null;

        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = allActiveCustomersLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = allActiveCustomersLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                switch (action) {
                    case "get-one":
                        customerAndItsCampaignDTO = allActiveCustomersAndItsCampaign.get(phoneNumber);
                        if (customerAndItsCampaignDTO != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(phoneNumber, customerAndItsCampaignDTO);
                        }
                        break;

                    case "get":
                        return new HashMap<>(allActiveCustomersAndItsCampaign);

                    case "update":
                        allActiveCustomersAndItsCampaign.put(phoneNumber, customerAndItsCampaignDTO);
                        break;

                    case "delete":
                        allActiveCustomersAndItsCampaign.remove(phoneNumber);
                        break;

                    default:
                        break;
                }

                return toReturn;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return toReturn;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                return toReturn;
            } finally {
                if (acquired) allActiveCustomersLock.unlock();
            }
        }
    }

    public static Map<String, EmployeeAndItsCampaignDTO> workOnAllActiveExtensionsAndTheirCampaign(
            String extension, EmployeeAndItsCampaignDTO employeeAndItsCampaignDTO, String action) {

        Map<String, EmployeeAndItsCampaignDTO> toReturn = null;

        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = allActiveExtensionsLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = allActiveExtensionsLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                switch (action) {
                    case "get-one":
                        employeeAndItsCampaignDTO = allActiveExtensionsAndTheirCampaign.get(extension);
                        if (employeeAndItsCampaignDTO != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(extension, employeeAndItsCampaignDTO);
                        }
                        break;

                    case "get":
                        return new HashMap<>(allActiveExtensionsAndTheirCampaign);

                    case "update":
                        allActiveExtensionsAndTheirCampaign.put(extension, employeeAndItsCampaignDTO);
                        break;

                    case "delete":
                        allActiveExtensionsAndTheirCampaign.remove(extension);
                        break;

                    default:
                        break;
                }

                return toReturn;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return toReturn;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                return toReturn;
            } finally {
                if (acquired) allActiveExtensionsLock.unlock();
            }
        }
    }

    public static Map<String, String> workOnAllActivePhoneAndTheirExtensions(
            String phoneNumber, String extension, String action) {

        Map<String, String> toReturn = null;

        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = allActivePhoneLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = allActivePhoneLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                switch (action) {
                    case "get-one":
                        extension = allActivePhoneAndTheirExtensions.get(phoneNumber);
                        if (extension != null) {
                            toReturn = new HashMap<>();
                            toReturn.put(phoneNumber, extension);
                        }
                        break;

                    case "get":
                        return new HashMap<>(allActivePhoneAndTheirExtensions);

                    case "update":
                        allActivePhoneAndTheirExtensions.put(phoneNumber, extension);
                        break;

                    case "delete":
                        allActivePhoneAndTheirExtensions.remove(phoneNumber);
                        break;

                    default:
                        break;
                }

                return toReturn;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return toReturn;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                return toReturn;
            } finally {
                if (acquired) allActivePhoneLock.unlock();
            }
        }
    }

    public static List<String> workOnAllMissedCustomersPhoneNumbers(String phoneNumber, String action) {

        List<String> toReturn = null;

        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = allMissedCustomersLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = allMissedCustomersLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                switch (action) {
                    case "get-one":
                        int index = allMissedCustomersPhoneNumbers.indexOf(phoneNumber);
                        if (index != -1) {
                            toReturn = new ArrayList<>();
                            toReturn.add(allMissedCustomersPhoneNumbers.get(index));
                        }
                        break;

                    case "get":
                        return new ArrayList<>(allMissedCustomersPhoneNumbers);

                    case "update":
                        allMissedCustomersPhoneNumbers.add(phoneNumber);
                        break;

                    case "delete":
                        index = allMissedCustomersPhoneNumbers.indexOf(phoneNumber);
                        if (index != -1) {
                            allMissedCustomersPhoneNumbers.remove(index);
                        }
                        break;

                    default:
                        break;
                }

                return toReturn;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return toReturn;
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
                return toReturn;
            } finally {
                if (acquired) allMissedCustomersLock.unlock();
            }
        }
    }

    // ============================================================
    // RUN TRACKING API (Memory-only; flushed by Spring service)
    // ============================================================

    public static List<Long> listCampaignIdsWithRunSummary() {
        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                // campaigns that have summary bucket in memory (RUNNING/STOPPED/COMPLETED)
                return new ArrayList<>(runSummaryByCampaign.keySet());

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            } finally {
                if (acquired) runTrackingLock.unlock();
            }
        }
    }

    public static void ensureRunInitialized(Long campaignId, String organization, String campaignName) {
        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                CampaignRunSummaryMem s = runSummaryByCampaign.get(campaignId);
                if (s == null) {
                    Instant now = Instant.now();
                    s = new CampaignRunSummaryMem();
                    s.campaignId = campaignId;
                    s.organization = organization;
                    s.campaignName = campaignName;
                    s.startedAt = now;
                    s.lastUpdatedAt = now;
                    s.status = RUN_STATUS_RUNNING;
                    s.totalDialed = 0L;
                    s.totalCost = 0L;
                    s.lastDialedPhone = null;
                    s.lastFromNumber = null;
                    s.lastToNumber = null;
                    s.lastDurationMs = 0L;
                    s.lastCallState = null;

                    // runId created later by DB
                    s.runId = null;

                    runSummaryByCampaign.put(campaignId, s);

                    //map-backed pending
                    pendingCallLogsByCampaign.put(campaignId, new ConcurrentHashMap<>());

                    //pendingCount counts distinct channels pending (not updates)
                    pendingCountByCampaign.put(campaignId, new AtomicLong(0L));

                    lastFlushAtByCampaign.put(campaignId, now);
                } else {
                    if (s.organization == null && organization != null) s.organization = organization;
                    if (s.campaignName == null && campaignName != null) s.campaignName = campaignName;
                }

                return;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } finally {
                if (acquired) runTrackingLock.unlock();
            }
        }
    }


	 public static void setRunIdForCampaign(Long campaignId, Long runId) {
	    if (campaignId == null || runId == null) return;
	
	    while (true) {
	        boolean acquired = false;
	        try {
	            int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
	            acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
	            if (!acquired) {
	                backoff();
	                continue;
	            }
	
	            CampaignRunSummaryMem s = runSummaryByCampaign.get(campaignId);
	            if (s != null) {
	                s.runId = runId;
	            }
	
	            // Stamp existing pending rows (important for first flush)
	            ConcurrentHashMap<String, CampaignRunCallLogMem> map = pendingCallLogsByCampaign.get(campaignId);
	            if (map != null && !map.isEmpty()) {
	                for (CampaignRunCallLogMem r : map.values()) {
	                    if (r != null && r.runId == null) {
	                        r.runId = runId;
	                    }
	                }
	            }
	
	            return;
	
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	            Thread.currentThread().interrupt();
	            return;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return;
	        } finally {
	            if (acquired) runTrackingLock.unlock();
	        }
	    }
	}


	 public static boolean recordCallEvent(
		        Long campaignId,
		        String organization,
		        String campaignName,
		        String fromNumber,
		        String toNumber,
		        String employeeExtension,
		        String channelId,
		        String callState,
		        Long callCost,
		        Long durationMs,
		        String extraJson
		) {

		    final String TAG = "[StartedCampaignData.recordCallEvent] ";

		    System.out.println(TAG + "ENTER"
		            + " campaignId=" + campaignId
		            + " org=" + organization
		            + " campaignName=" + campaignName
		            + " from=" + fromNumber
		            + " to=" + toNumber
		            + " empExt=" + employeeExtension
		            + " channelId=" + channelId
		            + " callState=" + callState
		            + " callCost=" + callCost
		            + " durationMs=" + durationMs);

		    try {
		        // campaignName fallback from existing summary
		        if (campaignName == null && campaignId != null) {
		            CampaignRunSummaryMem ss = runSummaryByCampaign.get(campaignId);
		            System.out.println(TAG + "campaignName null -> try summary lookup foundSummary=" + (ss != null));
		            if (ss != null && ss.campaignName != null) {
		                campaignName = ss.campaignName;
		                System.out.println(TAG + "campaignName resolved from summary=" + campaignName);
		            }
		        }

		        // Ensure run initialized
		        System.out.println(TAG + "ensureRunInitialized()");
		        ensureRunInitialized(campaignId, organization, campaignName);

		        while (true) {
		            boolean acquired = false;
		            try {
		                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
		                System.out.println(TAG + "tryLock timeoutSeconds=" + timeoutSeconds
		                        + " queueLen=" + runTrackingLock.getQueueLength());

		                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
		                if (!acquired) {
		                    System.out.println(TAG + "LOCK NOT ACQUIRED -> backoff()");
		                    backoff();
		                    continue;
		                }

		                // basic validation
		                if (campaignId == null) {
		                    System.out.println(TAG + "RETURN false: campaignId is null");
		                    return false;
		                }
		                if (channelId == null || channelId.trim().isEmpty()) {
		                    System.out.println(TAG + "RETURN false: channelId is null/empty");
		                    return false;
		                }

		                Instant now = Instant.now();

		                CampaignRunSummaryMem s = runSummaryByCampaign.get(campaignId);
		                System.out.println(TAG + "summary exists=" + (s != null));
		                if (s == null) {
		                    System.out.println(TAG + "RETURN false: runSummaryByCampaign missing for campaignId=" + campaignId);
		                    return false;
		                }

		                // -------------------------
		                // Update summary
		                // -------------------------
		                s.lastUpdatedAt = now;
		                s.status = (s.status == null) ? RUN_STATUS_RUNNING : s.status;
		                s.lastFromNumber = fromNumber;
		                s.lastToNumber = toNumber;
		                s.lastDialedPhone = toNumber;
		                s.lastDurationMs = (durationMs == null) ? 0L : durationMs;
		                s.lastCallState = callState;

		                System.out.println(TAG + "summary.update"
		                        + " status=" + s.status
		                        + " lastState=" + s.lastCallState
		                        + " lastDialedPhone=" + s.lastDialedPhone
		                        + " lastDurationMs=" + s.lastDurationMs
		                        + " totalDialed(before)=" + s.totalDialed
		                        + " totalCost(before)=" + s.totalCost
		                        + " runId=" + s.runId);

		                if ("INITIATED".equalsIgnoreCase(callState)) {
		                    s.totalDialed = (s.totalDialed == null) ? 1L : (s.totalDialed + 1L);
		                    System.out.println(TAG + "summary.totalDialed incremented => " + s.totalDialed);
		                }

		                // -------------------------
		                // Upsert row (latest per channelId)
		                // -------------------------
		                ConcurrentHashMap<String, CampaignRunCallLogMem> map = pendingCallLogsByCampaign.get(campaignId);
		                if (map == null) {
		                    System.out.println(TAG + "pendingCallLogsByCampaign missing -> create new map");
		                    map = new ConcurrentHashMap<>();
		                    pendingCallLogsByCampaign.put(campaignId, map);
		                }

		                String ch = channelId.trim();

		                // read previous row cost (to avoid double-counting)
		                CampaignRunCallLogMem prev = map.get(ch);
		                long prevCost = (long) ((prev == null || prev.callCost == 0d) ? 0L : prev.callCost);

		                // normalize incoming cost
		                long newCost = (callCost == null) ? 0L : callCost;

		                System.out.println(TAG + "cost.check"
		                        + " channel=" + ch
		                        + " prevExists=" + (prev != null)
		                        + " prevCost=" + prevCost
		                        + " newCost=" + newCost
		                        + " callState=" + callState);

		                // Only add cost when COMPLETED and only if cost increased
		                if ("COMPLETED".equalsIgnoreCase(callState) && newCost > prevCost) {
		                    long delta = newCost - prevCost;

		                    if (s.totalCost == null) s.totalCost = 0L;
		                    s.totalCost = s.totalCost + delta;

		                    System.out.println(TAG + "summary.totalCost UPDATED"
		                            + " delta=" + delta
		                            + " newTotalCost=" + s.totalCost);
		                } else {
		                    System.out.println(TAG + "summary.totalCost NOT UPDATED"
		                            + " reason="
		                            + (("COMPLETED".equalsIgnoreCase(callState)) ? "newCost<=prevCost" : "callState!=COMPLETED"));
		                }

		                CampaignRunCallLogMem row = new CampaignRunCallLogMem();
		                row.campaignId = campaignId;
		                row.organization = organization;
		                row.campaignName = campaignName;
		                row.eventAt = now;
		                row.channelId = ch;
		                row.fromNumber = fromNumber;
		                row.toNumber = toNumber;
		                row.employeeExtension = employeeExtension;
		                row.callState = callState;
		                row.durationMs = (durationMs == null) ? 0L : durationMs;
		                row.extraJson = extraJson;

		                // runId stamp from summary
		                row.runId = s.runId;
		                row.callCost = newCost;

		                boolean existedBefore = map.containsKey(ch);
		                boolean isNewChannel = !existedBefore;

		                map.put(ch, row);

		                System.out.println(TAG + "row.upsert"
		                        + " isNewChannel=" + isNewChannel
		                        + " existedBefore=" + existedBefore
		                        + " row.runId=" + row.runId
		                        + " row.cost=" + row.callCost
		                        + " row.state=" + row.callState);

		                // pendingCount increments ONLY on new channelId
		                AtomicLong cnt = pendingCountByCampaign.get(campaignId);
		                if (cnt == null) {
		                    System.out.println(TAG + "pendingCountByCampaign missing -> create new AtomicLong");
		                    cnt = new AtomicLong(0L);
		                    pendingCountByCampaign.put(campaignId, cnt);
		                }

		                long pendingDistinct = isNewChannel ? cnt.incrementAndGet() : cnt.get();
		                System.out.println(TAG + "pendingDistinct=" + pendingDistinct + " (incremented=" + isNewChannel + ")");

		                Instant lastFlush = lastFlushAtByCampaign.get(campaignId);
		                if (lastFlush == null) lastFlush = Instant.EPOCH;

		                boolean dueByCount = pendingDistinct >= FLUSH_EVERY_N_RECORDS;
		                boolean dueByTime = lastFlush.plus(FLUSH_EVERY_DURATION).isBefore(now);

		                System.out.println(TAG + "flush.check"
		                        + " dueByCount=" + dueByCount + " pendingDistinct=" + pendingDistinct + " threshold=" + FLUSH_EVERY_N_RECORDS
		                        + " dueByTime=" + dueByTime + " lastFlushAt=" + lastFlush + " now=" + now
		                        + " flushEveryDuration=" + FLUSH_EVERY_DURATION);

		                boolean due = dueByCount || dueByTime;
		                System.out.println(TAG + "EXIT return flushDue=" + due);

		                return due;

		            } catch (InterruptedException e) {
		                System.out.println(TAG + "INTERRUPTED");
		                e.printStackTrace();
		                Thread.currentThread().interrupt();
		                return false;

		            } catch (Exception e) {
		                System.out.println(TAG + "EXCEPTION " + e.getMessage());
		                e.printStackTrace();
		                return false;

		            } finally {
		                if (acquired) {
		                    runTrackingLock.unlock();
		                    System.out.println(TAG + "LOCK RELEASED");
		                }
		            }
		        }

		    } catch (Exception e) {
		        System.out.println(TAG + "FATAL EXCEPTION " + e.getMessage());
		        e.printStackTrace();
		        return false;
		    }
		}



    public static void markRunStatus(Long campaignId, String status) {
        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                CampaignRunSummaryMem s = runSummaryByCampaign.get(campaignId);
                if (s != null) {
                    s.status = status;
                    s.lastUpdatedAt = Instant.now();
                }

                return;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } finally {
                if (acquired) runTrackingLock.unlock();
            }
        }
    }

    public static void forceMarkFlushDue(Long campaignId) {
        try {
            AtomicLong cnt = pendingCountByCampaign.get(campaignId);
            if (cnt == null) {
                cnt = new AtomicLong(0L);
                pendingCountByCampaign.put(campaignId, cnt);
            }
            cnt.set(FLUSH_EVERY_N_RECORDS);

            lastFlushAtByCampaign.put(campaignId, Instant.EPOCH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void forceMarkAllFlushDue() {
        try {
            List<Long> ids = listCampaignIdsWithPendingLogs();
            if (ids == null || ids.isEmpty()) return;

            for (Long id : ids) {
                forceMarkFlushDue(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Atomic snapshot for flush decision + UI.
     * This prevents "pending from now" and "lastFlushAt from earlier" being read inconsistently.
     */
    public static FlushHintMem snapshotFlushHint(Long campaignId) {
        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                AtomicLong cnt = pendingCountByCampaign.get(campaignId);
                long pending = (cnt == null) ? 0L : cnt.get();

                Instant lastFlush = lastFlushAtByCampaign.get(campaignId);
                if (lastFlush == null) lastFlush = Instant.EPOCH;

                FlushHintMem hint = new FlushHintMem();
                hint.campaignId = campaignId;
                hint.pendingCount = pending;
                hint.lastFlushAt = lastFlush;
                return hint;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                FlushHintMem hint = new FlushHintMem();
                hint.campaignId = campaignId;
                hint.pendingCount = 0L;
                hint.lastFlushAt = Instant.EPOCH;
                return hint;
            } catch (Exception e) {
                e.printStackTrace();
                FlushHintMem hint = new FlushHintMem();
                hint.campaignId = campaignId;
                hint.pendingCount = 0L;
                hint.lastFlushAt = Instant.EPOCH;
                return hint;
            } finally {
                if (acquired) runTrackingLock.unlock();
            }
        }
    }

    /**
     * Optional helper (still kept for compatibility).
     */
    public static boolean isFlushDue(Long campaignId) {
        FlushHintMem hint = snapshotFlushHint(campaignId);
        boolean dueByCount = hint.pendingCount >= FLUSH_EVERY_N_RECORDS;
        boolean dueByTime = hint.lastFlushAt != null
                && hint.lastFlushAt.plus(FLUSH_EVERY_DURATION).isBefore(Instant.now());
        return dueByCount || dueByTime;
    }

    /**
     * Snapshot current run summary from memory (read-only copy).
     */
    public static CampaignRunSummaryMem snapshotRunSummary(Long campaignId) {
        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                CampaignRunSummaryMem s = runSummaryByCampaign.get(campaignId);
                if (s == null) return null;
                return s.copy();

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (acquired) runTrackingLock.unlock();
            }
        }
    }

    public static List<CampaignRunCallLogMem> snapshotPendingTail(Long campaignId, int max) {
        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                ConcurrentHashMap<String, CampaignRunCallLogMem> map = pendingCallLogsByCampaign.get(campaignId);
                if (map == null || map.isEmpty()) return Collections.emptyList();

                List<CampaignRunCallLogMem> all = new ArrayList<>(map.size());
                for (CampaignRunCallLogMem r : map.values()) {
                    if (r != null) all.add(r.copy());
                }

                // newest first
                all.sort((a, b) -> {
                    Instant ia = (a == null) ? null : a.eventAt;
                    Instant ib = (b == null) ? null : b.eventAt;
                    if (ia == null && ib == null) return 0;
                    if (ia == null) return 1;
                    if (ib == null) return -1;
                    return ib.compareTo(ia);
                });

                int limit = Math.max(1, max);
                if (all.size() > limit) {
                    return new ArrayList<>(all.subList(0, limit));
                }
                return all;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            } finally {
                if (acquired) runTrackingLock.unlock();
            }
        }
    }


    public static List<CampaignRunCallLogMem> drainPendingLogs(Long campaignId) {
        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                ConcurrentHashMap<String, CampaignRunCallLogMem> map = pendingCallLogsByCampaign.get(campaignId);
                if (map == null || map.isEmpty()) return Collections.emptyList();

                List<CampaignRunCallLogMem> drained = new ArrayList<>(map.size());
                for (CampaignRunCallLogMem r : map.values()) {
                    if (r != null) drained.add(r.copy());
                }

                map.clear();

                AtomicLong cnt = pendingCountByCampaign.get(campaignId);
                if (cnt != null) cnt.set(0L);

                lastFlushAtByCampaign.put(campaignId, Instant.now());

                return drained;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            } finally {
                if (acquired) runTrackingLock.unlock();
            }
        }
    }

    
    public static List<Long> listCampaignIdsWithPendingLogs() {
        while (true) {
            boolean acquired = false;
            try {
                int timeoutSeconds = runTrackingLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = runTrackingLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    backoff();
                    continue;
                }

                List<Long> out = new ArrayList<>();
                for (Map.Entry<Long, ConcurrentHashMap<String, CampaignRunCallLogMem>> e : pendingCallLogsByCampaign.entrySet()) {
                    ConcurrentHashMap<String, CampaignRunCallLogMem> map = e.getValue();
                    if (map != null && !map.isEmpty()) out.add(e.getKey());
                }
                return out;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return Collections.emptyList();
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            } finally {
                if (acquired) runTrackingLock.unlock();
            }
        }
    }


    public static long getPendingCount(Long campaignId) {
        AtomicLong cnt = pendingCountByCampaign.get(campaignId);
        return (cnt == null) ? 0L : cnt.get();
    }

    public static Instant getLastFlushAt(Long campaignId) {
        Instant t = lastFlushAtByCampaign.get(campaignId);
        return (t == null) ? Instant.EPOCH : t;
    }

    // -----------------------------
    // Inner memory models
    // -----------------------------

    public static class CampaignRunSummaryMem {
        public Long campaignId;

        // NEW: DB runId (CampaignRunDetails.id). Set by service once created.
        public Long runId;

        public String organization;
        public String campaignName;
        public Instant startedAt;
        public Instant lastUpdatedAt;

        public String lastDialedPhone;
        public Long totalDialed;
        public Long totalCost;
        public String status;        // RUNNING / STOPPED / COMPLETED
        public String lastFromNumber;
        public String lastToNumber;
        public Long lastDurationMs;
        public String lastCallState;

        public CampaignRunSummaryMem copy() {
            CampaignRunSummaryMem c = new CampaignRunSummaryMem();
            c.campaignId = this.campaignId;
            c.runId = this.runId; // NEW
            c.organization = this.organization;
            c.campaignName = this.campaignName;
            c.startedAt = this.startedAt;
            c.lastUpdatedAt = this.lastUpdatedAt;
            c.lastDialedPhone = this.lastDialedPhone;
            c.totalDialed = this.totalDialed;
            c.totalCost = this.totalCost;
            c.status = this.status;
            c.lastFromNumber = this.lastFromNumber;
            c.lastToNumber = this.lastToNumber;
            c.lastDurationMs = this.lastDurationMs;
            c.lastCallState = this.lastCallState;
            return c;
        }
    }

    public static class CampaignRunCallLogMem {
        public Long campaignId;

        // NEW: DB runId (CampaignRunDetails.id). Stamped from CampaignRunSummaryMem.runId.
        public Long runId;

        public String organization;
        public String campaignName;
        public Instant eventAt;

        public String channelId;
        public String fromNumber;
        public String toNumber;
        public String employeeExtension;
        public double callCost;
        public String callState;   // INITIATED / RINGING / IN_PROGRESS / COMPLETED / FAILED / BUSY / NOANSWER
        public Long durationMs;
        public String extraJson;   // optional debug payload

        public CampaignRunCallLogMem copy() {
            CampaignRunCallLogMem c = new CampaignRunCallLogMem();
            c.campaignId = this.campaignId;
            c.runId = this.runId; // NEW
            c.organization = this.organization;
            c.campaignName = this.campaignName;
            c.eventAt = this.eventAt;
            c.channelId = this.channelId;
            c.fromNumber = this.fromNumber;
            c.toNumber = this.toNumber;
            c.employeeExtension = this.employeeExtension;
            c.callState = this.callState;
            c.callCost = this.callCost;
            c.durationMs = this.durationMs;
            c.extraJson = this.extraJson;
            return c;
        }
    }

    public static class FlushHintMem {
        public Long campaignId;
        public long pendingCount;
        public Instant lastFlushAt;

        public FlushHintMem copy() {
            FlushHintMem c = new FlushHintMem();
            c.campaignId = this.campaignId;
            c.pendingCount = this.pendingCount;
            c.lastFlushAt = this.lastFlushAt;
            return c;
        }
    }
    
    public static PageInfoDTO getPageInfoForCampaign(Long campaignId) {
        if (campaignId == null) return null;
        return updateOrGetCampaignCommonPageInfo(campaignId, null, "get");
    }

    public static int getConfiguredPageSize() {
        return getPageSize(); // returns 3000
    }

}
