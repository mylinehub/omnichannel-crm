/*
 * File: src/main/java/com/mylinehub/voicebridge/ari/ExternalMediaManager.java
 */
package com.mylinehub.voicebridge.ari;

import com.mylinehub.voicebridge.models.StasisAppConfig;
import reactor.core.publisher.Mono;

public interface ExternalMediaManager {

    Mono<String> createBridge(StasisAppConfig cfg, String type, String name);

    Mono<Void> addChannelToBridge(StasisAppConfig cfg, String bridgeId, String channelId);

    Mono<String> createSnoopInbound(StasisAppConfig cfg, String callerChannelId, String app, String snoopId);

    Mono<String> createExternalMedia(
            StasisAppConfig cfg,
            String app,
            String externalId,
            String host,
            int port,
            String codec
    );

    Mono<Void> destroyBridge(StasisAppConfig cfg, String bridgeId);

    Mono<Void> hangupChannel(StasisAppConfig cfg, String channelId);

    /**
     * Returns Asterisk RTP "local" address/port for a channel from ARI channelvars:
     * - UNICASTRTP_LOCAL_ADDRESS
     * - UNICASTRTP_LOCAL_PORT
     *
     * This is the peer we must send RTP to (to avoid "wait until Asterisk sends first" deadlock).
     */
    Mono<UnicastRtpPeer> getUnicastRtpPeer(StasisAppConfig cfg, String channelId);

    final class UnicastRtpPeer {
        public final String ip;
        public final int port;

        public UnicastRtpPeer(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public String toString() {
            return ip + ":" + port;
        }
    }
}
