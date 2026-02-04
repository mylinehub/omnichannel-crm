package com.mylinehub.voicebridge.service.impl;

import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.crm.dto.CdrDTO;
import com.mylinehub.voicebridge.crm.mapper.CallSessionToCdrDTO;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSession;
import com.mylinehub.voicebridge.session.CallSessionManager;
import com.mylinehub.voicebridge.service.CallReportingService;
import com.mylinehub.voicebridge.service.LoginService;
import com.mylinehub.voicebridge.service.StasisAppConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Reporting service:
 *  - Logs CallBillingInfo
 *  - Sends CDRDTO to MyLineHub CRM API (per stasis-app, DB driven)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallReportingServiceImpl implements CallReportingService {

    private final WebClient.Builder webClientBuilder;
    private final LoginService loginService;
    private final CallSessionManager sessions;
    private final StasisAppConfigService configService;

    @Override
    public void reportCall(CallBillingInfo info) {
        if (info == null) {
            log.warn("[CallReport] null CallBillingInfo - nothing to report");
            return;
        }
        log.info("[CallReport] {}", info);
    }

    @Override
    public void reportCall(CallSession session, CallBillingInfo info) {
        if (session == null || info == null) {
            log.warn("[CallReport] null session or CallBillingInfo");
            return;
        }

        String channelId = session.getChannelId();
        String stasisAppName = sessions.getStasisApp(channelId);

        if (stasisAppName == null) {
            log.warn("[CallReport] No stasis_app found for chId={}", channelId);
            return;
        }

        StasisAppConfig cfg = configService.getConfigOrNull(stasisAppName);
        if (cfg == null) {
            log.warn("[CallReport] No config found for stasisApp={} chId={}", stasisAppName, channelId);
            return;
        }

        String baseUrl = cfg.getMylinehub_base_url();
        String path    = cfg.getMylinehub_crm_cdr_url();

        if (baseUrl == null || path == null) {
            log.warn("[CallReport] Missing CRM CDR API URL in config for {}", stasisAppName);
            return;
        }

        CdrDTO cdrDTO = CallSessionToCdrDTO.convert(session, info);

        log.info("[CallReport] (session) org={} sending CDRDTO: {}",
                info.getOrganization(), cdrDTO);

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        loginService.getValidSystemToken(cfg)
            .flatMap(token -> client.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(cdrDTO)
                .retrieve()
                .bodyToMono(String.class)
            )
            .doOnNext(body -> log.info("[CallReport] CRM CDR API success org={} body={}",
                    info.getOrganization(), shortPreview(body)))
            .doOnError(e -> log.error("[CallReport] CRM CDR API error org={} msg={}",
                    info.getOrganization(), e.getMessage(), e))
            .onErrorResume(e -> Mono.empty())
            .subscribe();
    }

    private String shortPreview(String v) {
        if (v == null) return "null";
        return v.length() > 120 ? v.substring(0, 120) + ":" : v;
    }
}
