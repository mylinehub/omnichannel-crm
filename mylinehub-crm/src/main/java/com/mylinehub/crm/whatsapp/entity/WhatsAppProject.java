package com.mylinehub.crm.whatsapp.entity;

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
@Table(name = "WhatsAppProject",
indexes = {
		  @Index(name = "whatsapp_project_id_Index", columnList = "id"),
		  @Index(name = "whatsapp_project_Organization_Index", columnList = "organization"),
		})
public class WhatsAppProject {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapp_project_sequence"
    )
    @SequenceGenerator(
            name="whatsapp_project_sequence",
            sequenceName = "whatsapp_project_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    //MYLINEHUB
    private String appName;
    //shipramittal1992@gmail.com // should be organization email
    private String appEmail;
    
    //953659690235569
    private String appID;
    //2d917b30ac83740f506b340d5d3ea4df
    private String appSecret;
    
    //v22.0
    private String apiVersion;
    
    //1895330753856220
    private String businessID;  
    
    //IT SERVICES
    private String businessPortfolio;
    
    
    //EAANjWTMStrEBOxk7JYJmpJLQsZAakqragMLv7aDaehuoBBPWmDop3c4NS1RoGTlI4tl2V3wO3grLowE4XZCpJ2hfBFClo2AEbGQ3ZAcXbbJZBiUil4oWJoys3VJE8UZC5t1P7G3Gashk3BOpfHKLjxXtIhmvgVrrCSGUpC5X5T2ZAXPsZCBgjjFZAcS7zN5mMYCmPwZDZD
    //EAANjWTMStrEBOxk7JYJmpJLQsZAakqragMLv7aDaehuoBBPWmDop3c4NS1RoGTlI4tl2V3wO3grLowE4XZCpJ2hfBFClo2AEbGQ3ZAcXbbJZBiUil4oWJoys3VJE8UZC5t1P7G3Gashk3BOpfHKLjxXtIhmvgVrrCSGUpC5X5T2ZAXPsZCBgjjFZAcS7zN5mMYCmPwZDZD
    private String accessToken;
    
    //ccee648ca70237998378b338474c799c
    private String clientToken;
    
    //whatapp business account id
    //2992282160936907
    
    //Phone Number Id
    //595217710339203
    
    //Phone Number
    //+919711761156
    
    //ID: Sys User 
    //61573872417416
    //Admin access

    
    //Registrining organization
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
}