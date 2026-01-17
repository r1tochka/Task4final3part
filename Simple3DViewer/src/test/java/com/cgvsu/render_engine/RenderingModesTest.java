package com.cgvsu.render_engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RenderingModesTest {

    @Test
    void defaultConstructorHasNoModesEnabled() {
        RenderingModes modes = new RenderingModes();
        assertFalse(modes.isDrawWireframe());
        assertFalse(modes.isUseTexture());
        assertFalse(modes.isUseLighting());
        assertFalse(modes.hasAnyModeEnabled());
    }

    @Test
    void hasAnyModeEnabledTrueWhenAnyFillFlagEnabled() {
        RenderingModes modes = new RenderingModes();
        modes.setDrawWireframe(true);
        assertFalse(modes.hasAnyModeEnabled());

        modes.setDrawWireframe(false);
        modes.setUseTexture(true);
        assertTrue(modes.hasAnyModeEnabled());

        modes.setUseTexture(false);
        modes.setUseLighting(true);
        assertTrue(modes.hasAnyModeEnabled());
    }
}
