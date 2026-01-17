package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    @Test
    void copyCreatesIndependentInstance() {
        Camera c = new Camera(new Vector3f(1, 2, 3), new Vector3f(0, 0, 0), 1.0f, 1.0f, 0.1f, 100f);
        Camera copy = c.copy();

        assertNotSame(c, copy);
        assertNotSame(c.getPosition(), copy.getPosition());
        assertNotSame(c.getTarget(), copy.getTarget());
        assertEquals(c.getPosition().getX(), copy.getPosition().getX(), 1e-6);
        assertEquals(c.getTarget().getZ(), copy.getTarget().getZ(), 1e-6);

        copy.movePosition(new Vector3f(10, 0, 0));
        assertNotEquals(c.getPosition().getX(), copy.getPosition().getX(), 1e-6);
    }

    @Test
    void panMovesPositionAndTargetTogether() {
        Camera c = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 1.0f, 1.0f, 0.1f, 100f);
        Vector3f posBefore = c.getPosition();
        Vector3f tgtBefore = c.getTarget();

        c.pan(1.0f, 0.5f);

        Vector3f posAfter = c.getPosition();
        Vector3f tgtAfter = c.getTarget();

        Vector3f deltaPos = posAfter.subtract(posBefore);
        Vector3f deltaTgt = tgtAfter.subtract(tgtBefore);

        assertEquals(deltaPos.getX(), deltaTgt.getX(), 1e-6);
        assertEquals(deltaPos.getY(), deltaTgt.getY(), 1e-6);
        assertEquals(deltaPos.getZ(), deltaTgt.getZ(), 1e-6);
    }

    @Test
    void zoomClampsDistanceToMinMax() {
        Camera c = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 1.0f, 1.0f, 0.1f, 100f);

        float dist0 = c.getPosition().subtract(c.getTarget()).length();

        c.zoom(-10_000f);
        float distMin = c.getPosition().subtract(c.getTarget()).length();
        assertTrue(distMin > 0.0f);
        assertTrue(distMin <= dist0);

        c.zoom(10_000f);
        float distMax = c.getPosition().subtract(c.getTarget()).length();
        assertTrue(distMax >= distMin);
    }

    @Test
    void rotateDoesNotChangeTargetAndKeepsFinitePosition() {
        Camera c = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 1.0f, 1.0f, 0.1f, 100f);
        Vector3f tgtBefore = c.getTarget();

        c.rotate(100f, 100f);
        c.rotate(-100f, -100f);

        Vector3f tgtAfter = c.getTarget();
        assertEquals(tgtBefore.getX(), tgtAfter.getX(), 1e-6);
        assertEquals(tgtBefore.getY(), tgtAfter.getY(), 1e-6);
        assertEquals(tgtBefore.getZ(), tgtAfter.getZ(), 1e-6);

        Vector3f pos = c.getPosition();
        assertTrue(Float.isFinite(pos.getX()));
        assertTrue(Float.isFinite(pos.getY()));
        assertTrue(Float.isFinite(pos.getZ()));
    }
}
