/*
 * File: src/main/java/com/mylinehub/voicebridge/recording/StereoWavFileWriter.java
 *
 * Changes (safe / no behavior break):
 * - Keeps public API identical.
 * - Removes per-write allocation of the interleaved output buffer (reuses a growable buffer).
 * - Keeps synchronized for safety (even if current usage is single IO thread per call).
 * - No format change: still 16-bit PCM, stereo, interleaved L/R, proper RIFF header rewrite on close.
 */

package com.mylinehub.voicebridge.recording;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StereoWavFileWriter implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(StereoWavFileWriter.class);

    private static final boolean DEEP_LOGS = false;
    private static final boolean RTP_DEEP_LOGS = false;

    private static final int TRACE_LIMIT = 0;

    private final RandomAccessFile raf;
    private final int sampleRate;
    private long dataBytes = 0;

    // Reusable interleave buffer (grown as needed). Avoids allocating a new byte[] per write.
    private byte[] interleaveBuf = new byte[0];

    public StereoWavFileWriter(String fullPath, int sampleRateHz) throws IOException {
        this(Path.of(fullPath), sampleRateHz);
    }

    public StereoWavFileWriter(Path path, int sampleRateHz) throws IOException {
        this.sampleRate = sampleRateHz;
        Files.createDirectories(path.getParent());
        this.raf = new RandomAccessFile(path.toFile(), "rw");
        this.raf.setLength(0);
        writeHeaderPlaceholder();
        if (DEEP_LOGS) {
            log.info("stereo_wav_open file={} sampleRateHz={} deepLogs={}",
                    path.toAbsolutePath(), sampleRateHz, DEEP_LOGS);
        }
    }

    /**
     * Writes a stereo frame block; left or right may be null (silence injected).
     * PCM16LE mono arrays are interleaved into stereo:
     *   L0 L1 R0 R1 L2 L3 R2 R3 ...
     */
    public synchronized void writeStereo(byte[] leftPcm16le, byte[] rightPcm16le) throws IOException {
        final long t0 = DEEP_LOGS ? System.nanoTime() : 0L;

        int leftLen  = leftPcm16le  == null ? 0 : leftPcm16le.length;
        int rightLen = rightPcm16le == null ? 0 : rightPcm16le.length;

        int maxLen = Math.max(leftLen, rightLen);
        if (maxLen == 0) {
            if (DEEP_LOGS) log.debug("stereo_wav_write_skip_empty leftLen=0 rightLen=0");
            return;
        }

        // Ensure even length (16-bit samples)
        int evenMaxLen = maxLen - (maxLen & 1);
        if (evenMaxLen != maxLen && DEEP_LOGS) {
            log.debug("stereo_wav_write_trim_odd maxLen={} evenMaxLen={}", maxLen, evenMaxLen);
        }
        maxLen = evenMaxLen;

        // out length = maxLen (bytes per channel) * 2 channels
        int outLen = maxLen * 2;
        ensureInterleaveCapacity(outLen);

        byte[] out = interleaveBuf;
        int o = 0;

        int leftSilenceSamples = 0;
        int rightSilenceSamples = 0;
        int framesTraced = 0;

        for (int i = 0; i < maxLen; i += 2) {

            // LEFT sample (or silence)
            if (leftPcm16le != null && i + 1 < leftLen) {
                out[o++] = leftPcm16le[i];
                out[o++] = leftPcm16le[i + 1];
            } else {
                out[o++] = 0;
                out[o++] = 0;
                leftSilenceSamples++;
            }

            // RIGHT sample (or silence)
            if (rightPcm16le != null && i + 1 < rightLen) {
                out[o++] = rightPcm16le[i];
                out[o++] = rightPcm16le[i + 1];
            } else {
                out[o++] = 0;
                out[o++] = 0;
                rightSilenceSamples++;
            }

            if (DEEP_LOGS && TRACE_LIMIT > 0 && framesTraced < TRACE_LIMIT) {
                short l = 0;
                short r = 0;

                if (leftPcm16le != null && i + 1 < leftLen) {
                    int lo = leftPcm16le[i] & 0xFF;
                    int hi = leftPcm16le[i + 1];
                    l = (short) ((hi << 8) | lo);
                }

                if (rightPcm16le != null && i + 1 < rightLen) {
                    int lo = rightPcm16le[i] & 0xFF;
                    int hi = rightPcm16le[i + 1];
                    r = (short) ((hi << 8) | lo);
                }

                log.debug("stereo_wav_trace frameIdx={} L={} R={}", framesTraced, l, r);
                framesTraced++;
            }
        }

        raf.write(out, 0, outLen);
        dataBytes += outLen;

        if (DEEP_LOGS) {
            long us = (System.nanoTime() - t0) / 1000;
            log.debug("stereo_wav_write_ok leftLen={} rightLen={} maxLen={} outBytes={} dataBytesTotal={} leftSilenceSamples={} rightSilenceSamples={} durUs={}",
                    leftLen, rightLen, maxLen, outLen, dataBytes,
                    leftSilenceSamples, rightSilenceSamples, us);
        }
    }

    private void ensureInterleaveCapacity(int need) {
        if (interleaveBuf.length >= need) return;
        int newCap = Math.max(need, interleaveBuf.length * 2 + 1024);
        interleaveBuf = new byte[newCap];
    }

    private void writeHeaderPlaceholder() throws IOException {
        raf.write(new byte[44]);
        if (DEEP_LOGS) log.debug("stereo_wav_header_placeholder_written bytes=44");
    }

    private void rewriteHeader() throws IOException {
        long riffSize = 36 + dataBytes;
        long byteRate = sampleRate * 2L /*bytes per sample*/ * 2 /*channels*/;
        int blockAlign = 2 * 2; // 2 bytes * 2 channels

        if (DEEP_LOGS) {
            log.debug("stereo_wav_rewrite_header riffSize={} dataBytes={} sampleRate={} byteRate={} blockAlign={}",
                    riffSize, dataBytes, sampleRate, byteRate, blockAlign);
        }

        ByteBuffer b = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes());
        b.putInt((int) riffSize);
        b.put("WAVE".getBytes());
        b.put("fmt ".getBytes());
        b.putInt(16);
        b.putShort((short) 1);     // PCM
        b.putShort((short) 2);     // 2 channels stereo
        b.putInt(sampleRate);
        b.putInt((int) byteRate);
        b.putShort((short) blockAlign);
        b.putShort((short) 16);    // 16-bit
        b.put("data".getBytes());
        b.putInt((int) dataBytes);

        raf.seek(0);
        raf.write(b.array());
    }

    @Override
    public synchronized void close() {
        final long t0 = DEEP_LOGS ? System.nanoTime() : 0L;
        try {
            rewriteHeader();
            raf.close();
            long us = DEEP_LOGS ? (System.nanoTime() - t0) / 1000 : 0;
            if (DEEP_LOGS) {
                log.info("stereo_wav_close dataBytes={} sampleRateHz={} durUs={}",
                        dataBytes, sampleRate, us);
            }
        } catch (Exception e) {
            log.error("stereo_wav_close_error", e);
        }
    }
}
