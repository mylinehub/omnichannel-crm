package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.service.dto.CrmCustomerDto;
import com.mylinehub.voicebridge.service.dto.CrmCustomerUpdateRequestDto;
import reactor.core.publisher.Mono;

public interface CrmCustomerService {

    Mono<CrmCustomerDto> getByPhoneNumberAndOrganization(
            String stasisAppName,
            String organization,
            String phoneNumber
    );

    // CRM returns Boolean, NOT a customer DTO
    Mono<Boolean> updateCustomerByOrganization(
            String stasisAppName,
            String organization,
            String oldPhone,
            CrmCustomerUpdateRequestDto body
    );
}
