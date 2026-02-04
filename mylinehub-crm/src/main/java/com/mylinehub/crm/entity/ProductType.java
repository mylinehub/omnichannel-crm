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
@Table(name = "PRODUCT_TYPE")
public class ProductType {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "product_type_sequence"
    )
    @SequenceGenerator(
            name="product_type_sequence",
            sequenceName = "product_type_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name ="PRODUCT_TYPE_ID")
    private String id;

    private String fullName;
    private Double discount;
    private Character periodOfAvailability;
    
    private String organization;

    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public ProductType(String fullName,
                       Double discount,
                       Character periodOfAvailability,
                       String organization) {
        this.fullName = fullName;
        this.discount = discount;
        this.periodOfAvailability = periodOfAvailability;
        this.organization=organization;
    }
}
