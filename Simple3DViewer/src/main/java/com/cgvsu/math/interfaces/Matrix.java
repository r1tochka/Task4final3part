package com.cgvsu.math.interfaces;

// контракт матрицы (операции линейной алгебры).
// умножение на вектор-столбец.

public interface Matrix<T extends Matrix<T, V>, V extends Vector<V>> {
    T add(T other);
    T subtract(T other);
    T multiply(float scalar);
    V multiply(V vector); // умножение матрицы на вектор-столбец
    T multiply(T other);

    T transpose();
    float determinant();
    T inverse();
    V solveLinearSystem(V vector);

    boolean equals(Object obj);
    int hashCode();
    String toString();
}
