package com.mylinehub.crm.entity.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRunViewDTO {

    private Long campaignId;
    private String organization;
    private String campaignName;

    private Instant startedAt;
    private Instant lastUpdatedAt;

    private String status;

    private String lastDialedPhone;
    private String lastFromNumber;
    private String lastToNumber;
    private String lastCallState;
    private Long lastDurationMs;

    private Long totalDialed;

    // Memory/flush visibility
    private Long pendingInMemoryCount;
    private Instant lastFlushAt;

    // Show last 20 (not yet flushed) for “latest”
    private List<CampaignRunCallLogRowDTO> pendingTail;
}
