package com.mylinehub.voicebridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.voicebridge.service.dto.CrmCustomerUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class CompletionJsonValidator {

  private final ObjectMapper mapper = new ObjectMapper();

  public CrmCustomerUpdateRequestDto parseAndValidateOrThrow(String json, String callerNumber) throws Exception {
    if (json == null || json.trim().isEmpty()) {
      throw new IllegalArgumentException("EMPTY_OUTPUT");
    }

    CrmCustomerUpdateRequestDto dto = mapper.readValue(json, CrmCustomerUpdateRequestDto.class);

    // Enforce: phoneNumber must be callerNumber unless explicitly changed (we can’t reliably detect explicitly said),
    // so safest is: if null/blank, force callerNumber.
    if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
      dto.setPhoneNumber(callerNumber);
    }

    // listedDate must be null (per your rule), ensure propertyInventory.listedDate is null
    if (dto.getPropertyInventory() != null) {
      dto.getPropertyInventory().setListedDate(null);
    }

    return dto;
  }
}
