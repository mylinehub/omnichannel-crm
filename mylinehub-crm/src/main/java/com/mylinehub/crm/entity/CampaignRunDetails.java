package com.mylinehub.crm.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "campaign_run_details",
        indexes = {
                @Index(name = "idx_campaign_run_campaign_id", columnList = "campaignId"),
                @Index(name = "idx_campaign_run_org", columnList = "organization")
        }
)
public class CampaignRunDetails {

    // This is your runId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOTE: if you want MULTIPLE runs per campaign, do NOT keep unique=true here.
    @Column(nullable = false)
    private Long campaignId;

    @Column(length = 100)
    private String organization;

    @Column(length = 200)
    private String campaignName;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant lastUpdatedAt;

    @Column(length = 32)
    private String lastDialedPhone;

    @Column(name = "total_dialed")
    private Long totalDialed;

    @Column(name = "total_cost")
    private Long totalCost;
    
    @Column(length = 30, nullable = false)
    private String status; // RUNNING / STOPPED / COMPLETED
}
