package com.cgvsu.render_engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ZBufferEdgeTest {

    @Test
    void zeroSizedBufferRejectsAllWrites() {
        ZBuffer z = new ZBuffer(0, 0);
        assertFalse(z.testAndSet(0, 0, 1.0));
        assertFalse(z.testAndSet(-1, -1, 1.0));
    }

    @Test
    void acceptsNegativeDepthAsCloserValue() {
        ZBuffer z = new ZBuffer(1, 1);
        assertTrue(z.testAndSet(0, 0, 0.0));
        assertTrue(z.testAndSet(0, 0, -1.0));
        assertFalse(z.testAndSet(0, 0, 1.0));
    }
}
