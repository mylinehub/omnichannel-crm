package com.mylinehub.crm.entity;

import java.time.Instant;
import java.util.Date;

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
@Table(name = "notification",
indexes = {
		  @Index(name = "Notification_Organization_Index", columnList = "organization"),
		  @Index(name = "Notification_forExtension_Index", columnList = "forExtension"),
		  @Index(name = "Notification_isDeleted_Index", columnList = "isDeleted"),
		})
public class Notification {
	
	@Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "notification_sequence"
    )
    @SequenceGenerator(
            name="notification_sequence",
            sequenceName = "notification_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "History_ID", nullable = false, unique = true)
	public Long id;
	@Column(nullable = false)
	public String forExtension;
	public String organization;
	@Column(nullable = false)
	public String alertType;
	public String notificationType;
	@Column(nullable = false)
	public String title;
	@Column(nullable = false)
	public String message;
	
	@Column(name = "creationDate", nullable = false, updatable = false)
	@CreationTimestamp
	public Date creationDate;

	@UpdateTimestamp
	private Instant lastUpdatedOn;
	    
	@Column(columnDefinition = "boolean default false")
	public boolean isDeleted;

}
