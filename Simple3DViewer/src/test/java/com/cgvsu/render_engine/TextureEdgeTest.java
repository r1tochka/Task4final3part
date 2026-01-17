package com.cgvsu.render_engine;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextureEdgeTest {

    @Test
    void sampleDoesNotThrowOnNaNUv() {
        WritableImage img = new WritableImage(2, 2);
        img.getPixelWriter().setColor(0, 0, Color.RED);
        Texture t = new Texture(img);

        assertDoesNotThrow(() -> t.sample(Float.NaN, 0.5f));
        assertDoesNotThrow(() -> t.sample(0.5f, Float.NaN));
        assertDoesNotThrow(() -> t.sample(Float.NaN, Float.NaN));
    }

    @Test
    void restorePaintLayerRejectsWrongLength() {
        WritableImage img = new WritableImage(2, 2);
        Texture t = new Texture(img);

        assertThrows(IllegalArgumentException.class, () -> t.restorePaintLayerArgb(new int[0]));
        assertThrows(IllegalArgumentException.class, () -> t.restorePaintLayerArgb(new int[3]));
    }
}
