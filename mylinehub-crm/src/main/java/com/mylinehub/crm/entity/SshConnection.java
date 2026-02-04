package com.mylinehub.crm.entity;

import java.time.Instant;

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
import org.hibernate.annotations.Type;
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
@Table(name ="SSH_CONNECTION",
indexes = {
		  @Index(name = "SshConnection_Organization_Index", columnList = "organization"),
		  @Index(name = "SshConnection_active_Index", columnList = "active"),
		  @Index(name = "SshConnection_domain_Index", columnList = "domain"),
		})
public class SshConnection {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "products_sequence"
    )
    @SequenceGenerator(
            name="products_sequence",
            sequenceName = "products_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    private Long id;
    
    private String phonecontext;
    private String organization;
    private String sshHostType;
    private String type;
    private String sshUser;
    private String password;
    private String authType;
    private boolean active;

    private String domain;
    
    private String port;
    
    @Column(columnDefinition = "TEXT", nullable = true)
    private String privateKey;
    
    @Column(columnDefinition = "TEXT", nullable = true)
    private String publicKey;
    
    @Column(columnDefinition = "TEXT", nullable = true)
    private String extraKey;
    
    private String pemFileName;
    private String pemFileLocation;
    private String connectionString;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
}
