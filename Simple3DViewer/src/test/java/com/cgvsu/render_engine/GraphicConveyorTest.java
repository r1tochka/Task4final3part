package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Point2f;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GraphicConveyorTest {

    @Test
    void lookAtDoesNotContainNaNWhenEyeEqualsTarget() {
        Matrix4f view = GraphicConveyor.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                assertTrue(Float.isFinite(view.get(r, c)));
            }
        }
    }

    @Test
    void perspectiveHasExpectedFixedCells() {
        Matrix4f p = GraphicConveyor.perspective(1.0f, 1.0f, 0.1f, 100f);
        assertEquals(-1.0f, p.get(3, 2), 1e-6);
        assertEquals(0.0f, p.get(3, 3), 1e-6);
    }

    @Test
    void multiplyMatrix4ByVector3DividesByWWhenPossible() {
        Matrix4f identity = Matrix4f.identity();
        Vector3f v = new Vector3f(1, 2, 3);
        Vector3f out = GraphicConveyor.multiplyMatrix4ByVector3(identity, v);
        assertEquals(1.0f, out.getX(), 1e-6);
        assertEquals(2.0f, out.getY(), 1e-6);
        assertEquals(3.0f, out.getZ(), 1e-6);
    }

    @Test
    void vertexToPointMapsNdcToScreen() {
        Point2f p00 = GraphicConveyor.vertexToPoint(new Vector3f(-1, 1, 0), 10, 10);
        assertEquals(0.0f, p00.getX(), 1e-6);
        assertEquals(0.0f, p00.getY(), 1e-6);

        Point2f p11 = GraphicConveyor.vertexToPoint(new Vector3f(1, -1, 0), 10, 10);
        assertEquals(9.0f, p11.getX(), 1e-6);
        assertEquals(9.0f, p11.getY(), 1e-6);
    }
}
