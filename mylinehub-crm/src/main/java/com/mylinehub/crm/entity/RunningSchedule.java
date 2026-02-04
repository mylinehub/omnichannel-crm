package com.mylinehub.crm.entity;

import lombok.*;

import java.time.Instant;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(indexes = {
		  @Index(name = "Running_schedule_extension_Index", columnList = "fromExtension"),
		  @Index(name = "Running_schedule_jobId_Index", columnList = "jobId"),
		  @Index(name = "Running_schedule_organization_Index", columnList = "organization"),
		})
public class RunningSchedule {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "running_schedule_sequence"
    )
    @SequenceGenerator(
            name="running_schedule_sequence",
            sequenceName = "running_schedule_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "SCHEDULE_ID", nullable = false, unique = true)
    private Long id;
    
    @Column(unique = true)
    private String jobId;
    
    private String functionality;
    private String scheduleType;
    private String cronExpression;
    
    
	private Date date;
    private Long campaignId;
	private String actionType;
	private String data;
	private String organization;
	private String domain;
	private int seconds;
	private String phoneNumber;
	private String callType;
	private String fromExtension;
	private String context;
	private int priority;
	private Long timeOut;
	private String firstName;
	private String protocol;
	private String phoneTrunk;
	
	 
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
	
}
