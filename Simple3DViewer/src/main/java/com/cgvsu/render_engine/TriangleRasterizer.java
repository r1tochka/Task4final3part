package com.cgvsu.render_engine;

 // растеризация треугольников в screen-space
 // ограничение области: прямоугольник охвата (min/max X/Y)
 // интерполяция:  барицентрические координаты + глубина (Z-buffer)
 // nекстурирование: перспективная коррекция через invW.
 // jсвещение: интерполяция интенсивности или подсчёт по нормали/позиции.
 
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class TriangleRasterizer {

    private TriangleRasterizer() {}

    public static void fillTriangle(
            GraphicsContext gc,
            ZBuffer zBuffer,
            ScreenVertex v0,
            ScreenVertex v1,
            ScreenVertex v2,
            int width,
            int height,
            Color color
    ) {
        PixelWriter pw = gc.getPixelWriter();

        float x0 = v0.getX();
        float y0 = v0.getY();
        double z0 = v0.getZ();

        float x1 = v1.getX();
        float y1 = v1.getY();
        double z1 = v1.getZ();

        float x2 = v2.getX();
        float y2 = v2.getY();
        double z2 = v2.getZ();

        // Ограничение области: прямоугольник охвата по screen-space координатам
        int minX = clamp((int) Math.max(0, Math.floor(Math.min(x0, Math.min(x1, x2)))), 0, width - 1);
        int maxX = clamp((int) Math.min(width - 1, Math.ceil(Math.max(x0, Math.max(x1, x2)))), 0, width - 1);
        int minY = clamp((int) Math.max(0, Math.floor(Math.min(y0, Math.min(y1, y2)))), 0, height - 1);
        int maxY = clamp((int) Math.min(height - 1, Math.ceil(Math.max(y0, Math.max(y1, y2)))), 0, height - 1);

        // Подсчёт барицентрических координат
        double denom = (double) ((y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2));
        if (Math.abs(denom) < 1e-12) {
            return;
        }

        // проверка принадлежности треугольнику через alpha/beta/gamma
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                double alpha = ((y1 - y2) * (x - x2) + (x2 - x1) * (y - y2)) / denom;
                double beta = ((y2 - y0) * (x - x2) + (x0 - x2) * (y - y2)) / denom;
                double gamma = 1.0 - alpha - beta;

                if (alpha >= 0 && beta >= 0 && gamma >= 0) {
                    // Интерполяция глубины
                    double depth = alpha * z0 + beta * z1 + gamma * z2;

                    // Проверка глубины
                    if (zBuffer.testAndSet(x, y, depth)) {
                        pw.setColor(x, y, color);
                    }
                }
            }
        }
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static void fillTriangle(
            GraphicsContext gc,
            ZBuffer zBuffer,
            ScreenVertex v0,
            ScreenVertex v1,
            ScreenVertex v2,
            int width,
            int height,
            Texture texture,
            Lighting lighting,
            Color baseColor,
            Vector3f cameraPosition
    ) {
        PixelWriter pw = gc.getPixelWriter();

        float x0 = v0.getX();
        float y0 = v0.getY();
        double z0 = v0.getZ();
        float invW0 = v0.getInvW();

        float x1 = v1.getX();
        float y1 = v1.getY();
        double z1 = v1.getZ();
        float invW1 = v1.getInvW();

        float x2 = v2.getX();
        float y2 = v2.getY();
        double z2 = v2.getZ();
        float invW2 = v2.getInvW();

        // Ограничение области
        int minX = (int) Math.max(0, Math.floor(Math.min(x0, Math.min(x1, x2))));
        int maxX = (int) Math.min(width - 1, Math.ceil(Math.max(x0, Math.max(x1, x2))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y0, Math.min(y1, y2))));
        int maxY = (int) Math.min(height - 1, Math.ceil(Math.max(y0, Math.max(y1, y2))));

        double denom = (double) ((y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2));
        if (Math.abs(denom) < 1e-12) {
            return;
        }

        Vector2f tex0 = v0.getTextureCoords();
        Vector2f tex1 = v1.getTextureCoords();
        Vector2f tex2 = v2.getTextureCoords();

        Vector3f normal0 = v0.getNormal();
        Vector3f normal1 = v1.getNormal();
        Vector3f normal2 = v2.getNormal();

        Vector3f worldPos0 = v0.getWorldPosition();
        Vector3f worldPos1 = v1.getWorldPosition();
        Vector3f worldPos2 = v2.getWorldPosition();

        Float light0 = v0.getLightingIntensity();
        Float light1 = v1.getLightingIntensity();
        Float light2 = v2.getLightingIntensity();

        boolean hasTexture = texture != null && tex0 != null && tex1 != null && tex2 != null;
        boolean hasPrecomputedLighting = lighting != null && light0 != null && light1 != null && light2 != null;
        boolean hasLighting = !hasPrecomputedLighting && lighting != null && normal0 != null && normal1 != null && normal2 != null
                && worldPos0 != null && worldPos1 != null && worldPos2 != null;

        // Перебор пикселей bbox + ветвление: базовый цвет / текстура / освещение
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                double alpha = ((y1 - y2) * (x - x2) + (x2 - x1) * (y - y2)) / denom;
                double beta = ((y2 - y0) * (x - x2) + (x0 - x2) * (y - y2)) / denom;
                double gamma = 1.0 - alpha - beta;

                if (alpha >= 0 && beta >= 0 && gamma >= 0) {
                    double depth = alpha * z0 + beta * z1 + gamma * z2;

                    if (zBuffer.testAndSet(x, y, depth)) {
                        Color pixelColor = baseColor;

                        if (hasTexture) {
                            // Перспективная коррекция UV: интерполяция u/w и v/w через invW
                            double invW = alpha * invW0 + beta * invW1 + gamma * invW2;
                            if (Math.abs(invW) > 1e-12) {
                                double w = 1.0 / invW;
                                double uOverW =
                                        alpha * tex0.getX() * invW0 +
                                                beta * tex1.getX() * invW1 +
                                                gamma * tex2.getX() * invW2;
                                double vOverW =
                                        alpha * tex0.getY() * invW0 +
                                                beta * tex1.getY() * invW1 +
                                                gamma * tex2.getY() * invW2;

                                float u = (float) (uOverW * w);
                                float v = (float) (vOverW * w);
                                pixelColor = texture.sample(u, v);
                            }
                        }

                        if (hasPrecomputedLighting) {
                            // Освещение: интерполяция заранее подсчитанной интенсивности
                            float intensity = (float) (alpha * light0 + beta * light1 + gamma * light2);
                            pixelColor = lighting.shadeColor(pixelColor, intensity);
                        } else if (hasLighting) {
                            // Освещение: интерполяция normal/worldPos + подсчёт интенсивности на пикселе
                            Vector3f interpolatedNormal = normal0.multiply((float) alpha)
                                    .add(normal1.multiply((float) beta))
                                    .add(normal2.multiply((float) gamma))
                                    .normalize();

                            Vector3f interpolatedWorldPos = worldPos0.multiply((float) alpha)
                                    .add(worldPos1.multiply((float) beta))
                                    .add(worldPos2.multiply((float) gamma));

                            float intensity = lighting.computeLightingIntensity(
                                    interpolatedNormal,
                                    interpolatedWorldPos,
                                    cameraPosition
                            );

                            pixelColor = lighting.shadeColor(pixelColor, intensity);
                        }

                        pw.setColor(x, y, pixelColor);
                    }
                }
            }
        }
    }

}

