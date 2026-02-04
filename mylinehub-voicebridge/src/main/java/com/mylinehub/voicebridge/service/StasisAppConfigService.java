/*
 * Auto-formatted:
 *   src/main/java/com/mylinehub/voicebridge/service/StasisAppConfigService.java
 *
 * Purpose:
 *   - Load ALL active Stasis app configs + instructions from DB at startup.
 *   - Cache them in memory for fast lookup.
 *   - Create and manage one AriWsClient per active stasis_app_name.
 *   - Provide APIs to upsert configs/instructions (DB + cache + ARI clients).
 */

package com.mylinehub.voicebridge.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.mylinehub.voicebridge.ari.AriWsClient;
import com.mylinehub.voicebridge.models.StasisAppConfig;
import com.mylinehub.voicebridge.models.StasisAppInstruction;
import com.mylinehub.voicebridge.repository.StasisAppConfigRepository;
import com.mylinehub.voicebridge.repository.StasisAppInstructionRepository;
import com.mylinehub.voicebridge.session.CallSessionManager;

/**
 * Service that:
 *  - Loads ALL active Stasis app configs + instructions from DB at startup.
 *  - Caches them in-memory for fast lookup.
 *  - Manages one AriWsClient per active Stasis app.
 *
 * Primary key: stasis_app_name (same as ARI app name / Stasis app).
 */
@Service
public class StasisAppConfigService {

  private static final Logger log = LoggerFactory.getLogger(StasisAppConfigService.class);

  private final StasisAppConfigRepository configRepo;
  private final StasisAppInstructionRepository instrRepo;
  private final CallSessionManager sessions;
  private final ApplicationContext applicationContext;
  private volatile boolean ariStarted = false;

  /**
   * Immutable snapshot of all active configs, keyed by stasis_app_name.
   */
  private volatile Map<String, StasisAppConfig> configByApp = Collections.emptyMap();

  /**
   * Immutable snapshot of all active instructions, keyed by stasis_app_name.
   */
  private volatile Map<String, StasisAppInstruction> instructionsByApp = Collections.emptyMap();

  /**
   * One ARI WebSocket client per active Stasis app.
   * Key = stasis_app_name (same as StasisAppConfig.stasis_app_name).
   */
  private final Map<String, AriWsClient> ariClientByApp = new ConcurrentHashMap<>();

  public StasisAppConfigService(StasisAppConfigRepository configRepo,
                                StasisAppInstructionRepository instrRepo,
                                CallSessionManager sessions,
                                ApplicationContext applicationContext) {
    this.configRepo = configRepo;
    this.instrRepo = instrRepo;
    this.sessions = sessions;
    this.applicationContext = applicationContext;
  }

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  /**
   * Load all configs and instructions from DB on startup, then build ARI clients.
   *
   * IMPORTANT:
   *   - All AriWsClient instances are created here based on active configs.
   *   - One WebSocket per stasis_app_name.
   */
  @PostConstruct
  public void loadAllFromDatabase() {
    log.info("STASIS-CONFIG startup_load_enter");

    loadConfigs();       // populates configByApp
    loadInstructions();  // populates instructionsByApp

    log.info("STASIS-CONFIG startup_load_done configs={} instructions={}",
        configByApp.size(), instructionsByApp.size());
  }
  
  @EventListener(ApplicationReadyEvent.class)
  public void startAriAfterReady() {
    if (ariStarted) {
      log.info("STASIS-CONFIG app_ready_skip alreadyStarted=true");
      return;
    }
    ariStarted = true;
    rebuildAriClients(configByApp);
  }


  private void loadConfigs() {
    List<StasisAppConfig> list = configRepo.findByActiveTrue();
    Map<String, StasisAppConfig> map = list.stream()
        .collect(Collectors.toMap(
            StasisAppConfig::getStasis_app_name,
            c -> c,
            (a, b) -> b)); // if duplicate keys, last wins

    this.configByApp = Collections.unmodifiableMap(map);

    log.info("STASIS-CONFIG loadConfigs_ok activeCount={}", map.size());
    if (!map.isEmpty()) {
      log.debug("STASIS-CONFIG apps={}", map.keySet());
    }
  }

  private void loadInstructions() {
    List<StasisAppInstruction> list = instrRepo.findByActiveTrue();
    Map<String, StasisAppInstruction> map = list.stream()
        .collect(Collectors.toMap(
            StasisAppInstruction::getStasis_app_name,
            i -> i,
            (a, b) -> b)); // if duplicate keys, last wins

    this.instructionsByApp = Collections.unmodifiableMap(map);

    log.info("STASIS-CONFIG loadInstructions_ok activeCount={}", map.size());
    if (!map.isEmpty()) {
      log.debug("STASIS-CONFIG instructionApps={}", map.keySet());
    }
  }

