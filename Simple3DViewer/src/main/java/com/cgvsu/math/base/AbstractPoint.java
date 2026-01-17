package com.cgvsu.math.base;

//базовая реализация точек (координаты + расстояние).
//операции: сумма/разность координат, вычисление distance.

import com.cgvsu.math.interfaces.Point;

public abstract class AbstractPoint<T extends AbstractPoint<T>> implements Point<T> {
    protected final float[] coordinates;

    protected AbstractPoint(float[] coordinates) {
        this.coordinates = coordinates.clone();
    }

    protected abstract T createNew(float[] coordinates);

    @Override
    public T add(T other) {
        float[] result = new float[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            result[i] = this.coordinates[i] + other.coordinates[i];
        }
        return createNew(result);
    }

    @Override
    public T subtract(T other) {
        float[] result = new float[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            result[i] = this.coordinates[i] - other.coordinates[i];
        }
        return createNew(result);
    }

    @Override
    public float distance(T other) {
        float sum = 0;
        for (int i = 0; i < coordinates.length; i++) {
            float diff = coordinates[i] - other.coordinates[i];
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AbstractPoint<?> other = (AbstractPoint<?>) obj;
        if (this.coordinates.length != other.coordinates.length) return false;

        for (int i = 0; i < coordinates.length; i++) {
            if (Math.abs(coordinates[i] - other.coordinates[i]) >= 1e-6f) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (float coordinate : coordinates) {
            result = 31 * result + Float.hashCode(coordinate);
        }
        return result;
    }
}
