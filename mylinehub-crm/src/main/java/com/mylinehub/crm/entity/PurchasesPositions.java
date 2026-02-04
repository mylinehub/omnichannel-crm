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
@Table(name = "PURCHASES_POSITIONS")
public class PurchasesPositions {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "purchases_positions_sequence"
    )
    @SequenceGenerator(
            name="purchases_positions_sequence",
            sequenceName = "purchases_positions_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "PURCHASE_NUMBER")
    private Long id;

    @OneToOne
    @JoinColumn(name = "PURCHASE_ID", nullable = false)
    private Purchases purchases;

    @OneToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    private Double amount;
    private Character reclamationExist;

    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public PurchasesPositions(Purchases purchases,
                              Product product,
                              Double amount,
                              Character reclamationExist,
                              String organization) {
        this.purchases = purchases;
        this.product = product;
        this.amount = amount;
        this.reclamationExist = reclamationExist;
        this.organization = organization;
    }
}
