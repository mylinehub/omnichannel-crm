package com.mylinehub.crm.rag.model;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_error_responses", indexes = {
	    @Index(name = "idx_ai_error_responses_assistantId", columnList = "assistantId"),
	    @Index(name = "idx_ai_error_responses_org", columnList = "organization")
	})
public class AIError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String organization;

    private String sessionId;  // (maps to thread ID)

    private String assistantId;

    @Lob
    private String errorString;

    @Lob
    private String responseString;

    private LocalDateTime createdAt = LocalDateTime.now();
    
}
