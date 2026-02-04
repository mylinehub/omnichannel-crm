package com.mylinehub.crm.rag.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transcription", indexes = {
	    @Index(name = "idx_transcription_documentId", columnList = "documentId"),
	    @Index(name = "idx_transcription_org", columnList = "organization")
	})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transcription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;

    private String provider;

    private String language;
    
    private String organization;

    @Column(columnDefinition="TEXT")
    private String text;

    private Instant createdOn = Instant.now();
}
