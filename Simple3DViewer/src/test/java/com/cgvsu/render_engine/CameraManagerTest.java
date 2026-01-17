package com.cgvsu.render_engine;

import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CameraManagerTest {

    private static Camera newCamera(float z) {
        return new Camera(new Vector3f(0, 0, z), new Vector3f(0, 0, 0), 1.0f, 1.0f, 0.1f, 100f);
    }

    @Test
    void constructorAddsInitialCamera() {
        Camera initial = newCamera(5);
        CameraManager mgr = new CameraManager(initial);

        assertEquals(1, mgr.getCameraCount());
        assertSame(initial, mgr.getActiveCamera());
        assertEquals(0, mgr.getActiveCameraIndex());
    }

    @Test
    void addCameraSetsActiveToNew() {
        CameraManager mgr = new CameraManager(newCamera(5));
        Camera c2 = newCamera(10);

        mgr.addCamera(c2);

        assertEquals(2, mgr.getCameraCount());
        assertSame(c2, mgr.getActiveCamera());
        assertEquals(1, mgr.getActiveCameraIndex());
    }

    @Test
    void removeActiveCameraCannotRemoveLast() {
        CameraManager mgr = new CameraManager(newCamera(5));
        assertFalse(mgr.removeActiveCamera());
        assertEquals(1, mgr.getCameraCount());
    }

    @Test
    void nextPreviousCameraCycles() {
        CameraManager mgr = new CameraManager(newCamera(5));
        mgr.addCamera(newCamera(10));
        mgr.addCamera(newCamera(15));

        int start = mgr.getActiveCameraIndex();
        mgr.nextCamera();
        assertNotEquals(start, mgr.getActiveCameraIndex());

        mgr.previousCamera();
        assertEquals(start, mgr.getActiveCameraIndex());
    }

    @Test
    void cloneActiveCameraCreatesCopyWithOffset() {
        CameraManager mgr = new CameraManager(newCamera(5));
        Camera before = mgr.getActiveCamera();

        mgr.cloneActiveCamera(new Vector3f(1, 2, 3));

        assertEquals(2, mgr.getCameraCount());
        Camera cloned = mgr.getActiveCamera();
        assertNotSame(before, cloned);

        Vector3f deltaPos = cloned.getPosition().subtract(before.getPosition());
        Vector3f deltaTgt = cloned.getTarget().subtract(before.getTarget());

        assertEquals(1.0f, deltaPos.getX(), 1e-6);
        assertEquals(2.0f, deltaPos.getY(), 1e-6);
        assertEquals(3.0f, deltaPos.getZ(), 1e-6);

        assertEquals(1.0f, deltaTgt.getX(), 1e-6);
        assertEquals(2.0f, deltaTgt.getY(), 1e-6);
        assertEquals(3.0f, deltaTgt.getZ(), 1e-6);
    }
}
