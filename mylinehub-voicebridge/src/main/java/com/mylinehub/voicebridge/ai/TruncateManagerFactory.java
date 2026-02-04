package com.mylinehub.voicebridge.ai;

import com.mylinehub.voicebridge.ai.impl.GoogleNoopTruncateManager;
import com.mylinehub.voicebridge.ai.impl.OpenAiRealtimeTruncateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * TruncateManagerFactory
 *
 * Returns a NEW per-call TruncateManager instance.
 * (Managers are stateful per call, so they must NOT be Spring singletons.)
 */
@Component
public class TruncateManagerFactory {

    private static final Logger log = LoggerFactory.getLogger(TruncateManagerFactory.class);

    public TruncateManager get(String mode) {

        if (mode == null || mode.isBlank()) {
            log.info("TruncateManagerFactory: mode=null => new OpenAiRealtimeTruncateManager()");
            return new OpenAiRealtimeTruncateManager();
        }

        switch (mode.toLowerCase()) {

            case "google":
                log.info("TruncateManagerFactory: mode=google => new GoogleNoopTruncateManager()");
                return new GoogleNoopTruncateManager();

            case "openai":
            default:
                log.info("TruncateManagerFactory: mode=openai => new OpenAiRealtimeTruncateManager()");
                return new OpenAiRealtimeTruncateManager();
        }
    }
}
