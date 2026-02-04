package com.mylinehub.crm.gst.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Table(name = "registeredgst",indexes = {
          @Index(name = "RegisteredGST_businessId_Index", columnList = "businessId"),
		 })
public class RegisteredGST {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SequenceGenerator(
            name = "RegisteredGST_sequence",
            sequenceName = "RegisteredGST_sequence",
            allocationSize = 1)
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "RegisteredGST_sequence")
    @Column(name = "RegisteredGST_ID")
    private Long id;
  
	@Column(unique = true)
    private String businessId;

	private String error;
	
	private String errorTrace;
	
	private String origin;
	
    @Column(columnDefinition="TEXT")
    private String gstResponse;
  
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
   
}
