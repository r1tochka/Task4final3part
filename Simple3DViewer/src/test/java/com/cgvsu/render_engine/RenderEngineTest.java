package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RenderEngineTest {

    private static Model makeSingleTriangleModel() {
        Model m = new Model();
        m.addVertex(new Vector3f(-1, -1, 0));
        m.addVertex(new Vector3f(1, -1, 0));
        m.addVertex(new Vector3f(0, 1, 0));

        Polygon p = new Polygon();
        ArrayList<Integer> idx = new ArrayList<>();
        idx.add(0);
        idx.add(1);
        idx.add(2);
        p.setVertexIndices(idx);
        m.addPolygon(p);

        return m;
    }

    @Test
    void renderDoesNotThrowOnMinimalModel() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 32;
        int h = 32;

        Camera cam = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 1.0f, (float) w / h, 0.1f, 100f);
        Model model = makeSingleTriangleModel();

        assertDoesNotThrow(() -> RenderEngine.render(gc, cam, model, w, h, null, null, Color.LIGHTGRAY, null, new RenderingModes()));
    }

    @Test
    void renderWritesAtLeastOnePixelForVisibleTriangle() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 64;
        int h = 64;

        Camera cam = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 1.0f, (float) w / h, 0.1f, 100f);
        Model model = makeSingleTriangleModel();

        RenderEngine.render(gc, cam, model, w, h, null, null, Color.RED, null, new RenderingModes());

        verify(pw, atLeastOnce()).setColor(anyInt(), anyInt(), any());
    }
}
