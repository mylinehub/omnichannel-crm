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
@Table(name = "AMI_CONNECTION",
	   indexes = {
		  @Index(name = "AmiConnection_PhoneContext_Index", columnList = "phonecontext"),
		  @Index(name = "AmiConnection_Organization_Index", columnList = "organization"),
		})
public class AmiConnection {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ami_connection_sequence"
    )
    @SequenceGenerator(
            name="ami_connection_sequence",
            sequenceName = "ami_connection_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    private String domain;
    
    @Column(columnDefinition = "integer default 5038")
    private int port;

    
    private String amiuser;
    private String password;
    private String phonecontext;
    private String organization;
    
    @Column(columnDefinition = "boolean default true")
    private boolean isactive;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    AmiConnection (String domain,String amiuser,String password,String phonecontext,String organization)
    {
    	this.domain = domain;
    	this.amiuser = amiuser;
    	this.password = password;
    	this.phonecontext = phonecontext;
    	this.organization=organization;
    }
    
}
