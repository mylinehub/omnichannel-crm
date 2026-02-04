/*
 * Auto-formatted + DEEP LOGS ONLY:
 * src/main/java/com/mylinehub/voicebridge/audio/CodecFactory.java
 *
 * IMPORTANT:
 *  - No behavior changes from your version.
 *  - Only added detailed logs for debugging codec/PT/clock mismatches.
 *  - ASCII-only comments (safe for Windows-1252 / STS).
 */

package com.mylinehub.voicebridge.audio;

import com.mylinehub.voicebridge.audio.codec.OpusCodec;
import com.mylinehub.voicebridge.audio.codec.PcMaCodec;
import com.mylinehub.voicebridge.audio.codec.PcMuCodec;
import com.mylinehub.voicebridge.models.StasisAppConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Factory for constructing codec instances based on configuration.
 *
 * Strict behavior:
 * - If rtp.codec is missing or unknown, we THROW instead of silently falling back.
 * - If opus is selected but PT/clock-rate look like pcmu, we WARN + auto-fix PT.
 *
 * Deep logging goals:
 * - Show exact config inputs (raw + snake_case mirrors).
 * - Show derived values (codecName, clockRate, pt).
 * - Show sanity warnings before returning the codec instance.
 *
 * Logging switches (per-file, non-error only):
 * - RTP_DEEP_LOGS:
 *     Master switch for all INFO/DEBUG/WARN logs in this class.
 *     Set to true temporarily while debugging codec selection issues.
 * - RTP_RTP_DEEP_LOGS:
 *     Reserved for RTP-packet-heavy logs. Declared for consistency across
 *     audio pipeline classes; currently unused in this factory.
 *
 * NOTE:
 * - All ERROR logs MUST remain always-on and MUST NOT be gated by these flags.
 */
@Component
public class CodecFactory {

  private static final Logger log = LoggerFactory.getLogger(CodecFactory.class);


  /**
   * Secondary switch intended for extremely verbose RTP logs.
   * Not used in this class, but kept for cross-file consistency.
   */
  private static final boolean RTP_DEEP_LOGS = false;
  private static final boolean DEEP_LOGS = false;

  public CodecFactory() {
  }

