package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class RenderEngineAfterModelDeletionTest {

    private static Model makeQuadAsTwoTriangles() {
        Model m = new Model();
        m.addVertex(new Vector3f(0, 0, 0)); // 0
        m.addVertex(new Vector3f(1, 0, 0)); // 1
        m.addVertex(new Vector3f(1, 1, 0)); // 2
        m.addVertex(new Vector3f(0, 1, 0)); // 3

        Polygon t0 = new Polygon();
        t0.setVertexIndices(new ArrayList<>(List.of(0, 1, 2)));
        m.addPolygon(t0);

        Polygon t1 = new Polygon();
        t1.setVertexIndices(new ArrayList<>(List.of(0, 2, 3)));
        m.addPolygon(t1);

        return m;
    }

    @Test
    void renderDoesNotThrowAfterVertexDeletionRemovesSomePolygons() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 64;
        int h = 64;
        Camera cam = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 1.0f, (float) w / h, 0.1f, 100f);

        Model m = makeQuadAsTwoTriangles();
        m.removeVertex(1);

        assertDoesNotThrow(() -> RenderEngine.render(gc, cam, m, w, h, null, null, Color.LIGHTGRAY, null, new RenderingModes()));
    }

    @Test
    void renderDoesNotThrowWhenModelHasNoPolygons() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 32;
        int h = 32;
        Camera cam = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 1.0f, (float) w / h, 0.1f, 100f);

        Model m = new Model();
        m.addVertex(new Vector3f(0, 0, 0));
        m.addVertex(new Vector3f(1, 0, 0));
        m.addVertex(new Vector3f(0, 1, 0));

        assertDoesNotThrow(() -> RenderEngine.render(gc, cam, m, w, h, null, null, Color.LIGHTGRAY, null, new RenderingModes()));
    }
}
