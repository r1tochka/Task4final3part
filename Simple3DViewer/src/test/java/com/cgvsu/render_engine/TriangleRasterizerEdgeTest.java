package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TriangleRasterizerEdgeTest {

    @Test
    void fillTriangleOffscreenDoesNotWritePixels() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 10;
        int h = 10;
        ZBuffer z = new ZBuffer(w, h);

        ScreenVertex v0 = new ScreenVertex(-100, -100, 1.0f);
        ScreenVertex v1 = new ScreenVertex(-90, -100, 1.0f);
        ScreenVertex v2 = new ScreenVertex(-95, -90, 1.0f);

        TriangleRasterizer.fillTriangle(gc, z, v0, v1, v2, w, h, Color.RED);

        verify(pw, never()).setColor(anyInt(), anyInt(), any());
    }

    @Test
    void fillTriangleWithTextureButMissingUvFallsBackToBaseColor() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 20;
        int h = 20;
        ZBuffer z = new ZBuffer(w, h);

        ScreenVertex v0 = new ScreenVertex(5, 5, 1.0f, 1.0f, null, null, null, null);
        ScreenVertex v1 = new ScreenVertex(15, 5, 1.0f, 1.0f, null, null, null, null);
        ScreenVertex v2 = new ScreenVertex(5, 15, 1.0f, 1.0f, null, null, null, null);

        Texture texture = mock(Texture.class);
        Lighting lighting = null;

        TriangleRasterizer.fillTriangle(gc, z, v0, v1, v2, w, h, texture, lighting, Color.GREEN, new Vector3f(0, 0, 5));

        verify(pw, atLeastOnce()).setColor(anyInt(), anyInt(), eq(Color.GREEN));
        verify(texture, never()).sample(anyFloat(), anyFloat());
    }

    @Test
    void fillTriangleWithTextureInvWZeroDoesNotSampleTexture() {
        GraphicsContext gc = mock(GraphicsContext.class);
        PixelWriter pw = mock(PixelWriter.class);
        when(gc.getPixelWriter()).thenReturn(pw);

        int w = 20;
        int h = 20;
        ZBuffer z = new ZBuffer(w, h);

        Vector2f uv = new Vector2f(0.5f, 0.5f);
        ScreenVertex v0 = new ScreenVertex(5, 5, 1.0f, 0.0f, uv, null, null, null);
        ScreenVertex v1 = new ScreenVertex(15, 5, 1.0f, 0.0f, uv, null, null, null);
        ScreenVertex v2 = new ScreenVertex(5, 15, 1.0f, 0.0f, uv, null, null, null);

        Texture texture = mock(Texture.class);

        TriangleRasterizer.fillTriangle(gc, z, v0, v1, v2, w, h, texture, null, Color.BLUE, new Vector3f(0, 0, 5));

        verify(texture, never()).sample(anyFloat(), anyFloat());
        verify(pw, atLeastOnce()).setColor(anyInt(), anyInt(), eq(Color.BLUE));
    }
}
