package com.cgvsu.render_engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LineRasterizerEdgeTest {

    @Test
    void drawLineCompletelyOutsideScreenDoesNotWritePixels() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 10;
        int h = 10;
        ZBuffer z = new ZBuffer(w, h);

        ScreenVertex a = new ScreenVertex(-100, -100, 1.0f);
        ScreenVertex b = new ScreenVertex(-50, -50, 1.0f);

        LineRasterizer.drawLine(gc, z, a, b, w, h, Color.BLACK, 1.0);

        verify(pw, never()).setColor(anyInt(), anyInt(), any());
    }

    @Test
    void drawLineDoesNotThrowOnHugeDepthBiasScale() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 5;
        int h = 5;
        ZBuffer z = new ZBuffer(w, h);

        ScreenVertex a = new ScreenVertex(0, 0, 1.0f);
        ScreenVertex b = new ScreenVertex(4, 4, 1.0f);

        LineRasterizer.drawLine(gc, z, a, b, w, h, Color.BLACK, 1e9);

        verify(pw, atLeastOnce()).setColor(anyInt(), anyInt(), eq(Color.BLACK));
    }
}
