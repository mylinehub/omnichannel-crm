package com.mylinehub.voicebridge.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "llm_completion_error",
    indexes = {
        @Index(name = "llm_err_created_at_idx", columnList = "created_at"),
        @Index(name = "llm_err_channel_idx", columnList = "channel_id"),
        @Index(name = "llm_err_stasis_idx", columnList = "stasis_app_name")
    }
)
public class LlmCompletionError {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "stasis_app_name", nullable = false)
  private String stasisAppName;

  @Column(name = "organization")
  private String organization;

  @Column(name = "channel_id")
  private String channelId;

  @Column(name = "caller_number")
  private String callerNumber;

  @Column(name = "model")
  private String model;

  @Column(name = "attempt", nullable = false)
  private Integer attempt;

  @Column(name = "error_type", nullable = false)
  private String errorType;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  @Column(name = "prompt_template", columnDefinition = "text")
  private String promptTemplate;

  @Column(name = "transcript_en", columnDefinition = "text")
  private String transcriptEn;

  @Column(name = "raw_output", columnDefinition = "text")
  private String rawOutput;
}
