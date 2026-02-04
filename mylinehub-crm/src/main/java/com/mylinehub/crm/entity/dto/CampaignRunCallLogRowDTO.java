package com.mylinehub.crm.entity.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRunCallLogRowDTO {
    private Instant eventAt;
    private String channelId;
    private String fromNumber;
    private String toNumber;
    private String employeeExtension;
    private String callState;
    private Double callCost;
    private Long durationMs;
    private String extraJson;
}
