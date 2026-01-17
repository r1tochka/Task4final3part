package com.cgvsu.render_engine;

 // программный рендер модели в Canvas.
 // Преобразования: model/view/projection, переход NDC -> экран.
 // запись пикселей через TriangleRasterizer/LineRasterizer + ZBuffer.
 
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.model.Model;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.ArrayList;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            final Lighting lighting)
    {
        render(graphicsContext, camera, mesh, width, height, texture, lighting, Color.LIGHTGRAY);
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            final Lighting lighting,
            final Color baseColor)
    {
        render(graphicsContext, camera, mesh, width, height, texture, lighting, baseColor, null);
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            final Lighting lighting,
            final Color baseColor,
            final List<Camera> helperCameras)
    {
        render(graphicsContext, camera, mesh, width, height, texture, lighting, baseColor, helperCameras, new RenderingModes());
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            final Lighting lighting,
            final Color baseColor,
            final List<Camera> helperCameras,
            final RenderingModes renderingModes)
    {
        // Подготовка: матрицы model/view/projection и буфер глубины
        Matrix4f modelMatrix = mesh.getModelMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix(); // система координат камеры
        Matrix4f projectionMatrix = camera.getProjectionMatrix(); // плоскость проецирования

        Matrix4f modelViewMatrix = viewMatrix.multiply(modelMatrix);
        Matrix4f modelViewProjectionMatrix = projectionMatrix.multiply(modelViewMatrix);

        ZBuffer zBuffer = new ZBuffer(width, height);

        Color wireColor = Color.BLACK;

        final int nPolygons = mesh.getPolygons().size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            ArrayList<Integer> vertexIndices = new ArrayList<>(mesh.getPolygons().get(polygonInd).getVertexIndices());
            final int nVerticesInPolygon = vertexIndices.size();

            if (nVerticesInPolygon < 3) {
                continue;
            }

            Vector3f v0World = mesh.getVertices().get(vertexIndices.get(0));
            Vector3f v1World = mesh.getVertices().get(vertexIndices.get(1));
            Vector3f v2World = mesh.getVertices().get(vertexIndices.get(2));
            Vector3f v0View = GraphicConveyor.multiplyMatrix4ByVector3(modelViewMatrix, v0World);
            Vector3f v1View = GraphicConveyor.multiplyMatrix4ByVector3(modelViewMatrix, v1World);
            Vector3f v2View = GraphicConveyor.multiplyMatrix4ByVector3(modelViewMatrix, v2World);

            Vector3f edge1 = v1View.subtract(v0View);
            Vector3f edge2 = v2View.subtract(v0View);
            Vector3f faceNormal = edge1.cross(edge2);

            Vector3f toCamera = v0View.multiply(-1.0f);
            boolean frontFacing = faceNormal.dot(toCamera) > 0.0f;

            // Проверка: отсечение обратных граней (back-face culling).
            ArrayList<Integer> textureIndices = new ArrayList<>(mesh.getPolygons().get(polygonInd).getTextureVertexIndices());
            ArrayList<Integer> normalIndices = new ArrayList<>(mesh.getPolygons().get(polygonInd).getNormalIndices());

            boolean hasTextureCoords = !textureIndices.isEmpty() && textureIndices.size() == vertexIndices.size();
            boolean hasNormals = !normalIndices.isEmpty() && normalIndices.size() == vertexIndices.size();

            ArrayList<ScreenVertex> screenVertices = new ArrayList<>(nVerticesInPolygon);
            Vector3f cameraPosition = camera.getPosition();

            // Подготовка вершин: clip-space -> NDC (деление на W) -> screen-space.
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f modelVertex = mesh.getVertices().get(vertexIndices.get(vertexInPolygonInd));
                Vector3f worldPosition = GraphicConveyor.multiplyMatrix4ByVector3(modelMatrix, modelVertex);
                Vector4f clipPos = modelViewProjectionMatrix.multiply(
                    new Vector4f(modelVertex.getX(), modelVertex.getY(), modelVertex.getZ(), 1.0f));
                float w = clipPos.getW();
                float invW = (Math.abs(w) > 1e-7f) ? (1.0f / w) : 1.0f;
                Vector3f transformed = new Vector3f(
                        clipPos.getX() * invW,
                        clipPos.getY() * invW,
                        clipPos.getZ() * invW
                );

                Vector2f textureCoords = null;
                if (hasTextureCoords && textureIndices.get(vertexInPolygonInd) < mesh.getTextureVertices().size()) {
                    textureCoords = mesh.getTextureVertices().get(textureIndices.get(vertexInPolygonInd));
                }

                Vector3f worldNormal = null;
                if (hasNormals && normalIndices.get(vertexInPolygonInd) < mesh.getNormals().size()) {
                    Vector3f modelNormal = mesh.getNormals().get(normalIndices.get(vertexInPolygonInd));
                    // Преобразование нормали: только rotate/scale (без translation).
                    Matrix4f rotationScaleMatrix = Matrix4f.modelMatrix(
                        new Vector3f(0, 0, 0),
                        mesh.getRotation(),
                        mesh.getScale()
                    );
                    worldNormal = GraphicConveyor.multiplyMatrix4ByVector3(rotationScaleMatrix, modelNormal).normalize();
                }

                ScreenVertex screenVertex = toScreenVertex(
                        transformed,
                        width,
                        height,
                        invW,
                        textureCoords,
                        worldNormal,
                        worldPosition,
                        null
                );
                screenVertices.add(screenVertex);
            }

            // Триангуляция полигона
            for (int i = 1; i < nVerticesInPolygon - 1; ++i) {
                ScreenVertex sv0 = screenVertices.get(0);
                ScreenVertex sv1 = screenVertices.get(i);
                ScreenVertex sv2 = screenVertices.get(i + 1);
                if (renderingModes.isUseTexture() || renderingModes.isUseLighting()) {
                    TriangleRasterizer.fillTriangle(
                            graphicsContext,
                            zBuffer,
                            sv0,
                            sv1,
                            sv2,
                            width,
                            height,
                            renderingModes.isUseTexture() ? texture : null,
                            renderingModes.isUseLighting() ? lighting : null,
                            baseColor,
                            cameraPosition
                    );
                } else {
                    TriangleRasterizer.fillTriangle(
                            graphicsContext,
                            zBuffer,
                            sv0,
                            sv1,
                            sv2,
                            width,
                            height,
                            baseColor
                    );
                }
            }

            // Каркас: отрисовка рёбер с depth-bias
            if (renderingModes.isDrawWireframe()) {
                Vector3f normalViewNorm = faceNormal.normalize();
                Vector3f toCameraNorm = toCamera.normalize();
                float cosTheta = Math.abs(normalViewNorm.dot(toCameraNorm));
                float grazing = 1.0f - cosTheta;
                double angleScale = 1.0 + 8.0 * Math.pow(grazing, 5.0);
                float distance = toCamera.length();
                double depthFactor = 1.0 / (1.0 + 0.15 * distance);

                double depthBiasScale = angleScale * depthFactor;

                for (int i = 0; i < nVerticesInPolygon; ++i) {
                    ScreenVertex a = screenVertices.get(i);
                    ScreenVertex b = screenVertices.get((i + 1) % nVerticesInPolygon);
                    LineRasterizer.drawLine(
                            graphicsContext,
                            zBuffer,
                            a,
                            b,
                            width,
                            height,
                            wireColor,
                            depthBiasScale
                    );
                }
            }
        }

        // Маркеры камер: отрисовка позиций helper-cameras в screen-space
        renderHelperCameras(graphicsContext, helperCameras, camera, projectionMatrix.multiply(viewMatrix), width, height);
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
        render(graphicsContext, camera, mesh, width, height, null, null, Color.LIGHTGRAY);
    }

    private static ScreenVertex toScreenVertex(
            final Vector3f vertex,
            final int width,
            final int height,
            final float invW,
            final Vector2f textureCoords,
            final Vector3f worldNormal,
            final Vector3f worldPosition,
            final Float lightingIntensity) {
        float ndcX = vertex.getX();
        float ndcY = vertex.getY();
        float ndcZ = vertex.getZ();
        float screenX = (ndcX + 1.0f) * 0.5f * (width - 1.0f);
        float screenY = (1.0f - ndcY) * 0.5f * (height - 1.0f);

        return new ScreenVertex(screenX, screenY, ndcZ, invW, textureCoords, worldNormal, worldPosition, lightingIntensity);
    }

    private static void renderHelperCameras(
            final GraphicsContext graphicsContext,
            final List<Camera> helperCameras,
            final Camera activeCamera,
            final Matrix4f viewProjectionMatrix,
            final int width,
            final int height) {
        if (helperCameras == null || helperCameras.isEmpty()) {
            return;
        }
        // Визуализация: простые 2D-маркеры (круги) для неактивных камер.
        graphicsContext.setFill(Color.CORNFLOWERBLUE);
        for (Camera helper : helperCameras) {
            if (helper == activeCamera) {
                continue;
            }
            Vector4f clip4 = viewProjectionMatrix.multiply(
                    new Vector4f(helper.getPosition().getX(), helper.getPosition().getY(), helper.getPosition().getZ(), 1.0f)
            );

            float w = clip4.getW();
            if (w <= 1e-7f) {
                continue;
            }

            Vector3f ndc = GraphicConveyor.multiplyMatrix4ByVector3(viewProjectionMatrix, helper.getPosition());
            float screenX = (ndc.getX() + 1.0f) * 0.5f * (width - 1.0f);
            float screenY = (1.0f - ndc.getY()) * 0.5f * (height - 1.0f);
            graphicsContext.fillOval(screenX - 4, screenY - 4, 8, 8);
        }
    }
}
