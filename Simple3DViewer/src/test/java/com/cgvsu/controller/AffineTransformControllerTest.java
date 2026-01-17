package com.cgvsu.controller;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AffineTransformControllerTest {

    @Test
    void translateChangesModelTranslation() {
        Model m = new Model();
        AffineTransformController c = new AffineTransformController();

        c.translate(m, 1.5f, -2.0f, 3.0f);

        assertEquals(1.5f, m.getTranslation().getX(), 1e-6);
        assertEquals(-2.0f, m.getTranslation().getY(), 1e-6);
        assertEquals(3.0f, m.getTranslation().getZ(), 1e-6);
    }

    @Test
    void rotateChangesModelRotation() {
        Model m = new Model();
        AffineTransformController c = new AffineTransformController();

        c.rotate(m, 0.1f, 0.2f, -0.3f);

        assertEquals(0.1f, m.getRotation().getX(), 1e-6);
        assertEquals(0.2f, m.getRotation().getY(), 1e-6);
        assertEquals(-0.3f, m.getRotation().getZ(), 1e-6);
    }

    @Test
    void scaleMultipliesCurrentScale() {
        Model m = new Model();
        AffineTransformController c = new AffineTransformController();

        c.scale(m, 2.0f, 3.0f, 4.0f);
        assertEquals(2.0f, m.getScale().getX(), 1e-6);
        assertEquals(3.0f, m.getScale().getY(), 1e-6);
        assertEquals(4.0f, m.getScale().getZ(), 1e-6);

        c.scale(m, 0.5f, 2.0f, 0.25f);
        assertEquals(1.0f, m.getScale().getX(), 1e-6);
        assertEquals(6.0f, m.getScale().getY(), 1e-6);
        assertEquals(1.0f, m.getScale().getZ(), 1e-6);
    }

    @Test
    void resetTransformationsRestoresIdentityTransform() {
        Model m = new Model();
        AffineTransformController c = new AffineTransformController();

        c.translate(m, 1, 2, 3);
        c.rotate(m, 0.1f, 0.2f, 0.3f);
        c.scale(m, 2, 2, 2);

        c.resetTransformations(m);

        assertEquals(0.0f, m.getTranslation().getX(), 1e-6);
        assertEquals(0.0f, m.getTranslation().getY(), 1e-6);
        assertEquals(0.0f, m.getTranslation().getZ(), 1e-6);

        assertEquals(0.0f, m.getRotation().getX(), 1e-6);
        assertEquals(0.0f, m.getRotation().getY(), 1e-6);
        assertEquals(0.0f, m.getRotation().getZ(), 1e-6);

        assertEquals(1.0f, m.getScale().getX(), 1e-6);
        assertEquals(1.0f, m.getScale().getY(), 1e-6);
        assertEquals(1.0f, m.getScale().getZ(), 1e-6);
    }

    @Test
    void axisSpecificHelpersChangeOnlyOneComponent() {
        Model m = new Model();
        AffineTransformController c = new AffineTransformController();

        c.translateX(m, 2.0f);
        c.translateY(m, -1.0f);
        c.translateZ(m, 0.5f);

        assertEquals(2.0f, m.getTranslation().getX(), 1e-6);
        assertEquals(-1.0f, m.getTranslation().getY(), 1e-6);
        assertEquals(0.5f, m.getTranslation().getZ(), 1e-6);

        c.rotateX(m, 0.25f);
        c.rotateY(m, 0.5f);
        c.rotateZ(m, -0.75f);

        assertEquals(0.25f, m.getRotation().getX(), 1e-6);
        assertEquals(0.5f, m.getRotation().getY(), 1e-6);
        assertEquals(-0.75f, m.getRotation().getZ(), 1e-6);

        c.scaleX(m, 2.0f);
        c.scaleY(m, 3.0f);
        c.scaleZ(m, 4.0f);

        assertEquals(2.0f, m.getScale().getX(), 1e-6);
        assertEquals(3.0f, m.getScale().getY(), 1e-6);
        assertEquals(4.0f, m.getScale().getZ(), 1e-6);

        assertTrue(m.getModelMatrix() != null);
    }
}
