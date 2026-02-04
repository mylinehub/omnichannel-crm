package com.mylinehub.voicebridge.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(
    name = "call_history_record",
    indexes = {
    		@Index(name="chr_caller_number_idx", columnList="caller_number"),
    		@Index(name="chr_org_idx", columnList="organization"),
    		@Index(name="chr_stasis_app_idx", columnList="stasis_app_name"),
    		@Index(name="chr_started_at_idx", columnList="started_at"),
    		@Index(name="chr_entry_type_idx", columnList="entry_type"),
    		@Index(name="chr_channel_id_idx", columnList="channel_id")
    		
    }
)
public class CallHistoryRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "entry_type", nullable = false, length = 128)
  private String entryType;
  
  // -----------------------------
  // Primary identifiers
  // -----------------------------
  @Column(name = "channel_id", nullable = false, length = 128)
  private String channelId;

  @Column(name = "stasis_app_name", length = 128)
  private String stasisAppName;

  @Column(name = "organization", length = 256)
  private String organization;

  @Column(name = "caller_number", length = 64)
  private String callerNumber;

  // -----------------------------
  // Timeline
  // -----------------------------
  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Column(name = "duration_seconds")
  private Long durationSeconds;

  @Column(name = "end_reason", length = 256)
  private String endReason;

  // -----------------------------
  // Content (TEXT, no @Lob)
  // -----------------------------
  @Column(name = "caller_transcript_en", columnDefinition = "text")
  private String callerTranscriptEn;

  @Column(name = "completeCallTranscriptOriginal", columnDefinition = "text")
  private String completeCallTranscriptOriginal;
  
  @Column(name = "completeCallTranscriptEn", columnDefinition = "text")
  private String completeCallTranscriptEn;
  
  @Column(name = "summary", columnDefinition = "text")
  private String summary;
  
  @Column(name = "rag_context", columnDefinition = "text")
  private String ragContext;

  @Column(name = "final_completion_json", columnDefinition = "text")
  private String finalCompletionJson;

  // -----------------------------
  // Billing / counters (copy what you need)
  // -----------------------------
  @Column(name = "total_caller_words")
  private Long totalCallerWords;

  @Column(name = "total_ai_chars_sent")
  private Long totalAiCharsSent;

  @Column(name = "total_ai_chars_recv")
  private Long totalAiCharsRecv;

  @Column(name = "total_approx_tokens")
  private Long totalApproxTokens;

  @Column(name = "total_rag_queries")
  private Long totalRagQueries;

  @Column(name = "total_rag_context_chars")
  private Long totalRagContextChars;

  // -----------------------------
  // Recording
  // -----------------------------
  @Column(name = "recording_path", length = 1024)
  private String recordingPath;

  @Column(name = "recording_file_name", length = 512)
  private String recordingFileName;

  // Optional: Postgres Large Object OID if you implement it later
  @Column(name = "recording_oid")
  private Long recordingOid;

  // -----------------------------
  // Useful config snapshot (keep small)
  // -----------------------------
  @Column(name = "bot_mode", length = 32)
  private String botMode;

  @Column(name = "rtp_codec", length = 32)
  private String rtpCodec;

  @Column(name = "ai_pcm_sample_rate_hz")
  private Integer aiPcmSampleRateHz;

  // -----------------------------
  // Ingest bookkeeping
  // -----------------------------
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
