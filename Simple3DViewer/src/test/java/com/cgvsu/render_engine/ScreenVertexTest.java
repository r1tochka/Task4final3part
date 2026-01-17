package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScreenVertexTest {

    @Test
    void defaultConstructorSetsInvWTo1AndNoAttributes() {
        ScreenVertex v = new ScreenVertex(1, 2, 3);
        assertEquals(1.0f, v.getInvW(), 1e-6);
        assertFalse(v.hasTextureCoords());
        assertFalse(v.hasNormal());
        assertFalse(v.hasWorldPosition());
        assertFalse(v.hasLightingIntensity());
    }

    @Test
    void hasFlagsReflectNullableFields() {
        ScreenVertex v = new ScreenVertex(
                1, 2, 3,
                0.5f,
                new Vector2f(0.1f, 0.2f),
                new Vector3f(0, 1, 0),
                new Vector3f(1, 2, 3),
                0.7f
        );

        assertTrue(v.hasTextureCoords());
        assertTrue(v.hasNormal());
        assertTrue(v.hasWorldPosition());
        assertTrue(v.hasLightingIntensity());
        assertEquals(0.5f, v.getInvW(), 1e-6);
    }
}
