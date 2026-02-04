package com.mylinehub.crm.entity;

import lombok.*;
import java.time.Instant;
import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "customer")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
    name = "customer_franchise_inventory",
    indexes = {
        @Index(name = "CFI_customer_id_idx", columnList = "CUSTOMER_ID"),
        @Index(name = "CFI_interest_idx", columnList = "interest"),
        @Index(name = "CFI_available_idx", columnList = "available"),
        @Index(name = "CFI_created_on_idx", columnList = "createdOn")
    }
)
public class CustomerFranchiseInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * If you want one franchise row per customer: keep unique=true
     * If you want multiple rows per customer: change to @ManyToOne and remove unique=true
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false, unique = true)
    @JsonIgnore
    private Customers customer;

    // =========================================================
    // Franchise fields
    // =========================================================
    @Column(name = "interest", columnDefinition = "varchar(255)")
    private String interest;

    @Column(columnDefinition = "boolean default true")
    private Boolean available;

    // optional auditing
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;

    @UpdateTimestamp
    private Instant lastUpdatedOn;
}
