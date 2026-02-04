package com.mylinehub.crm.rag.model;

import javax.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(name = "assistants", indexes = {
    @Index(name = "idx_assistants_name", columnList = "name"),
    @Index(name = "idx_assistants_assistantId", columnList = "assistantId"),
    @Index(name = "idx_assistants_org", columnList = "organization")
})
public class AssistantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String assistantId; // From OpenAI API

    @Column(nullable = false)
    private String name;

    private String model;


    private String organization;

    private String systemPromptId;


}
