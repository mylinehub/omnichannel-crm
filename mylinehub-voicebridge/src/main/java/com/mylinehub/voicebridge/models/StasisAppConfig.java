/*
 * File: src/main/java/com/mylinehub/voicebridge/models/StasisAppConfig.java
 *
 * Purpose:
 *   - Full per-Stasis-app configuration.
 *   - One row per stasis_app_name.
 *   - Mirrors stasis_app_config table (snake_case columns).
 *
 * Notes:
 *   - server.* remains in application.properties.
 *   - This entity MUST match DB column names exactly.
 */

package com.mylinehub.voicebridge.models;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "stasis_app_config")
public class StasisAppConfig {

  // =========================
  // PRIMARY
  // =========================
  @Id
  @Column(name = "stasis_app_name", nullable = false, length = 128)
  private String stasis_app_name;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  // =========================
  // IDENTITY
  // =========================
  @Column(name = "organization", length = 256)
  private String organization;

  @Column(name = "agent_name", length = 256)
  private String agent_name;

  @Column(name = "agent_defaultlanguage", length = 64)
  private String agent_defaultlanguage;

  @Column(name = "ai_barge_in_enabled")
  private Boolean ai_barge_in_enabled;

  // =========================
  // ARI / ASTERISK
  // =========================
  @Column(name = "ari_ws_url", length = 512)
  private String ari_ws_url;

  @Column(name = "ari_rest_base_url", length = 512)
  private String ari_rest_baseUrl;

  @Column(name = "ari_username", length = 128)
  private String ari_username;

  @Column(name = "ari_password", length = 256)
  private String ari_password;

  // =========================
  // RTP / AUDIO
  // =========================
  @Column(name = "rtp_codec", length = 32)
  private String rtp_codec;

  @Column(name = "rtp_payload_pt")
  private Integer rtp_payload_pt;

  @Column(name = "rtp_bind_ip", length = 64)
  private String rtp_bind_ip;

  @Column(name = "rtp_clock_rate")
  private Integer rtp_clock_rate;

  @Column(name = "rtp_frame_ms")
  private Integer rtp_frame_ms;

  @Column(name = "rtp_bind_port")
  private Integer rtp_bind_port;

  @Column(name = "rtp_external_host", length = 256)
  private String rtp_external_host;

  // =========================
  // OPENAI / REALTIME
  // =========================
  @Column(name = "ai_realtime_ws_url", length = 512)
  private String ai_realtime_ws_url;

  @Column(name = "ai_model_completion", length = 128)
  private String ai_model_completion;
  
  @Column(name = "ai_model_realtime", length = 128)
  private String ai_model_realtime;

  @Column(name = "ai_model_transcribe", length = 128)
  private String ai_model_transcribe;

  @Column(name = "ai_model_tts", length = 128)
  private String ai_model_tts;

  @Column(name = "ai_openai_api_key", length = 512)
  private String ai_openai_apiKey;

  @Column(name = "ai_realtime_session_url", length = 512)
  private String ai_realtime_session_url;

  @Column(name = "ai_concurrency_max_calls_per_instance")
  private Integer ai_concurrency_maxCallsPerInstance;

  @Column(name = "ai_temperature")
  private Double ai_temperature;

  @Column(name = "ai_pcm_sample_rate_hz")
  private Integer ai_pcm_sampleRateHz;

  @Column(name = "ai_voice", length = 64)
  private String ai_voice;

  @Column(name = "ai_voice_options", length = 512)
  private String ai_voice_options;

  @Column(name = "ai_voice_input", length = 64)
  private String ai_voice_input;

  @Column(name = "ai_voice_output", length = 64)
  private String ai_voice_output;

  @Column(name = "ai_inbound_chunk_ms")
  private Integer ai_inbound_chunk_ms;

  @Column(name = "call_max_seconds")
  private Integer call_max_seconds;

  @Column(name = "barge_in_energy_threshold")
  private Integer barge_in_energy_threshold;

  // =========================
  // MYLINEHUB ORG / AUTH
  // =========================
  @Column(name = "mylinehub_login_url", length = 512)
  private String mylinehub_login_url;

  @Column(name = "mylinehub_login_username", length = 256)
  private String mylinehub_login_username;

  @Column(name = "mylinehub_login_password", length = 256)
  private String mylinehub_login_password;

  @Column(name = "mylinehub_org_lookup_url", length = 512)
  private String mylinehub_org_lookup_url;

  @Column(name = "mylinehub_base_url", length = 512)
  private String mylinehub_base_url;

  @Column(name = "mylinehub_crm_cdr_url", length = 512)
  private String mylinehub_crm_cdr_url;

  @Column(name = "mylinehub_crm_customer_get_by_phone_url", length = 512)
  private String mylinehub_crm_customer_get_by_phone_url;

  @Column(name = "mylinehub_crm_customer_update_by_org_url", length = 512)
  private String mylinehub_crm_customer_update_by_org_url;
  
  @Column(name = "mylinehub_crm_deduct_ai_amount_url", length = 512)
  private String mylinehub_crm_deduct_ai_amount_url;
  
  // =========================
  // EXTERNAL BOT
  // =========================
  @Column(name = "bot_mode", length = 32)
  private String bot_mode;

  @Column(name = "bot_external_ws_url", length = 512)
  private String bot_external_ws_url;

