package com.cgvsu.model;

// NOTE: Назначение: подготовка модели к рендеру.
// NOTE: Операции: генерация UV (при отсутствии), триангуляция, пересчёт нормалей.
// NOTE: Выход: изменение списков полигонов/нормалей внутри Model.

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class ModelProcessor {

    private ModelProcessor(){}

    public static void preprocess(Model model) {
        // Подготовка UV: генерация координат при отсутствии данных в модели.
        if (model != null && (model.getTextureVertices() == null || model.getTextureVertices().isEmpty())) {
            generatePlanarTextureCoordinates(model);
        }

        // Триангуляция: преобразование полигонов (N-угольники -> треугольники).
        triangulate(model);

        // Пересчёт нормалей: нормали граней/вершин для освещения.
        recalculateNormals(model);
    }

    private static void generatePlanarTextureCoordinates(final Model model) {
        // Проверка входных данных: наличие вершин.
        if (model == null || model.getVertices() == null || model.getVertices().isEmpty()) {
            return;
        }

        // Подсчёт границ (min/max) по X/Z: развёртка в UV по плоскости XZ.
        float minX = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (Vector3f v : model.getVertices()) {
            float x = v.getX();
            float z = v.getZ();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }

        float dx = maxX - minX;
        float dz = maxZ - minZ;
        if (Math.abs(dx) < 1e-6f) {
            dx = 1.0f;
        }
        if (Math.abs(dz) < 1e-6f) {
            dz = 1.0f;
        }

        // Запись UV: нормализация координат в диапазон [0..1].
        model.clearTextureVertices();
        for (Vector3f v : model.getVertices()) {
            float u = (v.getX() - minX) / dx;
            float vCoord = (v.getZ() - minZ) / dz;
            model.addTextureVertex(new Vector2f(u, vCoord));
        }

        if (model.getPolygons() == null) {
            return;
        }
        // Привязка индексов UV: соответствие vertexIndices -> textureVertexIndices.
        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vIdx = polygon.getVertexIndices();
            if (vIdx == null || vIdx.isEmpty()) {
                continue;
            }
            polygon.setTextureVertexIndices(new ArrayList<>(vIdx));
        }
    }

    public static void triangulate(Model model) {
        // Источник: исходные полигоны модели.
        List<Polygon> originalPolygons = model.getPolygons();
        List<Polygon> triangulated = new ArrayList<>(originalPolygons.size());

        for (Polygon polygon : originalPolygons) {
            List<Integer> v = polygon.getVertexIndices();
            List<Integer> t = polygon.getTextureVertexIndices();

            int vertexCount = v.size();
            if (vertexCount <= 3) {
                triangulated.add(polygon);
                continue;
            }

            // Разбиение fan-методом: (0, i, i+1).
            for (int i = 1; i < vertexCount - 1; ++i) {
                Polygon triangle = new Polygon();

                ArrayList<Integer> triV = new ArrayList<>(3);
                triV.add(v.get(0));
                triV.add(v.get(i));
                triV.add(v.get(i + 1));
                triangle.setVertexIndices(triV);

                if (!t.isEmpty()) {
                    ArrayList<Integer> triT = new ArrayList<>(3);
                    triT.add(t.get(0));
                    triT.add(t.get(i));
                    triT.add(t.get(i + 1));
                    triangle.setTextureVertexIndices(triT);
                }

                triangle.setNormalIndices(new ArrayList<>());

                triangulated.add(triangle);
            }
        }

        // Перезапись списка полигонов: замена на триангулированный набор.
        model.clearPolygons();
        for (Polygon polygon : triangulated) {
            model.addPolygon(polygon);
        }
    }

    public static void recalculateNormals(Model model) {
        // Проверка входных данных: наличие геометрии.
        int vertexCount = model.getVertices().size();
        if (vertexCount == 0 || model.getPolygons().isEmpty()) {
            model.clearNormals();
            return;
        }

        // Накопление нормалей: суммирование нормалей граней по вершинам.
        Vector3f[] normalSums = new Vector3f[vertexCount];
        for (int i = 0; i < vertexCount; ++i) {
            normalSums[i] = new Vector3f(0.0f, 0.0f, 0.0f);
        }

        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vIdx = polygon.getVertexIndices();
            if (vIdx.size() < 3) {
                continue;
            }

            int i0 = vIdx.get(0);
            int i1 = vIdx.get(1);
            int i2 = vIdx.get(2);

            Vector3f p0 = model.getVertices().get(i0);
            Vector3f p1 = model.getVertices().get(i1);
            Vector3f p2 = model.getVertices().get(i2);

            Vector3f edge1 = p1.subtract(p0);
            Vector3f edge2 = p2.subtract(p0);

            Vector3f faceNormal = edge1.cross(edge2);

            // Проверка вырожденных граней: нулевая площадь.
            if (faceNormal.length() < 1e-6f) {
                continue;
            }

            faceNormal = faceNormal.normalize();

            normalSums[i0] = normalSums[i0].add(faceNormal);
            normalSums[i1] = normalSums[i1].add(faceNormal);
            normalSums[i2] = normalSums[i2].add(faceNormal);
        }

        model.clearNormals();

        // Нормализация суммарных нормалей: получение нормали вершины.
        for (int i = 0; i < vertexCount; ++i) {
            Vector3f sum = normalSums[i];
            if (sum.length() < 1e-6f) {

                model.addNormal(new Vector3f(0.0f, 0.0f, 1.0f));
            } else {

                Vector3f n = sum.normalize();
                model.addNormal(new Vector3f(n.getX(), n.getY(), n.getZ()));
            }
        }

        // Привязка индексов нормалей: соответствие vertexIndices -> normalIndices.
        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vIdx = polygon.getVertexIndices();
            ArrayList<Integer> nIdx = new ArrayList<>(vIdx.size());
            nIdx.addAll(vIdx);
            polygon.setNormalIndices(nIdx);
        }
    }
}

