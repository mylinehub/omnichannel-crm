/*
 * Auto-formatted: src/main/java/com/mylinehub/voicebridge/session/CallSession.java
 */
package com.mylinehub.voicebridge.session;

import com.mylinehub.voicebridge.ai.BotClient;
import com.mylinehub.voicebridge.ai.TruncateManager;
import com.mylinehub.voicebridge.audio.AudioCodec;
import com.mylinehub.voicebridge.barge.BargeInController;
import com.mylinehub.voicebridge.billing.CallBillingInfo;
import com.mylinehub.voicebridge.dsp.WebRtcApmProcessor;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.queue.OutboundQueue;
import com.mylinehub.voicebridge.queue.PlayoutScheduler;
import com.mylinehub.voicebridge.recording.CallRecordingManager;
import com.mylinehub.voicebridge.rtp.RtpPacketizer;
import com.mylinehub.voicebridge.rtp.RtpSymmetricEndpoint;
import com.mylinehub.voicebridge.service.CallTransferService;

import lombok.Getter;
import lombok.Setter;
import okhttp3.WebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Per-call state holder. Single source of truth for the whole call.
 *
 * RULE:
 *  - No singleton bean may store per-call mutable data.
 *  - Everything belongs here or in attrs.
 */
public class CallSession {

    // ---------------------------------------------------------------------
    // Generic per-call attrs for singleton beans
    // ---------------------------------------------------------------------
    private final ConcurrentHashMap<String, Object> attrs = new ConcurrentHashMap<>();
    public static final String ATTR_CUSTOMER_INFO = "crm.customerInfo";


