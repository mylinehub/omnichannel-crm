// src/main/java/com/mylinehub/voicebridge/service/dto/CrmFranchiseInventoryUpdateDto.java
package com.mylinehub.voicebridge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmFranchiseInventoryUpdateDto {
    private String interest;
    private Boolean available;
}
