package com.mylinehub.aiemail.startup;

import com.mylinehub.aiemail.imap.ImapIdleListener;
import com.mylinehub.aiemail.model.EmailConnectionType;
import com.mylinehub.aiemail.model.OrganizationEmailAccount;
import com.mylinehub.aiemail.repository.OrganizationEmailAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * On application start, loads all active email accounts and:
 *  - For IMAP_IDLE accounts: starts IMAP listener threads.
 *  - For SIEVE_HTTP accounts: no listener; expects HTTP push.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAccountStartupLoader {

    private final OrganizationEmailAccountRepository repo;
    private final ImapIdleListener imapIdleListener;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        List<OrganizationEmailAccount> active = repo.findByActiveTrue();
        log.info("[Startup] Loaded {} active email accounts", active.size());

        List<OrganizationEmailAccount> imapAccounts =
                repo.findByActiveTrueAndConnectionType(EmailConnectionType.IMAP_IDLE);
        log.info("[Startup] Starting IMAP listeners for {} accounts", imapAccounts.size());

        imapIdleListener.startForAccounts(imapAccounts);

        long sieveCount = active.stream()
                .filter(a -> a.getConnectionType() == EmailConnectionType.SIEVE_HTTP)
                .count();

        log.info("[Startup] {} accounts configured for SIEVE_HTTP (HTTP push)", sieveCount);
    }
}
