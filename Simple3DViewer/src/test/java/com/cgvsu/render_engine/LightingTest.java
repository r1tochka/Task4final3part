package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LightingTest {

    @Test
    void computeLightingIntensityClampedTo0_1() {
        Lighting lighting = new Lighting(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 0.2f, 0.8f);

        float i1 = lighting.computeLightingIntensity(new Vector3f(0, 0, 1), new Vector3f(0, 0, 0), new Vector3f(0, 0, 5));
        assertTrue(i1 >= 0.0f && i1 <= 1.0f);

        float i2 = lighting.computeLightingIntensity(new Vector3f(0, 0, -1), new Vector3f(0, 0, 0), new Vector3f(0, 0, 5));
        assertTrue(i2 >= 0.0f && i2 <= 1.0f);
    }

    @Test
    void shadeColorAppliesAmbientAndDiffuseAndClamps() {
        Lighting lighting = new Lighting(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), 0.3f, 1.0f);

        Color out = lighting.shadeColor(Color.color(1, 0.5, 0.0, 1.0), 1.0f);

        assertNotNull(out);
        assertTrue(out.getRed() >= 0.0 && out.getRed() <= 1.0);
        assertTrue(out.getGreen() >= 0.0 && out.getGreen() <= 1.0);
        assertTrue(out.getBlue() >= 0.0 && out.getBlue() <= 1.0);
        assertEquals(1.0, out.getOpacity(), 1e-9);
    }
}
