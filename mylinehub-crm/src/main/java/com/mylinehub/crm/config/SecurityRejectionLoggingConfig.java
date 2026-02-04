package com.mylinehub.crm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class SecurityRejectionLoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityRejectionLoggingConfig.class);

    @Bean
    public RequestRejectedHandler requestRejectedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, RequestRejectedException ex) -> {
            String uri = request.getRequestURI();
            String qs = request.getQueryString();
            String full = (qs == null) ? uri : (uri + "?" + qs);

            String remote = request.getRemoteAddr();
            String xff = request.getHeader("X-Forwarded-For");
            String ua = request.getHeader("User-Agent");
            String host = request.getHeader("Host");

            log.warn("[FIREWALL-REJECT] url={} remote={} xff={} host={} ua={} reason={}",
                    full, remote, xff, host, ua, ex.getMessage());

            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        };
    }
}
