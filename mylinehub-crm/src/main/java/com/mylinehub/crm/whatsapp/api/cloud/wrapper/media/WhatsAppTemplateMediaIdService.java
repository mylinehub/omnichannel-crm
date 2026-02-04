package com.mylinehub.crm.whatsapp.api.cloud.wrapper.media;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.SendMessageToWhatsApp;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.repository.WhatsAppPhoneNumberTemplatesRepository;

import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@AllArgsConstructor
public class WhatsAppTemplateMediaIdService {

    private static final int VALIDATE_AFTER_DAYS = 28;

    // =========================
    // DEEP LOGGING FLAG
    // =========================
    // Turn ON/OFF using JVM arg:
    //   -DWHATSAPP_TEMPLATE_MEDIA_DEEPLOG=true
    // or ENV:
    //   WHATSAPP_TEMPLATE_MEDIA_DEEPLOG=true
    private static final boolean DEEPLOG = false;

    private final SendMessageToWhatsApp sendMessageToWhatsApp;
    private final OkHttpUploadMediaClient okHttpUploadMediaClient;
    private final WhatsAppPhoneNumberTemplatesRepository templatesRepository;

    @Transactional
    public String getOrUploadMediaId(WhatsAppPhoneNumberTemplates templateRow) throws Exception {

        deep("==========================================================");
        deep("WhatsAppTemplateMediaIdService.getOrUploadMediaId() START");
        deep("==========================================================");

        if (templateRow == null) throw new IllegalArgumentException("templateRow is null");
        if (templateRow.getWhatsAppPhoneNumber() == null)
            throw new IllegalArgumentException("templateRow.whatsAppPhoneNumber is null");

        // Basic template context
        deep("templateRow.id=" + templateRow.getId());
        deep("templateRow.templateName=" + templateRow.getTemplateName());
        deep("templateRow.organization=" + templateRow.getOrganization());
        deep("templateRow.mediaType=" + templateRow.getMediaType());
        deep("templateRow.mediaPath=" + templateRow.getMediaPath());
        deep("templateRow.mediaId(existing)=" + templateRow.getMediaId());
        deep("templateRow.mediaIdLastUpdatedDate(existing)=" + templateRow.getMediaIdLastUpdatedDate());

        String apiVersion = safe(() -> templateRow.getWhatsAppPhoneNumber().getWhatsAppProject().getApiVersion());
        String token = safe(() -> templateRow.getWhatsAppPhoneNumber().getWhatsAppProject().getAccessToken());
        String phoneNumberId = templateRow.getWhatsAppPhoneNumber().getPhoneNumberID();

        deep("cloud.apiVersion=" + apiVersion);
        deep("cloud.phoneNumberId=" + phoneNumberId);
        deep("cloud.token.present=" + (token != null && !token.isBlank()));

        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            throw new IllegalArgumentException("Cloud phoneNumberId missing for templateRow id=" + templateRow.getId());
        }

        String mediaType = templateRow.getMediaType();
        if (mediaType == null || mediaType.trim().isEmpty()) {
            throw new IllegalArgumentException("mediaType is null/blank for template=" + templateRow.getTemplateName());
        }

        String mediaPath = templateRow.getMediaPath();
        if (mediaPath == null || mediaPath.isBlank()) {
            throw new IllegalArgumentException("mediaPath is empty for template=" + templateRow.getTemplateName());
        }

        String absolutePath = resolveAbsolute(mediaPath);
        deep("resolved.absolutePath=" + absolutePath);

        if (!Files.exists(Path.of(absolutePath))) {
            throw new IllegalArgumentException("mediaPath file not found: " + absolutePath);
        }

        String mediaId = templateRow.getMediaId();
        Date lastUpdated = templateRow.getMediaIdLastUpdatedDate();

        // ==========================================
        // Fast path: skip Meta validation within 28d
        // ==========================================
        if (mediaId != null && !mediaId.isBlank() && isWithinDays(lastUpdated, VALIDATE_AFTER_DAYS)) {
            deep("FAST-PATH HIT: mediaId exists and is within " + VALIDATE_AFTER_DAYS + " days");
            deep("Returning existing mediaId=" + mediaId);
            deep("==========================================================");
            deep("WhatsAppTemplateMediaIdService.getOrUploadMediaId() END");
            deep("==========================================================");
            return mediaId;
        }

        deep("FAST-PATH MISS: Need validation or upload");
        deep("mediaId present? " + (mediaId != null && !mediaId.isBlank()));
        deep("lastUpdated=" + lastUpdated);

