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
@Table(name = "SYS_CONFIG",
indexes = {
		  @Index(name = "SystemConfig_Organization_Index", columnList = "organization"),
		})
public class SystemConfig {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "config_sequence"
    )
    @SequenceGenerator(
            name="config_sequence",
            sequenceName = "config_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    @Column(columnDefinition = "integer default 1")
    int jwtTokenValidationDays;
    
    @Column(columnDefinition = "varchar(255) default 'MYLINEHUB'")
    String organization;
    
    @Column(columnDefinition = "varchar(255) default 'smtpout.secureserver.net'")
    String smtphost;
    @Column(columnDefinition = "integer default 587")
    int smtpport;
    @Column(columnDefinition = "varchar(255) default 'support@mylinehub.com'")
    String smtpusername;
    @Column(columnDefinition = "varchar(255) default 'EX00lan@INT.'")
    String smtppassword;
    
    @Column(columnDefinition = "varchar(255) default '+919711761156'")
    String whatsAppNotificationNumber;
    


    @Column(columnDefinition = "varchar(255) default 'mylinehub_onboarding'")
    String onboardingTemplate;
    @Column(columnDefinition = "varchar(255) default 'mylinehub_low_funds'")
    String lowBalanceTemplate;
    
    @Column(columnDefinition = "varchar(255) default 'Vue'")
    String gstEngineName;
    
    @Column(columnDefinition = "varchar(255) default 'Idfy'")
    String gstEngineNameSecond;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
}