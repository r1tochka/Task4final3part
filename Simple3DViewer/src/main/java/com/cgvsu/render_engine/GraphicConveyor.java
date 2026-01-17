package com.cgvsu.render_engine;
 //  математика пайплайна (model/view/projection) и вспомогательные преобразования
 // Матрицы: rotate/scale/translate, lookAt, perspective.
 // Преобразование вершины: Matrix4f * Vector4f с делением на W.
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Point2f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate(Vector3f translation, Vector3f rotation, Vector3f scale) {
        // Сборка матрицы model: scale -> rotateX/Y/Z -> translate
        float[][] scaleMatrix = {
                {scale.getX(), 0, 0, 0},
                {0, scale.getY(), 0, 0},
                {0, 0, scale.getZ(), 0},
                {0, 0, 0, 1}
        };
        float cosX = (float) Math.cos(rotation.getX());
        float sinX = (float) Math.sin(rotation.getX());
        float[][] rotateX = {
                {1, 0, 0, 0},
                {0, cosX, -sinX, 0},
                {0, sinX, cosX, 0},
                {0, 0, 0, 1}
        };

        float cosY = (float) Math.cos(rotation.getY());
        float sinY = (float) Math.sin(rotation.getY());
        float[][] rotateY = {
                {cosY, 0, sinY, 0},
                {0, 1, 0, 0},
                {-sinY, 0, cosY, 0},
                {0, 0, 0, 1}
        };

        float cosZ = (float) Math.cos(rotation.getZ());
        float sinZ = (float) Math.sin(rotation.getZ());
        float[][] rotateZ = {
                {cosZ, -sinZ, 0, 0},
                {sinZ, cosZ, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        float[][] translateMatrix = {
                {1, 0, 0, translation.getX()},
                {0, 1, 0, translation.getY()},
                {0, 0, 1, translation.getZ()},
                {0, 0, 0, 1}
        };
        Matrix4f scaleMat = new Matrix4f(scaleMatrix);
        Matrix4f rotXMat = new Matrix4f(rotateX);
        Matrix4f rotYMat = new Matrix4f(rotateY);
        Matrix4f rotZMat = new Matrix4f(rotateZ);
        Matrix4f transMat = new Matrix4f(translateMatrix);

        // Порядок умножения: справа налево — сначала scale, затем повороты, затем перенос
        return transMat.multiply(rotZMat).multiply(rotYMat).multiply(rotXMat).multiply(scaleMat);
    }

    public static Matrix4f rotateScaleTranslate() {
        return rotateScaleTranslate(
            new Vector3f(0, 0, 0),
            new Vector3f(0, 0, 0),
            new Vector3f(1, 1, 1)
        );
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {

        // Подсчёт базиса камеры: Z (назад), X (вправо), Y (вверх)
        Vector3f resultZ = eye.subtract(target);
        try {
            resultZ = resultZ.normalize();
        } catch (ArithmeticException e) {
            resultZ = new Vector3f(0, 0, -1);
        }

        Vector3f resultX = up.cross(resultZ);
        try {
            resultX = resultX.normalize();
        } catch (ArithmeticException e) {
            resultX = new Vector3f(1, 0, 0);
        }

        Vector3f resultY = resultZ.cross(resultX);
        try {
            resultY = resultY.normalize();
        } catch (ArithmeticException e) {
            resultY = new Vector3f(0, 1, 0);
        }

        // Сборка view-матрицы: оси базиса + перенос через -axis·eye.
        float[][] matrix = new float[][]{
                {resultX.getX(), resultX.getY(), resultX.getZ(), -resultX.dot(eye)},
                {resultY.getX(), resultY.getY(), resultY.getZ(), -resultY.dot(eye)},
                {resultZ.getX(), resultZ.getY(), resultZ.getZ(), -resultZ.dot(eye)},
                {0, 0, 0, 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        // Параметры перспективы: tan(fov/2), диапазон (far-near)
        float tanHalfFov = (float) Math.tan(fov * 0.5f);
        float range = farPlane - nearPlane;

        // Сборка матрицы перспективы: w-координата формируется через -z
        float[][] data = new float[4][4];
        data[0][0] = 1.0f / (aspectRatio * tanHalfFov);
        data[1][1] = 1.0f / tanHalfFov;
        data[2][2] = -(farPlane + nearPlane) / range;
        data[2][3] = -(2.0f * farPlane * nearPlane) / range;
        data[3][2] = -1.0f;
        data[3][3] = 0.0f;

        return new Matrix4f(data);
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        // Vector3f -> Vector4f(w=1)
        Vector4f vertex4 = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1.0f);
        Vector4f result4 = matrix.multiply(vertex4);

        // Деление на W: переход из clip-space в декартово пространство
        if (Math.abs(result4.getW()) > 1e-12f) {
            return new Vector3f(
                    result4.getX() / result4.getW(),
                    result4.getY() / result4.getW(),
                    result4.getZ() / result4.getW()
            );
        }
        return new Vector3f(result4.getX(), result4.getY(), result4.getZ());
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        float screenX = (vertex.getX() + 1.0f) * 0.5f * (width - 1.0f);
        float screenY = (1.0f - vertex.getY()) * 0.5f * (height - 1.0f);
        return new Point2f(screenX, screenY);
    }
}
