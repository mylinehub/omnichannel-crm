package com.mylinehub.aiemail.service.impl;

import com.mylinehub.aiemail.dto.EmailReportDTO;
import com.mylinehub.aiemail.service.EmailReportingService;
import com.mylinehub.aiemail.service.LoginService;
import com.mylinehub.aiemail.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Reporting service for AI email events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailReportingServiceImpl implements EmailReportingService {

    private final WebClient.Builder webClientBuilder;
    private final SystemConfigService configService;
    private final LoginService loginService;

    @Override
    public void reportEmail(EmailReportDTO dto) {
        if (dto == null) {
            log.warn("[EmailReport] null DTO - nothing to report");
            return;
        }

        log.info("[EmailReport] {}", dto);

        String baseUrl = configService.getOptional("MYLINEHUB_BASE_URL", null);
        String path = configService.getOptional("EMAIL_REPORT_URL", null);

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            log.warn("[EmailReport] MYLINEHUB_BASE_URL not configured. Skipping HTTP call.");
            return;
        }
        if (path == null || path.trim().isEmpty()) {
            log.warn("[EmailReport] EMAIL_REPORT_URL not configured. Skipping HTTP call.");
            return;
        }

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        loginService.getValidSystemToken()
                .flatMap(token -> client
                        .post()
                        .uri(uriBuilder -> uriBuilder.path(path).build())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .bodyValue(dto)
                        .retrieve()
                        .bodyToMono(String.class)
                )
                .doOnNext(body -> log.info("[EmailReport] CRM email report API success org={} body={}",
                        dto.getOrganization(), shortPreview(body)))
                .doOnError(e -> log.error("[EmailReport] CRM email report API error org={} msg={}",
                        dto.getOrganization(), e.getMessage(), e))
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }

    private String shortPreview(String value) {
        if (value == null) return "null";
        return (value.length() > 120) ? value.substring(0, 120) + "…" : value;
    }
}
