package com.cgvsu.render_engine;

// хранение набора камер и выбор активной камеры.
// добавление/удаление, переключение, клонирование с offset.

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CameraManager {

    private final List<Camera> cameras = new ArrayList<>();
    private int activeCameraIndex = 0;

    public CameraManager(final Camera initialCamera) {
        cameras.add(initialCamera);
    }

    public Camera getActiveCamera() {
        return cameras.get(activeCameraIndex);
    }

    public void addCamera(final Camera camera) {
        if (camera == null) {
            return;
        }
        // Добавление камеры + переключение activeCameraIndex на конец списка
        cameras.add(camera);
        activeCameraIndex = cameras.size() - 1;
    }

    public boolean removeActiveCamera() {
        if (cameras.size() == 1) {
            return false;
        }
        cameras.remove(activeCameraIndex);
        activeCameraIndex = Math.max(0, activeCameraIndex - 1);
        return true;
    }

    public void nextCamera() {
        //  переключение вперёд
        activeCameraIndex = (activeCameraIndex + 1) % cameras.size();
    }

    public void previousCamera() {
        // переключение назад
        activeCameraIndex = (activeCameraIndex - 1 + cameras.size()) % cameras.size();
    }

    public void cloneActiveCamera(final Vector3f offset) {
        // Клонирование активной камеры: copy + опциональный сдвиг position/target
        Camera active = getActiveCamera();
        Camera cloned = active.copy();
        if (offset != null) {
            cloned.movePosition(offset);
            cloned.moveTarget(offset);
        }
        addCamera(cloned);
    }

    public List<Camera> getAllCameras() {
        return Collections.unmodifiableList(cameras);
    }

    public int getActiveCameraIndex() {
        return activeCameraIndex;
    }

    public int getCameraCount() {
        return cameras.size();
    }
}