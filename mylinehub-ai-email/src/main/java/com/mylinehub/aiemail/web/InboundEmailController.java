package com.mylinehub.aiemail.web;

import com.mylinehub.aiemail.dto.InboundEmailDTO;
import com.mylinehub.aiemail.model.OrganizationEmailAccount;
import com.mylinehub.aiemail.repository.OrganizationEmailAccountRepository;
import com.mylinehub.aiemail.service.EmailProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * HTTP endpoint used by SIEVE_HTTP mode:
 *  - Mail server (Dovecot/Sieve or any other component) calls this API
 *    with parsed email fields.
 */
@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class InboundEmailController {

    private final OrganizationEmailAccountRepository accountRepository;
    private final EmailProcessingService emailProcessingService;

    @PostMapping("/inbound")
    public ResponseEntity<String> inbound(@Validated @RequestBody InboundEmailDTO dto) {

        if (dto.getTo() == null) {
            return ResponseEntity.badRequest().body("Missing 'to' field");
        }

        Optional<OrganizationEmailAccount> accountOpt =
                accountRepository.findByEmailAddressAndActive(dto.getTo(),true);

        if (accountOpt.isEmpty()) {
            log.warn("[InboundAPI] No matching account for to={}", dto.getTo());
            return ResponseEntity.badRequest().body("Unknown recipient");
        }

        OrganizationEmailAccount account = accountOpt.get();
        emailProcessingService.handleInboundEmail(account, dto);

        return ResponseEntity.ok("Accepted");
    }
}
