package com.cgvsu;

//  главный контроллер JavaFX (загрузка модели/текстуры, управление камерой, запуск рендера)
// Состояние: активная модель, активная камера (CameraManager), режимы рендера (RenderingModes).
// События: обработка меню, клавиатуры, мыши, перерисовка через AnimationTimer/requestRender.

import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.render_engine.RenderingModes;
import com.cgvsu.render_engine.Texture;
import com.cgvsu.render_engine.Lighting;
import javafx.fxml.FXML;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.canvas.Canvas;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.model.ModelProcessor;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.CameraManager;
import com.cgvsu.view.CameraViewController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    private float rotationSpeed = 0.05f; // Скорость вращения
    private float moveSpeed = 0.5f;      // Скорость движения

    private static final float SCALE_UP_FACTOR = 1.1f;
    private static final float SCALE_DOWN_FACTOR = 0.9f;

    private double lastMouseX, lastMouseY;

    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane canvasContainer;

    @FXML
    private Canvas canvas;

    @FXML
    private CheckBox drawWireframeCheckBox;

    @FXML
    private CheckBox useTextureCheckBox;

    @FXML
    private CheckBox useLightingCheckBox;

    @FXML
    private ColorPicker polygonColorPicker;

    private final ArrayList<Model> models = new ArrayList<>();
    private int activeModelIndex = -1;
    private Model mesh = null;

    @FXML
    private ComboBox<String> modelSelector;

    @FXML
    private Label modelInfoLabel;

    private RenderingModes renderingModes = new RenderingModes();
    private Texture texture = null;
    private Lighting lighting = null;

    private Color polygonBaseColor = Color.LIGHTGRAY;

    private Color canvasBackgroundColor = Color.WHITE;
    private Color canvasTextColor = Color.BLACK;

    private AnimationTimer renderTimer;
    private boolean renderDirty = true;
    private long lastRenderNs = 0L;
    private static final long MIN_FRAME_NS = 33_000_000L; // ~30 FPS cap

    private final CameraManager cameraManager = new CameraManager(
            new Camera(
                    new Vector3f(0, 0, 5),
                    new Vector3f(0, 0, 0),
                    1.0F,
                    1,
                    0.01F,
                    10000
            )
    );

    private final CameraViewController cameraViewController = new CameraViewController(cameraManager.getActiveCamera());

    private boolean suppressModelSelectorEvent = false;

    @FXML
    private void initialize() {
        // Инициализация: привязка размеров Canvas к AnchorPane + запрос перерисовки при resize
        if (canvas != null && canvasContainer != null) {
            canvas.widthProperty().bind(canvasContainer.widthProperty());
            canvas.heightProperty().bind(canvasContainer.heightProperty());
            canvas.widthProperty().addListener((obs, oldV, newV) -> requestRender());
            canvas.heightProperty().addListener((obs, oldV, newV) -> requestRender());
        }
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Первичная отрисовка: фон/заглушка до загрузки модели
        drawStaticScene(gc);

        // Подписки UI: переключатели режимов рендера
        if (drawWireframeCheckBox != null) {
            drawWireframeCheckBox.setOnAction(e -> {
                onDrawWireframeCheckBoxChange();
                requestRender();
            });
        }
        if (useTextureCheckBox != null) {
            useTextureCheckBox.setOnAction(e -> {
                onUseTextureCheckBoxChange();
                requestRender();
            });
        }
        if (useLightingCheckBox != null) {
            useLightingCheckBox.setOnAction(e -> {
                onUseLightingCheckBoxChange();
                requestRender();
            });
        }

        // Подписки UI: выбор базового цвета полигона
        if (polygonColorPicker != null) {
            polygonColorPicker.setValue(polygonBaseColor);
            polygonColorPicker.valueProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    polygonBaseColor = newV;
                    requestRender();
                }
            });
        }

        // Фокус ввода: получение клавиатурных событий от Canvas
        canvas.setFocusTraversable(true);
        canvas.requestFocus();
        setupKeyboardControls();
        setupMouseControls();

        if (modelSelector != null) {
            modelSelector.getItems().clear();
            modelSelector.setDisable(true);
        }
        updateModelInfoLabel();

        Platform.runLater(this::applyTheme);

        // Цикл рендера: AnimationTimer + throttle по времени
        startRenderingLoop();
    }

    private Model getActiveModel() {
        if (activeModelIndex < 0 || activeModelIndex >= models.size()) {
            return null;
        }
        return models.get(activeModelIndex);
    }

    private void setActiveModelIndex(final int index) {
        if (models.isEmpty()) {
            activeModelIndex = -1;
            mesh = null;
            updateModelSelector();
            updateModelInfoLabel();
            requestRender();
            return;
        }

        int newIndex = index;
        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex >= models.size()) {
            newIndex = models.size() - 1;
        }

        activeModelIndex = newIndex;
        mesh = getActiveModel();
        updateModelSelector();
        updateModelInfoLabel();
        if (mesh != null) {
            fitActiveCameraToModel(mesh);
        }
        requestRender();
    }

    private void updateModelSelector() {
        if (modelSelector == null) {
            return;
        }
        suppressModelSelectorEvent = true;
        modelSelector.getItems().clear();
        if (models.isEmpty()) {
            modelSelector.setDisable(true);
            suppressModelSelectorEvent = false;
            return;
        }

        for (int i = 0; i < models.size(); i++) {
            Model m = models.get(i);
            modelSelector.getItems().add("Model " + (i + 1) + " (V=" + m.getVertices().size() + ", P=" + m.getPolygons().size() + ")");
        }
        modelSelector.setDisable(false);

        if (activeModelIndex >= 0 && activeModelIndex < modelSelector.getItems().size()) {
            modelSelector.getSelectionModel().select(activeModelIndex);
        }

        suppressModelSelectorEvent = false;
    }

    private void updateModelInfoLabel() {
        if (modelInfoLabel == null) {
            return;
        }
        Model active = getActiveModel();
        if (active == null) {
            modelInfoLabel.setText("No models loaded");
        } else {
            modelInfoLabel.setText("Vertices: " + active.getVertices().size() + " | Polygons: " + active.getPolygons().size());
        }
    }

    private void showError(final String title, final String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirm(final String title, final String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void requestRender() {
        // отложенная перерисовка в AnimationTimer.
        renderDirty = true;
    }

    private void startRenderingLoop() {
        // защита от повторной инициализации
        if (renderTimer != null) {
            renderTimer.stop();
        }
        renderTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Проверка флага: отрисовка только при наличии изменений
                if (!renderDirty) {
                    return;
                }
                // Ограничение FPS: минимальный интервал между кадрами
                if (lastRenderNs != 0L && (now - lastRenderNs) < MIN_FRAME_NS) {
                    return;
                }
                lastRenderNs = now;
                renderDirty = false;

                // Отрисовка кадра
                drawStaticScene(canvas.getGraphicsContext2D());
            }
        };
        renderTimer.start();
    }

    private void syncActiveCamera() {
        // Синхронизация активной камеры: обновление CameraViewController
        Camera activeCamera = cameraManager.getActiveCamera();
        cameraViewController.setCamera(activeCamera);
    }

    private void setupMouseControls() {
        // Установка контроля мыши: получение событий от Canvas
        canvas.setOnMouseClicked(event -> {
            canvas.requestFocus();
            requestRender();
        });

        // Делегирование мыши: CameraViewController -> CameraController
        syncActiveCamera();
        cameraViewController.setupMouseControls(canvas, this::requestRender);
    }

    private void setupKeyboardControls() {
        // Установка контроля клавиатуры: получение событий от Canvas
        canvas.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case Q:
                    handleRotateYMinus();
                    break;

                case E:
                    handleRotateYPlus();
                    break;
                case R:
                    handleRotateXMinus();
                    break;

                case F:
                    handleRotateXPlus();
                    break;
                case W: // Вперед (по Z)

                    handleTranslateZMinus();
                    break;

                case S: // Назад (по Z)

                    handleTranslateZPlus();
                    break;

                case A: // Влево (по X)

                    handleTranslateXMinus();
                    break;

                case D: // Вправо (по X)

                    handleTranslateXPlus();
                    break;
                case SPACE: // Вверх (по Y)

                    handleTranslateYPlus();
                    break;

                case C: // Вниз (по Y)

                    handleTranslateYMinus();
                    break;
                case ADD:      // + на цифровой клавиатуре

                case EQUALS:   // = (рядом с backspace, требует Shift)
                case PLUS:     // +
                    handleScaleXUp();
                    handleScaleYUp();
                    handleScaleZUp();
                    break;

                case SUBTRACT: // - на цифровой клавиатуре

                case MINUS:    // -
                    handleScaleXDown();
                    handleScaleYDown();
                    handleScaleZDown();
                    break;
                case BACK_SPACE:
                    handleResetTransform();
                    break;
            }

            // Запрос перерисовки после изменения состояния
            requestRender();
        });
    }

    private Model requireActiveModel(final String actionTitle) {
        Model active = getActiveModel();
        if (active == null) {
            showError(actionTitle, "No active model");
            return null;
        }
        return active;
    }

    @FXML
    private void handleTranslateXPlus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.translateX(moveSpeed);
        requestRender();
    }

    @FXML
    private void handleTranslateXMinus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.translateX(-moveSpeed);
        requestRender();
    }

    @FXML
    private void handleTranslateYPlus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.translateY(moveSpeed);
        requestRender();
    }

    @FXML
    private void handleTranslateYMinus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.translateY(-moveSpeed);
        requestRender();
    }

    @FXML
    private void handleTranslateZPlus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.translateZ(moveSpeed);
        requestRender();
    }

    @FXML
    private void handleTranslateZMinus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.translateZ(-moveSpeed);
        requestRender();
    }

    @FXML
    private void handleRotateXPlus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.rotateX(rotationSpeed);
        requestRender();
    }

    @FXML
    private void handleRotateXMinus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.rotateX(-rotationSpeed);
        requestRender();
    }

    @FXML
    private void handleRotateYPlus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.rotateY(rotationSpeed);
        requestRender();
    }

    @FXML
    private void handleRotateYMinus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.rotateY(-rotationSpeed);
        requestRender();
    }

    @FXML
    private void handleRotateZPlus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.rotateZ(rotationSpeed);
        requestRender();
    }

    @FXML
    private void handleRotateZMinus() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.rotateZ(-rotationSpeed);
        requestRender();
    }

    @FXML
    private void handleScaleXUp() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.scaleX(SCALE_UP_FACTOR);
        requestRender();
    }

    @FXML
    private void handleScaleXDown() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.scaleX(SCALE_DOWN_FACTOR);
        requestRender();
    }

    @FXML
    private void handleScaleYUp() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.scaleY(SCALE_UP_FACTOR);
        requestRender();
    }

    @FXML
    private void handleScaleYDown() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.scaleY(SCALE_DOWN_FACTOR);
        requestRender();
    }

    @FXML
    private void handleScaleZUp() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.scaleZ(SCALE_UP_FACTOR);
        requestRender();
    }

    @FXML
    private void handleScaleZDown() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.scaleZ(SCALE_DOWN_FACTOR);
        requestRender();
    }

    @FXML
    private void handleResetTransform() {
        Model m = requireActiveModel("Transform");
        if (m == null) return;
        m.resetTransformations();
        requestRender();
    }

    private void drawStaticScene(GraphicsContext gc) {
        // Отрисовка статической сцены: фон/заглушка или модель
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Очистка и фон
        gc.clearRect(0, 0, width, height);
        gc.setFill(canvasBackgroundColor);
        gc.fillRect(0, 0, width, height);

        // Ветвление: заглушка или рендер модели
        if (mesh != null) {
            drawModelInfo(gc, width, height);
        } else {
            drawBlueCube(gc, width, height);
        }
    }

    private void drawBlueCube(GraphicsContext gc, double width, double height) {
        // Заглушка сцены: простой квадрат вместо 3D-модели
        double centerX = width / 2;
        double centerY = height / 2;
        double size = 200;

        gc.setFill(Color.rgb(100, 150, 255, 0.7));
        gc.fillRect(centerX - size / 2, centerY - size / 2, size, size);

        if (drawWireframeCheckBox != null && drawWireframeCheckBox.isSelected()) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2);
            gc.strokeRect(centerX - size / 2, centerY - size / 2, size, size);
        }
    }

    private void drawModelInfo(GraphicsContext gc, double width, double height) {
        // Инфо-панель: количество вершин/полигонов
        double margin = 20;
        double x = Math.max(margin, width - 220);
        double y = Math.max(margin, height - 20);

        gc.setFill(canvasTextColor);
        gc.fillText("Вершин: " + mesh.getVertices().size(), x, y - 20);
        gc.fillText("Полигонов: " + mesh.getPolygons().size(), x, y);

        Camera activeCamera = cameraManager.getActiveCamera();
        activeCamera.setAspectRatio((float) (width / height));

        if (lighting != null) {
            // Обновление освещения: актуализация направления света по камере
            lighting.update(activeCamera.getPosition(), activeCamera.getTarget(), activeCamera.getViewMatrix());
        }

        // Вызов рендера: Rasterizer + ZBuffer + опционально texture/lighting
        RenderEngine.render(
                gc,
                activeCamera,
                mesh,
                (int) width,
                (int) height,
                texture,
                lighting,
                polygonBaseColor,
                cameraManager.getAllCameras(),
                renderingModes
        );
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model loaded = ObjReader.read(fileContent);
            if (loaded == null) {
                showError("Load Model", "Failed to load model: empty result");
                return;
            }

            // Подготовка модели: UV/триангуляция/нормали
            ModelProcessor.preprocess(loaded);
            loaded.setOriginalVertices(new java.util.ArrayList<>(loaded.getVertices()));
            models.add(loaded);
            setActiveModelIndex(models.size() - 1);
        } catch (Exception exception) {
            showError("Load Model", exception.getMessage() != null ? exception.getMessage() : "Unknown error");
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawStaticScene(gc);
        requestRender();
    }

    @FXML
    private void onSaveActiveModelMenuItemClick() {
        Model active = getActiveModel();
        if (active == null) {
            showError("Save Model", "No active model");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");
        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            ObjWriter.write(file.getAbsolutePath(), active);
        } catch (Exception e) {
            showError("Save Model", "Failed to save model: " + e.getMessage());
        }
    }

    private void saveModelWithChooser(final String dialogTitle, final Model modelToSave) {
        if (modelToSave == null) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle(dialogTitle);
        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            ObjWriter.write(file.getAbsolutePath(), modelToSave);
        } catch (Exception e) {
            showError(dialogTitle, "Failed to save model: " + e.getMessage());
        }
    }

    private static Polygon copyPolygon(final com.cgvsu.model.Polygon src) {
        Polygon p = new Polygon();
        if (src.getVertexIndices() != null) {
            p.setVertexIndices(new ArrayList<>(src.getVertexIndices()));
        }
        if (src.getTextureVertexIndices() != null) {
            p.setTextureVertexIndices(new ArrayList<>(src.getTextureVertexIndices()));
        }
        if (src.getNormalIndices() != null) {
            p.setNormalIndices(new ArrayList<>(src.getNormalIndices()));
        }
        return p;
    }

    private static Model buildModelForSave(
            final List<Vector3f> vertices,
            final List<com.cgvsu.math.Vector2f> textureVertices,
            final List<Vector3f> normals,
            final List<com.cgvsu.model.Polygon> polygons) {
        Model m = new Model();
        m.resetTransformations();

        if (vertices != null) {
            m.setVertices(vertices);
        }
        if (textureVertices != null) {
            m.clearTextureVertices();
            for (var tv : textureVertices) {
                m.addTextureVertex(tv);
            }
        }
        if (normals != null) {
            m.clearNormals();
            for (var n : normals) {
                m.addNormal(n);
            }
        } else {
            m.clearNormals();
        }

        m.clearPolygons();
        if (polygons != null) {
            for (var p : polygons) {
                m.addPolygon(copyPolygon(p));
            }
        }
        return m;
    }

    @FXML
    private void onSaveActiveModelOriginalMenuItemClick() {
        Model active = requireActiveModel("Save Model");
        if (active == null) {
            return;
        }

        List<Vector3f> baseVertices = active.getOriginalVertices();
        if (baseVertices == null || baseVertices.isEmpty()) {
            baseVertices = new ArrayList<>(active.getVertices());
        }

        Model toSave = buildModelForSave(
                baseVertices,
                new ArrayList<>(active.getTextureVertices()),
                null,
                new ArrayList<>(active.getPolygons())
        );
        saveModelWithChooser("Save Active Model (Original)", toSave);
    }

    @FXML
    private void onSaveActiveModelTransformedMenuItemClick() {
        Model active = requireActiveModel("Save Model");
        if (active == null) {
            return;
        }

        List<Vector3f> transformedVertices = active.getTransformedVertices();

        Model toSave = buildModelForSave(
                transformedVertices,
                new ArrayList<>(active.getTextureVertices()),
                null,
                new ArrayList<>(active.getPolygons())
        );
        saveModelWithChooser("Save Active Model (Transformed)", toSave);
    }

    @FXML
    private void onModelSelected() {
        if (modelSelector == null) {
            return;
        }
        if (suppressModelSelectorEvent) {
            return;
        }
        int idx = modelSelector.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= models.size()) {
            return;
        }
        setActiveModelIndex(idx);
    }

    @FXML
    private void onDeleteActiveModel() {
        Model active = getActiveModel();
        if (active == null) {
            return;
        }
        if (!confirm("Delete Model", "Delete active model?")) {
            return;
        }

        models.remove(activeModelIndex);
        if (models.isEmpty()) {
            activeModelIndex = -1;
            mesh = null;
        } else if (activeModelIndex >= models.size()) {
            activeModelIndex = models.size() - 1;
            mesh = getActiveModel();
        } else {
            mesh = getActiveModel();
        }

        updateModelSelector();
        updateModelInfoLabel();
        requestRender();
    }

    @FXML
    private void onDeleteVertexByIndex() {
        Model active = getActiveModel();
        if (active == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Vertex");
        dialog.setHeaderText(null);
        dialog.setContentText("Vertex index (0.." + (active.getVertices().size() - 1) + "):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            int idx = Integer.parseInt(result.get().trim());
            if (!confirm("Delete Vertex", "Delete vertex " + idx + "?")) {
                return;
            }
            active.removeVertex(idx);
            ModelProcessor.preprocess(active);
            active.setOriginalVertices(new java.util.ArrayList<>(active.getVertices()));
            updateModelSelector();
            updateModelInfoLabel();
            requestRender();
        } catch (Exception e) {
            showError("Delete Vertex", "Invalid index");
        }
    }

    @FXML
    private void onDeletePolygonByIndex() {
        Model active = getActiveModel();
        if (active == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Polygon");
        dialog.setHeaderText(null);
        dialog.setContentText("Polygon index (0.." + (active.getPolygons().size() - 1) + "):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            int idx = Integer.parseInt(result.get().trim());
            if (!confirm("Delete Polygon", "Delete polygon " + idx + "?")) {
                return;
            }
            active.removePolygon(idx);
            ModelProcessor.preprocess(active);
            active.setOriginalVertices(new java.util.ArrayList<>(active.getVertices()));
            updateModelSelector();
            updateModelInfoLabel();
            requestRender();
        } catch (Exception e) {
            showError("Delete Polygon", "Invalid index");
        }
    }

    private boolean darkTheme = false;

    @FXML
    private void onToggleTheme() {
        darkTheme = !darkTheme;
        applyTheme();
    }

    private void applyTheme() {
        if (rootPane == null || rootPane.getScene() == null) {
            return;
        }
        String cssPath = darkTheme ? "/com/cgvsu/styles/theme-dark.css" : "/com/cgvsu/styles/theme-light.css";
        var url = getClass().getResource(cssPath);
        if (url == null) {
            return;
        }
        rootPane.getScene().getStylesheets().setAll(url.toExternalForm());

        canvasBackgroundColor = darkTheme ? Color.web("#050a14") : Color.WHITE;
        canvasTextColor = darkTheme ? Color.rgb(240, 240, 240) : Color.BLACK;
        requestRender();
    }

    @FXML
    private void onLoadTextureMenuItemClick() {
        // Диалог выбора файла: PNG/JPG/BMP.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Texture");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Image image = new Image(file.toURI().toString());
            texture = new Texture(image);
        } catch (Exception e) {
            showError("Texture Load Error", "Failed to load texture: " + e.getMessage());
            return;
        }

        // Переключение режима: текстура доступна только после загрузки
        renderingModes.setUseTexture(true);
        if (useTextureCheckBox != null) {
            useTextureCheckBox.setSelected(true);
        }

        requestRender();
    }

    @FXML
    private void onAddCamera() {
        // Добавление новой камеры: клонирование активной камеры
        cameraManager.cloneActiveCamera(new Vector3f(0, 0, 0));
        syncActiveCamera();
        requestRender();
    }

    @FXML
    private void onRemoveCamera() {
        // Удаление активной камеры
        cameraManager.removeActiveCamera();
        syncActiveCamera();
        requestRender();
    }

    @FXML
    private void onNextCamera() {
        // Переключение на следующую камеру
        cameraManager.nextCamera();
        syncActiveCamera();
        requestRender();
    }

    @FXML
    private void onPreviousCamera() {
        // Переключение на предыдущую камеру.
        cameraManager.previousCamera();
        syncActiveCamera();
        requestRender();
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        // Движение камеры вперед.
        cameraManager.getActiveCamera().movePosition(new Vector3f(0, 0, -TRANSLATION));
        requestRender();
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        // Движение камеры назад.
        cameraManager.getActiveCamera().movePosition(new Vector3f(0, 0, TRANSLATION));
        requestRender();
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        // Движение камеры влево.
        cameraManager.getActiveCamera().movePosition(new Vector3f(TRANSLATION, 0, 0));
        requestRender();
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        // Движение камеры вправо.
        cameraManager.getActiveCamera().movePosition(new Vector3f(-TRANSLATION, 0, 0));
        requestRender();
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        // Движение камеры вверх.
        cameraManager.getActiveCamera().movePosition(new Vector3f(0, TRANSLATION, 0));
        requestRender();
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        // Движение камеры вниз.
        cameraManager.getActiveCamera().movePosition(new Vector3f(0, -TRANSLATION, 0));
        requestRender();
    }

    @FXML
    public void onDrawWireframeCheckBoxChange() {
        // Изменение режима отрисовки: wireframe.
        if (drawWireframeCheckBox == null) {
            return;
        }
        renderingModes.setDrawWireframe(drawWireframeCheckBox.isSelected());
    }

    @FXML
    public void onUseTextureCheckBoxChange() {
        // Изменение режима отрисовки: текстура.
        if (useTextureCheckBox == null) {
            return;
        }

        // Проверка состояния: запрет включения при отсутствии texture.
        boolean selected = useTextureCheckBox.isSelected();
        if (selected && texture == null) {
            useTextureCheckBox.setSelected(false);
            renderingModes.setUseTexture(false);
            return;
        }

        renderingModes.setUseTexture(selected);
    }

    @FXML
    public void onUseLightingCheckBoxChange() {
        // Изменение режима отрисовки: освещение.
        if (useLightingCheckBox == null) {
            return;
        }
        renderingModes.setUseLighting(useLightingCheckBox.isSelected());

        if (renderingModes.isUseLighting()) {
            if (lighting == null) {
                // Инициализация освещения: привязка к текущей активной камере.
                Camera activeCamera = cameraManager.getActiveCamera();
                lighting = new Lighting(activeCamera.getPosition(), activeCamera.getTarget(), 0.25f, 0.85f);
            }
        } else {
            lighting = null;
        }
    }

    private void fitActiveCameraToModel(final Model model) {
        // Подгон камеры: bounding box -> центр + радиус -> дистанция по fov.
        if (model == null || model.getVertices() == null || model.getVertices().isEmpty()) {
            return;
        }

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (Vector3f v : model.getVertices()) {
            if (v.getX() < minX) minX = v.getX();
            if (v.getY() < minY) minY = v.getY();
            if (v.getZ() < minZ) minZ = v.getZ();
            if (v.getX() > maxX) maxX = v.getX();
            if (v.getY() > maxY) maxY = v.getY();
            if (v.getZ() > maxZ) maxZ = v.getZ();
        }

        Vector3f center = new Vector3f(
                (minX + maxX) * 0.5f,
                (minY + maxY) * 0.5f,
                (minZ + maxZ) * 0.5f
        );

        float dx = maxX - minX;
        float dy = maxY - minY;
        float dz = maxZ - minZ;
        float radius = 0.5f * (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (radius < 1e-6f) {
            radius = 1.0f;
        }

        Camera activeCamera = cameraManager.getActiveCamera();
        float fov = activeCamera.getFov();
        float distance = (float) (radius / Math.tan(fov * 0.5f));
        distance *= 1.25f;

        activeCamera.setTarget(center);
        activeCamera.setPosition(center.add(new Vector3f(0, 0, distance)));
    }
}