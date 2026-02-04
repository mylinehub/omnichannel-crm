package com.mylinehub.crm.entity;

import com.mylinehub.crm.enums.MODE_OF_TRANSPORT_CODE;
import lombok.*;

import java.time.Instant;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "TYPES_OF_TRANSPORT")
public class TypesOfTransport {

    @Id
    @Enumerated(EnumType.STRING)
    private MODE_OF_TRANSPORT_CODE code;
    private String fullName;
    private Double minLength;
    private Double maxLength;
    private Double minWeight;
    private Double maxWeight;
    private Integer transportCapacity;
    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    TypesOfTransport(String fullName,Double minLength,Double maxLength,Double minWeight,Double maxWeight,Integer transportCapacity,String organization)
    {
    	
    	this.fullName=fullName;
    	this.minLength=minLength;
    	this.maxLength=maxLength;
    	this.minWeight=minWeight;
    	this.maxWeight=maxWeight;
    	this.transportCapacity=transportCapacity;
    	this.organization=organization;
    	
    }

}
