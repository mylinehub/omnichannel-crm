package com.mylinehub.voicebridge.billing;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Per-call billing / analytics info for reporting.
 *
 * NOTE:
 * - This is call-level metadata only (no actual charging logic here).
 * - Actual billing / credit decisions are handled by external systems.
 */
@Getter
@Setter
@ToString
public class CallBillingInfo {

  /** Organization (tenant) for this call. */
  private String organization;

  /** ARI channel / call id. */
  private String channelId;

  /** When the call entered Stasis / started. */
  private Instant startTime;

  /** When Stasis ended / call terminated. */
  private Instant endTime;

  /** Total call duration in seconds. */
  private long durationSeconds;

  // ---- RAG / text / token-ish metrics --------------------------------------

  /** Total words spoken by the caller (from STT, same as CallSession.callerWordCount). */
  private long totalCallerWords;

  /** How many times we triggered a RAG fetch during this call. */
  private long totalRagQueries;

  /** Total characters of RAG context text we injected back into Realtime. */
  private long totalRagContextCharacters;

  /** Total characters of text we sent to Realtime (system instructions + RAG context, etc.). */
  private long totalAiCharactersSent;

  /** Total characters of text we received from Realtime (JSON events, etc., rough). */
  private long totalAiCharactersReceived;

  /**
   * Approximate total tokens for this call.
   * For now this can be derived from characters (e.g., chars/4) if you want,
   * or filled from a future usage API.
   */
  private long totalApproxTokens;

  // ---- Recording metadata ---------------------------------------------------

  /** File name used for recording (e.g. org_channel_ts.wav). */
  private String recordingFileName;

  /** Full path on local filesystem where recording is stored (if recording is enabled). */
  private String recordingPath;
}
