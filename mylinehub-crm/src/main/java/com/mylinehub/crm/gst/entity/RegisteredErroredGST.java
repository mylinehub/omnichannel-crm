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
@Table(name = "registerederrorgst")
public class RegisteredErroredGST {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SequenceGenerator(
            name = "RegisteredErrorGST_sequence",
            sequenceName = "RegisteredErrorGST_sequence",
            allocationSize = 1)
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "RegisteredErrorGST_sequence")
    @Column(name = "RegisteredErrorGST_ID")
    private Long id;

	
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
