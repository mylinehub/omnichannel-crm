package com.mylinehub.aiemail.imap;

import com.mylinehub.aiemail.dto.InboundEmailDTO;
import com.mylinehub.aiemail.model.OrganizationEmailAccount;
import com.mylinehub.aiemail.service.EmailProcessingService;
import jakarta.annotation.PreDestroy;
import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SubjectTerm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * For each active IMAP_IDLE account, opens an IMAP connection and listens
 * for new messages using IDLE with polling fallback.
 *
 * Now also supports optional thread history:
 *  - If OrganizationEmailAccount.threadHistoryDepth > 0
 *    it will fetch up to N previous emails in the same subject-thread
 *    and build a textual "threadContext" string for AI.
 */
@Slf4j
@Component
public class ImapIdleListener {

    private final EmailProcessingService emailProcessingService;
    private final ExecutorService executor = Executors.newCachedThreadPool(
            new CustomizableThreadFactory("imap-listener-"));

    public ImapIdleListener(EmailProcessingService emailProcessingService) {
        this.emailProcessingService = emailProcessingService;
    }

    public void startForAccounts(List<OrganizationEmailAccount> accounts) {
        for (OrganizationEmailAccount account : accounts) {
            executor.submit(() -> runLoop(account));
        }
    }

    private void runLoop(OrganizationEmailAccount account) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                listenOnce(account);
            } catch (Exception e) {
                log.error("[IMAP] Error in loop for {}: {}", account.getEmailAddress(), e.getMessage(), e);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void listenOnce(OrganizationEmailAccount account) throws Exception {
        Properties props = new Properties();
        String protocol = Boolean.TRUE.equals(account.getImapSsl()) ? "imaps" : "imap";

        Session session = Session.getInstance(props);
        Store store = session.getStore(protocol);
        store.connect(
                account.getImapHost(),
                account.getImapPort() == null ? -1 : account.getImapPort(),
                account.getImapUsername(),
                account.getImapPassword()
        );

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        inbox.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                for (Message m : e.getMessages()) {
                    handleMessage(account, inbox, m);
                }
            }
        });

        log.info("[IMAP] Connected and listening (IDLE/poll) for {}", account.getEmailAddress());

        // Try IDLE if supported, else fallback to polling
        while (store.isConnected() && inbox.isOpen()) {
            try {
                if (inbox instanceof com.sun.mail.imap.IMAPFolder imapFolder) {
                    imapFolder.idle();
                } else {
                    // Fallback: simple polling every 60s
                    TimeUnit.SECONDS.sleep(60);
                    inbox.getMessageCount(); // forces new message check
                }
            } catch (FolderClosedException fce) {
                log.warn("[IMAP] Folder closed for {}: {}", account.getEmailAddress(), fce.getMessage());
                break;
            } catch (MessagingException | InterruptedException ex) {
                log.warn("[IMAP] IDLE/poll error for {}: {}", account.getEmailAddress(), ex.getMessage());
                break;
            }
        }

        try {
            inbox.close();
        } catch (MessagingException ignore) {
        }
        try {
            store.close();
        } catch (MessagingException ignore) {
        }
    }

    /**
     * Handle a single new message:
     *  - Extract from / subject / body
     *  - Optionally build threadContext based on account.threadHistoryDepth
     *  - Build InboundEmailDTO and send to EmailProcessingService
     */
    private void handleMessage(OrganizationEmailAccount account, Folder inbox, Message message) {
        try {
            String subject = message.getSubject();
            Address[] fromArr = message.getFrom();
            String from = (fromArr != null && fromArr.length > 0) ? fromArr[0].toString() : null;

            String to = account.getEmailAddress();
            String bodyText = extractText(message);

            // Optional thread history depth from DB (0 or null => no history)
            int depth = account.getThreadHistoryDepth() != null ? account.getThreadHistoryDepth() : 0;
            String threadContext = null;
            if (depth > 0) {
                threadContext = buildThreadContext(inbox, message, depth);
            }

            InboundEmailDTO dto = InboundEmailDTO.builder()
                    .messageId(extractMessageId(message))
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .bodyText(bodyText)
                    .threadContext(threadContext)
                    .build();

            emailProcessingService.handleInboundEmail(account, dto);
        } catch (Exception e) {
            log.error("[IMAP] Failed to handle message for {}: {}", account.getEmailAddress(), e.getMessage(), e);
        }
    }

    /**
     * Build a simple textual context of up to `depth` previous emails in the
     * same subject-thread, ordered from newest to oldest.
     */
    private String buildThreadContext(Folder inbox, Message current, int depth) {
        try {
            String subject = current.getSubject();
            if (subject == null || subject.isBlank()) {
                return null;
            }

            // Simple subject-based search (can be improved using Message-ID / References).
            SearchTerm term = new SubjectTerm(subject);
            Message[] found = inbox.search(term);

            Date currentDate = current.getSentDate();
            List<Message> older = new ArrayList<>();
            for (Message m : found) {
                if (m == current) continue;
                Date d = m.getSentDate();
                if (d != null && currentDate != null && d.before(currentDate)) {
                    older.add(m);
                }
            }

            if (older.isEmpty()) {
                return null;
            }

            // Sort by date DESC (newest first)
            older.sort((a, b) -> {
                try {
                    Date da = a.getSentDate();
                    Date db = b.getSentDate();
                    if (da == null || db == null) return 0;
                    return db.compareTo(da);
                } catch (MessagingException e) {
                    return 0;
                }
            });

            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Message m : older) {
                if (count >= depth) break;
                count++;

                Address[] fromArr = m.getFrom();
                String from = (fromArr != null && fromArr.length > 0) ? fromArr[0].toString() : "unknown";
                Date d = m.getSentDate();

                sb.append("[Previous Email ").append(count).append("]\\n");
                sb.append("From: ").append(from).append("\\n");
                sb.append("Date: ").append(d).append("\\n");
                sb.append("Subject: ").append(m.getSubject()).append("\\n");
                sb.append("Body:\\n");
                sb.append(extractText(m)).append("\\n\\n");
            }

            String ctx = sb.toString().trim();
            return ctx.isEmpty() ? null : ctx;

        } catch (Exception e) {
            log.warn("[IMAP] Failed to build thread context: {}", e.getMessage(), e);
            return null;
        }
    }

    private String extractMessageId(Message message) throws MessagingException {
        String[] ids = message.getHeader("Message-ID");
        if (ids != null && ids.length > 0) {
            return ids[0];
        }
        return null;
    }

    private String extractText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            Object content = p.getContent();
            return content == null ? "" : content.toString();
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part part = mp.getBodyPart(i);
                if (part.isMimeType("text/plain")) {
                    if (text == null) {
                        text = extractText(part);
                    }
                    continue;
                } else if (part.isMimeType("text/html")) {
                    String s = extractText(part);
                    if (s != null) {
                        return s;
                    }
                } else {
                    return extractText(part);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = extractText(mp.getBodyPart(i));
                if (s != null && !s.isEmpty()) {
                    return s;
                }
            }
        }

        return "";
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
