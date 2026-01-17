package com.cgvsu.math.interfaces;

// контракт вектора (арифметика, норма, скалярное произведение).

public interface Vector<T extends Vector<T>> {
    T add(T other);
    T subtract(T other);
    T multiply(float scalar);
    T divide(float scalar);
    float length();
    T normalize();
    float dot(T other);

    boolean equals(Object obj);
    int hashCode();
    String toString();
}
