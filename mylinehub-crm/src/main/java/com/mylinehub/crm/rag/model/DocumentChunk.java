package com.mylinehub.crm.rag.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "document_chunk", indexes = {
        @Index(name = "idx_chunk_document", columnList = "documentId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;

    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT")
    private String text;

    private Integer tokenCount;
}
