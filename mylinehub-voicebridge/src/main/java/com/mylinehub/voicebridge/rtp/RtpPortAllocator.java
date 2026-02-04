package com.mylinehub.voicebridge.rtp;

import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple per-instance RTP port allocator.
 */
@Component
public class RtpPortAllocator {

    private static final Logger log = LoggerFactory.getLogger(RtpPortAllocator.class);

    private static final boolean DEEP_LOGS = false;
    private static final boolean RTP_DEEP_LOGS = false;

    /** Next port to allocate (even-only), atomically updated. */
    private final AtomicInteger nextPort = new AtomicInteger(0);

    /** Base port (corrected to even if needed). Example: 40000. */
    private int basePort;

    /**
     * Max port (inclusive) for this allocator's range.
     * Example with basePort=40000 => maxPortInclusive=45999.
     */
    private int maxPortInclusive;

    // Deep log counters only (no behavior impact)
    private static final int DEBUG_EVERY_N = 100;
    private volatile long allocationCount = 0;

    public RtpPortAllocator() {
    }

    @PostConstruct
    public void init() {
        Integer cfg = 41000;
        int base = (cfg != null ? cfg : 41000);

        // Ensure even base (RTP ports should be even).
        if ((base & 1) == 1) {
            int before = base;
            base = base + 1;
            if (DEEP_LOGS) {
                log.warn("rtp_port_allocator_base_odd corrected {} => {}", before, base);
            }
        }

        this.basePort = base;
        this.maxPortInclusive = basePort + 500;

        nextPort.set(basePort);

        if (DEEP_LOGS) {
            log.info("rtp_port_allocator_initialized basePort={} maxPortInclusive={} fromConfig={}",
                    basePort, maxPortInclusive, (cfg != null));
        }
    }

    /**
     * Allocate the next RTP port for a call (even only), wrapping within [basePort, maxPortInclusive].
     *
     * @return allocated port
     */
    public int allocatePort() {
        // getAndUpdate returns the current value and then applies the update function.
        int port = nextPort.getAndUpdate(current -> {
            int next = current + 2;
            // If we exceeded our configured range, wrap back to basePort.
            if (next > maxPortInclusive) {
                if (DEEP_LOGS) {
                    log.warn("rtp_port_allocator_wrap basePort={} maxPortInclusive={} lastAllocated={}",
                            basePort, maxPortInclusive, current);
                }
                next = basePort;
            }
            return next;
        });

        long count = ++allocationCount;

        if (DEEP_LOGS) {
            log.info("rtp_port_allocated port={} allocationCount={}", port, count);
        }

        // Periodic diagnostic snapshot
        if (DEEP_LOGS && log.isDebugEnabled() && (count % DEBUG_EVERY_N) == 0) {
            log.debug("rtp_port_alloc_periodic count={} nextWillBe={} basePort={} maxPortInclusive={}",
                    count, nextPort.get(), basePort, maxPortInclusive);
        }

        // Safety check warnings
        if (DEEP_LOGS && port < 1024) {
            log.warn("rtp_port_allocated_low_port_warning port={}", port);
        }
        if (DEEP_LOGS && port > 65000) {
            // This should never happen with our range logic, but keep the guard.
            log.warn("rtp_port_allocator_high_port_warning port={} nextWillBe={}",
                    port, nextPort.get());
        }

        return port;
    }
    
    
    public int[] allocatePortPair() {
        int p1 = allocatePort();
        int p2 = allocatePort();

        // ensure not same; extremely unlikely but safe
        if (p2 == p1) {
            p2 = allocatePort();
        }
        return new int[] { p1, p2 };
    }

}
