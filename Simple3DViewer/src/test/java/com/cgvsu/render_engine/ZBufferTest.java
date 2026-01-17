package com.cgvsu.render_engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ZBufferTest {

    @Test
    void testAndSetAcceptsCloserDepthAndRejectsFarther() {
        ZBuffer z = new ZBuffer(4, 3);

        assertTrue(z.testAndSet(1, 1, 10.0));
        assertFalse(z.testAndSet(1, 1, 12.0));
        assertTrue(z.testAndSet(1, 1, 9.0));
    }

    @Test
    void testAndSetRejectsOutOfBounds() {
        ZBuffer z = new ZBuffer(2, 2);
        assertFalse(z.testAndSet(-1, 0, 1.0));
        assertFalse(z.testAndSet(0, -1, 1.0));
        assertFalse(z.testAndSet(2, 0, 1.0));
        assertFalse(z.testAndSet(0, 2, 1.0));
    }

    @Test
    void clearResetsAllCellsToInfinity() {
        ZBuffer z = new ZBuffer(2, 2);
        assertTrue(z.testAndSet(0, 0, 5.0));
        z.clear();
        assertTrue(z.testAndSet(0, 0, 100.0), "After clear any finite z should be accepted");
    }
}
