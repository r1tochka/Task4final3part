package com.cgvsu.math.base;

//базовая реализация векторов произвольной размерности.
//Операции: сумма/разность/масштаб, длина, нормализация, скалярное произведение.

import com.cgvsu.math.interfaces.Vector;

public abstract class AbstractVector<T extends AbstractVector<T>> implements Vector<T> {

    protected final float[] components;

    protected AbstractVector(float[] components) {
        this.components = components.clone();
    }

    protected abstract T createNew(float[] components);

    @Override
    public T add(T other) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = this.components[i] + other.components[i];
        }
        return createNew(result);
    }

    @Override
    public T subtract(T other) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = this.components[i] - other.components[i];
        }
        return createNew(result);
    }

    @Override
    public T multiply(float scalar) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = this.components[i] * scalar;
        }
        return createNew(result);
    }

    @Override
    public T divide(float scalar) {
        if (Math.abs(scalar) < 1e-12f) {
            throw new ArithmeticException("Division by zero");
        }
        return multiply(1.0f / scalar);
    }

    @Override
    public float length() {
        float sum = 0;
        for (float component : components) {
            sum += component * component;
        }
        return (float) Math.sqrt(sum);
    }

    @Override
    public T normalize() {
        float len = length();
        if (len < 1e-12f) {
            throw new ArithmeticException("Cannot normalize zero vector");
        }
        return multiply(1.0f / len);
    }

    @Override
    public float dot(T other) {
        float result = 0;
        for (int i = 0; i < components.length; i++) {
            result += this.components[i] * other.components[i];
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AbstractVector<?> other = (AbstractVector<?>) obj;
        if (this.components.length != other.components.length) return false;

        for (int i = 0; i < components.length; i++) {
            if (Math.abs(components[i] - other.components[i]) >= 1e-6f) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (float component : components) {
            result = 31 * result + Float.hashCode(component);
        }
        return result;
    }
}
