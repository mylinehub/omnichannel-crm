/*
 * Auto-formatted:
 *   src/main/java/com/mylinehub/voicebridge/api/StasisAppAdminController.java
 */
package com.mylinehub.voicebridge.api;

import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.models.StasisAppInstruction;
import com.mylinehub.voicebridge.service.StasisAppConfigService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin API for managing per-Stasis-app configuration and instructions.
 *
 * NOTE:
 *  - Protect this behind auth / VPN / gateway in production.
 *  - Simple token check added for safety.
 */
@RestController
@RequestMapping("/api/stasis-app")
public class StasisAppAdminController {

  private static final Logger log = LoggerFactory.getLogger(StasisAppAdminController.class);

  /** TEMP admin token (move to config/env later) */
  private static final String ADMIN_TOKEN = "mylinehub10101001";

  private final StasisAppConfigService stasisService;

  public StasisAppAdminController(StasisAppConfigService stasisService) {
    this.stasisService = stasisService;
  }

  // -------------------------------------------------------------------------
  // Upsert config
  // -------------------------------------------------------------------------
  @PostMapping("/config")
  public ResponseEntity<?> upsertConfig(
      @RequestParam("token") String token,
      @RequestBody StasisAppConfig cfg) {

    if (!ADMIN_TOKEN.equals(token)) {
      log.warn("STASIS-API unauthorized_token token={}", token);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Invalid admin token");
    }

    StasisAppConfig saved = stasisService.saveConfig(cfg);
    log.info("STASIS-API upsertConfig_ok stasisAppName={}",
        saved.getStasis_app_name());
    return ResponseEntity.ok(saved);
  }

  // -------------------------------------------------------------------------
  // Upsert instruction
  // -------------------------------------------------------------------------
  @PostMapping("/instruction")
  public ResponseEntity<?> upsertInstruction(
      @RequestParam("token") String token,
      @RequestBody StasisAppInstruction instr) {

    if (!ADMIN_TOKEN.equals(token)) {
      log.warn("STASIS-API unauthorized_token token={}", token);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("Invalid admin token");
    }

    StasisAppInstruction saved = stasisService.saveInstruction(instr);
    log.info("STASIS-API upsertInstruction_ok stasisAppName={}",
        saved.getStasis_app_name());
    return ResponseEntity.ok(saved);
  }
}
