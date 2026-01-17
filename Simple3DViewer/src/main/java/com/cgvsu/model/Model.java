package com.cgvsu.model;

 // контейнер геометрии (vertices/uv/normals) и списка полигонов.
 // translation/rotation/scale
 
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Model {

    private final List<Vector3f> vertices = new ArrayList<>();
    private final List<Vector2f> textureVertices = new ArrayList<>();
    private final List<Vector3f> normals = new ArrayList<>();
    private final List<Polygon> polygons = new ArrayList<>();

    private Vector3f translation = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private Vector3f scale = new Vector3f(1, 1, 1);
    private Matrix4f modelMatrix = Matrix4f.identity();

    private final List<Vector3f> originalVertices = new ArrayList<>();
    private boolean isTransformed = false;

    public Model() {
        updateModelMatrix();
    }

    public List<Vector3f> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Vector2f> getTextureVertices() {
        return Collections.unmodifiableList(textureVertices);
    }

    public List<Vector3f> getNormals() {
        return Collections.unmodifiableList(normals);
    }

    public List<Polygon> getPolygons() {
        return Collections.unmodifiableList(polygons);
    }

    public void addVertex(Vector3f vertex) {
        this.vertices.add(vertex);
    }

    public void addTextureVertex(Vector2f textureVertex) {
        this.textureVertices.add(textureVertex);
    }

    public void addNormal(Vector3f normal) {
        this.normals.add(normal);
    }

    public void addPolygon(Polygon polygon) {
        this.polygons.add(polygon);
    }

    public void removePolygon(final int index) {
        if (index < 0 || index >= polygons.size()) {
            throw new IndexOutOfBoundsException("Polygon index out of bounds: " + index);
        }
        polygons.remove(index);
    }

    public void removeVertex(final int index) {
        if (index < 0 || index >= vertices.size()) {
            throw new IndexOutOfBoundsException("Vertex index out of bounds: " + index);
        }
        vertices.remove(index);
        updateIndicesAfterVertexRemoval(index);
    }

    private void updateIndicesAfterVertexRemoval(final int removedIndex) {
        if (polygons.isEmpty()) {
            return;
        }

        final List<Polygon> polygonsToRemove = new ArrayList<>();

        for (Polygon polygon : polygons) {
            List<Integer> vIdx = polygon.getVertexIndices();
            if (vIdx == null || vIdx.isEmpty()) {
                continue;
            }

            ArrayList<Integer> newVertexIndices = new ArrayList<>(vIdx.size());
            for (int idx : vIdx) {
                if (idx == removedIndex) {
                    continue;
                }
                if (idx > removedIndex) {
                    newVertexIndices.add(idx - 1);
                } else {
                    newVertexIndices.add(idx);
                }
            }

            if (newVertexIndices.size() < 3) {
                polygonsToRemove.add(polygon);
                continue;
            }

            polygon.setVertexIndices(newVertexIndices);

            // texture indices
            List<Integer> tIdx = polygon.getTextureVertexIndices();
            if (tIdx != null && !tIdx.isEmpty() && tIdx.size() == vIdx.size()) {
                ArrayList<Integer> newTextureIndices = new ArrayList<>(tIdx.size());
                for (int idx : tIdx) {
                    if (idx == removedIndex) {
                        continue;
                    }
                    if (idx > removedIndex) {
                        newTextureIndices.add(idx - 1);
                    } else {
                        newTextureIndices.add(idx);
                    }
                }
                if (newTextureIndices.size() == newVertexIndices.size()) {
                    polygon.setTextureVertexIndices(newTextureIndices);
                } else {
                    polygon.setTextureVertexIndices(new ArrayList<>());
                }
            }

            // normal indices
            List<Integer> nIdx = polygon.getNormalIndices();
            if (nIdx != null && !nIdx.isEmpty() && nIdx.size() == vIdx.size()) {
                ArrayList<Integer> newNormalIndices = new ArrayList<>(nIdx.size());
                for (int idx : nIdx) {
                    if (idx == removedIndex) {
                        continue;
                    }
                    if (idx > removedIndex) {
                        newNormalIndices.add(idx - 1);
                    } else {
                        newNormalIndices.add(idx);
                    }
                }
                if (newNormalIndices.size() == newVertexIndices.size()) {
                    polygon.setNormalIndices(newNormalIndices);
                } else {
                    polygon.setNormalIndices(new ArrayList<>());
                }
            }
        }

        polygons.removeAll(polygonsToRemove);
    }

    public void updateModelMatrix() {
        // Пересчёт матрицы модели: translation/rotation/scale - modelMatrix
        this.modelMatrix = Matrix4f.modelMatrix(translation, rotation, scale);
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public List<Vector3f> getTransformedVertices() {
        // Получение вершин в мировом пространстве: применение modelMatrix к каждой вершине
        List<Vector3f> transformed = new ArrayList<>();
        for (Vector3f vertex : vertices) {
            transformed.add(GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, vertex));
        }
        return transformed;
    }

    public List<Vector3f> getOriginalVertices() {
        return new ArrayList<>(originalVertices);
    }

    public void setOriginalVertices(List<Vector3f> vertices) {
        this.originalVertices.clear();
        this.originalVertices.addAll(vertices);
    }

    public void resetToOriginal() {
        // Сброс геометрии: восстановление vertices из originalVertices + сброс трансформаций.
        if (!originalVertices.isEmpty()) {
            this.vertices.clear();
            this.vertices.addAll(originalVertices);
            resetTransformations();
        }
    }

    public Vector3f getTranslation() { return translation; }
    public void setTranslation(Vector3f translation) {
        this.translation = translation;
        updateModelMatrix();
    }

    public Vector3f getRotation() { return rotation; }
    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
        updateModelMatrix();
    }

    public Vector3f getScale() { return scale; }
    public void setScale(Vector3f scale) {
        this.scale = scale;
        updateModelMatrix();
    }

    public boolean isTransformed() {
        return isTransformed;
    }

    public void setTransformed(boolean transformed) {
        isTransformed = transformed;
    }

    public void translate(Vector3f delta) {
        // Накопление переноса + пересчёт modelMatrix.
        this.translation = this.translation.add(delta);
        updateModelMatrix();
    }

    public void rotate(Vector3f delta) {
        // Накопление поворота (в радианах) + пересчёт modelMatrix.
        this.rotation = this.rotation.add(delta);
        updateModelMatrix();
    }

    public void scale(Vector3f factor) {
        // Масштабирование: покомпонентное умножение scale на factor.
        float newX = this.scale.getX() * factor.getX();
        float newY = this.scale.getY() * factor.getY();
        float newZ = this.scale.getZ() * factor.getZ();
        this.scale = new Vector3f(newX, newY, newZ);
        updateModelMatrix();
    }

    public void translateX(float delta) {
        this.translation = new Vector3f(
                this.translation.getX() + delta,
                this.translation.getY(),
                this.translation.getZ()
        );
        updateModelMatrix();
    }

    public void translateY(float delta) {
        this.translation = new Vector3f(
                this.translation.getX(),
                this.translation.getY() + delta,
                this.translation.getZ()
        );
        updateModelMatrix();
    }

    public void translateZ(float delta) {
        this.translation = new Vector3f(
                this.translation.getX(),
                this.translation.getY(),
                this.translation.getZ() + delta
        );
        updateModelMatrix();
    }

    public void rotateX(float delta) {
        this.rotation = new Vector3f(
                this.rotation.getX() + delta,
                this.rotation.getY(),
                this.rotation.getZ()
        );
        updateModelMatrix();
    }

    public void rotateY(float delta) {
        this.rotation = new Vector3f(
                this.rotation.getX(),
                this.rotation.getY() + delta,
                this.rotation.getZ()
        );
        updateModelMatrix();
    }

    public void rotateZ(float delta) {
        this.rotation = new Vector3f(
                this.rotation.getX(),
                this.rotation.getY(),
                this.rotation.getZ() + delta
        );
        updateModelMatrix();
    }

    public void scaleX(float factor) {
        this.scale = new Vector3f(
                this.scale.getX() * factor,
                this.scale.getY(),
                this.scale.getZ()
        );
        updateModelMatrix();
    }

    public void scaleY(float factor) {
        this.scale = new Vector3f(
                this.scale.getX(),
                this.scale.getY() * factor,
                this.scale.getZ()
        );
        updateModelMatrix();
    }

    public void scaleZ(float factor) {
        this.scale = new Vector3f(
                this.scale.getX(),
                this.scale.getY(),
                this.scale.getZ() * factor
        );
        updateModelMatrix();
    }

    public void resetTransformations() {
        // Сброс трансформаций: identity для translation/rotation/scale.
        this.translation = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
        updateModelMatrix();
    }

    public void setVertices(List<Vector3f> newVertices) {
        this.vertices.clear();
        this.vertices.addAll(newVertices);
    }

    public void clearNormals() {
        this.normals.clear();
    }

    public void clearTextureVertices() {
        this.textureVertices.clear();
    }

    public void clearPolygons() {
        this.polygons.clear();
    }
}
