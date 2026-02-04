package com.mylinehub.crm.entity;

import lombok.*;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;



@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name ="UsedRegistrationToken",
indexes = {
		  @Index(name = "usedToken_Index", columnList = "usedToken"),
		})
public class UsedRegistrationToken {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "UsedRegistrationToken_sequence"
    )
    @SequenceGenerator(
            name="UsedRegistrationToken_sequence",
            sequenceName = "UsedRegistrationToken_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    private Long id;

    @Column(unique = true)
    private String usedToken;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
}
