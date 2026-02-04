package com.mylinehub.crm.gst.api.cloud.wrapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Instant;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.crm.utils.OkHttpLoggerUtils;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;

import lombok.AllArgsConstructor;

/**
 * IDfy GST Prescreen (Async) client.
 *
 * FIXES:
 * 1) Do NOT crash when "completed_at" is missing (use optString).
 * 2) Poll with max attempts + timeout (no infinite loop).
 * 3) Use spring.gst.idfy.domain for BOTH async + polling (no hardcoded eve.idfy.com).
 * 4) Fail fast if async call doesn't return request_id.
 * 5) Attach _http_code for debugging.
 */
@Service
@AllArgsConstructor
public class OkHttpSendIdfyTaskProcessorAdvanceGST {

    private final ApplicationContext applicationContext;

    // Tune as needed
    private static final int POLL_SLEEP_MS = 2000;     // 2 seconds
    private static final int MAX_POLLS = 25;           // ~50 seconds total

    public JSONObject getGSTAdvanceDetails(String gstin, String API_KEY, String ACCOUNT_ID)
            throws InterruptedException, IOException {

        System.out.println("OkHttpSendIdfyTaskProcessorAdvanceGST getGSTAdvanceDetails");

        if (gstin == null || gstin.isBlank()) {
            throw new IllegalArgumentException("gstin missing/blank");
        }
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalArgumentException("API_KEY missing/blank");
        }
        if (ACCOUNT_ID == null || ACCOUNT_ID.isBlank()) {
            throw new IllegalArgumentException("ACCOUNT_ID missing/blank");
        }

        String domain = applicationContext.getEnvironment().getProperty("spring.gst.idfy.domain");
        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException("spring.gst.idfy.domain missing/blank");
        }

        OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils();
        CurlInterceptor curlInterceptor = new CurlInterceptor(myLogger);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(curlInterceptor)
                .build();

        MediaType JSON = MediaType.parse("application/json");

        // 1) Send async task
        JSONObject data = new JSONObject();
        data.put("gstin", gstin);

        JSONObject taskPayload = new JSONObject();
        taskPayload.put("task_id", "74f4c926-250c-43ca-9c43-453e87ceacd1");
        taskPayload.put("group_id", "8e16424a-58fc-4ba4-ab10-5bc8e7c3c41e");
        taskPayload.put("data", data);

        String asyncUrl = "https://" + domain + "/v3/tasks/async/retrieve/gst_prescreen";

        Request request = new Request.Builder()
                .url(asyncUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("account-id", ACCOUNT_ID)
                .addHeader("api-key", API_KEY)
                .post(RequestBody.create(JSON, taskPayload.toString()))
                .build();

        String requestId;
        JSONObject asyncResp;
        try (Response response = client.newCall(request).execute()) {
            asyncResp = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);
            attachHttpCode(asyncResp, response.code());
        }

        requestId = (asyncResp == null) ? null : asyncResp.optString("request_id", null);

        if (requestId == null || requestId.isBlank()) {
            throw new IOException("IDfy async did not return request_id. resp=" + asyncResp);
        }

        System.out.println("Request Id : " + requestId);

        // 2) Poll until completed OR failed OR timeout
        String pollUrl = "https://" + domain + "/v3/tasks?request_id=" + requestId;

        JSONObject lastPoll = null;

        for (int attempt = 1; attempt <= MAX_POLLS; attempt++) {

            System.out.println("Wait started at : " + Instant.now());
            Thread.sleep(POLL_SLEEP_MS);
            System.out.println("Wait end at : " + Instant.now());

            Request pollRequest = new Request.Builder()
                    .url(pollUrl)
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("account-id", ACCOUNT_ID)
                    .addHeader("api-key", API_KEY)
                    .build();

            try (Response pollResponse = client.newCall(pollRequest).execute()) {

                // Your function returns FIRST object from a JSON array - keep it
                JSONObject pollObj = new OkHttpResponseFunctions()
                        .extractFirstJSONObjectFromResponseAndCloseBufferViaJsonArray(pollResponse);

                lastPoll = pollObj;
                attachHttpCode(lastPoll, pollResponse.code());

                System.out.println("Polling attempt=" + attempt + " response=" + lastPoll);

                if (lastPoll == null) {
                    System.out.println("Polling returned null object. Will retry...");
                    continue;
                }

                // IMPORTANT: use optString so we never crash
                String status = lastPoll.optString("status", lastPoll.optString("state", ""));
                String completedAt = lastPoll.optString("completed_at", "");
                String error = lastPoll.optString("error", lastPoll.optString("message", ""));

                boolean failed = "failed".equalsIgnoreCase(status) || "error".equalsIgnoreCase(status);
                boolean completed = (!completedAt.isBlank())
                        || "completed".equalsIgnoreCase(status)
                        || "success".equalsIgnoreCase(status)
                        || "done".equalsIgnoreCase(status);

                if (failed) {
                    throw new IOException("IDfy task failed. status=" + status + " error=" + error + " resp=" + lastPoll);
                }

                if (completed) {
                    System.out.println("Task Completed!");
                    return lastPoll;
                }

                // Otherwise: still running/processing => continue loop
            }
        }

        // 3) Timeout
        throw new IOException("IDfy polling timeout after " + MAX_POLLS + " attempts. lastPoll=" + lastPoll);
    }

    private static void attachHttpCode(JSONObject obj, int code) {
        if (obj == null) return;
        try {
            obj.put("_http_code", code);
        } catch (Exception ignore) {
        }
    }
}
