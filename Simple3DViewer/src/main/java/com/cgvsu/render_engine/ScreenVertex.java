package com.cgvsu.render_engine;

// упакованные данные вершины после проекции (screen x/y + ndc z)
// данные для шейдинга: invW, UV, normal, worldPosition, lightingIntensity
// вход TriangleRasterizer/LineRasterizer

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

public class ScreenVertex {
    private final float x;
    private final float y;
    private final float z;
    private final float invW;
    private final Vector2f textureCoords;
    private final Vector3f normal;
    private final Vector3f worldPosition;
    private final Float lightingIntensity;

    public ScreenVertex(float x, float y, float z) {
        this(x, y, z, 1.0f, null, null, null, null);
    }

    public ScreenVertex(float x, float y, float z, float invW,
                        Vector2f textureCoords, Vector3f normal,
                        Vector3f worldPosition, Float lightingIntensity) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.invW = invW;
        this.textureCoords = textureCoords;
        this.normal = normal;
        this.worldPosition = worldPosition;
        this.lightingIntensity = lightingIntensity;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getInvW() {
        return invW;
    }

    public Vector2f getTextureCoords() {
        return textureCoords;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public boolean hasTextureCoords() {
        return textureCoords != null;
    }

    public boolean hasNormal() {
        return normal != null;
    }

    public Vector3f getWorldPosition() {
        return worldPosition;
    }

    public boolean hasWorldPosition() {
        return worldPosition != null;
    }

    public Float getLightingIntensity() {
        return lightingIntensity;
    }

    public boolean hasLightingIntensity() {
        return lightingIntensity != null;
    }
}

