package com.mylinehub.crm.entity;

import java.time.Instant;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
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
@Table(name = "LOGS",
	   indexes = {
	              @Index(name = "Logs_Organization_Index", columnList = "organization")})
public class Logs {
	
	@Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "logs_sequence"
    )
    @SequenceGenerator(
            name="logs_sequence",
            sequenceName = "logs_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
	

	String log;
	
	String data;
	
	String logClass;
	
	String functionality;
	
	Date createdDate;
	
	String organization;
	
	@Column(updatable = false)
	@CreationTimestamp
	private Instant createdOn;
	    
	@UpdateTimestamp
	private Instant lastUpdatedOn;
	    
	    
}