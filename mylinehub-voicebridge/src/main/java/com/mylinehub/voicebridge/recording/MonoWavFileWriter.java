// ============================================================
// FILE: src/main/java/com/mylinehub/voicebridge/recording/MonoWavFileWriter.java
// ============================================================
package com.mylinehub.voicebridge.recording;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MonoWavFileWriter
 * - Writes PCM16LE mono WAV at a fixed sample rate.
 * - No DSP/noise-gate/no trimming besides "even bytes" safety.
 */
public final class MonoWavFileWriter implements Closeable {

    private final RandomAccessFile raf;
    private final int sampleRateHz;
    private long dataBytes = 0;

    public MonoWavFileWriter(String fullPath, int sampleRateHz) throws IOException {
        this(Path.of(fullPath), sampleRateHz);
    }

    public MonoWavFileWriter(Path path, int sampleRateHz) throws IOException {
        this.sampleRateHz = sampleRateHz;
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);

        this.raf = new RandomAccessFile(path.toFile(), "rw");
        this.raf.setLength(0);
        writeHeaderPlaceholder();
    }

    public synchronized void writeMonoPcm16le(byte[] pcm16le) throws IOException {
        if (pcm16le == null || pcm16le.length == 0) return;
        int len = pcm16le.length & ~1; // even length only
        if (len <= 0) return;

        raf.write(pcm16le, 0, len);
        dataBytes += len;
    }

    private void writeHeaderPlaceholder() throws IOException {
        raf.write(new byte[44]);
    }

    private void rewriteHeader() throws IOException {
        long riffSize = 36 + dataBytes;
        long byteRate = sampleRateHz * 2L; // mono 16-bit => 2 bytes/sample
        int blockAlign = 2;

        ByteBuffer b = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);

        b.put(new byte[]{'R','I','F','F'});
        b.putInt((int) riffSize);
        b.put(new byte[]{'W','A','V','E'});

        b.put(new byte[]{'f','m','t',' '});
        b.putInt(16);
        b.putShort((short) 1);     // PCM
        b.putShort((short) 1);     // mono
        b.putInt(sampleRateHz);
        b.putInt((int) byteRate);
        b.putShort((short) blockAlign);
        b.putShort((short) 16);    // bits per sample

        b.put(new byte[]{'d','a','t','a'});
        b.putInt((int) dataBytes);

        raf.seek(0);
        raf.write(b.array());
    }

    @Override
    public synchronized void close() {
        try {
            rewriteHeader();
            raf.close();
        } catch (Exception ignored) {}
    }
}
