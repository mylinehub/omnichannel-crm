package com.mylinehub.voicebridge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.moczul.ok2curl.logger.Loggable;

public class OkHttpLoggerUtils implements Loggable {

    private static final Logger log = LoggerFactory.getLogger(OkHttpLoggerUtils.class);
    private static final boolean DEEP_LOGS = false;

    @Override
    public void log(String message) {
    	
    	if(DEEP_LOGS) {
	        log.debug("*******************************************************");
	        log.debug("OkHttpLoggerUtils Message / CURL");
	        log.debug("*******************************************************");
	        log.debug(message);
	        log.debug("*******************************************************");
	        log.debug("*******************************************************");
    	}
    }
}
