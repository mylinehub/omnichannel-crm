package com.mylinehub.crm.whatsapp.api.cloud.wrapper.media;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.SendMessageToWhatsApp;

import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@AllArgsConstructor
public class OkHttpUploadMediaClient {

    private final SendMessageToWhatsApp sendMessageToWhatsApp;

    public JSONObject uploadFromLocalPath(String version,
                                          String phoneNumberId,
                                          String token,
                                          String absoluteFilePath,
                                          String templateMediaType) throws Exception {

        File file = validateFile(absoluteFilePath);

        String mime = guessMimeFromTemplateTypeAndExtension(templateMediaType, file.getName());

        // If not resolved, try probing from OS
        if (mime == null || mime.trim().isEmpty()) {
            mime = Files.probeContentType(file.toPath());
        }

        // If still not resolved, last fallback based on templateMediaType
        if (mime == null || mime.trim().isEmpty()) {
            mime = fallbackMimeForTemplateType(templateMediaType);
        }

        // ultimate fallback
        if (mime == null || mime.trim().isEmpty()) {
            mime = "application/octet-stream";
        }

        return upload(version, phoneNumberId, token, file, mime);
    }

    private JSONObject upload(String version,
                              String phoneNumberId,
                              String token,
                              File file,
                              String mime) throws Exception {

        RequestBody fileBody = RequestBody.create(MediaType.parse(mime), file);

        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("messaging_product", "whatsapp")
                // WhatsApp expects the MIME here
                .addFormDataPart("type", mime)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        try (Response response = sendMessageToWhatsApp.sendMediaMessage(multipartBody, version, phoneNumberId, token)) {
            JSONObject json = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);

            if (json == null || json.optString("id", "").isEmpty()) {
                throw new RuntimeException("Upload succeeded but media id missing. Response=" + json);
            }
            return json;
        }
    }

    private File validateFile(String absoluteFilePath) {
        if (absoluteFilePath == null || absoluteFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("absoluteFilePath is null/blank");
        }
        File file = new File(absoluteFilePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File not found: " + absoluteFilePath);
        }
        return file;
    }

    private String fallbackMimeForTemplateType(String templateMediaType) {
        if (templateMediaType == null) return null;
        String t = templateMediaType.trim().toLowerCase(Locale.ROOT);

        switch (t) {
            case "image":
                return "image/jpeg";
            case "video":
                return "video/mp4";
            case "audio":
                return "audio/mpeg";
            case "document":
                return "application/pdf";
            case "sticker":
                // WhatsApp stickers are usually webp
                return "image/webp";
            default:
                return null;
        }
    }

    private String guessMimeFromTemplateTypeAndExtension(String templateMediaType, String filename) {
        if (filename == null) return null;

        String ext = "";
        int idx = filename.lastIndexOf('.');
        if (idx >= 0 && idx < filename.length() - 1) {
            ext = filename.substring(idx + 1).toLowerCase(Locale.ROOT).trim();
        }

        // First prefer extension (most reliable)
        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "webp":
                return "image/webp";
            case "gif":
                return "image/gif";

            case "mp4":
                return "video/mp4";
            case "mov":
                return "video/quicktime";
            case "3gp":
                return "video/3gpp";
            case "mkv":
                return "video/x-matroska";

            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "ogg":
                return "audio/ogg";
            case "aac":
                return "audio/aac";
            case "m4a":
                return "audio/mp4";

            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";

            default:
                break;
        }

        // If extension unknown, use templateMediaType only as a hint
        if (templateMediaType == null) return null;
        String t = templateMediaType.trim().toLowerCase(Locale.ROOT);

        switch (t) {
            case "image":
                return "image/jpeg";
            case "video":
                return "video/mp4";
            case "audio":
                return "audio/mpeg";
            case "document":
                return "application/pdf";
            case "sticker":
                return "image/webp";
            default:
                return null;
        }
    }
}
