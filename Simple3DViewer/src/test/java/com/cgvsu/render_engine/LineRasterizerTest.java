package com.cgvsu.render_engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LineRasterizerTest {

    @Test
    void drawLineSinglePointDrawsAtMostOnePixel() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        ZBuffer z = new ZBuffer(10, 10);
        ScreenVertex a = new ScreenVertex(3, 3, 1.0f);
        ScreenVertex b = new ScreenVertex(3, 3, 1.0f);

        LineRasterizer.drawLine(gc, z, a, b, 10, 10, Color.RED, 1.0);

        verify(pw, atMost(1)).setColor(eq(3), eq(3), eq(Color.RED));
    }

    @Test
    void drawLineRespectsZBufferDepthBias() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 10;
        int h = 10;
        ZBuffer z = new ZBuffer(w, h);

        ScreenVertex nearA = new ScreenVertex(0, 0, 1.0f);
        ScreenVertex nearB = new ScreenVertex(9, 9, 1.0f);
        LineRasterizer.drawLine(gc, z, nearA, nearB, w, h, Color.RED, 1.0);
        reset(pw);

        ScreenVertex farA = new ScreenVertex(0, 0, 10.0f);
        ScreenVertex farB = new ScreenVertex(9, 9, 10.0f);
        LineRasterizer.drawLine(gc, z, farA, farB, w, h, Color.BLUE, 1.0);

        verify(pw, never()).setColor(anyInt(), anyInt(), eq(Color.BLUE));
    }
}
