package com.mylinehub.crm.entity;

import lombok.*;

import java.time.Instant;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "REASONS_OF_ABSENTEEISM")
public class ReasonsOfAbsenteeism {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "reasons_of_absenteeism_sequence"
    )
    @SequenceGenerator(
            name="reasons_of_absenteeism_sequence",
            sequenceName = "reasons_of_absenteeism_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String absenteeismName;
    private Character consent;
    private String comments;
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public ReasonsOfAbsenteeism(String absenteeismName,
                                Character consent,
                                String comments,
                                String organization) {
        this.absenteeismName = absenteeismName;
        this.consent = consent;
        this.comments = comments;
        this.organization=organization;
    }
}
