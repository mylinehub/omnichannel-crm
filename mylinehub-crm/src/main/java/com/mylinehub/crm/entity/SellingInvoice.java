package com.mylinehub.crm.entity;

import com.mylinehub.crm.enums.CURRENCY;
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
@Table(name = "SELLING_INVOICE")
public class SellingInvoice {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "selling_invoice_sequence"
    )
    @SequenceGenerator(
            name="selling_invoice_sequence",
            sequenceName = "selling_invoice_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(name = "INVOICE_ID", nullable = false)
    private Long id;
    @Column(name = "INVOICE_DATE", nullable = false)
    private Date invoiceDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customers customer;

    @Column(nullable = false)
    private Double netWorth;

    @Column(nullable = false)
    private Double grossValue;

    @Column(nullable = false)
    private Double taxRate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CURRENCY currency;

    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public SellingInvoice(Date invoiceDate,
                          Customers customer,
                          Double netWorth,
                          Double grossValue,
                          Double taxRate,
                          CURRENCY currency,
                          String organization) {
        this.invoiceDate = invoiceDate;
        this.customer = customer;
        this.netWorth = netWorth;
        this.grossValue = grossValue;
        this.taxRate = taxRate;
        this.currency = currency;
        this.organization=organization;
    }
}
