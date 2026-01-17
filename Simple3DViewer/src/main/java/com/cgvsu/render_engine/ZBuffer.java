package com.cgvsu.render_engine;

 // буфер глубины (depth buffer) для отсечения невидимых пикселей.
 // массив width*height, индексирование через (x,y).
 // Инициализация: +Infinity как "пустая" глубина.
 
public class ZBuffer {

    private final int width;
    private final int height;
    private final double[] data;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new double[width * height];
        clear();
    }

    public void clear() {
        // Инициализация: "пустая" глубина как максимально дальняя.
        final double emptyDepth = Double.POSITIVE_INFINITY;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[index(x, y)] = emptyDepth;
            }
        }
    }

    private int index(int x, int y) {
        return y * width + x;
    }

    public boolean testAndSet(int x, int y, double depth) {
        // Проверка границ
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        int idx = index(x, y);
        // Проверка глубины: меньшая depth = ближе
        if (depth < data[idx]) {
            data[idx] = depth;
            return true;
        }
        return false;
    }
}
