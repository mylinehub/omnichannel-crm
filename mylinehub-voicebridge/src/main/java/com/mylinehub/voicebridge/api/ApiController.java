package com.mylinehub.voicebridge.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple REST controller providing health and liveness checks for the VoiceBridge service.
 *
 * <p>Purpose:
 * <ul>
 *   <li>Exposes minimal HTTP endpoints for operational monitoring.</li>
 *   <li>Used by readiness probes and external health checks.</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 *   <li>INFO-level logs are minimal to avoid noise.</li>
 *   <li>Thread-safe: controller methods are stateless.</li>
 * </ul>
 *
 * @apiNote Extend this controller for other service info endpoints if necessary.
 */
@RestController
public class ApiController {

  /**
   * Basic ping endpoint for health verification.
   *
   * @return "pong" string response if the service is alive.
   */
  @GetMapping("/api/ping")
  public String ping() {
    return "pong";
  }
}