  /**
   * Build or refresh ARI WS clients based on the given active config map.
   *
   * - Stop and remove clients for apps that are no longer active.
   * - Create and start clients for newly active apps.
   */
  private void rebuildAriClients(Map<String, StasisAppConfig> newConfigs) {
    synchronized (ariClientByApp) {
      // 1) Stop clients for apps that are no longer active
      ariClientByApp.keySet().removeIf(appName -> {
        StasisAppConfig cfg = newConfigs.get(appName);
        if (cfg == null || !cfg.isActive()) {
          AriWsClient client = ariClientByApp.get(appName);
          if (client != null) {
            log.info("STASIS-CONFIG ari_client_stop app={}", appName);
            try {
              client.shutdown();
            } catch (Exception e) {
              log.warn("STASIS-CONFIG ari_client_stop_error app={} msg={}", appName, e.getMessage(), e);
            }
          }
          return true; // remove from map
        }
        return false;
      });

      // 2) Create clients for NEW active apps
      for (StasisAppConfig cfg : newConfigs.values()) {
        String app = cfg.getStasis_app_name();
        if (!cfg.isActive()) {
          continue;
        }
        if (!ariClientByApp.containsKey(app)) {
          log.info("STASIS-CONFIG ari_client_create app={}", app);
          AriWsClient client = new AriWsClient(sessions,cfg, applicationContext);
          ariClientByApp.put(app, client);
          client.start(); // OPEN WS HERE
        }
      }

      log.info("STASIS-CONFIG ari_clients_ready count={}", ariClientByApp.size());
    }
  }

  @PreDestroy
  public void shutdownAllAriClients() {
    log.info("STASIS-CONFIG shutdown_all_ari_clients_enter");
    synchronized (ariClientByApp) {
      for (Map.Entry<String, AriWsClient> e : ariClientByApp.entrySet()) {
        String app = e.getKey();
        AriWsClient client = e.getValue();
        log.info("STASIS-CONFIG ari_client_shutdown app={}", app);
        try {
          client.shutdown();
        } catch (Exception ex) {
          log.warn("STASIS-CONFIG ari_client_shutdown_error app={} msg={}", app, ex.getMessage(), ex);
        }
      }
      ariClientByApp.clear();
    }
    log.info("STASIS-CONFIG shutdown_all_ari_clients_exit");
  }

  // ---------------------------------------------------------------------------
  // Read APIs for other components
  // ---------------------------------------------------------------------------

  public StasisAppConfig getConfigOrNull(String stasisAppName) {
    if (stasisAppName == null) {
      return null;
    }
    return configByApp.get(stasisAppName);
  }

  public StasisAppInstruction getInstructionOrNull(String stasisAppName) {
    if (stasisAppName == null) {
      return null;
    }
    return instructionsByApp.get(stasisAppName);
  }

  public Map<String, StasisAppConfig> getAllConfigsView() {
    return configByApp;
  }

  public Map<String, StasisAppInstruction> getAllInstructionsView() {
    return instructionsByApp;
  }

  public String getCompletionInstructionOrNull(String stasisAppName) {
	  StasisAppInstruction ins = getInstructionOrNull(stasisAppName);
	  if (ins == null) return null;
	  // field is already inside as you said:
	  String c = ins.getCompletionInstructions();
	  return (c != null && !c.isBlank()) ? c : null;
	}
  
  /**
   * Optional: get the ARI client for a given app (if you ever need it).
   */
  public AriWsClient getAriClientOrNull(String stasisAppName) {
    if (stasisAppName == null) {
      return null;
    }
    return ariClientByApp.get(stasisAppName);
  }

  // ---------------------------------------------------------------------------
  // Write APIs (used by REST controller)
  //   - Update DB
  //   - Update in-memory caches
  //   - Refresh ARI clients if configs changed
  // ---------------------------------------------------------------------------

  public synchronized StasisAppConfig saveConfig(StasisAppConfig cfg) {
    if (cfg == null || cfg.getStasis_app_name() == null) {
      throw new IllegalArgumentException("stasis_app_name is required");
    }

    StasisAppConfig saved = configRepo.save(cfg);

    Map<String, StasisAppConfig> newMap = new HashMap<>(this.configByApp);
    if (saved.isActive()) {
      newMap.put(saved.getStasis_app_name(), saved);
    } else {
      newMap.remove(saved.getStasis_app_name());
    }
    this.configByApp = Collections.unmodifiableMap(newMap);

    if (ariStarted) {
        // Rebuild ARI clients based on updated configs
        rebuildAriClients(this.configByApp);
      }
    
    log.info("STASIS-CONFIG saveConfig_ok stasisAppName={} active={}",
        saved.getStasis_app_name(), saved.isActive());

    return saved;
  }

  public synchronized StasisAppInstruction saveInstruction(StasisAppInstruction instr) {
    if (instr == null || instr.getStasis_app_name() == null) {
      throw new IllegalArgumentException("stasis_app_name is required");
    }

    StasisAppInstruction saved = instrRepo.save(instr);

    Map<String, StasisAppInstruction> newMap = new HashMap<>(this.instructionsByApp);
    if (saved.isActive()) {
      newMap.put(saved.getStasis_app_name(), saved);
    } else {
      newMap.remove(saved.getStasis_app_name());
    }
    this.instructionsByApp = Collections.unmodifiableMap(newMap);

    log.info("STASIS-CONFIG saveInstruction_ok stasisAppName={} active={}",
        saved.getStasis_app_name(), saved.isActive());

    return saved;
  }
  
  public boolean isFetchCustomerInfoEnabled(String stasisAppName) {
	  if (stasisAppName == null || stasisAppName.isBlank()) return false;
	  StasisAppInstruction ins = getInstructionOrNull(stasisAppName);
	  return ins != null && ins.isActive() && ins.isFetchCustomerInfo();
	}

}
