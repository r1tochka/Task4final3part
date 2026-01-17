package com.cgvsu.model;

 // индексы вершин/UV/нормалей для одной грани.
 // размер списков textureVertexIndices/normalIndices либо 0, либо равен vertexIndices.
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Polygon {

    private final List<Integer> vertexIndices;
    private final List<Integer> textureVertexIndices;
    private final List<Integer> normalIndices;

    public Polygon() {
        this.vertexIndices = new ArrayList<>();
        this.textureVertexIndices = new ArrayList<>();
        this.normalIndices = new ArrayList<>();
    }

    public void setVertexIndices(ArrayList<Integer> vertexIndices) {
        this.vertexIndices.clear();
        this.vertexIndices.addAll(vertexIndices);
    }

    public void setTextureVertexIndices(ArrayList<Integer> textureVertexIndices) {
        this.textureVertexIndices.clear();
        this.textureVertexIndices.addAll(textureVertexIndices);
    }

    public void setNormalIndices(ArrayList<Integer> normalIndices) {
        this.normalIndices.clear();
        this.normalIndices.addAll(normalIndices);
    }

    public List<Integer> getVertexIndices() {
        return Collections.unmodifiableList(vertexIndices);
    }

    public List<Integer> getTextureVertexIndices() {
        return Collections.unmodifiableList(textureVertexIndices);
    }

    public List<Integer> getNormalIndices() {
        return Collections.unmodifiableList(normalIndices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Polygon polygon = (Polygon) o;
        return Objects.equals(vertexIndices, polygon.vertexIndices) &&
                Objects.equals(textureVertexIndices, polygon.textureVertexIndices) &&
                Objects.equals(normalIndices, polygon.normalIndices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertexIndices, textureVertexIndices, normalIndices);
    }

    @Override
    public String toString() {
        return String.format("Polygon[vertices=%d, textures=%d, normals=%d]",
                vertexIndices.size(), textureVertexIndices.size(), normalIndices.size());
    }
}
