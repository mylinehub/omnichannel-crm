// ============================================================
// FILE: src/main/java/com/mylinehub/crm/controller/CampaignRunController.java
// ============================================================
package com.mylinehub.crm.controller;

import static com.mylinehub.crm.controller.ApiMapping.CAMPAIGN_RUN_REST_URL;
import static org.springframework.http.ResponseEntity.status;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.CampaignRunPageInfoDTO;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtVerify;
import com.mylinehub.crm.service.CampaignRunTrackingService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletResponse;

import java.time.Instant;
import java.util.List;
import java.io.OutputStream;

@RestController
@RequestMapping(produces = "application/json", path = CAMPAIGN_RUN_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class CampaignRunController {

    private final EmployeeRepository employeeRepository;
    private final CampaignRunTrackingService trackingService;

    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;

    private Employee authOrNull(String token, String organization) {
        try {
            if (token == null) return null;
            if (organization == null) return null;

            String raw = token.replace(jwtConfiguration.getTokenPrefix(), "");
            Employee employee = new JwtVerify().verifyTokenOrThrowError(raw, secretKey, employeeRepository);

            if (employee == null) return null;
            if (employee.getOrganization() == null) return null;

            if (!employee.getOrganization().trim().equals(organization.trim())) return null;
            return employee;
        } catch (Exception e) {
            return null;
        }
    }

    private Pageable pageable(Integer pageNumber, Integer size) {
        int p = (pageNumber == null) ? 0 : pageNumber;
        int s = (size == null) ? 50 : size;
        if (p < 0) p = 0;
        if (s <= 0) s = 50;
        return PageRequest.of(p, s);
    }

    // ============================================================
    // 1) All campaign IDs (DB + memory)
    // ============================================================

    @GetMapping("/listCampaignIdsMerged")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<String>> listCampaignIdsMerged(
            @RequestParam String organization,
            @RequestHeader(name = "Authorization") String token
    ) {
        List<String> out = null;
        try {
            Employee employee = authOrNull(token, organization);
            if (employee == null) return status(HttpStatus.UNAUTHORIZED).body(out);

            out = trackingService.listCampaignIdNameMerged(employee.getOrganization());
            return status(HttpStatus.OK).body(out);
        } catch (Exception e) {
            e.printStackTrace();
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body(out);
        }
    }

    
    // ============================================================
    // 2) All runIds per campaign (DB + memory)
    // ============================================================

    @GetMapping("/listRunIdsForCampaignMerged")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<List<String>> listRunIdsForCampaignMerged(
            @RequestParam Long campaignId,
            @RequestParam String organization,
            @RequestHeader(name = "Authorization") String token
    ) {
        List<String> out = null;
        try {
            Employee employee = authOrNull(token, organization);
            if (employee == null) return status(HttpStatus.UNAUTHORIZED).body(out);

            out = trackingService.listRunIdDateMerged(campaignId,employee.getOrganization());
            return status(HttpStatus.OK).body(out);
        } catch (Exception e) {
            e.printStackTrace();
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body(out);
        }
    }


    @GetMapping("/getCurrentRunLiveLogsMemoryOnly")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<CampaignRunPageInfoDTO> getCurrentRunLiveLogsMemoryOnly(
            @RequestParam Long campaignId,
            @RequestParam String organization,
            @RequestParam(defaultValue = "") final String searchText,
            @RequestHeader(name = "Authorization") String token
    ) {
        CampaignRunPageInfoDTO dto = null;
        try {
            Employee employee = authOrNull(token, organization);
            if (employee == null) return status(HttpStatus.UNAUTHORIZED).body(dto);

            dto = trackingService.getCurrentRunLiveLogsMemoryOnly(
                    campaignId,
                    searchText
            );
            return status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
        }
    }

    @GetMapping("/getCallLogsMergedForRun")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public ResponseEntity<CampaignRunPageInfoDTO> getCallLogsMergedForRun(
            @RequestParam Long campaignId,
            @RequestParam Long runId,
            @RequestParam String organization,
            @RequestParam(defaultValue = "0") final Integer pageNumber,
            @RequestParam(defaultValue = "50") final Integer size,
            @RequestParam(defaultValue = "") final String searchText,
            @RequestHeader(name = "Authorization") String token
    ) {
        CampaignRunPageInfoDTO dto = null;
        try {
            Employee employee = authOrNull(token, organization);
            if (employee == null) return status(HttpStatus.UNAUTHORIZED).body(dto);

            dto = trackingService.getCallLogsMergedForRun(
                    campaignId,
                    runId,
                    pageable(pageNumber, size),
                    searchText
            );
            return status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
        }
    }

    @GetMapping(value = "/exportRunExcelDbOnly")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public void exportRunExcelDbOnly(
            @RequestParam Long campaignId,
            @RequestParam Long runId,
            @RequestParam String organization,
            @RequestHeader(name = "Authorization") String token,
            javax.servlet.http.HttpServletResponse response
    ) throws java.io.IOException {

        try {
            Employee employee = authOrNull(token, organization);
            if (employee == null) {
                response.setStatus(javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            byte[] bytes = trackingService.exportRunExcelDbOnly(campaignId, runId, employee.getOrganization());
            if (bytes == null) {
                response.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            String ts = String.valueOf(Instant.now().toEpochMilli());
            String filename = "campaign_" + campaignId + "_run_" + runId + "_" + ts + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            response.setContentLength(bytes.length);

            try (java.io.OutputStream os = response.getOutputStream()) {
                os.write(bytes);
                os.flush();
            }

            response.setStatus(org.springframework.http.HttpStatus.OK.value());

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.setStatus(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/exportRunRecordings")
    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    public void exportRunRecordings(
            @RequestParam Long campaignId,
            @RequestParam Long runId,
            @RequestParam String organization,
            @RequestHeader(name = "Authorization") String token,
            HttpServletResponse response
    ) throws java.io.IOException {

        System.out.println("ENTER exportRunRecordings(campaignId=" + campaignId + ", runId=" + runId + ", org=" + organization + ")");

        try {
            Employee employee = authOrNull(token, organization);
            if (employee == null) {
                System.out.println("UNAUTHORIZED exportRunRecordings");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            byte[] bytes = trackingService.exportRunRecordings(campaignId, runId, employee.getOrganization());
            if (bytes == null || bytes.length == 0) {
                System.err.println("exportRunRecordings -> bytes empty");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            String ts = String.valueOf(Instant.now().toEpochMilli());
            String filename = "campaign_" + campaignId + "_recordings_" + runId + "_" + ts + ".zip";

            // ZIP headers
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            response.setContentLength(bytes.length);

            try (OutputStream os = response.getOutputStream()) {
                os.write(bytes);
                os.flush();
            }

            response.setStatus(HttpStatus.OK.value());
            System.out.println("EXIT exportRunRecordings -> bytes=" + bytes.length + ", filename=" + filename);

        } catch (IllegalArgumentException e) {
            System.err.println("BAD_REQUEST exportRunRecordings: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("ERROR exportRunRecordings: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return status(HttpStatus.OK).body("ok");
    }
}
