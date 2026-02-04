package com.mylinehub.crm.whatsapp.api.cloud.wrapper.media;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.utils.okhttp.OkHttpResponseFunctions;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.SendMessageToWhatsApp;

import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@AllArgsConstructor
public class WhatsAppMediaValidator {

    private final SendMessageToWhatsApp sendMessageToWhatsApp;

    /**
     * Returns true if mediaId is still valid (retrieveMediaUrl returns 200 and JSON contains url).
     */
    public boolean isMediaIdValid(String version, String phoneNumberId, String token, String mediaId) {
        try {
            RequestBody empty = RequestBody.create(MediaType.parse("text/plain"),"");

            try (Response response = sendMessageToWhatsApp.retrieveMediaUrl(empty, version, mediaId, phoneNumberId, token)) {
                if (response == null) return false;
                if (!response.isSuccessful()) return false;

                JSONObject json = new OkHttpResponseFunctions().extractJSONObjectFromResponseAndCloseBuffer(response);
                return json != null && !json.optString("url", "").isEmpty();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
