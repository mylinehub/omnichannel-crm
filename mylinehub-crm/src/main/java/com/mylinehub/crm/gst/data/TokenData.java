package com.mylinehub.crm.gst.data;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;
import org.json.JSONObject;

import com.mylinehub.crm.gst.api.cloud.wrapper.OkHttpSendVueAuthClient;
import com.mylinehub.crm.gst.data.dto.GstVerificationEngineDataParameterDto;
import com.mylinehub.crm.gst.data.dto.TokenDataDto;
import com.mylinehub.crm.gst.data.dto.TokenDataParameterDto;
import com.mylinehub.crm.gst.entity.GstVerificationEngine;

// As of now this Token data is only used by Vue, Idfy does not use it
public class TokenData {

    // Base timeout in seconds for lock acquisition
    private static final int BASE_TIMEOUT_SECONDS = 2;

    // Non-fair lock for tokenData operations
    private static final ReentrantLock tokenDataLock = new ReentrantLock(false);

    // Shared token map
    // Using ConcurrentHashMap since it may be read concurrently
    private static final Map<String, TokenDataDto> tokenData = new ConcurrentHashMap<>();

    public static Map<String, TokenDataDto> workWithTokenData(TokenDataParameterDto tokenDataParameterDto) {
        Map<String, TokenDataDto> toReturn = null;
        boolean acquired = false;

        while (!acquired) {
            try {
                // Calculate timeout: current queue length + base timeout
                int timeoutSeconds = tokenDataLock.getQueueLength() + BASE_TIMEOUT_SECONDS;
                acquired = tokenDataLock.tryLock(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) continue; // Retry if lock not acquired

                switch (tokenDataParameterDto.getAction()) {
                    case "get-one": {
                        TokenDataDto token = verifyTokenValidityOrElseEmbedNewAndThenReturn(tokenDataParameterDto);
                        toReturn = new HashMap<>();
                        toReturn.put(tokenDataParameterDto.getEngine(), token);
                        break;
                    }

                    case "get":
                        return new HashMap<>(tokenData);

                    case "update":
                        tokenData.put(tokenDataParameterDto.getEngine(), tokenDataParameterDto.getToken());
                        break;

                    case "delete":
                        tokenData.remove(tokenDataParameterDto.getEngine());
                        break;

                    default:
                        break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (acquired) tokenDataLock.unlock();
            }
        }

        return toReturn;
    }

    private static TokenDataDto verifyTokenValidityOrElseEmbedNewAndThenReturn(TokenDataParameterDto tokenDataParameterDto)
            throws Exception {

        System.out.println("verifyTokenValidityOrElseEmbedNewAndThenReturn");

        TokenDataDto token = null;
        String engineString = tokenDataParameterDto.getEngine();

        GstVerificationEngine engine = null;
        GstVerificationEngineDataParameterDto gstVerificationEngineDataParameterDto =
                new GstVerificationEngineDataParameterDto();
        gstVerificationEngineDataParameterDto.setEngineName(engineString);
        gstVerificationEngineDataParameterDto.setDetails(null);
        gstVerificationEngineDataParameterDto.setAction("get-one");

        Map<String, GstVerificationEngine> gstEngineMap =
                GSTVerificationEngineData.workWithGstVerificationData(gstVerificationEngineDataParameterDto);

        System.out.println("After generating engine map while extracting jwt token");

        if (gstEngineMap == null) {
            throw new Exception("Gst Engine Details Not Found For '" + engineString
                    + "'. Whats app on +919625048379 for resolution.");
        } else {
            engine = gstEngineMap.get(engineString);
        }

        try {
            token = tokenData.get(tokenDataParameterDto.getEngine());
            boolean fetchNew = true;

            if (engine != null) {

                if (token != null) {
                    boolean expired = isTokenExpired(token, engine.getValidityInHours());
                    System.out.println("We had previous token. expired=" + expired);

                    // If NOT expired => do not fetch new
                    if (!expired) {
                        fetchNew = false;
                    }
                }

                if (fetchNew) {
                    System.out.println("Fetching new token");
                    token = setNewToken(tokenDataParameterDto.getOkHttpSendVueAuthClient(), engine);
                }

            } else {
                throw new Exception("Gst Engine Details Not Found For '" + engineString
                        + "'. Whats app on +919625048379 for resolution.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return token;
    }

    /**
     * TRUE = expired, FALSE = still valid.
     *
     * Expired if token.getDateUpdated() is older than validityInHours.
     */
    public static boolean isTokenExpired(TokenDataDto token, int validityInHours) {
        if (token == null || token.getDateUpdated() == null) return true;

        Instant cutoff = Instant.now().minus(validityInHours, ChronoUnit.HOURS);

        // If token was updated BEFORE cutoff, it is expired
        return token.getDateUpdated().toInstant().isBefore(cutoff);
    }

    static TokenDataDto setNewToken(OkHttpSendVueAuthClient okHttpSendVueAuthClient, GstVerificationEngine engine)
            throws JSONException, IOException {

        System.out.println("Sending OkHTTP Request to retrieve new token");

        JSONObject data = okHttpSendVueAuthClient.sendMessage(engine.getClientId(), engine.getCientSecret());
        String access_token = data.getString("access_token");

        // WARNING: token is sensitive. Consider masking in logs in production.
        System.out.println("access_token: " + access_token);

        TokenDataDto tokenDataDto = new TokenDataDto();
        tokenDataDto.setToken(access_token);
        tokenDataDto.setDateUpdated(new Date());

        tokenData.put(engine.getEngineName(), tokenDataDto);

        System.out.println("Returning data");

        return tokenDataDto;
    }
}
