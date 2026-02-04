package com.mylinehub.crm.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "postId")
public class Post {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    @SequenceGenerator(
            name="post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1,
            initialValue = 100
    )

    private Long postId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AUTHOR")
    private Employee author;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, length = 40)
    @NotBlank(message = "Post Title cannot be empty or Null")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.REMOVE)
    private Set<Comment> comments;

    private String organization;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    
    public Post(Employee author,
                LocalDateTime createdAt,
                String title,
                String content,
                String organization) {
        this.author = author;
        this.createdAt = createdAt;
        this.title = title;
        this.content = content;
        this.organization = organization;
    }
}
