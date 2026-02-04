package com.mylinehub.crm.rag.model;

import lombok.*;
import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document", indexes = {
    @Index(name = "idx_document_filehash", columnList = "fileHash"),
    @Index(name = "idx_document_originalFilename", columnList = "originalFilename"),
    @Index(name = "idx_document_org", columnList = "organization")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String organization;

    private String originalFilename;

    @Column(nullable = false)
    private String fileHash;

    private String mimeType;

    private Long sizeBytes;

    private String uploader;

    private boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private Instant createdOn = Instant.now();

    private Instant lastUpdatedOn = Instant.now();
}
