///*
// * Auto-formatted: src/main/java/com/mylinehub/voicebridge/config/ConfigProperties.java
// */
//package com.mylinehub.voicebridge.config;
//
//import lombok.Data;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;
//
//import com.mylinehub.voicebridge.ai.impl.ExternalBotWsClientImpl;
//
//import jakarta.annotation.PostConstruct;
//
//@Data
//@Component
//@ConfigurationProperties(prefix = "")
//public class ConfigProperties {
//
//  private static final Logger log = LoggerFactory.getLogger(ConfigProperties.class);
//
//  // --- Server convenience / SSL mirrors (Spring reads server.ssl.* directly) ---
//  private Integer server_port;
//  private Integer server_http_port;
//  private Boolean server_ssl_enabled;
//  private String  server_ssl_key_store;
//  private String  server_ssl_key_store_password;
//  private String  server_ssl_key_store_type;
//  private String  server_ssl_key_alias;
//  private String  server_ssl_key_password;
//
//  // --- Asterisk ARI ---
//  private String ari_ws_url;
//  private String ari_rest_baseUrl;
//  private String ari_username;
//  private String ari_password;
//  private String ari_appName;
//
//  // --- RTP / Audio ---
//  private String  rtp_codec;
//  private int  rtp_payload_pt;
//  private String  rtp_bind_ip;
//  private Integer rtp_clock_rate;
//  private Integer rtp_frame_ms;
//  private Integer rtp_bind_port;
//  private String  rtp_external_host;
//  
//  // --- AI Models / OpenAI ---
//  private String ai_realtime_ws_url;
//  private String ai_model_realtime;
//  private String ai_model_tts;
//  // have both fields available; they bind independently
//  private String ai_openai_apiKey;   // from ai.openai.apiKey
//  private String ai_realtime_session_url;  // HTTPS endpoint to create ephemeral Realtime sessions
//  private String ai_model_transcribe;
//  private Integer ai_concurrency_maxCallsPerInstance;
//  private Integer ai_pcm_sampleRateHz;  // AI PCM stream sample-rate (Hz), e.g. 16000
//  private String ai_voice;
//  private String ai_voice_options;
//  private String ai_voice_output;
//  private String ai_voice_input;
//  private Double ai_temperature;
//  
//  /** Simple PCM amplitude threshold for barge-in speech detection (0–32767). */
//  private Integer barge_in_energy_threshold;
//  
//  // --- MyLineHub (Org / Lookup / Auth) ---
//  private String mylinehub_login_url;
//  private String mylinehub_login_username;
//  private String mylinehub_login_password;
//  private String mylinehub_org_lookup_url;
//  private String mylinehub_base_url;
//  private String mylinehub_crm_cdr_url;
//  
//  //External Bot
//  private String bot_mode; // openai | external
//  private String bot_external_ws_url;
//  private boolean bot_auth_required;
//  private String bot_auth_ws_type;
//  private String bot_external_basic_user;
//  private String bot_external_basic_pass;
//  private String bot_external_token;
//  private String bot_external_mode;
//  private String bot_external_lang;
//
//  //--- Google Gemini Live ---
//  private String  ai_google_live_ws_url;
//  private String  ai_google_live_apiKey;
//  private String  ai_google_live_model;
//  private Double  ai_google_live_temperature;
//  private String  ai_google_primary_language;
// 
//  // --- RAG / Vector Search ---
//  private String  rag_vector_store_url;
//  private Integer rag_topK;
//  private Integer rag_maxContextTokens;
//
//  // --- Recording ---
//  private Boolean recording_enabled;
//  private String  recording_mode;
//  private String  recording_s3_bucket;
//  private String  recording_s3_endpoint;
//
//  // --- Queue / Backpressure ---
//  private Integer queue_maxMs;
//  private Integer queue_pauseMs;
//  private Integer queue_watermarkHighPercent;
//  private Integer queue_watermarkLowPercent;
//
//  // --- Resilience / Retry ---
//  private Integer resilience_retry_maxAttempts;
//  private Integer resilience_circuitbreaker_failureThreshold;
//  private Integer ws_reconnect_initialDelayMs;
//  private Integer ws_reconnect_maxDelayMs;
//
//  // --- Observability / Metrics ---
//  private String metrics_namespace;
//
//  // --- Performance Targets ---
//  private Integer performance_mouthToEarMsTarget;
//  private Integer performance_jitterMsP95;
//
//  // --- Executors ---
//  private Integer executor_media_poolSize;
//  private Integer executor_io_poolSize;
//  
//  @PostConstruct
//  public void dumpRtpProps() {
//    log.info("RTP CONFIG RESOLVED => codec={}, clockRate={}, pt={}, frameMs={}, bindIp={}, bindPort={}",
//        rtp_codec, rtp_clock_rate, rtp_payload_pt, rtp_frame_ms, rtp_bind_ip, rtp_bind_port);
//  }
//}
