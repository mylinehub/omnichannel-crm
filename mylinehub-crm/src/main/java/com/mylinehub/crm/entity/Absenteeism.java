package com.mylinehub.crm.entity;

import lombok.*;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(indexes = {
		  @Index(name = "Absenteeism_Employee_Index", columnList = "EMPLOYEE_ID"),
		  @Index(name = "Absenteeism_DateFrom_Index", columnList = "DATE_FROM"),
		  @Index(name = "Absenteeism_DateTo_Index", columnList = "DATE_TO"),
		  @Index(name = "Absenteeism_Organization_Index", columnList = "organization"),
		})
public class Absenteeism {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "absenteeism_sequence"
    )
    @SequenceGenerator(
            name="absenteeism_sequence",
            sequenceName = "absenteeism_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;

    /*@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROA_CODE", nullable = false)
    private ReasonsOfAbsenteeism reasonOfAbsenteeismCode;*/
    
    private String absenteeismName;
    private String reasonForAbsense;

    @Column(name = "DATE_FROM")
    private Date dateFrom;
    @Column(name = "DATE_TO")
    private Date dateTo;
    
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    public Absenteeism(Employee employee,
                       //ReasonsOfAbsenteeism reasonOfAbsenteeismCode,
                       Date dateFrom,
                       Date dateTo,
                       String organization) {
        this.employee = employee;
        //this.reasonOfAbsenteeismCode = reasonOfAbsenteeismCode;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.organization=organization;
    }
}
