package com.mylinehub.crm.rag.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ingest_error")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;

    private String filename;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant createdOn = Instant.now();
}
