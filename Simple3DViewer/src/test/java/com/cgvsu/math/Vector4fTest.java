package com.cgvsu.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Vector4fTest {

    @Test
    public void testVector4fConstructor() {
        Vector4f v = new Vector4f(1.5f, 2.5f, 3.5f, 1.0f);
        assertEquals(1.5f, v.getX(), 0.0001f);
        assertEquals(2.5f, v.getY(), 0.0001f);
        assertEquals(3.5f, v.getZ(), 0.0001f);
        assertEquals(1.0f, v.getW(), 0.0001f);
    }

    @Test
    public void testVector4fFromVector3f() {
        Vector3f v3 = new Vector3f(1, 2, 3);
        Vector4f v4 = new Vector4f(v3);
        assertEquals(new Vector4f(1, 2, 3, 1), v4);
    }

    @Test
    public void testVector4fToVector3f() {
        Vector4f v4 = new Vector4f(2, 4, 6, 2);
        Vector3f v3 = v4.toVector3f();
        assertEquals(new Vector3f(1, 2, 3), v3);
    }

    @Test
    public void testVector4fDotProduct() {
        Vector4f v1 = new Vector4f(1, 2, 3, 4);
        Vector4f v2 = new Vector4f(5, 6, 7, 8);
        assertEquals(70, v1.dot(v2), 0.0001f);
    }

    @Test
    public void testVector4fLength() {
        Vector4f v = new Vector4f(1, 2, 2, 4);
        assertEquals(5, v.length(), 0.0001f);
    }

    @Test
    public void testVector4fNormalization() {
        Vector4f v = new Vector4f(1, 2, 2, 4);
        Vector4f normalized = v.normalize();
        assertEquals(1, normalized.length(), 0.0001f);
    }

    @Test
    public void testVector4fDistance() {
        Vector4f v1 = new Vector4f(1, 1, 1, 1);
        Vector4f v2 = new Vector4f(2, 2, 2, 2);
        assertEquals(2, v1.distance(v2), 0.0001f);

        Vector4f v3 = new Vector4f(0, 0, 0, 0);
        Vector4f v4 = new Vector4f(1, 0, 0, 0);
        assertEquals(1, v3.distance(v4), 0.0001f);

        Vector4f v5 = new Vector4f(1, 2, 3, 4);
        Vector4f v6 = new Vector4f(5, 6, 7, 8);
        assertEquals(8, v5.distance(v6), 0.0001f);
    }
}
