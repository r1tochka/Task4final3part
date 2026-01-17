package com.cgvsu.view;

// связывание Canvas и CameraController (подписки на события мыши).

import com.cgvsu.controller.CameraController;
import com.cgvsu.render_engine.Camera;
import javafx.scene.canvas.Canvas;

public class CameraViewController {
    private final CameraController cameraController;

    public CameraViewController(Camera camera) {
        this.cameraController = new CameraController(camera);
    }

    public void setCamera(final Camera camera) {
        cameraController.setCamera(camera);
    }

    public void setupMouseControls(Canvas canvas) {
        setupMouseControls(canvas, null);
    }

    public void setupMouseControls(final Canvas canvas, final Runnable onChange) {
        // Подписки на события: делегирование в CameraController + callback перерисовки
        canvas.setOnMousePressed(event -> {
            cameraController.handleMousePressed(event);
            if (onChange != null) {
                onChange.run();
            }
        });
        canvas.setOnMouseDragged(event -> {
            cameraController.handleMouseDragged(event);
            if (onChange != null) {
                onChange.run();
            }
        });
        canvas.setOnMouseReleased(event -> {
            cameraController.handleMouseReleased(event);
            if (onChange != null) {
                onChange.run();
            }
        });
        canvas.setOnMouseExited(event -> {
            // Сброс состояния: защита от "залипания" режима при выходе курсора за canvas
            cameraController.resetState();
            if (onChange != null) {
                onChange.run();
            }
        });
        canvas.setOnScroll(event -> {
            cameraController.handleMouseScroll(event);
            if (onChange != null) {
                onChange.run();
            }
        });

        canvas.setOnContextMenuRequested(event -> {
            // Сброс состояния: защита от конфликтов с context menu
            cameraController.resetState();
            event.consume();
            if (onChange != null) {
                onChange.run();
            }
        });
    }
}
