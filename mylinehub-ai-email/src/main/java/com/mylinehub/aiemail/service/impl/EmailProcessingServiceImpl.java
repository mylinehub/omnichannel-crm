package com.mylinehub.aiemail.service.impl;

import com.mylinehub.aiemail.dto.EmailReportDTO;
import com.mylinehub.aiemail.dto.InboundEmailDTO;
import com.mylinehub.aiemail.dto.LanguageHeuristicResult;
import com.mylinehub.aiemail.model.OrganizationEmailAccount;
import com.mylinehub.aiemail.service.*;
import com.mylinehub.aiemail.util.TextStatsUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Main orchestration service for inbound email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailProcessingServiceImpl implements EmailProcessingService {

    private final RagService ragService;
    private final OpenAiEmailComposerService openAiEmailComposerService;
    private final SmtpSenderService smtpSenderService;
    private final EmailReportingService emailReportingService;
    private final LanguageHeuristicService languageHeuristicService;

    @Override
    public void handleInboundEmail(OrganizationEmailAccount account, InboundEmailDTO inbound) {

        log.info("[EmailProcess] Handling inbound email for org={} email={} from={} subject={}",
                account.getOrganizationName(),
                account.getEmailAddress(),
                inbound.getFrom(),
                inbound.getSubject());

        final String baseText = inbound.getBodyText() != null ? inbound.getBodyText() : inbound.getBodyHtml();
        if (baseText == null) {
        	log.info("[EmailProcess] Inbound email body is null. Hence returning without auto reply.");
            return;
        }

        Instant receivedAt = Instant.now();

        // 1) Detect language & English translation (if needed)
        languageHeuristicService.analyze(baseText)
                .flatMap(heuristic -> {

                    String languageCode = heuristic.getLanguage();
                    String englishForRag = heuristic.getEnglishTranslation();
                    if (englishForRag == null || englishForRag.isBlank()) {
                        englishForRag = baseText;
                    }

                    log.info("[EmailProcess] languageCode={} usingEnglishForRagLength={}",
                            languageCode, englishForRag.length());

                    // 2) Call RAG using English content
                    return ragService.fetchContext(account.getOrganizationName(), englishForRag)
                            // 3) After RAG, call OpenAI composer with languageCode
                            .flatMap(ctx -> openAiEmailComposerService.composeReply(
                                    account.getOrganizationName(),
                                    baseText,
                                    ctx,
                                    languageCode
                            ).map(reply -> new EmailProcessResult(reply, heuristic)));
                })
                .doOnSuccess(result -> {
                    // 4) Send reply via SMTP
                	String replyText = result.reply();
                    smtpSenderService.sendReply(
                            account,
                            inbound.getFrom(),
                            "Re: " + (inbound.getSubject() == null ? "" : inbound.getSubject()),
                            replyText
                    );

                    // 5) Build and send report
                    EmailHeuristicReport(result.heuristic(), account, inbound, receivedAt,replyText);
                })
                .subscribe();
    }

    private void EmailHeuristicReport(LanguageHeuristicResult heuristic,
                                      OrganizationEmailAccount account,
                                      InboundEmailDTO inbound,
                                      Instant receivedAt,
                                      String replyText) {

    	 // Inbound text (original)
        String inboundText = inbound.getBodyText() != null
                ? inbound.getBodyText()
                : inbound.getBodyHtml();

        int inputWords  = TextStatsUtil.wordCount(inboundText);
        int inputTokens = TextStatsUtil.approxTokenCount(inboundText);

        int replyWords  = TextStatsUtil.wordCount(replyText);
        int replyTokens = TextStatsUtil.approxTokenCount(replyText);

        EmailReportDTO report = EmailReportDTO.builder()
                .organization(account.getOrganizationName())
                .emailAddress(account.getEmailAddress())
                .from(inbound.getFrom())
                .to(inbound.getTo())
                .subject(inbound.getSubject())
                .receivedAt(receivedAt)
                .repliedAt(Instant.now())
                .aiHandled(true)
                .aiSummary(truncate(inboundText, 200))
                .language(heuristic != null ? heuristic.getLanguage() : null)
                .inputWordCount(inputWords)
                .inputTokenCount(inputTokens)
                .aiWordCount(replyWords)
                .aiTokenCount(replyTokens)
                .build();

        emailReportingService.reportEmail(report);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) + "ae¦" : s;
    }

    /**
     * Small immutable holder for composition result + heuristic.
     */
    private record EmailProcessResult(String reply, LanguageHeuristicResult heuristic) { }
}