    public void putAttr(String key, Object value) {
        if (key == null) return;
        if (value == null) attrs.remove(key);
        else attrs.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(String key, Class<T> cls) {
        Object v = attrs.get(key);
        if (v == null || cls == null || !cls.isInstance(v)) return null;
        return (T) v;
    }

    public Map<String, Object> getAttrsView() {
        return java.util.Collections.unmodifiableMap(attrs);
    }

    private final CallTransferService transferService;
    private final StasisAppConfig props;
    
    // ---------------------------------------------------------------------
    // Caller identity (for recordings / CRM)
    // ---------------------------------------------------------------------
    @Getter @Setter
    private String callerNumber;

    // ---------------------------------------------------------------------
    // Core call identity
    // ---------------------------------------------------------------------
    @Getter
    private final String channelId;

    @Getter @Setter
    private String organization;

    // NEW: stasis app name (per call). This makes completion stable even after session maps are removed.
    @Getter @Setter
    private String stasisAppName;

    // ---------------------------------------------------------------------
    // Outbound queue + playout
    // ---------------------------------------------------------------------
    @Getter
    private final OutboundQueue outboundQueue;

    @Getter
    private final PlayoutScheduler playoutScheduler;

    @Getter @Setter
    private BotClient botClient;

    // ---------------------------------------------------------------------
    // AI websocket
    // ---------------------------------------------------------------------
    @Getter @Setter
    private WebSocket aiWebSocket;

    // ---------------------------------------------------------------------
    // Inbound PCM buffer (caller->AI chunking)
    // ---------------------------------------------------------------------
    @Getter @Setter
    private byte[] inboundPcmBuffer;

    @Getter @Setter
    private int inboundPcmBytes;

    // ---------------------------------------------------------------------
    // RAG state
    // ---------------------------------------------------------------------
    @Getter @Setter
    private boolean ragEnabled;

    @Getter @Setter
    private boolean ragTemporarilyDisabled;

    @Getter @Setter
    private int ragErrorCount = 0;

    @Getter @Setter
    private int ragMaxErrors = 3;

    /**
     * FULL USER TRANSCRIPT (ENGLISH) is stored here.
     * Name remains callerTranscript to avoid changing many references.
     *
     * IMPORTANT:
     * - This should ONLY be appended via appendCallerTranscriptEn()
     * - Do not append "cleaned" RAG text here (keep separate buffer below).
     */
    @Getter
    private final StringBuilder callerTranscript = new StringBuilder(4096);

    /**
     * NEW: Separate buffer for RAG trigger window text.
     * This can store cleaned/normalized text without polluting the English transcript.
     */
    @Getter
    private final StringBuilder ragTriggerTranscript = new StringBuilder(4096);

    /**
     * callerWordCount now strictly tracks English transcript words (appendCallerTranscriptEn()).
     * Do NOT use this for RAG threshold counting.
     */
    @Getter @Setter
    private int callerWordCount = 0;

    /**
     * NEW: separate word counter for RAG threshold logic (cleaned transcript words).
     * This prevents double-counting when both transcript storage + RAG logic run.
     */
    @Getter @Setter
    private int ragWordCount = 0;

    /**
     * Next threshold for RAG trigger. Recommended first trigger = 5 (as your comments intended).
     */
    @Getter @Setter
    private int nextRagThreshold = 5;

    @Getter @Setter
    private int ragBlockSize = 10;

    // ---------------------------------------------------------------------
    // Billing / analytics
    // ---------------------------------------------------------------------
    @Getter @Setter
    private CallBillingInfo billingInfo;
    

    // ---------------------------------------------------------------------
    // AI audio stats
    // ---------------------------------------------------------------------
    @Getter @Setter
    private long aiDecodedBytes;

    @Getter @Setter
    private long aiFirstAudioNs;

    @Getter @Setter
    private long aiLastAudioNs;

    // ---------------------------------------------------------------------
    // Debug JSON
    // ---------------------------------------------------------------------
    @Getter @Setter
    private String lastJsonSent;

    @Getter @Setter
    private String lastJsonReceived;

    // ---------------------------------------------------------------------
    // Recording (single stereo WAV per call)
    // ---------------------------------------------------------------------
    @Getter @Setter
    private CallRecordingManager recordingManager;

    // ------------------------------------------------------------------
    // AI realtime session readiness flag
    // We only start sending audio after OpenAI sends "session.created"
    // ------------------------------------------------------------------
    @Getter @Setter
    private volatile boolean aiReady = false;

    @Getter @Setter
    private String realTimeAPISessionId;

    @Getter
    private final StringBuilder ragContextBuffer = new StringBuilder();

    @Getter @Setter
    private int ragContextMaxChars;

    private volatile String finalCompletionJson;

    // NEW: completion metadata (helps your final step + debugging)
    @Getter @Setter
    private String endReason;

    @Getter @Setter
    private long endEpochMs;
    
    @Getter @Setter
    private AudioCodec codec;
    
    
	 // ---------------------------------------------------------------------
	 // ARI media graph IDs (single source of truth)
	 // ---------------------------------------------------------------------
	 @Getter @Setter private String talkBridgeId;        // caller + extMediaOut
	 @Getter @Setter private String tapBridgeId;         // snoop(in) + extMediaIn
	
	 @Getter @Setter private String snoopChannelId;      // snoop on caller
	 @Getter @Setter private String extMediaInChannelId; // Asterisk -> VB (caller-only)
	 @Getter @Setter private String extMediaOutChannelId;// VB -> Asterisk (AI audio)
	
	 // ---------------------------------------------------------------------
	    // RTP per-call objects (single instance only)
	 // ---------------------------------------------------------------------
	 @Getter
	 private final RtpSymmetricEndpoint rtpInEndpoint;   // inbound: Asterisk -> VB (caller-only)

	 @Getter
	 private final RtpSymmetricEndpoint rtpOutEndpoint;  // outbound: VB -> Asterisk (AI audio)

	 @Getter
	 private final RtpPacketizer packetizerOut;
	    
	 // ---------------------------------------------------------------------
	 // Dual RTP ports (single source of truth)
	 // ---------------------------------------------------------------------
	 @Getter @Setter private int rtpInPort;              // VB listens here for caller-only RTP
	 @Getter @Setter private int rtpOutPort;             // VB sends from here to Asterisk extMediaOut
	
	 @Getter @Setter private int rtpInLearnedSSrc; 
	 @Getter @Setter private Integer rtpInLearnedPayloadType;
    
	 @Getter @Setter
	 private transient Object realtimeAiState;

	 
	@Getter @Setter
	private transient WebRtcApmProcessor apm;

	@Getter @Setter
	private transient TruncateManager truncateManager;

	// Barge-in per-call (created in AriBridgeImpl after session creation)
	@Getter @Setter
	private BargeInController bargeController;
	@Getter @Setter
	volatile long lastAiPcmEnqueuedNs;
	
	@Getter @Setter
	private boolean redirectChannel;
	
	@Getter @Setter
	private boolean ivrCall;
	
    // ------------------------------------------------------------------
    // Completion markers (exactly-once)
    // ------------------------------------------------------------------
    private final AtomicBoolean completed = new AtomicBoolean(false);
	 // ------------------------------------------------------------------
	 // Call shutdown marker (used to flush ThreadLocal decoder on RTP thread)
	 // ------------------------------------------------------------------
	 private final AtomicBoolean stopping = new AtomicBoolean(false);
	
	 /** Mark call as stopping (idempotent). */
	 public void markStopping() {
	     stopping.set(true);
	 }
	
	 /** True once call is stopping/ending. */
	 public boolean isStopping() {
	     return stopping.get();
	 }

 
    public boolean markCompletedOnce() {
        return completed.compareAndSet(false, true);
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public void setFinalCompletionJson(String json) {
        this.finalCompletionJson = json;
    }

    public String getFinalCompletionJson() {
        return finalCompletionJson;
    }

    // ------------------------------------------------------------------
    // Transcript helpers
    // ------------------------------------------------------------------

    /** Append English transcript text safely (delta or full chunk). */
    public void appendCallerTranscriptEn(String text) {
        if (text == null) return;
        String t = text.trim();
        if (t.isEmpty()) return;

        // Prevent accidental giant memory usage in long calls

        if (callerTranscript.length() > 0) {
            callerTranscript.append(' ');
        }
        callerTranscript.append(t);

        // Update word count approx (English transcript only)
        callerWordCount += countWords(t);
    }

    /** Get transcript as String (English). */
    public String getCallerTranscriptEnText() {
        return callerTranscript.toString().trim();
    }

    /**
     * Append cleaned/normalized transcript fragment for RAG triggering ONLY.
     * This should be used by your RAG threshold logic (word counting + lastNWords window).
     */
    public void appendRagTriggerText(String cleanedText) {
        if (cleanedText == null) return;
        String t = cleanedText.trim();
        if (t.isEmpty()) return;

        final int MAX_CHARS = 60000;
        if (ragTriggerTranscript.length() >= MAX_CHARS) {
            return;
        }

        if (ragTriggerTranscript.length() > 0) {
            ragTriggerTranscript.append(' ');
        }
        ragTriggerTranscript.append(t);

        // Update RAG word count only (separate from English transcript words)
        ragWordCount += countWords(t);
    }

    public String getRagTriggerTranscriptText() {
        return ragTriggerTranscript.toString().trim();
    }

    /** Safe access to accumulated RAG context text (for completion JSON / debugging). */
    public String getRagContextBufferText() {
        return ragContextBuffer.toString();
    }

    private static int countWords(String s) {
        int n = 0;
        boolean inWord = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean ws = Character.isWhitespace(c);
            if (!ws && !inWord) {
                inWord = true;
                n++;
            } else if (ws) {
                inWord = false;
            }
        }
        return n;
    }


    /**
     * Constructor makes the call "complete" at creation time:
     *  - Creates outbound queue
     *  - Stores packetizer & endpoint (already built by AriBridge)
     *  - Creates playout scheduler using same queue/packetizer/endpoint/codec
     *
     * No later setters required => no duplication/drift possible.
     */
    public CallSession(
            String id,
            long maxQueueMs,
            RtpPacketizer packetizerOut,
            RtpSymmetricEndpoint rtpInEndpoint,
            RtpSymmetricEndpoint rtpOutEndpoint,
            AudioCodec codec,
            int frameMs,
            CallTransferService transferService,
            StasisAppConfig props
            ) {

    	this.transferService = transferService;
    	this.props = props;
        this.channelId = id;

        this.outboundQueue = new OutboundQueue(maxQueueMs, OutboundQueue.OverflowPolicy.DROP_NEW,codec,frameMs);

        this.packetizerOut = packetizerOut;
        this.rtpInEndpoint = rtpInEndpoint;
        this.rtpOutEndpoint = rtpOutEndpoint;

        this.playoutScheduler = new PlayoutScheduler(
        	    this.outboundQueue,
        	    packetizerOut,
        	    rtpOutEndpoint,
        	    codec,
        	    frameMs,
        	    transferService,   // <-- pass from ARI wiring
        	    props              // <-- pass from ARI wiring
        	);
        this.playoutScheduler.setSession(this);

        this.codec = codec;
        this.billingInfo = new CallBillingInfo();
        this.billingInfo.setChannelId(id);

        this.ragContextMaxChars = 4000;
        this.ragEnabled = false;
        this.ragTemporarilyDisabled = false;

        // sensible defaults
        this.endReason = null;
        this.endEpochMs = 0L;
        this.stasisAppName = null;
    }
    
    // ------------------------------------------------------------------
    // IVR-only constructor (NO RTP / NO queue / NO playout / NO codec)
    // ------------------------------------------------------------------
    public CallSession(String id,CallTransferService transferService,StasisAppConfig props) {
    	
      this.transferService = transferService;
      this.props = props;
      this.channelId = id;

      // IVR does not need queue/playout/RTP
      this.outboundQueue = null;
      this.playoutScheduler = null;

      this.packetizerOut = null;
      this.rtpInEndpoint = null;
      this.rtpOutEndpoint = null;

      // No codec required for IVR
      this.codec = null;

      // Billing still required for history/deduct
      this.billingInfo = new CallBillingInfo();
      this.billingInfo.setChannelId(id);

      this.ragContextMaxChars = 4000;
      this.ragEnabled = false;
      this.ragTemporarilyDisabled = false;

      this.endReason = null;
      this.endEpochMs = 0L;
      this.stasisAppName = null;
    }

}
