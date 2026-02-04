package com.mylinehub.crm.entity;

import java.time.Instant;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
@Table(name = "CUSTOMER_TO_CAMPAIGN",
indexes = {
		  @Index(name = "StickyCustomerData_Organization_Index", columnList = "organization"),
		  @Index(name = "StickyCustomerData_CUSTOMER_ID_Index", columnList = "CUSTOMER_ID"),
		})
public class StickyCustomerData  {

    /**
	 * 
	 */
	@SequenceGenerator(
            name = "sticky_customer_sequence",
            sequenceName = "sticky_customer_sequence",
            allocationSize = 1)
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "sticky_customer_sequence")
    @Column(name = "STICKY_CUSTOMER_DATA_ID")
    private Long id;
	
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CUSTOMER_ID")
    public Customers customer;
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "USER_IDc")
	public Employee employee;
	
	public Date createdDate;
	
	public String organization;
    
	@Column(updatable = false)
	@CreationTimestamp
	private Instant createdOn;
	    
	@UpdateTimestamp
	private Instant lastUpdatedOn;
	    
}
