package com.cgvsu.model;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModelDeletionTest {

    private static Model makeTwoTriangleQuadModel() {
        Model m = new Model();

        m.addVertex(new Vector3f(0, 0, 0)); // 0
        m.addVertex(new Vector3f(1, 0, 0)); // 1
        m.addVertex(new Vector3f(1, 1, 0)); // 2
        m.addVertex(new Vector3f(0, 1, 0)); // 3

        m.addTextureVertex(new Vector2f(0, 0)); // 0
        m.addTextureVertex(new Vector2f(1, 0)); // 1
        m.addTextureVertex(new Vector2f(1, 1)); // 2
        m.addTextureVertex(new Vector2f(0, 1)); // 3

        m.addNormal(new Vector3f(0, 0, 1)); // 0
        m.addNormal(new Vector3f(0, 0, 1)); // 1
        m.addNormal(new Vector3f(0, 0, 1)); // 2
        m.addNormal(new Vector3f(0, 0, 1)); // 3

        Polygon p0 = new Polygon();
        p0.setVertexIndices(new ArrayList<>(List.of(0, 1, 2)));
        p0.setTextureVertexIndices(new ArrayList<>(List.of(0, 1, 2)));
        p0.setNormalIndices(new ArrayList<>(List.of(0, 1, 2)));
        m.addPolygon(p0);

        Polygon p1 = new Polygon();
        p1.setVertexIndices(new ArrayList<>(List.of(0, 2, 3)));
        p1.setTextureVertexIndices(new ArrayList<>(List.of(0, 2, 3)));
        p1.setNormalIndices(new ArrayList<>(List.of(0, 2, 3)));
        m.addPolygon(p1);

        return m;
    }

    @Test
    void removePolygonRemovesOnlyThatPolygon() {
        Model m = makeTwoTriangleQuadModel();
        assertEquals(2, m.getPolygons().size());

        m.removePolygon(0);
        assertEquals(1, m.getPolygons().size());
        assertEquals(List.of(0, 2, 3), m.getPolygons().get(0).getVertexIndices());
    }

    @Test
    void removePolygonThrowsOnOutOfBounds() {
        Model m = makeTwoTriangleQuadModel();
        assertThrows(IndexOutOfBoundsException.class, () -> m.removePolygon(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> m.removePolygon(2));
    }

    @Test
    void removeVertexUpdatesPolygonIndicesAndRemovesDegeneratePolygons() {
        Model m = makeTwoTriangleQuadModel();
        assertEquals(4, m.getVertices().size());
        assertEquals(2, m.getPolygons().size());

        m.removeVertex(1);

        assertEquals(3, m.getVertices().size());
        assertEquals(1, m.getPolygons().size());

        Polygon remaining = m.getPolygons().get(0);
        assertEquals(List.of(0, 1, 2), remaining.getVertexIndices());
        assertEquals(List.of(0, 1, 2), remaining.getTextureVertexIndices());
        assertEquals(List.of(0, 1, 2), remaining.getNormalIndices());

        for (Polygon p : m.getPolygons()) {
            for (int idx : p.getVertexIndices()) {
                assertTrue(idx >= 0 && idx < m.getVertices().size());
            }
        }
    }

    @Test
    void removeVertexThrowsOnOutOfBounds() {
        Model m = new Model();
        m.addVertex(new Vector3f(0, 0, 0));

        assertThrows(IndexOutOfBoundsException.class, () -> m.removeVertex(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> m.removeVertex(1));
    }

    @Test
    void removeVertexDoesNotThrowWhenNoPolygonsExist() {
        Model m = new Model();
        m.addVertex(new Vector3f(0, 0, 0));
        m.addVertex(new Vector3f(1, 0, 0));

        assertDoesNotThrow(() -> m.removeVertex(0));
        assertEquals(1, m.getVertices().size());
    }
}
