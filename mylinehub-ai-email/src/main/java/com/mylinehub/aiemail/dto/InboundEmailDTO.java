package com.mylinehub.aiemail.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

/**
 * Simplified representation of an incoming email.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboundEmailDTO {

    private String messageId;
    private String from;
    private String to;
    private String subject;
    private String bodyText;
    private String bodyHtml;
    /**
     * Optional text representing previous emails in this thread,
     * e.g. older messages formatted like:
     *
     *   [Email 1] From: X, Date: Y
     *   Body: ...
     *
     *   [Email 2] ...
     *
     * For IMAP_IDLE accounts, this can be auto-built.
     * For SIEVE_HTTP accounts, the mail server can populate this field.
     */
    private String threadContext;
}
