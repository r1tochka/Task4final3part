package com.cgvsu.render_engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class TriangleRasterizerTest {

    @Test
    void fillTriangleRespectsZBufferAndDoesNotOverdrawFartherFragment() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int width = 20;
        int height = 20;
        ZBuffer z = new ZBuffer(width, height);

        ScreenVertex v0Near = new ScreenVertex(5, 5, 1.0f);
        ScreenVertex v1Near = new ScreenVertex(15, 5, 1.0f);
        ScreenVertex v2Near = new ScreenVertex(5, 15, 1.0f);

        ScreenVertex v0Far = new ScreenVertex(5, 5, 10.0f);
        ScreenVertex v1Far = new ScreenVertex(15, 5, 10.0f);
        ScreenVertex v2Far = new ScreenVertex(5, 15, 10.0f);

        TriangleRasterizer.fillTriangle(gc, z, v0Near, v1Near, v2Near, width, height, Color.RED);
        reset(pw);

        TriangleRasterizer.fillTriangle(gc, z, v0Far, v1Far, v2Far, width, height, Color.BLUE);

        verify(pw, never()).setColor(anyInt(), anyInt(), eq(Color.BLUE));
    }

    @Test
    void fillTriangleDegenerateDoesNothing() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int width = 10;
        int height = 10;
        ZBuffer z = new ZBuffer(width, height);

        ScreenVertex v0 = new ScreenVertex(1, 1, 1.0f);
        ScreenVertex v1 = new ScreenVertex(2, 2, 1.0f);
        ScreenVertex v2 = new ScreenVertex(3, 3, 1.0f);

        TriangleRasterizer.fillTriangle(gc, z, v0, v1, v2, width, height, Color.RED);

        verify(pw, never()).setColor(anyInt(), anyInt(), any());
    }
}
