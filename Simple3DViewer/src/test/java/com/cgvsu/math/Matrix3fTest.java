package com.cgvsu.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Matrix3fTest {

    @Test
    public void testMatrix3fIdentity() {
        Matrix3f identity = Matrix3f.identity();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float expected = (i == j) ? 1.0f : 0.0f;
                assertEquals(expected, identity.get(i, j), 0.0001f);
            }
        }
    }

    @Test
    public void testMatrix3fZero() {
        Matrix3f zero = Matrix3f.zero();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(0.0f, zero.get(i, j), 0.0001f);
            }
        }
    }

    @Test
    public void testMatrix3fDeterminant() {
        Matrix3f matrix = new Matrix3f(new float[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });
        assertEquals(0, matrix.determinant(), 0.0001f);
    }

    @Test
    public void testMatrix3fInverse() {
        Matrix3f matrix = new Matrix3f(new float[][]{
                {4, 7, 2},
                {3, 5, 1},
                {2, 3, 4}
        });
        Matrix3f inverse = matrix.inverse();
        Matrix3f product = matrix.multiply(inverse);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float expected = (i == j) ? 1.0f : 0.0f;
                assertEquals(expected, product.get(i, j), 0.001f);
            }
        }
    }

    @Test
    public void testMatrix3fVectorMultiplication() {
        Matrix3f matrix = new Matrix3f(new float[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });
        Vector3f vector = new Vector3f(1, 2, 3);
        Vector3f result = matrix.multiply(vector);
        assertEquals(new Vector3f(14, 32, 50), result);
    }

    @Test
    public void testMatrix3fSystemSolving() {
        Matrix3f A = new Matrix3f(new float[][]{
                {2, 1, -1},
                {-3, -1, 2},
                {-2, 1, 2}
        });
        Vector3f b = new Vector3f(8, -11, -3);
        Vector3f x = A.solveLinearSystem(b);
        assertEquals(new Vector3f(2, 3, -1), x);
    }

    @Test
    public void testMatrix3fTranspose() {
        Matrix3f matrix = new Matrix3f(new float[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });
        Matrix3f transposed = matrix.transpose();
        Matrix3f expected = new Matrix3f(new float[][]{
                {1, 4, 7},
                {2, 5, 8},
                {3, 6, 9}
        });
        assertEquals(expected, transposed);
    }
}
