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
		  @Index(name = "Purchases_Organization_Index", columnList = "organization"),
		  @Index(name = "Purchases_CUSTOMER_ID_Index", columnList = "CUSTOMER_ID"),
		  @Index(name = "Purchases_purchaseDate_Index", columnList = "purchaseDate"),
		})
public class Purchases {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "purchases_sequence"
    )
    @SequenceGenerator(
            name="purchases_sequence",
            sequenceName = "purchases_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "PURCHASE_ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customers customer;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "Employee_ID", nullable = false)
    private Employee soldBy;
    
    private boolean receiptExist;
    private boolean invoiceExist;

    @OneToOne
    @JoinColumn(name = "INVOICE_ID")
    private SellingInvoice invoice;
    
    @Column(columnDefinition = "DATE DEFAULT CURRENT_DATE")
    private Date purchaseDate;
    
    private String purchaseName;
    private String quantity;
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public Purchases(Customers customer,
                     boolean receiptExist,
                     boolean invoiceExist,
                     SellingInvoice invoice,
                     Date purchaseDate,
                     String organization) {
        this.customer = customer;
        this.receiptExist = receiptExist;
        this.invoiceExist = invoiceExist;
        this.invoice = invoice;
        this.purchaseDate = purchaseDate;
        this.organization = organization;
    }
}
