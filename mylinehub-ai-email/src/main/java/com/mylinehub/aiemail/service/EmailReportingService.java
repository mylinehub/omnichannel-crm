package com.mylinehub.aiemail.service;

import com.mylinehub.aiemail.dto.EmailReportDTO;

/**
 * Sends email-level reporting (for analytics / CRM) to MyLineHub backend.
 */
public interface EmailReportingService {

    void reportEmail(EmailReportDTO dto);
}
