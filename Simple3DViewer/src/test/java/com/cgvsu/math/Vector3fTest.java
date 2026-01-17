package com.cgvsu.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Vector3fTest {

    @Test
    public void testVector3fConstructor() {
        Vector3f v = new Vector3f(1.5f, 2.5f, 3.5f);
        assertEquals(1.5f, v.getX(), 0.0001f);
        assertEquals(2.5f, v.getY(), 0.0001f);
        assertEquals(3.5f, v.getZ(), 0.0001f);
    }

    @Test
    public void testVector3fAddition() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        Vector3f result = v1.add(v2);
        assertEquals(new Vector3f(5, 7, 9), result);
    }

    @Test
    public void testVector3fCrossProduct() {
        Vector3f v1 = new Vector3f(1, 0, 0);
        Vector3f v2 = new Vector3f(0, 1, 0);
        Vector3f result = v1.cross(v2);
        assertEquals(new Vector3f(0, 0, 1), result);
    }

    @Test
    public void testVector3fDotProduct() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        assertEquals(32, v1.dot(v2), 0.0001f);
    }

    @Test
    public void testVector3fLength() {
        Vector3f v = new Vector3f(1, 2, 2);
        assertEquals(3, v.length(), 0.0001f);
    }

    @Test
    public void testVector3fNormalization() {
        Vector3f v = new Vector3f(1, 2, 2);
        Vector3f normalized = v.normalize();
        assertEquals(1, normalized.length(), 0.0001f);
    }

    @Test
    public void testVector3fDistance() {
        Vector3f v1 = new Vector3f(0, 0, 0);
        Vector3f v2 = new Vector3f(1, 2, 2);
        assertEquals(3, v1.distance(v2), 0.0001f);
    }
}
