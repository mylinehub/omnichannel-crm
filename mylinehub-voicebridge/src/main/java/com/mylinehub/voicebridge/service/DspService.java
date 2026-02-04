package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.dsp.WebRtcApmProcessor;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.session.CallSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DspService {
	
	  private static final Logger log = LoggerFactory.getLogger(DspService.class);
	  private static final boolean DEEP_LOGS = false;
	 
	  public void initDspPerCall(CallSession s, StasisAppConfig p, int codecRateHz) {
		  if (s == null || p == null) return;

		  // Your DB field name: dspEnabled (Boolean)
		  boolean enabled = Boolean.TRUE.equals(p.getDspEnabled());

		  // Strict: we only enable DSP when codec clock is 8000 (telephony-safe, no resample).
		  // If codecRateHz isn't 8000, we skip DSP (no assumptions).
		  if (!enabled || codecRateHz != 8000) {
		    s.setApm(null);
		    if (DEEP_LOGS) {
		      log.info("DSP disabled_or_rate_mismatch channel={} enabled={} codecRateHz={}",
		          s.getChannelId(), enabled, codecRateHz);
		    }
		    return;
		  }

		  // Telephony safe config: AEC=ON, NS=ON, AGC=OFF (as decided)
		  s.setApm(new WebRtcApmProcessor(codecRateHz, true, true, false));

		  if (DEEP_LOGS) {
		    log.info("DSP enabled channel={} codecRateHz={} aec={} ns={} agc={}",
		        s.getChannelId(), codecRateHz, true, true, false);
		  }
		}
	  
	  public byte[] maybeDspNearEnd(CallSession s, byte[] pcm16, int codecRateHz) {
		  if (s == null || pcm16 == null || pcm16.length == 0) return pcm16;

		  WebRtcApmProcessor apm = s.getApm();
		  if (apm == null) return pcm16;

		  // Strict guard: APM was created for 8k only
		  if (codecRateHz != apm.rateHz()) return pcm16;

		  // Estimate render delay using queue depth (ms).
		  // This is the best available approximation without adding more plumbing.
		  int delayMs = 0;
		  try {
		    long depth = (s.getOutboundQueue() != null ? s.getOutboundQueue().depthMs() : 0L);
		    delayMs = (int) Math.max(0, Math.min(800, depth));
		  } catch (Exception ignore) {}

		  return apm.processNearEnd(pcm16, delayMs);
		}

	  public void recreateApmOnBarge(CallSession s) {
		  if (s == null) return;

		  WebRtcApmProcessor old = s.getApm();
		  if (old == null) return;

		  int rate = old.rateHz();

		  // Dispose old and create a fresh instance with same config
		  try { old.close(); } catch (Exception ignore) {}

		  s.setApm(new WebRtcApmProcessor(rate, true, true, false));

		  if (DEEP_LOGS) {
		    log.info("DSP apm_recreated channel={} rateHz={}", s.getChannelId(), rate);
		  }
		}

}
