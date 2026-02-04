/*
 * Auto-formatted + DEEP LOGS: src/main/java/com/mylinehub/voicebridge/queue/PlayoutScheduler.java
 *
 * NEW ARCHITECTURE:
 * - PlayoutScheduler is ONLY a paced sender.
 * - It polls ONLY encoded frames from OutboundQueue.pollEncoded().
 * - It NEVER calls codec.encodePcmToPayload().
 * - It uses payload directly for RTP packetization + send.
 *
 * RECORDING TAP:
 * - If CallSession has RecordingManager, we enable queue record tap and use pcmFrame when present.
 * - If not recording, queue will not retain pcmFrame (memory improvement).
 */
package com.mylinehub.voicebridge.queue;

import com.mylinehub.voicebridge.audio.AudioCodec;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.rtp.RtpPacketizer;
import com.mylinehub.voicebridge.rtp.RtpSymmetricEndpoint;
import com.mylinehub.voicebridge.service.CallTransferService;
import com.mylinehub.voicebridge.session.CallSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;

public class PlayoutScheduler {

  private static final Logger log = LoggerFactory.getLogger(PlayoutScheduler.class);

  private static final boolean DEEP_LOGS = true;
  private static final boolean RTP_DEEP_LOGS = false;

  private final OutboundQueue queue;
  private final RtpPacketizer packetizer;
  private final RtpSymmetricEndpoint endpoint;
  private final AudioCodec codec;

  private final int frameMs;
  private final int samplesPerFrame;

  private Thread worker;
  private volatile boolean running = false;

  // Monotonic pacing clock
  private long nextSendTimeNs = 0;

  // Back-pointer for recording
  private volatile CallSession session;

  // Diagnostics
  private long diagFramesPolled = 0;
  private long diagFramesSent = 0;
  private long diagPollNulls = 0;
  private long diagBytesSentPcm = 0;
  private long diagBytesSentPayload = 0;
  private long diagOverruns = 0;
  private long diagDropMissingPayload = 0;
  private long diagRecordTapWrites = 0;
  private long diagRecordTapMisses = 0;

  // -------------------------------------------------------------------
  // Transfer deps (passed from ARI wiring; PlayoutScheduler is NOT a bean)
  // -------------------------------------------------------------------
  private final CallTransferService transferService;
  private final StasisAppConfig props;

  // Trigger transfer when queue is almost empty
  private static final long TRANSFER_QUEUE_EMPTY_MS = 80;
  private static final long TRANSFER_DELAY_MS = 3000;

  
  public PlayoutScheduler(OutboundQueue queue,
          RtpPacketizer packetizer,
          RtpSymmetricEndpoint endpoint,
          AudioCodec codec,
          int frameMs,
          CallTransferService transferService,
          StasisAppConfig props) {

	this.transferService = transferService;
	this.props = props;
    this.queue = queue;
    this.packetizer = packetizer;
    this.endpoint = endpoint;
    this.codec = codec;

    this.frameMs = Math.max(10, frameMs);
    int rate = codec.sampleRate();
    this.samplesPerFrame = (rate * this.frameMs) / 1000;

    if (DEEP_LOGS) {
      log.info("[PLAYOUT] playout_init rateHz={} frameMs={} samplesPerFrame={} pt={} ssrc={}",
          rate, this.frameMs, this.samplesPerFrame,
          packetizer.payloadType(), packetizer.ssrc());
    }
  }

  public void setSession(CallSession s) {
    this.session = s;

    // Enable record tap in queue only if session recording exists
    boolean recEnabled = (s != null && s.getRecordingManager() != null);
    queue.setRecordTapEnabled(recEnabled);

    if (DEEP_LOGS) {
      log.debug("[PLAYOUT] session_set ssrc={} channelId={} recordTapEnabled={}",
          packetizer.ssrc(), (s != null ? s.getChannelId() : "null"), recEnabled);
    }
  }

  public void start() {
    if (worker != null) {
      log.warn("[PLAYOUT] start_ignored_already_running ssrc={}", packetizer.ssrc());
      return;
    }
    running = true;
    worker = new Thread(this::runLoop, "playout-" + packetizer.ssrc());
    worker.setDaemon(true);
    worker.start();

    if (DEEP_LOGS) {
      log.info("[PLAYOUT] playout_started ssrc={} pt={} frameMs={}",
          packetizer.ssrc(), packetizer.payloadType(), frameMs);
    }
  }

