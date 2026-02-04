package com.mylinehub.crm.rag.model;

import com.pgvector.PGvector;
import lombok.*;

import javax.persistence.*;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "embedding", indexes = {
        @Index(name = "idx_embedding_documentChunkId", columnList = "documentChunkId"),
        @Index(name = "idx_embedding_org", columnList = "organization")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentChunkId;

    private String organization;

    /**
     * Use PGvector type for PostgreSQL vector column
     */
    @Type(type = "com.mylinehub.crm.rag.util.PGVectorType")
    @Column(name = "vector", columnDefinition = "vector(1536)")
    private PGvector vector;
}
