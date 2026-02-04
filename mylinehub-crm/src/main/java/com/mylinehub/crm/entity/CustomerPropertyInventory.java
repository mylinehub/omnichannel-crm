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
    name = "customer_property_inventory",
    indexes = {
        @Index(name = "CPI_customer_id_idx", columnList = "CUSTOMER_ID"),
        @Index(name = "CPI_pid_idx", columnList = "pid"),
        @Index(name = "CPI_city_idx", columnList = "city"),
        @Index(name = "CPI_area_idx", columnList = "area"),
        @Index(name = "CPI_call_status_idx", columnList = "callStatus"),
        @Index(name = "CPI_property_type_idx", columnList = "propertyType"),
        @Index(name = "CPI_listed_date_idx", columnList = "listedDate"),
        @Index(
                name = "CPI_updated_listed_idx",
                columnList = "updatedByAi, listedDate, id"
            )
    }
)
public class CustomerPropertyInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false, unique = true)
    @JsonIgnore
    private Customers customer;

    // =========================================================
    // Property / Inventory fields
    // =========================================================

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;
    
    @Column(name = "premise_name", columnDefinition = "varchar(255)")
    private String premiseName;

    @Column(name = "listedDate")
    private Instant listedDate;

    @Column(name = "propertyType", columnDefinition = "varchar(100)")
    private String propertyType;

    // Excel column "rent" (true/false). If later you need Rent/Sale/Both,
    // change this to availability enum/string.
    @Column(name = "rent", columnDefinition = "boolean default false")
    private boolean rent;

    @Column(name = "rentValue", columnDefinition = "bigint")
    private Long rentValue;

    @Column(name = "bhk", columnDefinition = "integer")
    private Integer bhk;

    @Column(name = "furnishedType", columnDefinition = "varchar(50)")
    private String furnishedType;

    @Column(name = "sqFt", columnDefinition = "integer")
    private Integer sqFt;

    // Keep city here too if your excel has it per-property
    @Column(name = "city", columnDefinition = "varchar(255)")
    private String city;


    @Column(columnDefinition = "TEXT")
    private String nearby;

    @Column(columnDefinition = "TEXT")
    private String area;

    @Column(name = "callStatus", columnDefinition = "varchar(100)")
    private String callStatus;

    @Column(name = "propertyAge", columnDefinition = "integer")
    private Integer propertyAge;

    @Column(name = "unitType", columnDefinition = "varchar(100)")
    private String unitType;

    @Column(name = "tenant", columnDefinition = "varchar(100)")
    private String tenant;

    @Column(name = "facing", columnDefinition = "varchar(50)")
    private String facing;

    @Column(name = "totalFloors", columnDefinition = "integer")
    private Integer totalFloors;

    @Column(name = "brokerage", columnDefinition = "varchar(50)")
    private String brokerage;

    @Column(name = "balconies", columnDefinition = "integer")
    private Integer balconies;

    @Column(name = "washroom", columnDefinition = "integer")
    private Integer washroom;

    @Column(name = "unitNo", columnDefinition = "varchar(50)")
    private String unitNo;

    @Column(name = "floorNo", columnDefinition = "varchar(50)")
    private String floorNo;

    @Column(name = "pid", columnDefinition = "varchar(100)")
    private String pid;

    @Column(name = "property_description1", columnDefinition = "TEXT")
    private String propertyDescription1;
    
    @Column(name = "moreThanOneProperty", columnDefinition = "boolean default false")
    private Boolean moreThanOneProperty;
    
    @Column(name = "updatedByAi", columnDefinition = "boolean default false")
    private Boolean updatedByAi;
    
    @Column(columnDefinition = "boolean default true")
    private Boolean available;
    
    // optional auditing
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;

    @UpdateTimestamp
    private Instant lastUpdatedOn;
}
