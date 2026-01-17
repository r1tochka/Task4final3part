package com.cgvsu.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Vector2fTest {

    @Test
    public void testVector2fConstructor() {
        Vector2f v = new Vector2f(1.5f, 2.5f);
        assertEquals(1.5f, v.getX(), 0.0001f);
        assertEquals(2.5f, v.getY(), 0.0001f);
    }

    @Test
    public void testVector2fAddition() {
        Vector2f v1 = new Vector2f(1, 2);
        Vector2f v2 = new Vector2f(3, 4);
        Vector2f result = v1.add(v2);
        assertEquals(new Vector2f(4, 6), result);
    }

    @Test
    public void testVector2fSubtraction() {
        Vector2f v1 = new Vector2f(1, 2);
        Vector2f v2 = new Vector2f(3, 4);
        Vector2f result = v1.subtract(v2);
        assertEquals(new Vector2f(-2, -2), result);
    }

    @Test
    public void testVector2fScalarMultiplication() {
        Vector2f v = new Vector2f(2, 3);
        Vector2f result = v.multiply(2);
        assertEquals(new Vector2f(4, 6), result);
    }

    @Test
    public void testVector2fScalarDivision() {
        Vector2f v = new Vector2f(2, 3);
        Vector2f result = v.divide(2);
        assertEquals(new Vector2f(1, 1.5f), result);
    }

    @Test
    public void testVector2fLength() {
        Vector2f v = new Vector2f(3, 4);
        assertEquals(5, v.length(), 0.0001f);
    }

    @Test
    public void testVector2fNormalization() {
        Vector2f v = new Vector2f(3, 4);
        Vector2f normalized = v.normalize();
        assertEquals(1, normalized.length(), 0.0001f);
    }

    @Test
    public void testVector2fDotProduct() {
        Vector2f v1 = new Vector2f(1, 2);
        Vector2f v2 = new Vector2f(3, 4);
        assertEquals(11, v1.dot(v2), 0.0001f);
    }

    @Test
    public void testVector2fDistance() {
        Vector2f v1 = new Vector2f(0, 0);
        Vector2f v2 = new Vector2f(3, 4);
        assertEquals(5, v1.distance(v2), 0.0001f);
    }
}
