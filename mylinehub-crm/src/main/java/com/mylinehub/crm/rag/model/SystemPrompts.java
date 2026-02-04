package com.mylinehub.crm.rag.model;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumber;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "system_prompts",
indexes = {
		  @Index(name = "system_prompts_id_Index", columnList = "id"),
		  @Index(name = "system_prompts_Organization_Index", columnList = "organization"),
		  @Index(name = "system_prompts_type_Index", columnList = "type"),
		  @Index(name = "system_prompts_active_Index", columnList = "active"),
		})
public class SystemPrompts {

	  @Id
	    @GeneratedValue(
	            strategy = GenerationType.SEQUENCE,
	            generator = "systemprompts_sequence"
	    )
	    @SequenceGenerator(
	            name="whatsapp_prompt_sequence",
	            sequenceName = "systemprompts_sequence",
	            allocationSize = 1,
	            initialValue = 100
	    )
	    @Column(nullable = false)
	    private Long id;

	    
	    @Column(columnDefinition="TEXT")
	    private String prompt;
	    private String type;
	    private boolean active = true;
	    private String organization;
	    
	    @Column(updatable = false)
	    @CreationTimestamp
	    private Instant createdOn;
	    
	    @UpdateTimestamp
	    private Instant lastUpdatedOn;
    
}
