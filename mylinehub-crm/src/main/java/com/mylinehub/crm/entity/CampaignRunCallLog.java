package com.mylinehub.crm.entity;

import lombok.*;
import java.time.Instant;
import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "campaign_run_call_log",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_run_channel", columnNames = {"run_id", "channel_id"})
    },
    indexes = {
    	    @Index(name = "idx_run_call_campaign_time", columnList = "campaign_id,event_at"),
    	    @Index(name = "idx_run_call_org", columnList = "organization"),
    	    @Index(name = "idx_run_call_employee_ext", columnList = "employee_extension"),
    	    @Index(name = "idx_run_call_campaign_channel", columnList = "campaign_id,channel_id"),
    	    @Index(name = "idx_run_call_channel_time", columnList = "channel_id,event_at"),
    	    @Index(name = "idx_run_call_run_time", columnList = "run_id,event_at"),
    	    @Index(name = "idx_run_call_state_time", columnList = "call_state,event_at")
    }
)
public class CampaignRunCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "channel_id", length = 200, nullable = false)
    private String channelId;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "organization", length = 100)
    private String organization;

    @Column(name = "campaign_name", length = 200)
    private String campaignName;

    @Column(name = "event_at", nullable = false)
    private Instant eventAt;

    @Column(name = "from_number", length = 40)
    private String fromNumber;

    @Column(name = "to_number", length = 40)
    private String toNumber;

    @Column(name = "employee_extension", length = 20)
    private String employeeExtension;

    @Column(name = "call_cost")
    private Double callCost;
    
    @Column(name = "call_state", length = 40)
    private String callState;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Lob
    @Column(name = "extra_json", columnDefinition = "TEXT")
    private String extraJson;
}
