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
@Table(name = "CONFERENCE",
indexes = {
		  @Index(name = "Conference_Organization_Index", columnList = "organization"),
		  @Index(name = "Conference_confextension_Index", columnList = "confextension"),
		  @Index(name = "Conference_phonecontext_Index", columnList = "phonecontext"),
		})
public class Conference {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "conference_sequence"
    )
    @SequenceGenerator(
            name="conference_sequence",
            sequenceName = "conference_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    @Column(unique = true)
    private  String confextension;
    private String confname;
    private  String domain;
    private  String organization;
    private  String phonecontext;
    
    @Column(columnDefinition = "varchar(255) default 'PJSIP\'")
    private  String protocol;
    
    private String owner;
    private String bridge;
    private String userprofile;
    private String menu;
    
    @Column(columnDefinition = "boolean default false")
    private boolean isdynamic;
    
    @Column(columnDefinition = "boolean default false")
    private boolean isroomactive;
    
    @Column(columnDefinition = "boolean default true")
    private boolean isconferenceactive;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    Conference( String phoneContext, String organization, String confextension, String domain)
    {
    	this.phonecontext=phoneContext;
    	this.organization=organization;
    	this.confextension=confextension;
    	this.domain=domain;
    }
    
}
