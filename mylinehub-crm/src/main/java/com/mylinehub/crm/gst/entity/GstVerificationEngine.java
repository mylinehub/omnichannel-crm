package com.mylinehub.crm.gst.entity;


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
@Table(name = "GstVerificationEngine",
indexes = {
		  @Index(name = "GstVerificationEngine_engineName_Index", columnList = "engineName"),
		})
public class GstVerificationEngine {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "GstVerificationEngine_sequence"
    )
    @SequenceGenerator(
            name="GstVerificationEngine_sequence",
            sequenceName = "GstVerificationEngine_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    @Column(unique = true)
    private  String engineName;
    private String clientId;
    private  String cientSecret;
    private String apiKey;
    private  String accountId;
    private  String jwt;
    private int validityInHours;
    
    @Column(columnDefinition = "boolean default true")
    private boolean active;
   
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
 
    
}
