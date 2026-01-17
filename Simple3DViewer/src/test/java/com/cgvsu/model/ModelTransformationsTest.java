package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTransformationsTest {

    @Test
    void transformedVerticesReflectTranslation() {
        Model m = new Model();
        m.addVertex(new Vector3f(1, 2, 3));

        m.translate(new Vector3f(1, 0, 0));

        List<Vector3f> tv = m.getTransformedVertices();
        assertEquals(1, tv.size());
        assertEquals(2.0f, tv.get(0).getX(), 1e-6);
        assertEquals(2.0f, tv.get(0).getY(), 1e-6);
        assertEquals(3.0f, tv.get(0).getZ(), 1e-6);
    }

    @Test
    void transformedVerticesReflectScale() {
        Model m = new Model();
        m.addVertex(new Vector3f(1, 2, 3));

        m.scale(new Vector3f(2, 3, 4));

        List<Vector3f> tv = m.getTransformedVertices();
        assertEquals(1, tv.size());
        assertEquals(2.0f, tv.get(0).getX(), 1e-6);
        assertEquals(6.0f, tv.get(0).getY(), 1e-6);
        assertEquals(12.0f, tv.get(0).getZ(), 1e-6);
    }

    @Test
    void resetToOriginalRestoresVerticesAndResetsTransforms() {
        Model m = new Model();
        m.addVertex(new Vector3f(1, 0, 0));
        m.addVertex(new Vector3f(0, 1, 0));

        m.setOriginalVertices(m.getVertices());

        m.translateX(10);
        m.removeVertex(0);

        assertEquals(1, m.getVertices().size());

        m.resetToOriginal();

        assertEquals(2, m.getVertices().size());
        assertEquals(0.0f, m.getTranslation().getX(), 1e-6);
        assertEquals(0.0f, m.getTranslation().getY(), 1e-6);
        assertEquals(0.0f, m.getTranslation().getZ(), 1e-6);
        assertEquals(1.0f, m.getScale().getX(), 1e-6);
        assertEquals(1.0f, m.getScale().getY(), 1e-6);
        assertEquals(1.0f, m.getScale().getZ(), 1e-6);
    }
}
