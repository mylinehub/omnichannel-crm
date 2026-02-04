package com.mylinehub.voicebridge.api;

import com.mylinehub.voicebridge.models.CallHistoryRecord;
import com.mylinehub.voicebridge.repository.CallHistoryRecordRepository;
import com.mylinehub.voicebridge.service.CallHistoryInternalQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/call-history")
public class InternalCallHistoryController {

    private static final String INTERNAL_TOKEN = "MYLINEHUB100010001";

    private final CallHistoryInternalQueryService queryService;
    private final CallHistoryRecordRepository callHistoryRepo;

    // -----------------------------
    // SEARCH (existing)
    // -----------------------------
    @GetMapping("/search")
    public List<CallHistoryRecordMiniDTO> search(
            @RequestParam("phone") String phone,
            @RequestParam("entryType") String entryType,
            @RequestParam("token") String token
    ) {
        assertToken(token);

        List<CallHistoryRecord> result = queryService.searchByPhoneAndEntryType(phone, entryType);
        return result.stream().map(InternalCallHistoryController::toDto).collect(Collectors.toList());
    }

    // ============================================================
    // API 1: Download ONE recording by entry ID (existing)
    // GET /internal/call-history/recording?id=123&token=...
    // ============================================================
    @GetMapping("/recording")
    public ResponseEntity<Resource> downloadOneRecording(
            @RequestParam("id") Long id,
            @RequestParam("token") String token
    ) {
        assertToken(token);

        CallHistoryRecord r = queryService.getByIdOrNull(id);
        if (r == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CallHistoryRecord not found for id=" + id);
        }

        Path filePath = resolveRecordingFile(r);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recording file not found: " + filePath);
        }

        String downloadName = safeFileName(
                (r.getRecordingFileName() != null && !r.getRecordingFileName().isBlank())
                        ? r.getRecordingFileName()
                        : ("call_" + r.getId() + ".wav")
        );

        MediaType mt = guessMediaType(downloadName);

