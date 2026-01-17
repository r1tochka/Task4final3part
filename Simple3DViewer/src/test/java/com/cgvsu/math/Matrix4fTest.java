package com.cgvsu.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Matrix4fTest {

    @Test
    public void testMatrix4fIdentity() {
        Matrix4f identity = Matrix4f.identity();
        assertEquals(1.0f, identity.determinant(), 0.0001f);
    }

    @Test
    public void testMatrix4fZero() {
        Matrix4f zero = Matrix4f.zero();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(0.0f, zero.get(i, j), 0.0001f);
            }
        }
    }

    @Test
    public void testMatrix4fDeterminant() {
        Matrix4f matrix = new Matrix4f(new float[][]{
                {1, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 3, 0},
                {0, 0, 0, 4}
        });
        assertEquals(24, matrix.determinant(), 0.0001f);
    }

    @Test
    public void testMatrix4fVectorMultiplication() {
        Matrix4f matrix = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Vector4f vector = new Vector4f(1, 2, 3, 4);
        Vector4f result = matrix.multiply(vector);
        assertEquals(new Vector4f(30, 70, 110, 150), result);
    }

    @Test
    public void testMatrix4fSystemSolving() {
        Matrix4f A = new Matrix4f(new float[][]{
                {2, 1, 1, 1},
                {1, 3, 1, 1},
                {1, 1, 4, 1},
                {1, 1, 1, 5}
        });
        Vector4f b = new Vector4f(5, 6, 7, 8);
        Vector4f x = A.solveLinearSystem(b);

        Vector4f Ax = A.multiply(x);
        assertEquals(b, Ax);
    }

    @Test
    public void testMatrix4fTranspose() {
        Matrix4f matrix = new Matrix4f(new float[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        Matrix4f transposed = matrix.transpose();
        Matrix4f expected = new Matrix4f(new float[][]{
                {1, 5, 9, 13},
                {2, 6, 10, 14},
                {3, 7, 11, 15},
                {4, 8, 12, 16}
        });
        assertEquals(expected, transposed);
    }
}
