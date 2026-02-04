// ============================================================
// FILE: src/main/java/com/mylinehub/crm/service/CampaignRunTrackingService.java
// ============================================================
package com.mylinehub.crm.service;

import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.data.dto.PageInfoDTO;
import com.mylinehub.crm.entity.Campaign;
import com.mylinehub.crm.entity.CampaignRunCallLog;
import com.mylinehub.crm.entity.CampaignRunDetails;
import com.mylinehub.crm.entity.dto.CampaignRunCallLogRowDTO;
import com.mylinehub.crm.entity.dto.CampaignRunPageInfoDTO;
import com.mylinehub.crm.enums.AUTODIALER_TYPE;
import com.mylinehub.crm.repository.CampaignRunCallLogRepository;
import com.mylinehub.crm.repository.CampaignRunDetailsRepository;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.mylinehub.crm.repository.CampaignRepository;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CampaignRunTrackingService {

    private final CampaignRunDetailsRepository runDetailsRepo;
    private final CampaignRepository campaignRepo;
    private final CampaignRunCallLogRepository callLogRepo;
    private final RestTemplate restTemplate;
    
    int lastNSecondsMemoryDataOnly = 300;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter IST_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(IST);

    private static final DateTimeFormatter RUN_LABEL_FMT =
            DateTimeFormatter.ofPattern("ddMMMMuuuu_hh:mma", Locale.ENGLISH);
    
    // ============================================================
    // DEBUG HELPERS (System.out ONLY)
    // ============================================================

    private static String rid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static void logI(String rid, String msg) {
        System.out.println("[CRT][" + rid + "] " + msg);
    }

    private static void logE(String rid, String msg, Exception e) {
        System.err.println("[CRT][" + rid + "] " + msg);
        if (e != null) e.printStackTrace();
    }

    private static String safe(Object o) {
        return (o == null) ? "null" : String.valueOf(o);
    }

    private static int sizeOf(Collection<?> c) {
        return (c == null) ? -1 : c.size();
    }

    // ============================================================
    // FLUSH (DB WRITE BRIDGE) - used by scheduler runnable
    // ============================================================

    @Transactional
    public void flushIfDue(Long campaignId) {
        String _rid = rid();
        logI(_rid, "ENTER flushIfDue(campaignId=" + campaignId + ")");

        if (campaignId == null) {
            logI(_rid, "flushIfDue -> campaignId is null, RETURN");
            return;
        }

        boolean due = false;
        try {
            due = StartedCampaignData.isFlushDue(campaignId);
            logI(_rid, "flushIfDue -> StartedCampaignData.isFlushDue(" + campaignId + ")=" + due);
        } catch (Exception e) {
            logE(_rid, "flushIfDue -> ERROR in StartedCampaignData.isFlushDue(" + campaignId + ")", e);
            return;
        }

        if (!due) {
            logI(_rid, "flushIfDue -> not due, RETURN");
            return;
        }

        logI(_rid, "flushIfDue -> due=true, calling flushNow()");
        flushNow(campaignId);

        logI(_rid, "EXIT flushIfDue(campaignId=" + campaignId + ")");
    }

    @Transactional
    public void flushNow(Long campaignId) {
        String _rid = rid();
        logI(_rid, "ENTER flushNow(campaignId=" + campaignId + ")");

        if (campaignId == null) {
            logI(_rid, "flushNow -> campaignId is null, RETURN");
            return;
        }

        StartedCampaignData.CampaignRunSummaryMem mem = null;
        try {
            mem = StartedCampaignData.snapshotRunSummary(campaignId);
            logI(_rid, "flushNow -> snapshotRunSummary(" + campaignId + ") mem=" + (mem == null ? "null" : ("runId=" + mem.runId + ", status=" + safe(mem.status))));
        } catch (Exception e) {
            logE(_rid, "flushNow -> ERROR snapshotRunSummary(" + campaignId + ")", e);
        }

        List<StartedCampaignData.CampaignRunCallLogMem> drained = null;
        try {
            drained = StartedCampaignData.drainPendingLogs(campaignId);
            logI(_rid, "flushNow -> drainPendingLogs(" + campaignId + ") drainedSize=" + sizeOf(drained));
        } catch (Exception e) {
            logE(_rid, "flushNow -> ERROR drainPendingLogs(" + campaignId + ")", e);
        }

        if ((drained == null || drained.isEmpty()) && mem == null) {
            logI(_rid, "flushNow -> drained empty AND mem null => NOTHING TO FLUSH, RETURN");
            return;
        }

        Long runId = (mem != null) ? mem.runId : null;
        CampaignRunDetails runDetails = null;

        logI(_rid, "flushNow -> resolved runId from mem=" + runId);

        if (runId != null) {
            try {
                runDetails = runDetailsRepo.findByIdAndCampaignId(runId, campaignId).orElse(null);
                logI(_rid, "flushNow -> runDetailsRepo.findByIdAndCampaignId(runId=" + runId + ", campaignId=" + campaignId + ") found=" + (runDetails != null));
            } catch (Exception e) {
                logE(_rid, "flushNow -> ERROR findByIdAndCampaignId(runId=" + runId + ", campaignId=" + campaignId + ")", e);
            }
        } else {
            logI(_rid, "flushNow -> runId is null, will create new runDetails");
        }

        if (runDetails == null) {
            logI(_rid, "flushNow -> creating NEW CampaignRunDetails (db row)");
            runDetails = new CampaignRunDetails();
            runDetails.setCampaignId(campaignId);

            try {
                if (mem != null) {
                    logI(_rid, "flushNow -> initializing runDetails from mem");
                    runDetails.setOrganization(mem.organization);
                    runDetails.setCampaignName(mem.campaignName);
                    runDetails.setStartedAt(mem.startedAt != null ? mem.startedAt : Instant.now());
                    runDetails.setLastUpdatedAt(mem.lastUpdatedAt != null ? mem.lastUpdatedAt : Instant.now());
                    runDetails.setStatus(mem.status);
                    runDetails.setLastDialedPhone(mem.lastDialedPhone);
                    runDetails.setTotalDialed(mem.totalDialed != null ? mem.totalDialed : 0L);
                    runDetails.setTotalCost(mem.totalCost != null ? mem.totalCost : 0L);
                } else {
                    logI(_rid, "flushNow -> initializing runDetails from defaults (mem=null)");
                    runDetails.setStartedAt(Instant.now());
                    runDetails.setLastUpdatedAt(Instant.now());
                    runDetails.setStatus(StartedCampaignData.RUN_STATUS_RUNNING);
                    runDetails.setTotalDialed(0L);
                    runDetails.setTotalCost(0L);
                }

                runDetails = runDetailsRepo.save(runDetails);
                logI(_rid, "flushNow -> runDetails saved. new runDetailsId=" + (runDetails != null ? runDetails.getId() : null));

                if (runDetails != null) {
                    StartedCampaignData.setRunIdForCampaign(campaignId, runDetails.getId());
                    runId = runDetails.getId();
                    logI(_rid, "flushNow -> setRunIdForCampaign(campaignId=" + campaignId + ", runId=" + runId + ")");
                }
            } catch (Exception e) {
                logE(_rid, "flushNow -> ERROR creating/saving new runDetails", e);
            }
        }

        // UPSERT drained call logs
        if (drained != null && !drained.isEmpty()) {
            try {
                logI(_rid, "flushNow -> batchUpsertCallLogs drainedSize=" + drained.size() + ", runId=" + runId + ", campaignId=" + campaignId);
                callLogRepo.batchUpsertCallLogs(drained, runId, campaignId);
                logI(_rid, "flushNow -> batchUpsertCallLogs DONE");
            } catch (Exception e) {
                logE(_rid, "flushNow -> ERROR batchUpsertCallLogs", e);
            }
        } else {
            logI(_rid, "flushNow -> drained empty, skip batchUpsertCallLogs");
        }

        if (mem != null && runDetails != null) {
            try {
                logI(_rid, "flushNow -> updating runDetails from mem and saving (upsert summary)");
                runDetails.setOrganization(mem.organization);
                runDetails.setCampaignName(mem.campaignName);
                runDetails.setLastUpdatedAt(mem.lastUpdatedAt != null ? mem.lastUpdatedAt : Instant.now());
                runDetails.setStatus(mem.status);
                runDetails.setLastDialedPhone(mem.lastDialedPhone);
                runDetails.setTotalDialed(mem.totalDialed != null ? mem.totalDialed : 0L);
                runDetails.setTotalCost(mem.totalCost != null ? mem.totalCost : 0L);
                runDetailsRepo.save(runDetails);
                logI(_rid, "flushNow -> runDetailsRepo.save(summary) DONE");
            } catch (Exception e) {
                logE(_rid, "flushNow -> ERROR saving updated runDetails summary", e);
            }
        } else {
            logI(_rid, "flushNow -> mem is null OR runDetails is null, skip summary save");
        }

        logI(_rid, "EXIT flushNow(campaignId=" + campaignId + ")");
    }

    
    // ============================================================
    // 1A) OLD RUN TABLE (DB ONLY) + SEARCH + PAGINATION (STABLE)
    // ============================================================

    /**
     * IMPORTANT:
     * - This is for old runs (explicit runId selection)
     * - DB ONLY (NO memory overlay)
     * - Pagination is stable because DB does paging.
     */
    public CampaignRunPageInfoDTO getCallLogsMergedForRun(
            Long campaignId,
            Long runId,
            Pageable pageable,
            String searchText
    ) {
        String _rid = rid();
        logI(_rid, "ENTER getCallLogsMergedForRun(campaignId=" + campaignId + ", runId=" + runId
                + ", pageNumber=" + (pageable == null ? "null" : pageable.getPageNumber())
                + ", pageSize=" + (pageable == null ? "null" : pageable.getPageSize())
                + ", searchText=" + safe(searchText) + ")");

        if (campaignId == null || runId == null) {
            logI(_rid, "getCallLogsMergedForRun -> campaignId or runId null, returning empty page");
            return CampaignRunPageInfoDTO.builder()
                    .totalRecords(0)
                    .numberOfPages(0)
                    .stateCounts(Collections.emptyMap())
                    .runNumber(0)
                    .data(Collections.emptyList())
                    .build();
        }

        String q = normalize(searchText);
        logI(_rid, "getCallLogsMergedForRun -> normalized search q=[" + q + "]");

        Page<CampaignRunCallLog> dbPage = null;
        try {
            dbPage = (q.isEmpty())
                    ? callLogRepo.findByRunIdOrderByEventAtDesc(runId, pageable)
                    : callLogRepo.findByRunIdWithSearchOrderByEventAtDesc(runId, q, pageable);

            logI(_rid, "getCallLogsMergedForRun -> dbPage: totalElements=" + dbPage.getTotalElements()
                    + ", totalPages=" + dbPage.getTotalPages()
                    + ", contentSize=" + sizeOf(dbPage.getContent()));
        } catch (Exception e) {
            logE(_rid, "getCallLogsMergedForRun -> ERROR fetching db page", e);
            return CampaignRunPageInfoDTO.builder()
                    .totalRecords(0)
                    .numberOfPages(0)
                    .runNumber(0)
                    .stateCounts(Collections.emptyMap())
                    .data(Collections.emptyList())
                    .build();
        }

        List<CampaignRunCallLogRowDTO> rows = new ArrayList<>();
        for (CampaignRunCallLog e : dbPage.getContent()) {
            if (e == null) continue;
            rows.add(toRowDto(e));
        }
        
        List<Object[]> counts =
                (q.isEmpty())
                        ? callLogRepo.countByStateForRun(runId)
                        : callLogRepo.countByStateForRunWithSearch(runId, q);

        Map<String, Integer> stateCounts = toStateCountsMap(counts);

        CampaignRunDetails run = runDetailsRepo.findByIdAndCampaignId(runId, campaignId).orElse(null);

        String campaignName = (run != null) ? run.getCampaignName() : null;
        String campaignRunDate = (run != null && run.getStartedAt() != null) ? IST_FMT.format(run.getStartedAt()) : null;

        Long totalDialed = (run != null && run.getTotalDialed() != null) ? run.getTotalDialed() : 0L;
        Long totalCost   = (run != null && run.getTotalCost() != null)   ? run.getTotalCost()   : 0L;
        
        return CampaignRunPageInfoDTO.builder()
                .totalRecords(dbPage.getTotalElements())
                .numberOfPages(dbPage.getTotalPages())
                .runNumber(0)
                .campaignName(campaignName)
                .campaignRunDate(campaignRunDate)
                .totalCost(totalCost)
                .totalDialed(totalDialed)
                .stateCounts(stateCounts)
                .data(rows)
                .build();

    }
    
    
    private Map<String, Integer> toStateCountsMap(List<Object[]> rows) {
        Map<String, Integer> map = new LinkedHashMap<>(); // stable order

        if (rows == null) return map;

        for (Object[] r : rows) {
            String state = (r[0] == null) ? "UNACCEPTED" : String.valueOf(r[0]).trim().toUpperCase();
            long cnt = (r[1] == null) ? 0L : ((Number) r[1]).longValue();

            int c = (cnt > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) cnt;

            // new key -> set to c, else increment
            map.put(state, map.getOrDefault(state, 0) + c);
        }
        return map;
    }


   
    public CampaignRunPageInfoDTO getCurrentRunLiveLogsMemoryOnly(
            Long campaignId,
            String searchText
    ) {
        String _rid = rid();
        logI(_rid, "ENTER getCurrentRunLiveLogsMemoryOnly(campaignId=" + campaignId
                + ", searchText=" + safe(searchText) + ")");

        if (campaignId == null) {
            return CampaignRunPageInfoDTO.builder()
                    .totalRecords(0)
                    .numberOfPages(0)
                    .runNumber(0)
                    .stateCounts(Collections.emptyMap())
                    .data(Collections.emptyList())
                    .build();
        }

        String q = normalize(searchText);
        
        int intcutOffSeconds = lastNSecondsMemoryDataOnly;

        Map<Long, Campaign> cm =
                StartedCampaignData.workOnAllActiveCampaigns(campaignId, null, "get-one");
        Campaign cc = (cm != null) ? cm.get(campaignId) : null;

        String at = (cc == null) ? null : cc.getAutodialertype();

        if (AUTODIALER_TYPE.WHATSAPP_MESSAGE.name().equalsIgnoreCase(at)) {
            intcutOffSeconds = 30;
        }

        Instant cutoff = Instant.now().minus(Duration.ofSeconds(intcutOffSeconds));

        //  get current runId (so we don't mix runs)
        StartedCampaignData.CampaignRunSummaryMem mem = null;
        Long runId = null;
        try {
            mem = StartedCampaignData.snapshotRunSummary(campaignId);
            runId = (mem == null) ? null : mem.runId;
            logI(_rid, "MEM_ONLY -> snapshotRunSummary runId=" + runId);
        } catch (Exception e) {
            logE(_rid, "MEM_ONLY -> ERROR snapshotRunSummary(" + campaignId + ")", e);
        }

        //  since StartedCampaignData now stores latest row per channelId,
        // snapshotPendingTail already returns distinct channel rows.
        List<StartedCampaignData.CampaignRunCallLogMem> pending;
        try {
            pending = StartedCampaignData.snapshotPendingTail(campaignId, Integer.MAX_VALUE);
            logI(_rid, "MEM_ONLY -> pending distinct snapshot size=" + sizeOf(pending));
        } catch (Exception e) {
            logE(_rid, "MEM_ONLY -> ERROR snapshotPendingTail(" + campaignId + ")", e);
            pending = Collections.emptyList();
        }

        if (pending == null || pending.isEmpty()) {
            return CampaignRunPageInfoDTO.builder()
                    .totalRecords(0)
                    .numberOfPages(0)
                    .runNumber(0)
                    .stateCounts(Collections.emptyMap())
                    .data(Collections.emptyList())
                    .build();
        }

         // Filter: last N seconds + optional search + optional runId match
        List<StartedCampaignData.CampaignRunCallLogMem> filtered = new ArrayList<>();
        for (StartedCampaignData.CampaignRunCallLogMem r : pending) {
            if (r == null) continue;
            if (r.channelId == null || r.channelId.trim().isEmpty()) continue;
            if (r.eventAt == null) continue;

            if (r.eventAt.isBefore(cutoff)) continue;

            // If runId exists, enforce it (avoid mixing runs)
            if (runId != null) {
                if (r.runId == null || !runId.equals(r.runId)) continue;
            }

            if (!passesSearch(r, q)) continue;

            filtered.add(r);
        }

        if (filtered.isEmpty()) {
            return CampaignRunPageInfoDTO.builder()
                    .totalRecords(0)
                    .numberOfPages(0)
                    .runNumber(0)
                    .stateCounts(Collections.emptyMap())
                    .data(Collections.emptyList())
                    .build();
        }

        // newest first
        filtered.sort((a, b) -> {
            Instant ia = (a == null) ? null : a.eventAt;
            Instant ib = (b == null) ? null : b.eventAt;
            if (ia == null && ib == null) return 0;
            if (ia == null) return 1;
            if (ib == null) return -1;
            return ib.compareTo(ia);
        });

        List<CampaignRunCallLogRowDTO> outRows = new ArrayList<>(filtered.size());
        for (StartedCampaignData.CampaignRunCallLogMem r : filtered) {
            outRows.add(toRowDto(r));
        }

        double totalRecords = outRows.size();
        double numberOfPages = (totalRecords == 0) ? 0 : 1;

        logI(_rid, "EXIT getCurrentRunLiveLogsMemoryOnly -> cutoff=" + cutoff
                + ", runId=" + runId
                + ", outRowsSize=" + outRows.size());

        int runNumber = computeRunNumberFromMemory(campaignId);

        return CampaignRunPageInfoDTO.builder()
                .totalRecords(totalRecords)
                .numberOfPages(numberOfPages)
                .runNumber(runNumber)
                .stateCounts(Collections.emptyMap())
                .data(outRows)
                .build();
    }

    
    public List<String> listCampaignIdNameMerged(String organization) {
        String _rid = rid();
        logI(_rid, "ENTER listCampaignIdNameMerged(org=" + organization + ")");

        // id -> name
        LinkedHashMap<Long, String> map = new LinkedHashMap<>();

        // 1) DB (latest name per campaignId)
        try {
            List<Object[]> pairs = runDetailsRepo.findLatestCampaignIdNamePairsForOrg(organization);
            logI(_rid, "DB campaign pairs size=" + sizeOf(pairs));

            if (pairs != null) {
                for (Object[] r : pairs) {
                    if (r == null || r.length < 2) continue;
                    Long cid = (r[0] == null) ? null : ((Number) r[0]).longValue();
                    String name = (r[1] == null) ? "" : String.valueOf(r[1]).trim();
                    if (cid == null) continue;
                    map.putIfAbsent(cid, name);
                }
            }
        } catch (Exception e) {
            logE(_rid, "ERROR DB findLatestCampaignIdNamePairsForOrg", e);
        }

        List<String> out = new ArrayList<>();
        for (Map.Entry<Long, String> e : map.entrySet()) {
            out.add(e.getKey() + " - " + (e.getValue() == null ? "" : e.getValue()));
        }

        logI(_rid, "EXIT listCampaignIdNameMerged -> outSize=" + out.size());
        return out;
    }

    
    public List<String> listRunIdDateMerged(Long campaignId, String organization) {
        String _rid = rid();
        logI(_rid, "ENTER listRunIdDateMerged(campaignId=" + campaignId + ", org=" + organization + ")");

        LinkedHashMap<Long, Instant> map = new LinkedHashMap<>();

        // 1) DB runs
        try {
            List<Object[]> rows = runDetailsRepo.findRunIdAndStartedAtForCampaignOrg(organization, campaignId);
            logI(_rid, "DB run rows size=" + sizeOf(rows));
            if (rows != null) {
                for (Object[] r : rows) {
                    if (r == null || r.length < 2) continue;
                    Long runId = (r[0] == null) ? null : ((Number) r[0]).longValue();
                    Instant startedAt = null;
                    if (r[1] instanceof java.sql.Timestamp) {
                        startedAt = ((java.sql.Timestamp) r[1]).toInstant();
                    } else if (r[1] instanceof java.util.Date) {
                        startedAt = Instant.ofEpochMilli(((java.util.Date) r[1]).getTime());
                    } else if (r[1] instanceof Instant) {
                        startedAt = (Instant) r[1];
                    }
                    if (runId == null) continue;
                    map.putIfAbsent(runId, startedAt);
                }
            }
        } catch (Exception e) {
            logE(_rid, "ERROR DB findRunIdAndStartedAtForCampaignOrg", e);
        }
        
        List<String> out = new ArrayList<>();
        for (Map.Entry<Long, Instant> e : map.entrySet()) {
            String dt = "";
            if (e.getValue() != null) {
                dt = RUN_LABEL_FMT.format(e.getValue().atZone(IST)).toUpperCase(Locale.ENGLISH);
            }
            out.add(e.getKey() + " - " + dt);
        }

        logI(_rid, "EXIT listRunIdDateMerged -> outSize=" + out.size());
        return out;
    }
    
    public byte[] exportRunExcelDbOnly(Long campaignId, Long runId, String organization) {
        String _rid = rid();
        logI(_rid, "ENTER exportRunExcelDbOnly(campaignId=" + campaignId + ", runId=" + runId + ", org=" + organization + ")");

        if (campaignId == null || runId == null || organization == null || organization.trim().isEmpty()) {
            throw new IllegalArgumentException("campaignId/runId/organization required");
        }

        // -------------------------
        // 1) Fetch RunDetails (DB ONLY)
        // -------------------------
        CampaignRunDetails run = runDetailsRepo.findByIdAndCampaignId(runId, campaignId).orElse(null);
        if (run == null) {
            throw new IllegalArgumentException("Run not found for campaignId=" + campaignId + ", runId=" + runId);
        }

        // SECURITY / correctness: ensure org matches
        if (run.getOrganization() != null && !run.getOrganization().trim().equalsIgnoreCase(organization.trim())) {
            throw new IllegalArgumentException("Run does not belong to organization=" + organization);
        }

        // -------------------------
        // 2) Fetch CallLogs (DB ONLY)
        // -------------------------
        List<CampaignRunCallLog> dbLogs = callLogRepo.findByRunIdOrderByEventAtAsc(runId);
        logI(_rid, "export(dbOnly) -> dbLogs size=" + sizeOf(dbLogs));

        // -------------------------
        // 3) Build Excel
        // -------------------------
        try (Workbook wb = new XSSFWorkbook()) {
            CreationHelper helper = wb.getCreationHelper();

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // NOTE: You are writing IST as string; dateStyle not required.
            // If you want true Excel date cells later, we can change.

            // -------------------------
            // Sheet 1: RunDetails
            // -------------------------
            Sheet s1 = wb.createSheet("RunDetails");

            String[] cols1 = new String[] {
            	    "campaignId", "campaignName", "runId", "organization",
            	    "startedAt(IST)", "lastUpdatedAt(IST)", "status",
            	    "lastDialedPhone", "totalDialed", "totalCost"
            	};


            Row h1 = s1.createRow(0);
            for (int i = 0; i < cols1.length; i++) {
                Cell c = h1.createCell(i);
                c.setCellValue(cols1[i]);
                c.setCellStyle(headerStyle);
            }

            Row r1 = s1.createRow(1);

            r1.createCell(0).setCellValue(campaignId);
            r1.createCell(1).setCellValue(nz(run.getCampaignName()));
            r1.createCell(2).setCellValue(runId);
            r1.createCell(3).setCellValue(nz(run.getOrganization()));

            r1.createCell(4).setCellValue(run.getStartedAt() == null ? "" : IST_FMT.format(run.getStartedAt()));
            r1.createCell(5).setCellValue(run.getLastUpdatedAt() == null ? "" : IST_FMT.format(run.getLastUpdatedAt()));
            r1.createCell(6).setCellValue(nz(run.getStatus()));
            r1.createCell(7).setCellValue(nz(run.getLastDialedPhone()));
            r1.createCell(8).setCellValue(run.getTotalDialed() == null ? 0L : run.getTotalDialed());
            r1.createCell(9).setCellValue(run.getTotalCost() == null ? 0L : run.getTotalCost());

            for (int i = 0; i < cols1.length; i++) s1.autoSizeColumn(i);

            // -------------------------
            // Sheet 2: CallLogs (DB ONLY)
            // -------------------------
            Sheet s2 = wb.createSheet("CallLogs");

            String[] cols2 = new String[] {
                    "eventAt(IST)", "channelId", "fromNumber", "toNumber",
                    "employeeExtension", "callState", "durationMs", "extraJson"
            };

            Row h2 = s2.createRow(0);
            for (int i = 0; i < cols2.length; i++) {
                Cell c = h2.createCell(i);
                c.setCellValue(cols2[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (dbLogs != null) {
                for (CampaignRunCallLog e : dbLogs) {
                    if (e == null) continue;
                    Row rr = s2.createRow(rowIdx++);

                    rr.createCell(0).setCellValue(e.getEventAt() == null ? "" : IST_FMT.format(e.getEventAt()));
                    rr.createCell(1).setCellValue(nz(e.getChannelId()));
                    rr.createCell(2).setCellValue(nz(e.getFromNumber()));
                    rr.createCell(3).setCellValue(nz(e.getToNumber()));
                    rr.createCell(4).setCellValue(nz(e.getEmployeeExtension()));
                    rr.createCell(5).setCellValue(nz(e.getCallState()));
                    rr.createCell(6).setCellValue(e.getDurationMs() == null ? 0 : e.getDurationMs());
                    rr.createCell(7).setCellValue(nz(e.getExtraJson()));
                }
            }

            for (int i = 0; i < cols2.length; i++) s2.autoSizeColumn(i);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);

            logI(_rid, "EXIT exportRunExcelDbOnly -> bytes=" + bos.size());
            return bos.toByteArray();
        } catch (Exception e) {
            logE(_rid, "ERROR building excel (dbOnly)", e);
            throw new RuntimeException("Failed to create excel", e);
        }
    }

    
    public byte[] exportRunRecordings(Long campaignId, Long runId, String organization) {
        System.out.println("ENTER TrackingService.exportRunRecordings(campaignId=" + campaignId + ", runId=" + runId + ", org=" + organization + ")");

        if (campaignId == null || runId == null || organization == null || organization.trim().isEmpty()) {
            throw new IllegalArgumentException("campaignId/runId/organization required");
        }

        // -------------------------
        // 1) Fetch Campaign (for ai recording config)
        // -------------------------
        Campaign campaign = campaignRepo.findById(campaignId).orElse(null);
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign not found for campaignId=" + campaignId);
        }

        if (campaign.getOrganization() != null
                && !campaign.getOrganization().trim().equalsIgnoreCase(organization.trim())) {
            throw new IllegalArgumentException("Campaign does not belong to organization=" + organization);
        }

        String domain = nz(campaign.getAiRecordingDomin());               // domain (typo in field name)
        String port = nz(campaign.getAiRecordingPort());
        String serverPath = nz(campaign.getAiRecordingServerPath());      // optional
        String serverToken = nz(campaign.getAiRecordingServerToken());    // token to call voicebridge

        System.out.println("Campaign recording cfg: domain=" + domain + ", port=" + port +
                ", serverPath=" + serverPath + ", tokenPresent=" + (!serverToken.isEmpty()));

        if (domain.isEmpty() || port.isEmpty()) {
            throw new IllegalArgumentException("aiRecordingDomin/aiRecordingPort required for localhost server type");
        }

        if (serverToken.isEmpty()) {
            throw new IllegalArgumentException("aiRecordingServerToken required");
        }

        // -------------------------
        // 2) Fetch channelIds for run+campaign
        // -------------------------
        List<String> channelIds = callLogRepo.findDistinctChannelIdsByRunIdAndCampaignId(runId, campaignId);
        if (channelIds == null || channelIds.isEmpty()) {
            throw new IllegalArgumentException("No channelIds found for campaignId=" + campaignId + ", runId=" + runId);
        }

        channelIds = channelIds.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .toList();

        if (channelIds.isEmpty()) {
            throw new IllegalArgumentException("No valid channelIds found for campaignId=" + campaignId + ", runId=" + runId);
        }

        System.out.println("Found channelIds=" + channelIds.size());

        // -------------------------
        // 3) Call VoiceBridge POST to build ZIP
        // -------------------------
        //String url = "http://" + domain + ":" + port + "/internal/call-history/recordings/zip-by-channel";

        String url = "http://" + domain + ":" + port + serverPath;

        VoiceBridgeZipRequest req = new VoiceBridgeZipRequest();
        req.token = serverToken;
        req.organization = organization.trim();
        req.channelIds = channelIds;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<VoiceBridgeZipRequest> entity = new HttpEntity<>(req, headers);

            ResponseEntity<byte[]> resp = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("VoiceBridge returned status=" + resp.getStatusCode());
            }

            byte[] zipBytes = resp.getBody();
            if (zipBytes == null || zipBytes.length == 0) {
                throw new RuntimeException("VoiceBridge returned empty zip");
            }

            System.out.println("EXIT TrackingService.exportRunRecordings -> zipBytes=" + zipBytes.length);
            return zipBytes;

        } catch (Exception e) {
            System.err.println("VoiceBridge ZIP call failed: " + e.getMessage());
            throw new RuntimeException("Failed to export run recordings: " + e.getMessage(), e);
        }
    }

    // --------------------------------------
    // DTO sent to VoiceBridge
    // --------------------------------------
    public static class VoiceBridgeZipRequest {
        public String token;
        public String organization;
        public List<String> channelIds;
    }


    private static String nz(String s) {
        return (s == null) ? "" : s;
    }
    

    private CampaignRunCallLogRowDTO toRowDto(CampaignRunCallLog e) {
        return new CampaignRunCallLogRowDTO(
                e.getEventAt(),
                e.getChannelId(),
                e.getFromNumber(),
                e.getToNumber(),
                e.getEmployeeExtension(),
                e.getCallState(),
                e.getCallCost(),
                e.getDurationMs(),
                e.getExtraJson()
        );
    }

    private CampaignRunCallLogRowDTO toRowDto(StartedCampaignData.CampaignRunCallLogMem r) {
        return new CampaignRunCallLogRowDTO(
                r.eventAt,
                r.channelId,
                r.fromNumber,
                r.toNumber,
                r.employeeExtension,
                r.callState,
                r.callCost,
                r.durationMs,
                r.extraJson
        );
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.isEmpty() ? "" : t.toLowerCase();
    }

    private boolean passesSearch(StartedCampaignData.CampaignRunCallLogMem r, String qLower) {
        if (qLower == null || qLower.isEmpty()) return true;
        return contains(r.channelId, qLower)
                || contains(r.fromNumber, qLower)
                || contains(r.toNumber, qLower)
                || contains(r.employeeExtension, qLower)
                || contains(r.callState, qLower)
                || contains(r.extraJson, qLower);
    }

    private boolean contains(String s, String qLower) {
        if (s == null) return false;
        return s.toLowerCase().contains(qLower);
    }
    
    private int computeRunNumberFromMemory(Long campaignId) {
        try {
            PageInfoDTO p = StartedCampaignData.getPageInfoForCampaign(campaignId);
            if (p == null) return 0;

            int pageSize = StartedCampaignData.getConfiguredPageSize();

            int currentPage = Math.max(0, p.getCurrentPage());
            int recordOfPage = Math.max(0, p.getRecordOfPage());

            // assume recordOfPage is 1-based; if you know it's 0-based, remove +0 adjustment
            if (recordOfPage == 0) return (currentPage-1) * pageSize; // fallback

            // 0-based page, 1-based record
            return (currentPage-1) * pageSize + recordOfPage;

        } catch (Exception e) {
            return 0;
        }
    }

}
