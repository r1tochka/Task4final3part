package com.cgvsu.render_engine;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextureTest {

    @Test
    void sampleClampsUvAndReturnsColorFromImage() {
        WritableImage img = new WritableImage(2, 2);
        img.getPixelWriter().setColor(0, 0, Color.RED);
        img.getPixelWriter().setColor(1, 0, Color.GREEN);
        img.getPixelWriter().setColor(0, 1, Color.BLUE);
        img.getPixelWriter().setColor(1, 1, Color.WHITE);

        Texture t = new Texture(img);

        assertEquals(Color.BLUE, t.sample(0.0f, 0.0f));
        assertEquals(Color.RED, t.sample(0.0f, 1.0f));

        assertNotNull(t.sample(-100.0f, 100.0f));
        assertNotNull(t.sample(100.0f, -100.0f));
    }

    @Test
    void paintLayerSnapshotAndRestoreRoundTrip() {
        WritableImage img = new WritableImage(3, 3);
        Texture t = new Texture(img);

        int[] before = t.snapshotPaintLayerArgb();
        t.stampCircle(1, 1, 1, Color.color(1, 0, 0, 0.5));
        int[] after = t.snapshotPaintLayerArgb();
        assertNotEquals(before[4], after[4]);

        t.restorePaintLayerArgb(before);
        int[] restored = t.snapshotPaintLayerArgb();
        assertArrayEquals(before, restored);
    }
}
