package com.cgvsu.render_engine;

// растеризация отрезка (каркас) с учётом ZBuffer
// Интерполяция: шаг по длине, линейная интерполяция Z

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class LineRasterizer {

    private LineRasterizer() {}

    public static void drawLine(
            GraphicsContext gc,
            ZBuffer zBuffer,
            ScreenVertex a,
            ScreenVertex b,
            int width,
            int height,
            Color color,
            double depthBiasScale
    ) {
        PixelWriter pw = gc.getPixelWriter();

        int x0 = Math.round(a.getX());
        int y0 = Math.round(a.getY());
        double z0 = a.getZ();

        int x1 = Math.round(b.getX());
        int y1 = Math.round(b.getY());
        double z1 = b.getZ();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int length = Math.max(dx, dy);
        if (length == 0) {
            if (x0 >= 0 && x0 < width && y0 >= 0 && y0 < height) {
                if (zBuffer.testAndSet(x0, y0, z0)) {
                    pw.setColor(x0, y0, color);
                }
            }
            return;
        }

        double dz = (z1 - z0) / length;
        double z = z0;

        int err = dx - dy;

        int x = x0;
        int y = y0;
        final double baseDepthBias = 1e-5;
        final double depthBias = baseDepthBias * depthBiasScale;


        for (int i = 0; i <= length; ++i) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                double zb = z - depthBias;
                if (zBuffer.testAndSet(x, y, zb)) {
                    pw.setColor(x, y, color);
                }
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }

            z += dz;
        }
    }
}

