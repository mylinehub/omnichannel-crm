package com.mylinehub.crm.entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "SIP_PROVIDER",
indexes = {
		  @Index(name = "SipProvider_Organization_Index", columnList = "organization"),
		  @Index(name = "SipProvider_active_Index", columnList = "active"),
		  @Index(name = "SipProvider_phoneNumber_Index", columnList = "phoneNumber"),
		})
public class SipProvider {
	
	@Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sip_provider_sequence"
    )
    @SequenceGenerator(
            name="sip_provider_sequence",
            sequenceName = "sip_provider_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
	
	String organization;
	String providerName;
	
	@Column(columnDefinition = "boolean default true")
	boolean active;
	
	@Column(unique = true)
	String phoneNumber;
	String company;
	String costCalculation;
	String meteredPlanAmount;
	
	@Column(updatable = false)
	@CreationTimestamp
	private Instant createdOn;
	    
	@UpdateTimestamp
	private Instant lastUpdatedOn;
	    
}
	