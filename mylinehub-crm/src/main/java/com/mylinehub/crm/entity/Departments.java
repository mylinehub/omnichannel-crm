package com.mylinehub.crm.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import java.time.Instant;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Table(name = "DEPARTMENTS",
indexes = {
		  @Index(name = "Departments_departmentName_Index", columnList = "departmentName"),
		  @Index(name = "Departments_Organization_Index", columnList = "organization"),
		})
public class Departments {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "departments_sequence"
    )
    @SequenceGenerator(
            name="departments_sequence",
            sequenceName = "departments_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "DEPARTMENT_CODE")
    private Long id;

    private String departmentName;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    //@JoinColumn(name = "MANAGER_ID")
    //@JsonBackReference
    private Employee managers;

    private String city;
    
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    Departments(String departmentName,String city,String organization)
    {
    	this.departmentName=departmentName;
    	this.city=city;
    	this.organization = organization;
    }

}
