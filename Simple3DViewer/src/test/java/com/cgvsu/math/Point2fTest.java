package com.cgvsu.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Point2fTest {

    @Test
    public void testPoint2fConstructor() {
        Point2f p = new Point2f(1.5f, 2.5f);
        assertEquals(1.5f, p.getX(), 0.0001f);
        assertEquals(2.5f, p.getY(), 0.0001f);
    }

    @Test
    public void testPoint2fAddition() {
        Point2f p1 = new Point2f(1, 2);
        Point2f p2 = new Point2f(3, 4);
        Point2f result = p1.add(p2);
        assertEquals(new Point2f(4, 6), result);
    }

    @Test
    public void testPoint2fSubtraction() {
        Point2f p1 = new Point2f(5, 6);
        Point2f p2 = new Point2f(1, 2);
        Point2f result = p1.subtract(p2);
        assertEquals(new Point2f(4, 4), result);
    }

    @Test
    public void testPoint2fDistance() {
        Point2f p1 = new Point2f(0, 0);
        Point2f p2 = new Point2f(3, 4);
        assertEquals(5, p1.distance(p2), 0.0001f);
    }

    @Test
    public void testPoint2fToVector() {
        Point2f p = new Point2f(2, 3);
        Vector2f v = p.toVector2f();
        assertEquals(new Vector2f(2, 3), v);
    }

    @Test
    public void testPoint2fFromVector() {
        Vector2f v = new Vector2f(2, 3);
        Point2f p = Point2f.fromVector2f(v);
        assertEquals(new Point2f(2, 3), p);
    }

    @Test
    public void testPoint2fTranslation() {
        Point2f p = new Point2f(1, 1);
        Vector2f v = new Vector2f(2, 3);
        Point2f result = p.translate(v);
        assertEquals(new Point2f(3, 4), result);
    }
}
