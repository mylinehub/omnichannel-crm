package com.mylinehub.crm.entity;

import com.mylinehub.crm.enums.UNITS_OF_MEASURE;
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
@Table(name = "PRODUCT_UNITS",
indexes = {
		  @Index(name = "ProductUnits_Organization_Index", columnList = "organization"),
		})
public class ProductUnits {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "product_units_sequence"
    )
    @SequenceGenerator(
            name="product_units_sequence",
            sequenceName = "product_units_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "UOM")
    private UNITS_OF_MEASURE unitOfMeasure;

    @Column(name = "UOM_ALT")
    private String alternativeUnitOfMeasure;

    private Double conversionFactor;

    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public ProductUnits(Product product,
                        UNITS_OF_MEASURE unitOfMeasure,
                        String alternativeUnitOfMeasure,
                        Double conversionFactor,
                        String organization) {
        this.product = product;
        this.unitOfMeasure = unitOfMeasure;
        this.alternativeUnitOfMeasure = alternativeUnitOfMeasure;
        this.conversionFactor = conversionFactor;
        this.organization=organization;
    }
}
