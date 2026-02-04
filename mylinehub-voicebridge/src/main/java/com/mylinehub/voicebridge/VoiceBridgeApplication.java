package com.mylinehub.voicebridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/*
 *** SWAGGER ***
HTTPS (8082):
https://localhost:8082/swagger-ui.html
https://localhost:8082/webjars/swagger-ui/index.html
https://localhost:8082/v3/api-docs

 */
/**
 * Application bootstrap for the MyLineHub VoiceBridge service.
 *
 * <p>Purpose:
 * <ul>
 *   <li>Starts the Spring Boot context and initializes all configured beans.</li>
 *   <li>Acts as the single entry point for the service process.</li>
 * </ul>
 *
 * <p>Logging:
 * <ul>
 *   <li>INFO: high-level lifecycle events (startup/shutdown).</li>
 *   <li>DEBUG: detailed component init if enabled via logging properties.</li>
 * </ul>
 *
 * @apiNote No side effects beyond starting the Spring application context.
 * @implNote Keep this class minimal; configuration lives in {@code application.properties}.
 */
@SpringBootApplication
public class VoiceBridgeApplication {

  /**
   * Application entry point.
   *
   * @param args command line arguments (unused)
   */
  public static void main(String[] args) {
    SpringApplication.run(VoiceBridgeApplication.class, args);
  }
}
