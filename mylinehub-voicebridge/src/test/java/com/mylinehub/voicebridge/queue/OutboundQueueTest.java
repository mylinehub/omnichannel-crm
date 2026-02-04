///*
// * Auto-formatted: src/test/java/com/mylinehub/voicebridge/queue/OutboundQueueTest.java
// */
//package com.mylinehub.voicebridge.queue;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class OutboundQueueTest {
//
//    @Test
//    void enforcesChunkMultiple() {
//        OutboundQueue q =
//                new OutboundQueue(4000, OutboundQueue.OverflowPolicy.DROP_OLDEST);
//
//        byte[] bad = new byte[321]; // not multiple of 320 (your alignment rule)
//        assertFalse(q.enqueue(
//                new OutboundQueue.PcmChunk("x", bad, 100, null, 8000)
//        ));
//    }
//
//    @Test
//    void dropOldestOnOverflow() {
//        OutboundQueue q =
//                new OutboundQueue(100, OutboundQueue.OverflowPolicy.DROP_OLDEST);
//
//        byte[] ok = new byte[320];
//
//        assertTrue(q.enqueue(
//                new OutboundQueue.PcmChunk("a", ok, 80, null, 8000)
//        ));
//        assertTrue(q.enqueue(
//                new OutboundQueue.PcmChunk("b", ok, 80, null, 8000)
//        ));
//
//        // triggers eviction under DROP_OLDEST
//        assertTrue(q.depthMs() <= 100);
//    }
//
//    @Test
//    void orderPreserved() {
//        OutboundQueue q =
//                new OutboundQueue(1000, OutboundQueue.OverflowPolicy.DROP_OLDEST);
//
//        byte[] ok = new byte[320];
//
//        q.enqueue(new OutboundQueue.PcmChunk("a", ok, 80, null, 8000));
//        q.enqueue(new OutboundQueue.PcmChunk("b", ok, 80, null, 8000));
//
//        assertEquals("a", q.poll().getId());
//        assertEquals("b", q.poll().getId());
//    }
//}
