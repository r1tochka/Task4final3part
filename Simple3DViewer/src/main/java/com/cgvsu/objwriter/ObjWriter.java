package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ObjWriter {

    private ObjWriter() {
    }

    public static void write(final String fileName, final Model model) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            writeVertices(writer, model.getVertices());
            writer.write("\n");

            // OBJ expects vt/vn indices even if we write them as 1:1 with vertices.
            // If model already has texture vertices, we write them; otherwise generate simple planar UV.
            List<Vector2f> textureCoords = model.getTextureVertices();
            if (textureCoords == null || textureCoords.isEmpty()) {
                textureCoords = generateTextureCoordinates(model.getVertices());
            }
            writeTextureVertices(writer, textureCoords);
            writer.write("\n");

            // If model already has normals, write them; otherwise compute per-vertex normals from polygons.
            List<Vector3f> normals = model.getNormals();
            if (normals == null || normals.isEmpty()) {
                normals = calculateNormals(model.getVertices(), model.getPolygons());
            }
            writeNormals(writer, normals);
            writer.write("\n");

            writePolygons(writer, model.getPolygons(), model.getTextureVertices(), model.getNormals());
        }
    }

    private static void writeVertices(final FileWriter writer, final List<Vector3f> vertices) throws IOException {
        writer.write("# Vertices\n");
        for (Vector3f v : vertices) {
            writer.write(String.format(Locale.US, "v %.6f %.6f %.6f\n", v.getX(), v.getY(), v.getZ()));
        }
    }

    private static void writeTextureVertices(final FileWriter writer, final List<Vector2f> textureCoords) throws IOException {
        writer.write("# Texture coordinates\n");
        for (Vector2f t : textureCoords) {
            writer.write(String.format(Locale.US, "vt %.6f %.6f\n", t.getX(), t.getY()));
        }
    }

    private static void writeNormals(final FileWriter writer, final List<Vector3f> normals) throws IOException {
        writer.write("# Normals\n");
        for (Vector3f n : normals) {
            writer.write(String.format(Locale.US, "vn %.6f %.6f %.6f\n", n.getX(), n.getY(), n.getZ()));
        }
    }

    private static void writePolygons(
            final FileWriter writer,
            final List<Polygon> polygons,
            final List<Vector2f> textureVertices,
            final List<Vector3f> normals) throws IOException {

        writer.write("# Polygons\n");

        final boolean hasTexture = textureVertices != null && !textureVertices.isEmpty();
        final boolean hasNormals = normals != null && !normals.isEmpty();

        for (Polygon polygon : polygons) {
            List<Integer> vIdx = polygon.getVertexIndices();
            if (vIdx == null || vIdx.size() < 3) {
                continue;
            }

            List<Integer> tIdx = polygon.getTextureVertexIndices();
            List<Integer> nIdx = polygon.getNormalIndices();

            final boolean polyHasTexture = hasTexture && tIdx != null && !tIdx.isEmpty() && tIdx.size() == vIdx.size();
            final boolean polyHasNormals = hasNormals && nIdx != null && !nIdx.isEmpty() && nIdx.size() == vIdx.size();

            writer.write("f");
            for (int i = 0; i < vIdx.size(); i++) {
                int v = vIdx.get(i) + 1;
                if (polyHasTexture && polyHasNormals) {
                    int vt = tIdx.get(i) + 1;
                    int vn = nIdx.get(i) + 1;
                    writer.write(String.format(Locale.US, " %d/%d/%d", v, vt, vn));
                } else if (polyHasTexture) {
                    int vt = tIdx.get(i) + 1;
                    writer.write(String.format(Locale.US, " %d/%d", v, vt));
                } else if (polyHasNormals) {
                    int vn = nIdx.get(i) + 1;
                    writer.write(String.format(Locale.US, " %d//%d", v, vn));
                } else {
                    writer.write(String.format(Locale.US, " %d", v));
                }
            }
            writer.write("\n");
        }
    }

    private static List<Vector2f> generateTextureCoordinates(final List<Vector3f> vertices) {
        List<Vector2f> textureCoords = new ArrayList<>();
        if (vertices == null || vertices.isEmpty()) {
            return textureCoords;
        }

        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (Vector3f v : vertices) {
            minX = Math.min(minX, v.getX());
            maxX = Math.max(maxX, v.getX());
            minY = Math.min(minY, v.getY());
            maxY = Math.max(maxY, v.getY());
        }

        float rangeX = maxX - minX;
        float rangeY = maxY - minY;
        if (Math.abs(rangeX) < 1e-6f) {
            rangeX = 1.0f;
        }
        if (Math.abs(rangeY) < 1e-6f) {
            rangeY = 1.0f;
        }

        for (Vector3f v : vertices) {
            float u = (v.getX() - minX) / rangeX;
            float vv = (v.getY() - minY) / rangeY;
            textureCoords.add(new Vector2f(u, vv));
        }

        return textureCoords;
    }

    private static List<Vector3f> calculateNormals(final List<Vector3f> vertices, final List<Polygon> polygons) {
        List<Vector3f> normalSums = new ArrayList<>();
        if (vertices == null) {
            return normalSums;
        }
        for (int i = 0; i < vertices.size(); i++) {
            normalSums.add(new Vector3f(0, 0, 0));
        }
        if (polygons == null) {
            return normalSums;
        }

        for (Polygon polygon : polygons) {
            List<Integer> vIdx = polygon.getVertexIndices();
            if (vIdx == null || vIdx.size() < 3) {
                continue;
            }

            Vector3f v0 = vertices.get(vIdx.get(0));
            Vector3f v1 = vertices.get(vIdx.get(1));
            Vector3f v2 = vertices.get(vIdx.get(2));

            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);

            Vector3f faceNormal = edge1.cross(edge2);
            if (faceNormal.length() < 1e-6f) {
                continue;
            }
            faceNormal = faceNormal.normalize();

            for (int idx : vIdx) {
                Vector3f current = normalSums.get(idx);
                normalSums.set(idx, current.add(faceNormal));
            }
        }

        List<Vector3f> normalized = new ArrayList<>(normalSums.size());
        for (Vector3f n : normalSums) {
            if (n.length() < 1e-6f) {
                normalized.add(new Vector3f(0, 0, 1));
            } else {
                normalized.add(n.normalize());
            }
        }
        return normalized;
    }
}
