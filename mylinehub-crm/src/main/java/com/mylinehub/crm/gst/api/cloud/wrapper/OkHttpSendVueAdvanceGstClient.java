package com.mylinehub.crm.gst.api.cloud.wrapper;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.crm.utils.OkHttpLoggerUtils;
import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;

import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@AllArgsConstructor
public class OkHttpSendVueAdvanceGstClient {

    private final ApplicationContext applicationContext;
    private final OkHttpSendVueAuthClient okHttpSendVueAuthClient;

    /**
     * Vue Advanced GST call.
     * - Uses Authorization: Bearer <token> and x-api-key: <clientSecret>
     * - If token is invalid (401/403 + "Not a valid token"), refresh token and retry once.
     *
     * IMPORTANT:
     * - clientId MUST come from DB engine config (engine.getClientId()).
     * - Do NOT depend on spring.gst.deep.vue.clientId property.
     */
    public JSONObject sendMessage(String token, String clientId, String clientSecret, String gstNumber) throws Exception {

        System.out.println("OkHttpSendVueAdvanceGstClient sendMessage");

        String domain = applicationContext.getEnvironment().getProperty("spring.gst.deep.vue.domain");
        if (domain == null || domain.isBlank()) {
            throw new IllegalStateException("spring.gst.deep.vue.domain missing/blank");
        }
        if (gstNumber == null || gstNumber.isBlank()) {
            throw new IllegalArgumentException("gstNumber missing/blank");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId missing/blank (must come from engine.getClientId())");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("clientSecret missing/blank");
        }

        OkHttpLoggerUtils myLogger = new OkHttpLoggerUtils();
        CurlInterceptor curlInterceptor = new CurlInterceptor(myLogger);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(curlInterceptor)
                .build();

        // 1) First attempt with given token
        JSONObject resp = executeOnce(client, domain, token, clientSecret, gstNumber);

        // 2) If token invalid -> refresh + retry once
        if (isInvalidTokenResponse(resp)) {
            System.out.println("Vue GST call indicates invalid token. Refreshing token and retrying once...");

            JSONObject auth = okHttpSendVueAuthClient.sendMessage(clientId, clientSecret);
            if (auth == null) {
                throw new Exception("Vue auth returned null while refreshing token");
            }

            String newToken = auth.optString("access_token", null);
            if (newToken == null || newToken.isBlank()) {
                throw new Exception("Vue auth did not return access_token while refreshing token: " + auth);
            }

            resp = executeOnce(client, domain, newToken, clientSecret, gstNumber);

            if (isInvalidTokenResponse(resp)) {
                throw new Exception("Vue GST still failing due to invalid token after refresh");
            }
        }

        // 3) If response still represents an error, fail fast so controller can fall back
        if (looksLikeError(resp)) {
            throw new Exception("Vue GST call failed: " + resp);
        }

        return resp;
    }

    private JSONObject executeOnce(OkHttpClient client, String domain, String token, String clientSecret, String gstNumber)
            throws Exception {

        String url = "https://" + domain + "/v1/verification/gstin-advanced?gstin_number=" + gstNumber;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("x-api-key", clientSecret)
                .addHeader("Accept", "application/json")
                .build();

        System.out.println("Before sending Vue GST call. domain=" + domain
                + " gst=" + gstNumber
                + " token=" + mask(token)
                + " secret=" + mask(clientSecret));

        try (Response response = client.newCall(request).execute()) {
            JSONObject obj = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);

            // Attach status for debugging if extractor doesn't include it
            try {
                if (obj != null) obj.put("_http_code", response.code());
            } catch (Exception ignore) {}

            return obj;
        }
    }

    private boolean isInvalidTokenResponse(JSONObject obj) {
        if (obj == null) return true;

        // DeepVue style seen often: {"detail":"Not a valid token"}
        String detail = obj.optString("detail", "");
        if (detail != null && detail.toLowerCase().contains("not a valid token")) {
            return true;
        }

        // Some APIs return message/error fields
        String message = obj.optString("message", "");
        if (message != null && message.toLowerCase().contains("not a valid token")) {
            return true;
        }

        int http = obj.optInt("_http_code", 200);
        return (http == 401 || http == 403) && (detail.toLowerCase().contains("token") || message.toLowerCase().contains("token"));
    }

    private boolean looksLikeError(JSONObject obj) {
        if (obj == null) return true;

        int http = obj.optInt("_http_code", 200);
        if (http >= 400) return true;

        // If API returns "detail", it is likely error
        if (obj.has("detail")) return true;

        // Your success payload usually has "data" (engine1). Keep conservative:
        // If neither "data" nor "result" exists, treat as error-ish.
        if (!obj.has("data") && !obj.has("result")) return true;

        return false;
    }

    private static String mask(String s) {
        if (s == null || s.isEmpty()) return "null/empty";
        int keep = Math.min(3, s.length());
        String tail = s.substring(Math.max(0, s.length() - 4));
        return s.substring(0, keep) + "****" + tail;
    }
}
