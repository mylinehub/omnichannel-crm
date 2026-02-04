package com.mylinehub.crm.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;

import com.mylinehub.crm.ami.events.EventListner;

/**
 * Production-grade in-memory registry for AMI connections.
 *
 * Design goals:
 * - One domain => one Entry (factory + connection + listener).
 * - Fast "verify & return": do NOT create/log in a new connection here.
 *   If an existing connection is present and looks usable, return it.
 *   Otherwise return empty/null and let ConnectionStream/refresh/manager handle reconnect.
 * - No index-based deletes, no list alignment bugs.
 * - Safe normalization and safe snapshots.
 */
public final class CurrentConnections {

    private CurrentConnections() {}

    /**
     * One domain => one AMI connection + its factory + listener.
     * Keyed by normalized domain (trim + lower-case).
     */
    private static final Map<String, Entry> BY_DOMAIN = new ConcurrentHashMap<>();
    private static final long STALE_AFTER_MS = 5 * 60 * 1000L;
    
    private static String norm(String domain) {
        if (domain == null) return "";
        String d = domain.trim();
        if (d.isEmpty()) return "";
        return d.toLowerCase(Locale.ROOT);
    }
    
    private static boolean looksUsable(Entry e) {
        if (e == null) return false;

        ManagerConnection c = e.connection;
        if (c == null) return false;

        // 1) Prefer explicit state if available (pure local check)
        try {
            Object stObj = c.getState(); // may not exist in some versions
            if (stObj != null) {
                String state = String.valueOf(stObj).toUpperCase(Locale.ROOT);

                if (state.contains("CONNECTED")) return true;

                if (state.contains("DISCONNECTED") || state.contains("LOGOFF") || state.contains("OFFLINE")) {
                    return false;
                }
                // If state is something else/unknown -> fall through to lastEventMillis check
            }
        } catch (Throwable ignore) {
            // state not supported -> fall through
        }

        // 2) Fallback: last event activity (still no network call)
        long last = e.lastEventMillis;
        if (last <= 0) {
            // We have never seen an event. Connection might be new or dead.
            // Treat as NOT usable so caller can login/refresh properly.
            return false;
        }

        long age = System.currentTimeMillis() - last;
        return age <= STALE_AFTER_MS;
    }

    public static final class Entry {
        private final String domainKey;
        private final String originalDomain;

        private volatile ManagerConnectionFactory factory;
        private volatile ManagerConnection connection;
        private volatile EventListner listener;

        private volatile long lastEventMillis;
        public long getLastEventMillis() { return lastEventMillis; }
        
        private Entry(String domainKey, String originalDomain) {
            this.domainKey = domainKey;
            this.originalDomain = originalDomain;
        }

        public String getDomainKey() { return domainKey; }
        public String getOriginalDomain() { return originalDomain; }

        public ManagerConnectionFactory getFactory() { return factory; }
        public ManagerConnection getConnection() { return connection; }
        public EventListner getListener() { return listener; }

        /** Local-only check; does NOT call network. */
        public boolean hasUsableConnection() {
            return looksUsable(this);
        }
    }

    /** Full entry for domain (factory/connection/listener) */
    public static Optional<Entry> getEntry(String domain) {
        String key = norm(domain);
        if (key.isEmpty()) return Optional.empty();
        return Optional.ofNullable(BY_DOMAIN.get(key));
    }


    public static void markActivity(String domain) {
        String key = norm(domain);
        Entry e = BY_DOMAIN.get(key);
        if (e != null) e.lastEventMillis = System.currentTimeMillis();
    }
    
    /**
     * Verify & return:
     * - If there is an entry AND the connection looks usable (local state only), return it.
     * - Else return empty.
     *
     * IMPORTANT: This method does NOT create connections and does NOT call login().
     */
    public static Optional<ManagerConnection> getVerifiedConnection(String domain) {
        String key = norm(domain);
        if (key.isEmpty()) return Optional.empty();

        Entry e = BY_DOMAIN.get(key);
        if (e == null) return Optional.empty();

        ManagerConnection c = e.connection;
        return looksUsable(e) ? Optional.of(c) : Optional.empty();
    }

