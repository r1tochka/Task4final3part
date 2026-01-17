package com.cgvsu.render_engine;


import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Arrays;
import javafx.scene.image.PixelFormat;
import java.nio.IntBuffer;
import javafx.scene.image.WritablePixelFormat;

public class Texture {
    private final Image baseImage;
    private final PixelReader baseReader;
    private final WritableImage paintLayer;
    private final PixelReader paintReader;
    private final PixelWriter paintWriter;
    private final WritableImage compositeImage;
    private final PixelReader compositeReader;
    private final PixelWriter compositeWriter;

    private final int width;
    private final int height;

    public Texture(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        this.baseImage = image;
        this.width = (int) image.getWidth();
        this.height = (int) image.getHeight();

        PixelReader reader = image.getPixelReader();
        if (reader == null) {
            throw new IllegalArgumentException("Image pixel reader cannot be null");
        }
        this.baseReader = reader;

        this.paintLayer = new WritableImage(width, height);
        this.paintReader = paintLayer.getPixelReader();
        this.paintWriter = paintLayer.getPixelWriter();

        this.compositeImage = new WritableImage(baseReader, width, height);
        this.compositeReader = compositeImage.getPixelReader();
        this.compositeWriter = compositeImage.getPixelWriter();
    }

    public Image getImage() {
        return compositeImage;
    }

    public WritableImage getWritableImage() {
        return compositeImage;
    }

    public int[] snapshotPaintLayerArgb() {
        // Снимок слоя покраски: выгрузка пикселей ARGB в массив (undo/redo)
        final int[] pixels = new int[width * height];
        final WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
        paintReader.getPixels(0, 0, width, height, format, pixels, 0, width);
        return pixels;
    }

    public void restorePaintLayerArgb(final int[] pixels) {
        // Проверка входных данных: длина массива должна совпадать с width*height
        if (pixels == null || pixels.length != width * height) {
            throw new IllegalArgumentException("Invalid paint layer snapshot");
        }
        final WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
        paintWriter.setPixels(0, 0, width, height, format, pixels, 0, width);
        rebuildCompositeFromPaint();
    }

    public void clearPaintLayer() {
        final int[] rowTransparent = new int[width];
        Arrays.fill(rowTransparent, 0x00000000);

        final WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
        for (int y = 0; y < height; y++) {
            paintWriter.setPixels(0, y, width, 1, format, rowTransparent, 0, width);
        }

        copyBaseToComposite();
    }

    private void copyBaseToComposite() {
        final int[] row = new int[width];
        final WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
        for (int y = 0; y < height; y++) {
            baseReader.getPixels(0, y, width, 1, format, row, 0, width);
            compositeWriter.setPixels(0, y, width, 1, format, row, 0, width);
        }
    }

    private void rebuildCompositeFromPaint() {
        final WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
        final int[] baseRow = new int[width];
        final int[] paintRow = new int[width];
        final int[] outRow = new int[width];

        for (int y = 0; y < height; y++) {
            baseReader.getPixels(0, y, width, 1, format, baseRow, 0, width);
            paintReader.getPixels(0, y, width, 1, format, paintRow, 0, width);
            for (int x = 0; x < width; x++) {
                outRow[x] = blendArgbOver(baseRow[x], paintRow[x]);
            }
            compositeWriter.setPixels(0, y, width, 1, format, outRow, 0, width);
        }
    }

