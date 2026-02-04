package com.mylinehub.voicebridge.ai;

import com.mylinehub.voicebridge.ai.impl.ExternalBotWsClientImpl;
import com.mylinehub.voicebridge.ai.impl.GoogleLiveAiClientImpl;
import com.mylinehub.voicebridge.ai.impl.RealtimeAiClientImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Central factory to select correct AI client implementation per call.
 *
 * IMPORTANT:
 *  - bot.mode NOW comes from database (StasisAppConfig.bot_mode)
 *    => NOT from application.properties anymore.
 *
 *  - Valid values:
 *        "openai"   => RealtimeAiClientImpl
 *        "google"   => GoogleAiClientImpl
 *        "external" => ExternalBotWsClientImpl (Exotel-style bot)
 *
 *  - Default fallback = openai
 *
 *  - THIS CLASS MUST NOT KEEP ANY PER-CALL STATE.
 */
@Component
public class AiClientFactory {

    private static final Logger log = LoggerFactory.getLogger(AiClientFactory.class);

    private final RealtimeAiClientImpl openai;
    private final GoogleLiveAiClientImpl google;
    private final ExternalBotWsClientImpl external;

    public AiClientFactory(RealtimeAiClientImpl openai,
    		               GoogleLiveAiClientImpl google,
                           ExternalBotWsClientImpl external) {
        this.openai = openai;
        this.google = google;
        this.external = external;
    }

    /**
     * Resolve AI client based on mode string from DB.
     *
     * @param mode raw string from StasisAppConfig.bot_mode
     * @return correct implementation of RealtimeAiClient
     */
    public BotClient get(String mode) {

        if (mode == null || mode.isBlank()) {
            log.info("AiClientFactory: mode=null => using OPENAI (default)");
            return openai;
        }

        switch (mode.toLowerCase()) {

            case "google":
                log.info("AiClientFactory: mode=google => using GoogleAiClientImpl");
                return google;

            case "external":
                log.info("AiClientFactory: mode=external => using ExternalBotWsClientImpl");
                return external;
            	  
            case "openai":
            default:
                log.info("AiClientFactory: mode=openai => using RealtimeAiClientImpl");
                return openai;
        }
    }
}
