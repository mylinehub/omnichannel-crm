package com.mylinehub.crm.entity;


import java.time.Instant;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Table(name = "CUSTOMER_TO_CAMPAIGN", uniqueConstraints = { @UniqueConstraint(name = "UniqueCampaignAndCustomer", columnNames = { "CAMPAIGN_ID", "CUSTOMER_ID" })})
public class CustomerToCampaign  {

    /**
	 * 
	 */
	@SequenceGenerator(
            name = "customer_to_campaign_sequence",
            sequenceName = "customer_to_campaign_sequence",
            allocationSize = 1)
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "customer_to_campaign_sequence")
    @Column(name = "sticky_customer_data_id")
    private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CAMPAIGN_ID")
	public Campaign campaign;
	
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CUSTOMER_ID")
    public Customers customer; 
    
	@Column(columnDefinition = "varchar(255) default 'false'")
	public String isCalledOnce;
	
	@Column(columnDefinition = "varchar(255) default ''")
	public String lastConnectedExtension;
	
	public String organization;
	
	@Column(updatable = false)
	@CreationTimestamp
	private Instant createdOn;
	    
	@UpdateTimestamp
	private Instant lastUpdatedOn;
	    
    
	
}