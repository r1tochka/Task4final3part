package com.cgvsu.math.base;

// базовая реализация матриц N x N с общими операциями.

import com.cgvsu.math.interfaces.Matrix;
import com.cgvsu.math.interfaces.Vector;

public abstract class AbstractMatrix<T extends AbstractMatrix<T, V>, V extends Vector<V>>
        implements Matrix<T, V> {

    protected final float[][] data;
    protected final int size;

    protected AbstractMatrix(float[][] data, int size) {
        if (data.length != size) {
            throw new IllegalArgumentException("Matrix must be " + size + "x" + size);
        }
        for (int i = 0; i < size; i++) {
            if (data[i].length != size) {
                throw new IllegalArgumentException("Matrix must be " + size + "x" + size);
            }
        }

        this.size = size;
        this.data = new float[size][size];

        for (int i = 0; i < size; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, size);
        }
    }

    protected abstract T createNew(float[][] data);

    @Override
    public T add(T other) {
        float[][] result = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        return createNew(result);
    }

    @Override
    public T subtract(T other) {
        float[][] result = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = this.data[i][j] - other.data[i][j];
            }
        }
        return createNew(result);
    }

    @Override
    public T multiply(float scalar) {
        float[][] result = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = this.data[i][j] * scalar;
            }
        }
        return createNew(result);
    }

    @Override
    public T multiply(T other) {
        float[][] result = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    result[i][j] += this.data[i][k] * other.data[k][j];
                }
            }
        }
        return createNew(result);
    }

    @Override
    public T transpose() {
        float[][] result = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = this.data[j][i];
            }
        }
        return createNew(result);
    }

    public float get(int row, int col) {
        return data[row][col];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AbstractMatrix<?, ?> other = (AbstractMatrix<?, ?>) obj;
        if (this.size != other.size) return false;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (Math.abs(this.data[i][j] - other.data[i][j]) >= 1e-6f) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = size;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result = 31 * result + Float.hashCode(data[i][j]);
            }
        }
        return result;
    }
}