  public void stop() {
    running = false;
    if (worker != null) {
      worker.interrupt();
      if (DEEP_LOGS) {
        log.info("[PLAYOUT] playout_stopped ssrc={} framesPolled={} framesSent={} pollNulls={} pcmBytesSent={} payloadBytesSent={} overruns={} dropMissingPayload={} recordTapWrites={} recordTapMisses={}",
            packetizer.ssrc(),
            diagFramesPolled, diagFramesSent, diagPollNulls,
            diagBytesSentPcm, diagBytesSentPayload,
            diagOverruns, diagDropMissingPayload,
            diagRecordTapWrites, diagRecordTapMisses);
      }
      worker = null;
    }
    nextSendTimeNs = 0;
  }

  private void runLoop() {
    final int codecRate = codec.sampleRate();

    if (RTP_DEEP_LOGS) {
      log.info("[PLAYOUT] runLoop_enter ssrc={} pt={} codecRateHz={} frameMs={} samplesPerFrame={}",
          packetizer.ssrc(), packetizer.payloadType(), codecRate, frameMs, samplesPerFrame);
    }

    while (running && !Thread.currentThread().isInterrupted()) {

    	// ------------------------------------------------------------
    	// OPTION-1 FIX: Don't consume queue until RTP peer is ready.
    	// Prevents first words like "Hi Mohit" getting cut.
    	// ------------------------------------------------------------
    	if (endpoint != null && !endpoint.isPeerReady()) {
    	  // still allow transfer logic to run while waiting
    	  maybeTriggerDeferredTransfer();

    	  if (RTP_DEEP_LOGS && (diagPollNulls % 200) == 0) {
    	    log.debug("[PLAYOUT] wait_peer_ready ssrc={} {} depthMs={}",
    	        packetizer.ssrc(),
    	        endpoint.peerDebug(),
    	        queue.depthMs());
    	  }

    	  LockSupport.parkNanos(5_000_000L); // 5ms
    	  continue;
    	}

      OutboundQueue.EncodedFrame f = queue.pollEncoded();
      if (f == null) {
          diagPollNulls++;

          // If a transfer is pending and queue is drained, trigger it now
          maybeTriggerDeferredTransfer();

          if (RTP_DEEP_LOGS && (diagPollNulls % 500) == 0) {
            log.debug("[PLAYOUT] poll_null ssrc={} pollNulls={} depthMs={} nextSendTimeNs={}",
                packetizer.ssrc(), diagPollNulls, queue.depthMs(), nextSendTimeNs);
          }
          LockSupport.parkNanos(2_000_000L); // 2ms
          continue;
        }

      diagFramesPolled++;

      byte[] pcmFrame = f.getPcmFrame();
      byte[] payload = f.getPayload();

      if (payload == null || payload.length == 0) {
        diagDropMissingPayload++;
        log.warn("[PLAYOUT] drop_missing_payload id={} mark={} ssrc={} pt={} pcmBytes={}",
            f.getId(), f.getMark(), packetizer.ssrc(), packetizer.payloadType(),
            (pcmFrame == null ? 0 : pcmFrame.length));
        continue;
      }

      // Outbound recording tap uses PCM if present
      CallSession s = this.session;
      if (pcmFrame != null && pcmFrame.length > 0 && s != null && s.getRecordingManager() != null) {
        try {
          s.getRecordingManager().writeOutboundAsync(pcmFrame);
          diagRecordTapWrites++;
        } catch (Exception e) {
          log.warn("[REC] outbound_tap_error id={} msg={}", f.getId(), e.getMessage());
        }
      } else {
        if (s != null && s.getRecordingManager() != null) {
          // recording requested but pcmFrame not retained (should not happen if setSession enabled it)
          diagRecordTapMisses++;
          if (RTP_DEEP_LOGS) {
            log.debug("[REC] outbound_tap_missing_pcm id={} mark={}", f.getId(), f.getMark());
          }
        }
      }

      // RTP packetize + send using pre-encoded payload
      byte[] packet = packetizer.packetize(payload, samplesPerFrame);
      endpoint.send(packet);

      if (s != null && s.getTruncateManager() != null) {
        s.getTruncateManager().onFramePlayed(samplesPerFrame);
      }

      diagFramesSent++;
      
      if (RTP_DEEP_LOGS && (diagFramesSent % 50) == 0) { // every 50 frames = ~1s at 20ms
    	  log.debug("[PLAYOUT] truncate_tick channel={} framesSent={} samplesPerFrame={}",
    	      (s != null ? s.getChannelId() : "null"),
    	      diagFramesSent,
    	      samplesPerFrame);
    	}
      
      diagBytesSentPcm += (pcmFrame == null ? 0 : pcmFrame.length);
      diagBytesSentPayload += payload.length;

      if (RTP_DEEP_LOGS && (diagFramesSent % 10) == 0) {
        log.debug("[RTP] send_frame id={} framesSent={} pt={} tsInc={} payloadBytes={} pktBytes={} depthMs={}",
            f.getId(), diagFramesSent, packetizer.payloadType(), samplesPerFrame,
            payload.length, packet.length, queue.depthMs());
      }

      pace();
    }

    if (RTP_DEEP_LOGS) {
      log.info("[PLAYOUT] runLoop_exit ssrc={} framesPolled={} framesSent={} pcmBytesSent={} payloadBytesSent={} overruns={} dropMissingPayload={}",
          packetizer.ssrc(), diagFramesPolled, diagFramesSent,
          diagBytesSentPcm, diagBytesSentPayload, diagOverruns, diagDropMissingPayload);
    }
  }

