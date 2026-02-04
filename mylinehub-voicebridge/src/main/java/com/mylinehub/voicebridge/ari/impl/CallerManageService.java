package com.mylinehub.voicebridge.ari.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class CallerManageService {

  private static final Logger log = LoggerFactory.getLogger(CallerManageService.class);

  // ============================================================
  // FLAGS
  // ============================================================
  private static final boolean DEEP_LOGS = true;

  // Keep this small; this is on call setup path
  private static final int ARI_VAR_TIMEOUT_MS = 600;

  private final WebClient web = WebClient.builder().build();
  private final ObjectMapper mapper = new ObjectMapper();

  // ============================================================
  // Public API
  // ============================================================

  public static final class ResolutionResult {
	  public final String originalCaller;
	  public final String resolvedCaller;
	  public final String source;   // ARI | UUI | FALLBACK
	  public final String language; // extracted from UUI after pipe

	  public ResolutionResult(String originalCaller, String resolvedCaller, String source, String language) {
	    this.originalCaller = originalCaller;
	    this.resolvedCaller = resolvedCaller;
	    this.source = source;
	    this.language = language;
	  }
	}


  /**
   * Resolution order (CURRENT USE-CASE, SIMPLE):
   * 1) ARI caller (if real phone)
   * 2) UUI (X_UUI) hex -> ASCII -> PHONE|LANG
   * 3) fallback
   */
  public ResolutionResult resolveCaller(
      StasisAppConfig cfg,
      String channelId,
      String ariCaller
  ) {

    String original = ariCaller;
    String caller = normalizeBlankToNull(ariCaller);

    if (DEEP_LOGS) {
      log.warn("CALLER enter channel={} ariCaller='{}'",
          nvl(channelId), maskPhoneish(ariCaller));
    }

    // 1) ARI caller
    if (looksLikeRealPhone(caller)) {
      String finalCaller = normalizeToE164(caller);
      if (DEEP_LOGS) {
        log.warn("CALLER resolved via ARI channel={} final='{}'",
            nvl(channelId), maskPhoneish(finalCaller));
      }
      return new ResolutionResult(original, finalCaller, "ARI", "");
    }

    // 2) UUI: X_UUI hex -> ASCII -> PHONE|LANG
    String uui = ariGetVarBlocking(cfg, channelId, "X_UUI");
    UuiParsed parsed = decodeUuiHexToPhoneAndLang(uui);

    String phoneDecoded = parsed.phoneRaw;
    String langDecoded  = parsed.language;

    String ani = normalizeToE164(phoneDecoded);

    if (DEEP_LOGS) {
      log.warn("CALLER check UUI channel={} X_UUI(raw)='{}' ascii='{}' phone='{}' lang='{}' normalized='{}'",
          nvl(channelId),
          maskPhoneish(uui),
          maskPhoneish(parsed.ascii),
          maskPhoneish(phoneDecoded),
          nvl(langDecoded),
          maskPhoneish(ani));
    }

    if (looksLikeRealPhone(ani)) {
      if (DEEP_LOGS) {
        log.warn("CALLER resolved via UUI channel={} final='{}' lang='{}'",
            nvl(channelId), maskPhoneish(ani), nvl(langDecoded));
      }
      return new ResolutionResult(original, ani, "UUI", nvl(langDecoded));
    }

    // 3) fallback
    String fb = "unknown";
    if (DEEP_LOGS) {
      log.warn("CALLER fallback channel={} final='{}'",
          nvl(channelId), maskPhoneish(fb));
    }
    return new ResolutionResult(original, fb, "FALLBACK", "");
  }


  // ============================================================
  // ARI VAR FETCH (LOUD LOGS)
  // ============================================================

  private String ariGetVarBlocking(StasisAppConfig cfg, String channelId, String varName) {
    if (cfg == null) return "";
    if (isBlank(cfg.getAri_rest_baseUrl()) || isBlank(cfg.getAri_username())) return "";
    if (isBlank(channelId) || isBlank(varName)) return "";

    String url = "";
    try {
      url = cfg.getAri_rest_baseUrl()
          + "/channels/" + channelId
          + "/variable?variable=" + URLEncoder.encode(varName, StandardCharsets.UTF_8);

      if (DEEP_LOGS) {
        log.warn("ARI-VAR GET begin channel={} var={} url='{}'", nvl(channelId), varName, url);
      }

      String json = web.get()
          .uri(url)
          .header(HttpHeaders.AUTHORIZATION, basic(cfg.getAri_username(), cfg.getAri_password()))
          .retrieve()
          .bodyToMono(String.class)
          .timeout(Duration.ofMillis(ARI_VAR_TIMEOUT_MS))
          .onErrorResume(e -> {
            if (DEEP_LOGS) {
              log.warn("ARI-VAR GET error channel={} var={} msg={}", nvl(channelId), varName, e.getMessage());
            }
            return Mono.empty();
          })
          .block();

      if (isBlank(json)) {
        if (DEEP_LOGS) {
          log.warn("ARI-VAR GET empty channel={} var={}", nvl(channelId), varName);
        }
        return "";
      }

      JsonNode node = mapper.readTree(json);
      String val = node.path("value").asText("");

      if (DEEP_LOGS) {
        log.warn("ARI-VAR GET ok channel={} var={} value='{}'", nvl(channelId), varName, maskPhoneish(val));
      }
      return val;

    } catch (Exception e) {
      if (DEEP_LOGS) {
        log.warn("ARI-VAR GET exception channel={} var={} url='{}' msg={}",
            nvl(channelId), varName, url, e.getMessage());
      }
      return "";
    }
  }

  private String basic(String u, String pw) {
    String raw = nvl(u) + ":" + nvl(pw);
    return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  // ============================================================
  // UUI HEX -> DIGITS
  // ============================================================

  // ============================================================
  // HEURISTICS + NORMALIZATION
  // ============================================================

  private static boolean looksLikeRealPhone(String num) {
    if (isBlank(num)) return false;
    String s = num.startsWith("+") ? num.substring(1) : num;
    for (char c : s.toCharArray()) {
      if (c < '0' || c > '9') return false;
    }
    return s.length() >= 8;
  }

  private static boolean looksLikeExtension(String num) {
    if (isBlank(num)) return true;
    String s = num.startsWith("+") ? num.substring(1) : num;
    for (char c : s.toCharArray()) {
      if (c < '0' || c > '9') return true;
    }
    return s.length() <= 7;
  }

  private static String normalizeToE164(String digits) {
    if (isBlank(digits)) return "";

    String s = digits.trim();

    if (s.startsWith("+")) return s;
    if (looksLikeExtension(s)) return s;

    for (char c : s.toCharArray()) {
      if (c < '0' || c > '9') return s;
    }

    // If it looks like an international/national number, prefix +
    if (s.length() >= 8 && s.length() <= 15 && !s.startsWith("0")) {
      return "+" + s;
    }
    return s;
  }

  private static String maskPhoneish(String s) {
    if (s == null) return "";
    StringBuilder out = new StringBuilder(s.length());
    for (char c : s.toCharArray()) {
      out.append((c >= '0' && c <= '9') ? '*' : c);
    }
    return out.toString();
  }

  private static String normalizeBlankToNull(String s) {
    return isBlank(s) ? null : s.trim();
  }

  private static String nvl(String s) { return s == null ? "" : s; }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
  
  private static UuiParsed decodeUuiHexToPhoneAndLang(String uui) {
	  if (isBlank(uui)) return new UuiParsed("", "", "");

	  String hex = uui.split(";", 2)[0].trim();
	  if (hex.length() < 2 || (hex.length() % 2) != 0) return new UuiParsed("", "", "");

	  StringBuilder ascii = new StringBuilder();
	  for (int i = 0; i < hex.length(); i += 2) {
	    int hi = Character.digit(hex.charAt(i), 16);
	    int lo = Character.digit(hex.charAt(i + 1), 16);
	    if (hi < 0 || lo < 0) return new UuiParsed("", "", "");
	    ascii.append((char) ((hi << 4) | lo));
	  }

	  String decoded = ascii.toString().trim();

	  String phonePart = decoded;
	  String langPart = "";

	  int pipe = decoded.indexOf('|');
	  if (pipe >= 0) {
	    phonePart = decoded.substring(0, pipe).trim();
	    langPart  = decoded.substring(pipe + 1).trim();
	  }

	  StringBuilder phoneOut = new StringBuilder();
	  for (char c : phonePart.toCharArray()) {
	    if ((c >= '0' && c <= '9') || (c == '+' && phoneOut.length() == 0)) phoneOut.append(c);
	  }

	  return new UuiParsed(phoneOut.toString(), langPart, decoded);
	}

  
  private static final class UuiParsed {
	  final String phoneRaw;
	  final String language;
	  final String ascii;

	  UuiParsed(String phoneRaw, String language, String ascii) {
	    this.phoneRaw = phoneRaw;
	    this.language = language;
	    this.ascii = ascii;
	  }
	}

}