        // ==========================================
        // Validate existing mediaId with Meta
        // ==========================================
        if (mediaId != null && !mediaId.isBlank()) {
            deep("VALIDATION START: Checking if existing mediaId is still valid on Meta");
            boolean valid = isMediaIdValid(apiVersion, phoneNumberId, token, mediaId);
            deep("VALIDATION RESULT: mediaId=" + mediaId + " valid=" + valid);

            if (valid) {
                deep("Existing mediaId is valid. Refreshing lastUpdatedDate only.");
                templateRow.setMediaIdLastUpdatedDate(new Date());
                WhatsAppPhoneNumberTemplates saved = templatesRepository.save(templateRow);

                WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(
                        saved.getWhatsAppPhoneNumber().getPhoneNumber(),
                        saved,
                        "update-existing"
                );

                deep("Saved templateRow with refreshed mediaIdLastUpdatedDate=" + saved.getMediaIdLastUpdatedDate());
                deep("Returning existing mediaId=" + mediaId);
                deep("==========================================================");
                deep("WhatsAppTemplateMediaIdService.getOrUploadMediaId() END");
                deep("==========================================================");
                return mediaId;
            }

            deep("Existing mediaId is NOT valid anymore. Will upload again.");
        } else {
            deep("No existing mediaId found. Will upload.");
        }

        // ==========================================
        // Upload new media
        // ==========================================
        deep("UPLOAD START: Uploading file to Meta media endpoint");
        deep("upload.apiVersion=" + apiVersion);
        deep("upload.phoneNumberId=" + phoneNumberId);
        deep("upload.mediaType(template)=" + mediaType);
        deep("upload.file=" + absolutePath);

        JSONObject uploadJson = okHttpUploadMediaClient.uploadFromLocalPath(
                apiVersion,
                phoneNumberId,
                token,
                absolutePath,
                mediaType
        );

        deep("UPLOAD RESPONSE JSON=" + uploadJson);

        String newMediaId = uploadJson.optString("id", "");
        if (newMediaId.isBlank()) {
            throw new RuntimeException("Upload response missing media id. Response=" + uploadJson);
        }

        deep("UPLOAD OK: newMediaId=" + newMediaId);

        templateRow.setMediaId(newMediaId);
        templateRow.setMediaIdLastUpdatedDate(new Date());

        WhatsAppPhoneNumberTemplates saved = templatesRepository.save(templateRow);

        WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(
                saved.getWhatsAppPhoneNumber().getPhoneNumber(),
                saved,
                "update-existing"
        );

        deep("DB UPDATED: templateRow.id=" + saved.getId()
                + " mediaId=" + saved.getMediaId()
                + " mediaIdLastUpdatedDate=" + saved.getMediaIdLastUpdatedDate());

        deep("==========================================================");
        deep("WhatsAppTemplateMediaIdService.getOrUploadMediaId() END");
        deep("==========================================================");

        return newMediaId;
    }

    private boolean isMediaIdValid(String version, String phoneNumberId, String token, String mediaId) {
        try {
            deep("isMediaIdValid(): calling retrieveMediaUrl with mediaId=" + mediaId
                    + ", version=" + version + ", phoneNumberId=" + phoneNumberId);

            RequestBody empty = RequestBody.create(MediaType.parse("text/plain"), "");

            try (Response response = sendMessageToWhatsApp.retrieveMediaUrl(empty, version, mediaId, phoneNumberId, token)) {

                if (response == null) {
                    deep("isMediaIdValid(): response is NULL -> invalid");
                    return false;
                }

                deep("isMediaIdValid(): httpCode=" + response.code() + " success=" + response.isSuccessful());

                if (!response.isSuccessful()) return false;

                JSONObject json = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);
                String url = (json == null) ? "" : json.optString("url", "");
                deep("isMediaIdValid(): response.json=" + json);
                deep("isMediaIdValid(): url.present=" + (url != null && !url.isBlank()));

                return json != null && url != null && !url.isBlank();
            }
        } catch (Exception e) {
            deep("isMediaIdValid(): EXCEPTION -> invalid. err=" + e.getClass().getSimpleName() + " msg=" + e.getMessage());
            return false;
        }
    }

    private boolean isWithinDays(Date lastUpdated, int days) {
        if (lastUpdated == null) return false;
        long diffDays = ChronoUnit.DAYS.between(lastUpdated.toInstant(), Instant.now());
        deep("isWithinDays(): lastUpdated=" + lastUpdated + " diffDays=" + diffDays + " thresholdDays=" + days);
        return diffDays >= 0 && diffDays < days;
    }

    private String resolveAbsolute(String inputPath) {
        Path p = Paths.get(inputPath);
        if (p.isAbsolute()) return p.normalize().toString();
        return Paths.get(System.getProperty("user.dir")).resolve(p).normalize().toString();
    }

    // =========================
    // Deep log helpers
    // =========================
    private static void deep(String msg) {
        if (DEEPLOG) {
            System.out.println("[DEEPLOG][TEMPLATE_MEDIA] " + msg);
        }
    }

    private static String safe(SupplierWithException<String> s) {
        try {
            return s.get();
        } catch (Exception e) {
            return null;
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
