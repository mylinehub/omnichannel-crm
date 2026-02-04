package com.mylinehub.aiemail.service;

import com.mylinehub.aiemail.model.OrganizationEmailAccount;

/**
 * Low-level SMTP sender that uses the per-account SMTP settings.
 */
public interface SmtpSenderService {

    void sendReply(OrganizationEmailAccount account,
                   String to,
                   String subject,
                   String bodyText);
}
