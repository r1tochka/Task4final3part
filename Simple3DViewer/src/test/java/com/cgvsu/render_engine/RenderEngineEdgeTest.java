package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class RenderEngineEdgeTest {

    private static Model makeMinimalModel() {
        Model m = new Model();
        m.addVertex(new Vector3f(0, 0, 0));
        m.addVertex(new Vector3f(1, 0, 0));
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
    void renderWithEmptyHelperCamerasListDoesNotThrow() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        Camera cam = new Camera(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 1.0f, 1.0f, 0.1f, 100f);
        Model model = makeMinimalModel();

        RenderingModes modes = new RenderingModes(true, false, false);

        assertDoesNotThrow(() -> RenderEngine.render(gc, cam, model, 32, 32, null, null, Color.LIGHTGRAY, new ArrayList<>(), modes));
    }
}
