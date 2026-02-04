package com.mylinehub.crm.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name ="PRODUCTS",
indexes = {
		  @Index(name = "Product_Organization_Index", columnList = "organization"),
		  @Index(name = "Product_NAME_OF_PRODUCT_Index", columnList = "NAME_OF_PRODUCT"),
		  @Index(name = "Product_productStringType_Index", columnList = "productStringType"),
		})
public class Product {

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
    @Column(name ="PRODUCT_ID")
    private Long id;

    @Column(name ="NAME_OF_PRODUCT")
    private String name;

    private String productType;

    private String productStringType;
    private String units;
    private Double sellingPrice;
    private Double purchasePrice;
    private Double taxRate;


    private String unitsOfMeasure;

    private String organization;
    
    private String imageName;

    private String imageType;

    @Column(columnDefinition = "varchar(500)")
    private String imageData;
    @Column(columnDefinition = "bigint default 0")
    private Long imageSize;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public Product(Long id,
    		       String name,
                   String productType,
                   String productStringType,
                   String units,
                   Double sellingPrice,
                   Double purchasePrice,
                   Double taxRate,
                   String unitsOfMeasure,
                   String organization,
                   String imageName,
                   String imageType) {
    	this.id = id;
        this.name = name;
        this.productType = productType;
        this.productStringType = productStringType;
        this.units = units;
        this.sellingPrice = sellingPrice;
        this.purchasePrice = purchasePrice;
        this.taxRate = taxRate;
        this.unitsOfMeasure = unitsOfMeasure;
        this.organization=organization;
        this.imageName=imageName;
        this.imageType=imageType;
    }
}