  @Column(name = "bot_auth_required")
  private Boolean bot_auth_required;

  @Column(name = "bot_auth_ws_type", length = 64)
  private String bot_auth_ws_type;

  @Column(name = "bot_external_basic_user", length = 256)
  private String bot_external_basic_user;

  @Column(name = "bot_external_basic_pass", length = 256)
  private String bot_external_basic_pass;

  @Column(name = "bot_external_token", length = 512)
  private String bot_external_token;

  @Column(name = "bot_external_mode", length = 64)
  private String bot_external_mode;

  @Column(name = "bot_external_lang", length = 64)
  private String bot_external_lang;

  // =========================
  // GOOGLE GEMINI LIVE
  // =========================
  @Column(name = "ai_google_live_ws_url", length = 512)
  private String ai_google_live_ws_url;

  @Column(name = "ai_google_live_api_key", length = 512)
  private String ai_google_live_apiKey;

  @Column(name = "ai_google_live_model", length = 256)
  private String ai_google_live_model;

  @Column(name = "ai_google_live_temperature")
  private Double ai_google_live_temperature;

  @Column(name = "ai_google_primary_language", length = 64)
  private String ai_google_primary_language;

  // =========================
  // RAG
  // =========================
  @Column(name = "rag_vector_store_url", length = 512)
  private String rag_vector_store_url;

  @Column(name = "rag_topk")
  private Integer rag_topK;

  @Column(name = "rag_max_context_tokens")
  private Integer rag_maxContextTokens;

  // =========================
  // FLAGS / JSON
  // =========================
  @Column(name = "fetch_customer")
  private Boolean fetch_Customer;

  @Column(name = "savePropertyInventory")
  private Boolean savePropertyInventory;

  @Column(name = "save_call_details")
  private Boolean save_Call_Details;

  @Column(name = "do_rag")
  private Boolean do_rag;

  @Column(name = "do_json_summarization")
  private Boolean do_Json_Summarization;

  @JdbcTypeCode(SqlTypes.LONGVARCHAR)
  @Column(name = "json_summarization_meta_data", nullable = false, columnDefinition = "text")
  private String json_Summarization_Meta_Data;

  // =========================
  // RECORDING
  // =========================
  @Column(name = "recording_enabled")
  private Boolean recording_enabled;

  @Column(name = "recording_mode", length = 64)
  private String recording_mode;

  @Column(name = "recording_s3_bucket", length = 256)
  private String recording_s3_bucket;

  @Column(name = "recording_s3_endpoint", length = 256)
  private String recording_s3_endpoint;

  @Column(name = "recording_local_base_path", length = 512)
  private String recordingLocalBasePath;

  // =========================
  // QUEUE
  // =========================
  @Column(name = "queue_max_ms")
  private Integer queue_maxMs;

  @Column(name = "queue_pause_ms")
  private Integer queue_pauseMs;

  @Column(name = "queue_watermark_high_percent")
  private Integer queue_watermarkHighPercent;

  @Column(name = "queue_watermark_low_percent")
  private Integer queue_watermarkLowPercent;

  // =========================
  // RESILIENCE
  // =========================
  @Column(name = "resilience_retry_max_attempts")
  private Integer resilience_retry_maxAttempts;

  @Column(name = "resilience_circuitbreaker_failure_threshold")
  private Integer resilience_circuitbreaker_failureThreshold;

  @Column(name = "ws_reconnect_initial_delay_ms")
  private Integer ws_reconnect_initialDelayMs;

  @Column(name = "ws_reconnect_max_delay_ms")
  private Integer ws_reconnect_maxDelayMs;

  // =========================
  // METRICS
  // =========================
  @Column(name = "metrics_namespace", length = 128)
  private String metrics_namespace;

  @Column(name = "performance_mouth_to_ear_ms_target")
  private Integer performance_mouthToEarMsTarget;

  @Column(name = "performance_jitter_msp95")
  private Integer performance_jitterMsP95;

  // =========================
  // EXECUTORS
  // =========================
  @Column(name = "executor_media_pool_size")
  private Integer executor_media_poolSize;

  @Column(name = "executor_io_pool_size")
  private Integer executor_io_poolSize;
  
  @Column(name = "dsp_enabled")
  private Boolean dspEnabled = false;
  
  @Column(name = "bargin_enabled")
  private Boolean barinEnabled = false;

  @Column(name = "dynamic_cost")
  private Boolean dynamicCost = true;
  
  @Column(name = "call_cost")
  private Integer callCost;

  @Column(name = "call_cost_mode")
  private String callCostMode;
  
  @Column(name = "saveIvrToFranchiseListing")
  private Boolean saveIvrToFranchiseListing;

  @Column(name = "ivr_recording_path", length = 512)
  private String ivr_recording_path;

  @Column(name = "thankyou_recording_path", length = 512)
  private String thankyou_recording_path;
  
  @Column(name = "thankyou_recording_length_seconds")
  private Integer thankyou_recording_length_seconds;
  
  @Column(name = "redirect_trunk", length = 512)
  private String redirectTrunk;

  @Column(name = "redirect_protocol", length = 512)
  private String redirectProtocol;
  
  @JdbcTypeCode(SqlTypes.LONGVARCHAR)
  @Column(name = "ivr_dtmf_rules_json", columnDefinition = "text")
  private String ivr_dtmf_rules_json;
}