        try {
            long len = Files.size(filePath);
            InputStreamResource body = new InputStreamResource(new BufferedInputStream(Files.newInputStream(filePath)));

            return ResponseEntity.ok()
                    .contentType(mt)
                    .contentLength(len)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                    .cacheControl(CacheControl.noCache())
                    .body(body);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file: " + e.getMessage());
        }
    }

    // ============================================================
    // API 2: Download ZIP of all recordings after a time (existing)
    // GET /internal/call-history/recordings/zip?from=...&token=...
    // ============================================================
    @GetMapping(value = "/recordings/zip", produces = "application/zip")
    public ResponseEntity<Resource> downloadZipAfter(
            @RequestParam("from") String fromIsoInstant,
            @RequestParam("token") String token,
            @RequestParam(value = "entryType", required = false) String entryType
    ) {
        assertToken(token);

        Instant from;
        try {
            from = Instant.parse(fromIsoInstant.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid 'from'. Use ISO-8601 Instant like 2025-12-19T00:00:00Z");
        }

        List<CallHistoryRecord> rows = queryService.findAfterStartedAt(from, entryType);

        String zipName = "call_recordings_after_" + from.toString().replace(":", "-") + ".zip";

        Path tmpZip = createTempZipOrThrow();

        try (java.util.zip.ZipOutputStream zos =
                     new java.util.zip.ZipOutputStream(Files.newOutputStream(tmpZip, StandardOpenOption.TRUNCATE_EXISTING))) {

            for (CallHistoryRecord r : rows) {
                Path filePath;
                try {
                    filePath = resolveRecordingFile(r);
                } catch (ResponseStatusException badPath) {
                    continue;
                }

                if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) continue;

                String entryFile = safeFileName(
                        (r.getRecordingFileName() != null && !r.getRecordingFileName().isBlank())
                                ? r.getRecordingFileName()
                                : ("call_" + r.getId() + ".wav")
                );

                String zipEntryName = r.getId() + "_" + entryFile;

                zos.putNextEntry(new java.util.zip.ZipEntry(zipEntryName));
                Files.copy(filePath, zos);
                zos.closeEntry();
            }

            zos.finish();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to build zip: " + e.getMessage());
        }

        return streamZip(tmpZip, zipName);
    }

    // ============================================================
    // NEW API 3: ZIP BY CHANNEL IDS (used by CRM)
    // POST /internal/call-history/recordings/zip-by-channel
    // Body: { token, organization, channelIds: [...] }
    // ============================================================
    @PostMapping(value = "/recordings/zip-by-channel", produces = "application/zip")
    public ResponseEntity<Resource> downloadZipByChannelIds(@RequestBody ZipByChannelRequest req) {
        if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body required");
        assertToken(req.token);

        String org = (req.organization == null) ? "" : req.organization.trim();
        if (org.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organization required");

        List<String> channelIds = (req.channelIds == null) ? List.of() : req.channelIds.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .toList();

        if (channelIds.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "channelIds required");

        List<CallHistoryRecord> rows = callHistoryRepo.findByOrganizationAndChannelIdInOrderByStartedAtAsc(org, channelIds);

        String zipName = "call_recordings_" + org.replace(" ", "_") + "_" + Instant.now().toString().replace(":", "-") + ".zip";

        Path tmpZip = createTempZipOrThrow();

        try (java.util.zip.ZipOutputStream zos =
                     new java.util.zip.ZipOutputStream(Files.newOutputStream(tmpZip, StandardOpenOption.TRUNCATE_EXISTING))) {

            for (CallHistoryRecord r : rows) {
                Path filePath;
                try {
                    filePath = resolveRecordingFile(r);
                } catch (ResponseStatusException badPath) {
                    continue;
                }

                if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) continue;

                String entryFile = safeFileName(
                        (r.getRecordingFileName() != null && !r.getRecordingFileName().isBlank())
                                ? r.getRecordingFileName()
                                : ("call_" + r.getId() + ".wav")
                );

                String safeChannel = safeFileName((r.getChannelId() == null) ? "channel" : r.getChannelId());
                String zipEntryName = safeChannel + "/" + r.getId() + "_" + entryFile;

                zos.putNextEntry(new java.util.zip.ZipEntry(zipEntryName));
                Files.copy(filePath, zos);
                zos.closeEntry();
            }

            zos.finish();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to build zip: " + e.getMessage());
        }

        return streamZip(tmpZip, zipName);
    }

    // -----------------------------
    // Request DTO
    // -----------------------------
    public static final class ZipByChannelRequest {
        public String token;
        public String organization;
        public List<String> channelIds;
    }

    // -----------------------------
    // DTO (existing)
    // -----------------------------
    public static final class CallHistoryRecordMiniDTO {
        public Long id;
        public String organization;
        public String stasisAppName;
        public String callerNumber;
        public Instant startedAt;
        public Instant endedAt;
        public Long durationSeconds;
        public String endReason;

        public String completeCallTranscriptOriginal;
        public String completeCallTranscriptEn;

        public String recordingPath;
        public String recordingFileName;
    }

    private static CallHistoryRecordMiniDTO toDto(CallHistoryRecord r) {
        CallHistoryRecordMiniDTO d = new CallHistoryRecordMiniDTO();
        d.id = r.getId();
        d.organization = r.getOrganization();
        d.stasisAppName = r.getStasisAppName();
        d.callerNumber = r.getCallerNumber();
        d.startedAt = r.getStartedAt();
        d.endedAt = r.getEndedAt();
        d.durationSeconds = r.getDurationSeconds();
        d.endReason = r.getEndReason();

        d.completeCallTranscriptOriginal = r.getCompleteCallTranscriptOriginal();
        d.completeCallTranscriptEn = r.getCompleteCallTranscriptEn();

        d.recordingPath = r.getRecordingPath();
        d.recordingFileName = r.getRecordingFileName();
        return d;
    }

    // -----------------------------
    // Helpers
    // -----------------------------
    private static void assertToken(String token) {
        if (token == null || !INTERNAL_TOKEN.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private static Path createTempZipOrThrow() {
        try {
            Path tmpZip = Files.createTempFile("call_recordings_", ".zip");
            tmpZip.toFile().deleteOnExit();
            return tmpZip;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create temp zip");
        }
    }

    private static ResponseEntity<Resource> streamZip(Path zipPath, String zipName) {
        try {
            long len = Files.size(zipPath);
            InputStreamResource body = new InputStreamResource(new BufferedInputStream(Files.newInputStream(zipPath)));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .contentLength(len)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFileName(zipName) + "\"")
                    .cacheControl(CacheControl.noCache())
                    .body(body);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read zip: " + e.getMessage());
        }
    }

    private static Path resolveRecordingFile(CallHistoryRecord r) {
        String dir = (r.getRecordingPath() == null) ? "" : r.getRecordingPath().trim();
        String file = (r.getRecordingFileName() == null) ? "" : r.getRecordingFileName().trim();

        if (dir.isEmpty() || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recording path/file missing for id=" + r.getId());
        }

        Path base = Paths.get(dir).normalize();
        Path target = base.resolve(file).normalize();

        if (!target.startsWith(base)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsafe recording path for id=" + r.getId());
        }
        return target;
    }

    private static String safeFileName(String name) {
        String n = (name == null) ? "recording.wav" : name.trim();
        if (n.isEmpty()) n = "recording.wav";
        n = n.replace("\r", "").replace("\n", "");
        n = n.replace("\\", "_").replace("/", "_");
        return n;
    }

    private static MediaType guessMediaType(String fileName) {
        String f = (fileName == null) ? "" : fileName.toLowerCase();
        if (f.endsWith(".wav")) return MediaType.parseMediaType("audio/wav");
        if (f.endsWith(".mp3")) return MediaType.parseMediaType("audio/mpeg");
        if (f.endsWith(".ogg")) return MediaType.parseMediaType("audio/ogg");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
