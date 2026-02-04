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
@Table(indexes = {
		  @Index(name = "Supplier_Organization_Index", columnList = "organization"),
		  @Index(name = "Supplier_suppliertype_Index", columnList = "suppliertype"),
		  @Index(name = "Supplier_transportcapacity_Index", columnList = "transportcapacity"),
		  @Index(name = "Supplier_supplierName_Index", columnList = "supplierName"),
		})
public class Supplier {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "supplier_sequence"
    )
    @SequenceGenerator(
            name="supplier_sequence",
            sequenceName = "supplier_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    private Long supplierId;
    
    @Column(unique = true)
    private String supplierName;

    @Enumerated(EnumType.STRING)
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "MODE_OF_TRANSPORT_CODE")
    private TypesOfTransport modeOfTransportCode;

    private String modeOfTransport;
    private String activityStatus;
    private String organization;
    private String transportcapacity;
    private String suppliertype;
    private String priceunits;
    private String weightunit;
    private String lengthunit;
    
    String supplierPhoneNumber;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public Supplier(String supplierName,
                    TypesOfTransport modeOfTransportCode,
                    String activityStatus,
                    String organization) {
        this.supplierName = supplierName;
        this.modeOfTransportCode = modeOfTransportCode;
        this.activityStatus = activityStatus;
        this.organization=organization;
    }
}