    public void stampCircle(final int centerX, final int centerY, final int radius, final Color color) {
        if (color == null) {
            return;
        }
        final int r2 = radius * radius;
        for (int dy = -radius; dy <= radius; dy++) {
            int y = centerY + dy;
            if (y < 0 || y >= height) {
                continue;
            }
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy > r2) {
                    continue;
                }
                int x = centerX + dx;
                if (x < 0 || x >= width) {
                    continue;
                }
                applyPaintPixel(x, y, color);
            }
        }
    }

    public void drawStrokeSegment(
            final int x0,
            final int y0,
            final int x1,
            final int y1,
            final int radius,
            final Color color
    ) {
        if (color == null) {
            return;
        }
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dist = Math.hypot(dx, dy);
        if (dist < 1e-6) {
            stampCircle(x0, y0, radius, color);
            return;
        }
        double step = Math.max(1.0, radius * 0.5);
        int steps = (int) Math.ceil(dist / step);
        for (int i = 0; i <= steps; i++) {
            double t = (steps == 0) ? 0.0 : (double) i / (double) steps;
            int x = (int) Math.round(x0 + dx * t);
            int y = (int) Math.round(y0 + dy * t);
            stampCircle(x, y, radius, color);
        }
    }

    public Color sample(float u, float v) {
        u = Math.max(0.0f, Math.min(1.0f, u));
        v = Math.max(0.0f, Math.min(1.0f, v));

        int x = (int) (u * (width - 1));
        int y = (int) ((1.0f - v) * (height - 1));

        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));

        return compositeReader.getColor(x, y);
    }

    public boolean isLoaded() {
        return baseImage != null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void applyPaintPixel(final int x, final int y, final Color brushColor) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        Color prevOverlay = paintReader.getColor(x, y);
        Color newOverlay = alphaOver(prevOverlay, brushColor);
        paintWriter.setColor(x, y, newOverlay);
        Color base = baseReader.getColor(x, y);
        Color out = alphaOver(base, newOverlay);
        compositeWriter.setColor(x, y, out);
    }

    private static Color alphaOver(final Color dst, final Color src) {
        double sa = src.getOpacity();
        double da = dst.getOpacity();

        double outA = sa + da * (1.0 - sa);
        if (outA <= 1e-9) {
            return Color.color(0, 0, 0, 0);
        }

        double outR = (src.getRed() * sa + dst.getRed() * da * (1.0 - sa)) / outA;
        double outG = (src.getGreen() * sa + dst.getGreen() * da * (1.0 - sa)) / outA;
        double outB = (src.getBlue() * sa + dst.getBlue() * da * (1.0 - sa)) / outA;
        return new Color(clamp01(outR), clamp01(outG), clamp01(outB), clamp01(outA));
    }

    private static double clamp01(final double v) {
        if (v < 0.0) {
            return 0.0;
        }
        if (v > 1.0) {
            return 1.0;
        }
        return v;
    }

    private static int blendArgbOver(final int dst, final int src) {
        int sa = (src >>> 24) & 0xFF;
        if (sa == 0) {
            return dst;
        }
        if (sa == 255) {
            return src;
        }

        int da = (dst >>> 24) & 0xFF;

        double saF = sa / 255.0;
        double daF = da / 255.0;
        double outA = saF + daF * (1.0 - saF);
        if (outA <= 1e-9) {
            return 0;
        }

        int sr = (src >>> 16) & 0xFF;
        int sg = (src >>> 8) & 0xFF;
        int sb = src & 0xFF;
        int dr = (dst >>> 16) & 0xFF;
        int dg = (dst >>> 8) & 0xFF;
        int db = dst & 0xFF;

        double outR = (sr * saF + dr * daF * (1.0 - saF)) / outA;
        double outG = (sg * saF + dg * daF * (1.0 - saF)) / outA;
        double outB = (sb * saF + db * daF * (1.0 - saF)) / outA;

        int oa = (int) Math.round(outA * 255.0);
        int or = (int) Math.round(outR);
        int og = (int) Math.round(outG);
        int ob = (int) Math.round(outB);

        oa = clampByte(oa);
        or = clampByte(or);
        og = clampByte(og);
        ob = clampByte(ob);

        return (oa << 24) | (or << 16) | (og << 8) | ob;
    }

    private static int clampByte(int v) {
        if (v < 0) {
            return 0;
        }
        if (v > 255) {
            return 255;
        }
        return v;
    }
}

