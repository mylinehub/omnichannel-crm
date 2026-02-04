package com.mylinehub.voicebridge.service;

import com.mylinehub.voicebridge.models.CallHistoryRecord;
import com.mylinehub.voicebridge.repository.CallHistoryRecordRepository;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CallHistoryBufferService {

  private static final Logger log = LoggerFactory.getLogger(CallHistoryBufferService.class);

  // =========================
  // DEEP LOGS TOGGLE (TOP)
  // =========================
  private static final boolean DEEP_LOGS = false;
  
  // If you prefer env var style, you can run JVM with: -DCALL_HIST_DEEP_LOGS=true

  // =========================
  // Flush policy
  // =========================
  private static final int FLUSH_BATCH_SIZE = 200;
  private static final Duration MAX_BUFFER_AGE = Duration.ofMinutes(20);

  private final CallHistoryRecordRepository repo;

  // In-memory buffer
  private final ConcurrentLinkedQueue<CallHistoryRecord> queue = new ConcurrentLinkedQueue<>();

  /**
   * Track "oldest enqueue time" for age-based flush.
   * NOTE: Using a single timestamp is an approximation once we drain partially.
   */
  private final AtomicLong oldestEnqueueEpochMs = new AtomicLong(0L);

  /**
   * Optional: track last successful flush time (for debugging/observability).
   */
  private final AtomicReference<Instant> lastFlushAt = new AtomicReference<>(null);

  public CallHistoryBufferService(CallHistoryRecordRepository repo) {
    this.repo = repo;
  }

  public void enqueue(CallHistoryRecord r) {
    if (r == null) {
      if (DEEP_LOGS) log.debug("CALL-HIST enqueue_skip reason=null_record");
      return;
    }

    // Set createdAt if missing
    if (r.getCreatedAt() == null) {
      r.setCreatedAt(Instant.now());
      if (DEEP_LOGS) log.debug("CALL-HIST enqueue_setCreatedAt createdAt={}", r.getCreatedAt());
    }

    queue.add(r);

    // If this was the first element (or we were "idle"), set oldest enqueue time.
    // compareAndSet keeps the original oldest timestamp while buffer grows.
    oldestEnqueueEpochMs.compareAndSet(0L, System.currentTimeMillis());

    int size = queue.size();
    if (DEEP_LOGS) {
      log.debug("CALL-HIST enqueue_ok size={} oldestMs={}", size, oldestEnqueueEpochMs.get());
    }

    if (size >= FLUSH_BATCH_SIZE) {
      if (DEEP_LOGS) log.debug("CALL-HIST enqueue_trigger size_threshold size={}", size);
      // quick trigger; real work happens in flushNow()
      flushIfNeeded("size_threshold");
    }
  }

  /**
   * Runs every 60s. Flush if:
   * - queue size >= 200
   * - OR oldest record has been waiting >= 20 minutes
   */
  @Scheduled(fixedDelay = 60_000L)
  public void scheduledFlush() {
    if (DEEP_LOGS) {
      log.debug("CALL-HIST scheduledFlush tick size={} oldestMs={} lastFlushAt={}",
          queue.size(), oldestEnqueueEpochMs.get(), lastFlushAt.get());
    }
    flushIfNeeded("scheduled");
  }

  public void flushIfNeeded(String trigger) {
    try {
      int size = queue.size();
      if (size <= 0) {
        oldestEnqueueEpochMs.set(0L);
        if (DEEP_LOGS) log.debug("CALL-HIST flushIfNeeded_skip trigger={} reason=empty", trigger);
        return;
      }

      long oldestMs = oldestEnqueueEpochMs.get();
      boolean ageExceeded = false;
      long ageMs = -1L;

      if (oldestMs > 0L) {
        ageMs = System.currentTimeMillis() - oldestMs;
        ageExceeded = ageMs >= MAX_BUFFER_AGE.toMillis();
      }

      if (DEEP_LOGS) {
        log.debug("CALL-HIST flushIfNeeded_check trigger={} size={} oldestMs={} ageMs={} ageExceeded={} batchSize={} maxAgeMs={}",
            trigger, size, oldestMs, ageMs, ageExceeded, FLUSH_BATCH_SIZE, MAX_BUFFER_AGE.toMillis());
      }

      if (size < FLUSH_BATCH_SIZE && !ageExceeded) {
        if (DEEP_LOGS) log.debug("CALL-HIST flushIfNeeded_skip trigger={} reason=policy_not_met size={} ageExceeded={}",
            trigger, size, ageExceeded);
        return;
      }

      // flush now
      flushNow(trigger, size, ageExceeded);

    } catch (Exception e) {
      log.error("CALL-HIST flushIfNeeded_error trigger={} msg={}", trigger, e.getMessage(), e);
    }
  }

  @Transactional
  public void flushNow(String trigger, int sizeAtDecision, boolean ageExceeded) {
    List<CallHistoryRecord> batch = new ArrayList<>(Math.min(sizeAtDecision, FLUSH_BATCH_SIZE));

    if (DEEP_LOGS) {
      log.debug("CALL-HIST flushNow_start trigger={} sizeAtDecision={} currentSize={} ageExceeded={} oldestMs={}",
          trigger, sizeAtDecision, queue.size(), ageExceeded, oldestEnqueueEpochMs.get());
    }

    // Drain up to FLUSH_BATCH_SIZE
    for (int i = 0; i < FLUSH_BATCH_SIZE; i++) {
      CallHistoryRecord r = queue.poll();
      if (r == null) break;
      batch.add(r);
    }

    if (batch.isEmpty()) {
      // queue was drained by another thread between decision and now
      oldestEnqueueEpochMs.set(0L);
      if (DEEP_LOGS) log.debug("CALL-HIST flushNow_noop trigger={} reason=batch_empty", trigger);
      return;
    }

    try {
      if (DEEP_LOGS) log.debug("CALL-HIST flushNow_db_saveAll trigger={} batchSize={} remainingBeforeSave={}",
          trigger, batch.size(), queue.size());

      repo.saveAll(batch);

      lastFlushAt.set(Instant.now());

      // Reset oldest time if queue emptied; else keep as-is (approx).
      if (queue.isEmpty()) {
        oldestEnqueueEpochMs.set(0L);
        if (DEEP_LOGS) log.debug("CALL-HIST flushNow_postSave queueEmpty=true oldestReset=0");
      } else {
        // Approx: We don't know the true oldest of remaining items without per-item enqueue timestamps.
        // Keeping current oldest is fine: it only makes age-based flush more eager, never less safe.
        if (DEEP_LOGS) log.debug("CALL-HIST flushNow_postSave queueEmpty=false oldestMs_kept={}", oldestEnqueueEpochMs.get());
      }

      log.info("CALL-HIST flushed trigger={} saved={} remaining={} ageExceeded={} lastFlushAt={}",
          trigger, batch.size(), queue.size(), ageExceeded, lastFlushAt.get());

    } catch (Exception e) {
      // If DB fails: put records back to queue (best-effort)
      for (CallHistoryRecord r : batch) {
        queue.add(r);
      }
      // ensure oldest is set so age-based flush retries later
      oldestEnqueueEpochMs.compareAndSet(0L, System.currentTimeMillis());

      log.error("CALL-HIST flush_failed trigger={} batch={} msg={}",
          trigger, batch.size(), e.getMessage(), e);

      if (DEEP_LOGS) {
        log.debug("CALL-HIST flush_failed_requeued trigger={} requeued={} newSize={} oldestMs={}",
            trigger, batch.size(), queue.size(), oldestEnqueueEpochMs.get());
      }
    }
  }
  
  @PreDestroy
  public void onShutdown() {
    try {
      int size = queue.size();
      log.info("CALL-HIST shutdown_start pending={}", size);

      // Flush everything in multiple batches (not only 200)
      flushAllOnShutdown();

      log.info("CALL-HIST shutdown_done pendingNow={}", queue.size());
    } catch (Exception e) {
      log.error("CALL-HIST shutdown_error msg={}", e.getMessage(), e);
    }
  }

  @Transactional
  public void flushAllOnShutdown() {
    int flushedTotal = 0;

    while (true) {
      int sizeNow = queue.size();
      if (sizeNow <= 0) {
        oldestEnqueueEpochMs.set(0L);
        return;
      }

      // drain up to FLUSH_BATCH_SIZE using your existing flushNow logic
      flushNow("shutdown", sizeNow, true);
      flushedTotal += Math.min(sizeNow, FLUSH_BATCH_SIZE);

      // Safety: if DB keeps failing, avoid infinite loop
      // If flushNow failed, it re-queues, so queue size won't decrease.
      if (queue.size() >= sizeNow) {
        log.error("CALL-HIST shutdown_flush_stuck sizeNow={} sizeAfter={} -> stopping to avoid infinite loop",
            sizeNow, queue.size());
        return;
      }
    }
  }

}