    /**
     * Backwards-simple: may return null if missing/unusable.
     * Prefer getVerifiedConnection(domain).
     */
    public static ManagerConnection getConnectionIfUsable(String domain) {
        return getVerifiedConnection(domain).orElse(null);
    }

    /**
     * Get current connection for a domain (may be null). This does NOT verify usability.
     * Prefer getVerifiedConnection(domain) if you want "connected-looking only".
     */
    public static ManagerConnection getConnection(String domain) {
        Entry e = BY_DOMAIN.get(norm(domain));
        return e == null ? null : e.connection;
    }

    /**
     * Put/replace all objects for this domain.
     * If replacing an existing connection object, we logoff the old one (best-effort).
     */
    public static void upsert(String domain,
                              ManagerConnectionFactory factory,
                              ManagerConnection connection,
                              EventListner listener) {

        String key = norm(domain);
        if (key.isEmpty()) return;

        // If caller passes nulls, still allow update (e.g., to clear listener/connection).
        BY_DOMAIN.compute(key, (k, existing) -> {
            if (existing == null) {
                existing = new Entry(k, domain == null ? "" : domain.trim());
            }

            ManagerConnection oldConn = existing.connection;
            if (oldConn != null && oldConn != connection) {
                try { oldConn.logoff(); } catch (Exception ignore) {}
            }

            existing.factory = factory;
            existing.connection = connection;
            existing.listener = listener;
            return existing;
        });
    }

    /**
     * Only update connection for domain (keeps existing factory/listener).
     * Best-effort logoff old connection if different object.
     */
    public static void upsertConnectionOnly(String domain, ManagerConnection connection) {
        String key = norm(domain);
        if (key.isEmpty()) return;

        BY_DOMAIN.compute(key, (k, existing) -> {
            if (existing == null) existing = new Entry(k, domain == null ? "" : domain.trim());

            ManagerConnection oldConn = existing.connection;
            if (oldConn != null && oldConn != connection) {
                try { oldConn.logoff(); } catch (Exception ignore) {}
            }

            existing.connection = connection;
            return existing;
        });
    }

    /**
     * Remove everything for a domain (best-effort logoff).
     */
    public static void remove(String domain) {
        String key = norm(domain);
        if (key.isEmpty()) return;

        Entry removed = BY_DOMAIN.remove(key);
        if (removed != null) {
            ManagerConnection conn = removed.connection;
            if (conn != null) {
                try { conn.logoff(); } catch (Exception ignore) {}
            }
        }
    }

    /** Useful for iterating all domains */
    public static List<Entry> snapshotEntries() {
        // Snapshot + unmodifiable to prevent accidental external mutation patterns.
        return Collections.unmodifiableList(new ArrayList<>(BY_DOMAIN.values()));
    }

    public static List<ManagerConnection> snapshotConnections() {
        List<ManagerConnection> out = new ArrayList<>();
        for (Entry e : BY_DOMAIN.values()) {
            if (e.connection != null) out.add(e.connection);
        }
        return Collections.unmodifiableList(out);
    }

    public static int size() {
        return BY_DOMAIN.size();
    }

    /**
     * Clear everything (best-effort logoff all current connections).
     */
    public static void clearAll() {
        for (Entry e : BY_DOMAIN.values()) {
            ManagerConnection c = e.connection;
            if (c != null) {
                try { c.logoff(); } catch (Exception ignore) {}
            }
        }
        BY_DOMAIN.clear();
    }

    /**
     * Helper for callers that want to ensure domain is present (does not connect).
     */
    public static void ensureDomainKeyExists(String domain) {
        String key = norm(domain);
        if (key.isEmpty()) return;

        BY_DOMAIN.computeIfAbsent(key, k -> new Entry(k, Objects.toString(domain, "").trim()));
    }
}
