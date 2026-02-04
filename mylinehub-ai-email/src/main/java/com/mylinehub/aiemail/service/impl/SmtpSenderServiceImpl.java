package com.mylinehub.aiemail.service.impl;

import com.mylinehub.aiemail.model.OrganizationEmailAccount;
import com.mylinehub.aiemail.service.SmtpSenderService;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Simple SMTP sender using Jakarta Mail.
 */
@Slf4j
@Service
public class SmtpSenderServiceImpl implements SmtpSenderService {

    @Override
    public void sendReply(OrganizationEmailAccount account, String to, String subject, String bodyText) {

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");

            if (Boolean.TRUE.equals(account.getSmtpStartTls())) {
                props.put("mail.smtp.starttls.enable", "true");
            }

            props.put("mail.smtp.host", account.getSmtpHost());
            props.put("mail.smtp.port", String.valueOf(account.getSmtpPort()));

            Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(account.getSmtpUsername(), account.getSmtpPassword());
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(account.getEmailAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(bodyText);

            Transport.send(message);

            log.info("[SMTP] Sent reply from {} to {} subject={}",
                    account.getEmailAddress(), to, subject);
        } catch (Exception e) {
            log.error("[SMTP] Failed to send email from {} to {} subject={} msg={}",
                    account.getEmailAddress(), to, subject, e.getMessage(), e);
        }
    }
}
