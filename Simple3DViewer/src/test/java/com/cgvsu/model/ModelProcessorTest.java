package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelProcessorTest {

    @Test
    void triangulateSplitsQuadIntoTwoTrianglesAndPreservesTextureIndices() {
        Model model = new Model();
        model.addVertex(new Vector3f(0, 0, 0));
        model.addVertex(new Vector3f(1, 0, 0));
        model.addVertex(new Vector3f(1, 1, 0));
        model.addVertex(new Vector3f(0, 1, 0));

        model.addTextureVertex(new Vector2f(0, 0));
        model.addTextureVertex(new Vector2f(1, 0));
        model.addTextureVertex(new Vector2f(1, 1));
        model.addTextureVertex(new Vector2f(0, 1));

        Polygon quad = new Polygon();
        quad.setVertexIndices(new ArrayList<>(List.of(0, 1, 2, 3)));
        quad.setTextureVertexIndices(new ArrayList<>(List.of(0, 1, 2, 3)));
        model.addPolygon(quad);

        ModelProcessor.triangulate(model);

        assertEquals(2, model.getPolygons().size());

        Polygon t0 = model.getPolygons().get(0);
        assertEquals(List.of(0, 1, 2), t0.getVertexIndices());
        assertEquals(List.of(0, 1, 2), t0.getTextureVertexIndices());
        assertNotNull(t0.getNormalIndices());
        assertTrue(t0.getNormalIndices().isEmpty());

        Polygon t1 = model.getPolygons().get(1);
        assertEquals(List.of(0, 2, 3), t1.getVertexIndices());
        assertEquals(List.of(0, 2, 3), t1.getTextureVertexIndices());
        assertNotNull(t1.getNormalIndices());
        assertTrue(t1.getNormalIndices().isEmpty());
    }

    @Test
    void preprocessTriangulatesAndRecalculatesNormals() {
        Model model = new Model();
        model.addVertex(new Vector3f(0, 0, 0));
        model.addVertex(new Vector3f(1, 0, 0));
        model.addVertex(new Vector3f(1, 1, 0));
        model.addVertex(new Vector3f(0, 1, 0));

        Polygon quad = new Polygon();
        quad.setVertexIndices(new ArrayList<>(List.of(0, 1, 2, 3)));
        model.addPolygon(quad);

        assertTrue(model.getNormals().isEmpty());

        ModelProcessor.preprocess(model);

        assertEquals(2, model.getPolygons().size(), "Preprocess should triangulate polygons");
        assertEquals(4, model.getNormals().size(), "Preprocess should recalculate per-vertex normals");
        for (Vector3f n : model.getNormals()) {
            assertNotNull(n);
            assertEquals(1.0f, n.length(), 1e-5f);
        }

        for (Polygon p : model.getPolygons()) {
            assertEquals(p.getVertexIndices().size(), p.getNormalIndices().size(), "Normal indices must match vertex indices");
        }
    }
}
