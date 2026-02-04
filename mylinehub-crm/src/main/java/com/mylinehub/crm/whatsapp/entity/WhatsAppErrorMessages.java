package com.mylinehub.crm.whatsapp.entity;

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
@Table(name = "whatsapperrormessages",
indexes = {
		  @Index(name = "whatsapperrormessages_phoneNumberMain_Index", columnList = "phoneNumberMain"),
		})
public class WhatsAppErrorMessages {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SequenceGenerator(
            name = "whatsapperrormessages_sequence",
            sequenceName = "whatsapperrormessages_sequence",
            allocationSize = 1)
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "whatsapperrormessages_sequence")
    @Column(name = "whatsapperrormessages_ID")
    private Long id;

	 @Column(columnDefinition="TEXT")
	private String error;
	 
	private String phoneNumberMain;
	
    @Column(columnDefinition="TEXT")
    private String messageInput;
  
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
   
}
