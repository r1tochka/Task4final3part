package com.cgvsu.math;

// 2D-точка (экранные координаты, вспомогательная геометрия).


import com.cgvsu.math.base.AbstractPoint;

public class Point2f extends AbstractPoint<Point2f> {

    public Point2f(float x, float y) {
        super(new float[]{x, y});
    }

    @Override
    protected Point2f createNew(float[] coordinates) {
        return new Point2f(coordinates[0], coordinates[1]);
    }

    public float getX() { return coordinates[0]; }
    public float getY() { return coordinates[1]; }

    public Vector2f toVector2f() {
        return new Vector2f(getX(), getY());
    }

    public static Point2f fromVector2f(Vector2f vector) {
        return new Point2f(vector.getX(), vector.getY());
    }

    public Point2f translate(Vector2f vector) {
        return new Point2f(getX() + vector.getX(), getY() + vector.getY());
    }

    @Override
    public String toString() {
        return String.format("P(%.4f, %.4f)", getX(), getY());
    }
}