  public AudioCodec create(StasisAppConfig props) {

    // ---- Resolve codec name strictly ----
    String raw = props.getRtp_codec();
    if (DEEP_LOGS) {
      log.debug("codec_factory_raw_rtp_codec raw='{}'", raw);
    }

    if (raw == null || raw.trim().isEmpty()) {
      // ERROR logs are always visible; not gated by RTP_DEEP_LOGS/RTP_RTP_DEEP_LOGS.
      log.error("codec_factory_missing_codec rtp_codec is empty or null");
      throw new IllegalStateException(
          "rtp_codec/rtp.codec is NOT set. Refusing to default silently. " +
          "Set rtp.codec=pcmu|pcma|opus in properties."
      );
    }

    String codecName = raw.toLowerCase(Locale.ROOT).trim();

    Integer clockRateObj = props.getRtp_clock_rate();
    int clockRate = (clockRateObj != null && clockRateObj > 0)
        ? clockRateObj
        : -1;

    int pt = props.getRtp_payload_pt();

    if (RTP_DEEP_LOGS) {
      log.info(
          "codec_factory_input codec='{}', clockRateObj={}, resolvedClockRate={}, payloadPt={}, frameMs={}, bindIp={}, extHost={}",
          codecName,
          clockRateObj,
          clockRate,
          pt,
          props.getRtp_frame_ms(),
          props.getRtp_bind_ip(),
          props.getRtp_external_host()
      );
    }

    if (RTP_DEEP_LOGS) {
      log.debug("codec_factory_inputs_debug codecName='{}' raw='{}' pt={} clockRate={} clockRateObj={}",
          codecName, raw, pt, clockRate, clockRateObj);
    }

    switch (codecName) {

      case "opus": {
        int fs = clockRate > 0 ? clockRate : 48_000;

        if (RTP_DEEP_LOGS) {
          log.debug("codec_factory_opus_pre_sanity fs={} pt={} clockRate={} clockRateObj={}",
              fs, pt, clockRate, clockRateObj);
        }

        // Sanity: opus PT must be dynamic (96-127 typically). If user left PT=0, override.
        if (pt == 0 || pt == 8) {
          int fixedPt = 111; // safe default; will be overwritten if you learn remote PT
          if (RTP_DEEP_LOGS) {
            log.warn(
                "codec_factory_opus_pt_fix Opus selected but rtp.payload.pt={} looks like G.711. Overriding initial PT to {}.",
                pt, fixedPt
            );
          }
          pt = fixedPt; // NOTE: logic unchanged from your version
        }

        if (RTP_DEEP_LOGS) {
          log.info("codec_select opus fs={} pt={} clockRateObj={} resolvedClockRate={}",
              fs, pt, clockRateObj, clockRate);
        }

        OpusCodec c = new OpusCodec(fs);

        if (RTP_DEEP_LOGS) {
          log.debug("codec_factory_return codecClass={} codecName={} sampleRate={} ptHint={}",
              c.getClass().getSimpleName(), c.name(), c.sampleRate(), pt);
        }

        return c;
      }

      case "pcma": {
        int fs = clockRate > 0 ? clockRate : 8_000;

        if (RTP_DEEP_LOGS) {
          log.debug("codec_factory_pcma_pre_sanity fs={} pt={} clockRate={} clockRateObj={}",
              fs, pt, clockRate, clockRateObj);
        }

        // Sanity: pcma PT should be 8
        if (pt != 8 && pt != 0) {
          if (RTP_DEEP_LOGS) {
            log.warn("codec_factory_pcma_pt_unusual PCMA selected but rtp.payload.pt={} is unusual (expected 8).", pt);
          }
        }

        if (RTP_DEEP_LOGS) {
          log.info("codec_select pcma fs={} pt={} clockRateObj={} resolvedClockRate={}",
              fs, pt, clockRateObj, clockRate);
        }

        PcMaCodec c = new PcMaCodec(fs);

        if (RTP_DEEP_LOGS) {
          log.debug("codec_factory_return codecClass={} codecName={} sampleRate={} ptHint={}",
              c.getClass().getSimpleName(), c.name(), c.sampleRate(), pt);
        }

        return c;
      }

      case "pcmu": {
        int fs = clockRate > 0 ? clockRate : 8_000;

        if (RTP_DEEP_LOGS) {
          log.debug("codec_factory_pcmu_pre_sanity fs={} pt={} clockRate={} clockRateObj={}",
              fs, pt, clockRate, clockRateObj);
        }

        // Sanity: pcmu PT should be 0
        if (pt != 0) {
          if (RTP_DEEP_LOGS) {
            log.warn("codec_factory_pcmu_pt_unusual PCMU selected but rtp.payload.pt={} is unusual (expected 0).", pt);
          }
        }

        if (RTP_DEEP_LOGS) {
          log.info("codec_select pcmu fs={} pt={} clockRateObj={} resolvedClockRate={}",
              fs, pt, clockRateObj, clockRate);
        }

        PcMuCodec c = new PcMuCodec();

        if (RTP_DEEP_LOGS) {
          log.debug("codec_factory_return codecClass={} codecName={} sampleRate={} ptHint={}",
              c.getClass().getSimpleName(), c.name(), c.sampleRate(), pt);
        }

        return c;
      }

      default:
        // ERROR always visible
        log.error("codec_factory_unknown_codec raw='{}' normalized='{}'", raw, codecName);
        throw new IllegalStateException(
            "Unknown rtp.codec='" + raw + "'. Valid: pcmu|pcma|opus. " +
            "Refusing to default silently."
        );
    }
  }
}
