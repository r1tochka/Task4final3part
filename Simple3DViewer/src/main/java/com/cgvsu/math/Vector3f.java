package com.cgvsu.math;

// вектор 3D (позиции/направления/нормали)
//  dot/cross/normalize + базовая арифметика (AbstractVector)

import com.cgvsu.math.base.AbstractVector;

public class Vector3f extends AbstractVector<Vector3f> {

    public Vector3f(float x, float y, float z) {
        super(new float[]{x, y, z});
    }

    @Override
    protected Vector3f createNew(float[] components) {
        return new Vector3f(components[0], components[1], components[2]);
    }

    public float getX() {
        return components[0];
    }

    public float getY() {
        return components[1];
    }

    public float getZ() {
        return components[2];
    }

    public Vector3f cross(Vector3f other) {
        return new Vector3f(
                components[1] * other.components[2] - components[2] * other.components[1],
                components[2] * other.components[0] - components[0] * other.components[2],
                components[0] * other.components[1] - components[1] * other.components[0]
        );
    }

    public float distance(Vector3f other) {
        float dx = components[0] - other.components[0];
        float dy = components[1] - other.components[1];
        float dz = components[2] - other.components[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%.2f, %.2f, %.2f)", 
                components[0], components[1], components[2]);
    }
}