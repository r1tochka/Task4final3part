package com.cgvsu.math.interfaces;

// контракт точки (операции и расстояние).

public interface Point<T extends Point<T>> {
    T add(T other);
    T subtract(T other);
    float distance(T other);

    boolean equals(Object obj);
    int hashCode();
    String toString();
}