  private void pace() {
    long now = System.nanoTime();
    if (nextSendTimeNs == 0) {
      nextSendTimeNs = now;
    }
    nextSendTimeNs += frameMs * 1_000_000L;

    long sleepNs = nextSendTimeNs - now;
    if (sleepNs > 0) {
      LockSupport.parkNanos(sleepNs);
    } else {
      diagOverruns++;
      if (RTP_DEEP_LOGS) {
        log.debug("[PACE] overrun ssrc={} lateNs={} overrunsTotal={}",
            packetizer.ssrc(), -sleepNs, diagOverruns);
      }
      if (-sleepNs > 2L * frameMs * 1_000_000L) {
        nextSendTimeNs = System.nanoTime();
        log.warn("[PACE] reset_clock ssrc={} reason=too_late lateNs={} newNextSendTimeNs={}",
            packetizer.ssrc(), -sleepNs, nextSendTimeNs);
      }
    }
  }
  
  private void maybeTriggerDeferredTransfer() {
	  final CallSession s = this.session;
	  if (s == null) return;

	  try {
	    if (s.isStopping() || s.isCompleted()) return;
	  } catch (Exception ignore) {}

	  Boolean requested = s.getAttr("transfer.requested", Boolean.class);
	  if (!Boolean.TRUE.equals(requested)) return;

	  Boolean completed = s.getAttr("transfer.completed", Boolean.class);
	  if (Boolean.TRUE.equals(completed)) return;

	  Long reqAt = s.getAttr("transfer.requestEpochMs", Long.class);
	  long now = System.currentTimeMillis();
	  if (reqAt == null) reqAt = now;

	  // NEW: wait at least 3 sec after transfer requested
	  if (now - reqAt < TRANSFER_DELAY_MS) {
	    return;
	  }

	  long depth = -1;
	  try { depth = queue.depthMs(); } catch (Exception ignore) {}
	  if (depth < 0 || depth > TRANSFER_QUEUE_EMPTY_MS) return;

	  String dnis  = s.getAttr("transfer.dnis", String.class);
	  String phone = s.getAttr("transfer.phone", String.class);

	  if (dnis == null || dnis.isBlank() || phone == null || phone.isBlank()) {
	    log.warn("[TRANSFER] trigger_drop_missing_data channel={} depthMs={} dnis='{}' phone='{}'",
	        s.getChannelId(), depth, dnis, phone);
	    return;
	  }

	  if (transferService == null || props == null) {
	    log.error("[TRANSFER] trigger_missing_deps channel={} transferServiceNull={} propsNull={}",
	        s.getChannelId(), (transferService == null), (props == null));
	    return;
	  }

	  s.putAttr("transfer.completed", Boolean.TRUE);

	  try {
	    log.info("[TRANSFER] trigger_ready channel={} depthMs={} waitedMs={} -> exten={} phone={}",
	        s.getChannelId(), depth, (now - reqAt), dnis, phone);

	    transferService.transferCallerToDialplan(s, props, dnis, phone);

	    log.info("[TRANSFER] trigger_done channel={}", s.getChannelId());

	  } catch (Exception e) {
	    log.error("[TRANSFER] trigger_error channel={} msg={}", s.getChannelId(), e.getMessage(), e);
	    s.putAttr("transfer.completed", Boolean.FALSE);
	  }
	}


}
