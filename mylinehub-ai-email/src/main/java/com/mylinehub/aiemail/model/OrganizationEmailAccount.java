package com.mylinehub.aiemail.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


/**
 * Represents one AI-managed email inbox.
 *
 * - emailAddress is UNIQUE across system.
 * - organizationName is used for RAG namespace and footer.
 * - All IMAP/SMTP connection parameters are stored here.
 */
@Entity
@Table(name = "organization_email_account", uniqueConstraints = {
        @UniqueConstraint(name = "uk_email_address", columnNames = "email_address")},
        indexes = {
		  @Index(name = "orgemail_organizationName", columnList = "organizationName"),
		  @Index(name = "orgemail_emailAddress", columnList = "emailAddress"),
		  @Index(name = "orgemail_active", columnList = "active")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationEmailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Logical organization identifier (for RAG + footer). */
    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    /** Unique email address managed by AI. */
    @Column(name = "email_address", nullable = false, length = 320)
    private String emailAddress;

    /** Vendor hint (GMAIL, GODADDY, ZOHO, CUSTOM...). */
    @Column(name = "email_vendor", nullable = false, length = 64)
    private String emailVendor;

    /** Connection type: IMAP_IDLE or SIEVE_HTTP. */
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false, length = 32)
    private EmailConnectionType connectionType;

    // IMAP settings
    @Column(name = "imap_host")
    private String imapHost;

    @Column(name = "imap_port")
    private Integer imapPort;

    @Column(name = "imap_ssl")
    private Boolean imapSsl;

    @Column(name = "imap_username")
    private String imapUsername;

    /** In production encrypt this; here it's plain for simplicity. */
    @Column(name = "imap_password")
    private String imapPassword;

    // SMTP settings
    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_starttls")
    private Boolean smtpStartTls;

    @Column(name = "smtp_username")
    private String smtpUsername;

    /** SMTP password (encrypt in real deployment). */
    @Column(name = "smtp_password")
    private String smtpPassword;

    /**
     * How many previous emails from this conversation/thread
     * should be fetched and sent to AI for better context.
     *
     * 0 or null => do not fetch previous emails.
     */
    private Integer threadHistoryDepth;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
