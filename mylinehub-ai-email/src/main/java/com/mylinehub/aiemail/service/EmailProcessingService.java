package com.mylinehub.aiemail.service;

import com.mylinehub.aiemail.dto.InboundEmailDTO;
import com.mylinehub.aiemail.model.OrganizationEmailAccount;

/**
 * High-level orchestration:
 *  - Given an incoming email and its owning OrganizationEmailAccount,
 *    fetch RAG context, call OpenAI, construct final reply and send via SMTP.
 */
public interface EmailProcessingService {

    void handleInboundEmail(OrganizationEmailAccount account, InboundEmailDTO inbound);
}
