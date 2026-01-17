package com.cgvsu.controller;

//Обработка мыши для управления камерой.
//Режимы: вращение (ЛКМ), панорама (ПКМ), зум (колесо).
//Параметры: коэффициенты чувствительности ROTATION/PAN/ZOOM.

import com.cgvsu.render_engine.Camera;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class CameraController {

    private Camera camera;
    private double mousePrevX, mousePrevY;
    private boolean isRotating = false;
    private boolean isPanning = false;

    private final float ROTATION_SENSITIVITY = 0.01f;
    private final float PAN_SENSITIVITY = 0.03f;
    private final float ZOOM_SENSITIVITY = 0.1f;

    public CameraController(Camera camera) {
        this.camera = camera;
    }

    public void setCamera(final Camera camera) {
        if (camera == null) {
            return;
        }
        this.camera = camera;
    }

    public void handleMousePressed(MouseEvent event) {
        resetState();
        mousePrevX = event.getX();
        mousePrevY = event.getY();

        if (event.getButton() == MouseButton.PRIMARY) {
            isRotating = true;
        } else if (event.getButton() == MouseButton.SECONDARY) {
            isPanning = true;
        }
    }

    public void resetState() {
        isRotating = false;
        isPanning = false;
    }

    public void handleMouseDragged(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        double deltaX = mouseX - mousePrevX;
        double deltaY = mouseY - mousePrevY;

        if (isRotating && camera != null) {
            camera.rotate((float) (-deltaX * ROTATION_SENSITIVITY),
                         (float) (-deltaY * ROTATION_SENSITIVITY));
        } else if (isPanning && camera != null) {
            camera.pan((float) (deltaX * PAN_SENSITIVITY),
                      (float) (-deltaY * PAN_SENSITIVITY));
        }

        mousePrevX = mouseX;
        mousePrevY = mouseY;
    }

    public void handleMouseReleased(MouseEvent event) {
        resetState();
    }

    public void handleMouseScroll(ScrollEvent event) {
        if (camera != null) {
            double deltaY = event.getDeltaY();
            camera.zoom((float) (-deltaY * ZOOM_SENSITIVITY));
        }
    }
}
