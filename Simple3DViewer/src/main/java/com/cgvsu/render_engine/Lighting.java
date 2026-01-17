package com.cgvsu.render_engine;

 // модель освещения (ambient + diffuse).
 // привязка к камере (cameraPosition -> cameraTarget).

 
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import javafx.scene.paint.Color;

public class Lighting {

    private Vector3f lightDirection;
    private final Color ambientColor;
    private final Color diffuseColor;
    private final float ambientIntensity;
    private final float diffuseIntensity;

    public Lighting(Vector3f cameraPosition, Vector3f cameraTarget,
                    float ambientIntensity, float diffuseIntensity) {
        this.lightDirection = cameraTarget.subtract(cameraPosition).normalize();
        this.ambientIntensity = Math.max(0.0f, Math.min(1.0f, ambientIntensity));
        this.diffuseIntensity = Math.max(0.0f, Math.min(1.0f, diffuseIntensity));
        this.ambientColor = Color.WHITE;
        this.diffuseColor = Color.WHITE;
    }

    public Lighting(Vector3f cameraPosition, Vector3f cameraTarget,
                    Color ambientColor, Color diffuseColor,
                    float ambientIntensity, float diffuseIntensity) {
        this.lightDirection = cameraTarget.subtract(cameraPosition).normalize();
        this.ambientIntensity = Math.max(0.0f, Math.min(1.0f, ambientIntensity));
        this.diffuseIntensity = Math.max(0.0f, Math.min(1.0f, diffuseIntensity));
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
    }

    public void update(Vector3f cameraPosition, Vector3f cameraTarget, Matrix4f viewMatrix) {
        // обновление направления света: привязка к направлению взгляда камеры
        this.lightDirection = cameraTarget.subtract(cameraPosition).normalize();
    }

    public float computeLightingIntensity(Vector3f normal, Vector3f vertexPosition, Vector3f cameraPosition) {
        Vector3f lightDir = cameraPosition.subtract(vertexPosition).normalize();
        Vector3f n = normal.normalize();
        float diffuse = n.dot(lightDir);
        return Math.max(0.0f, Math.min(1.0f, diffuse));
    }

    public Color shadeColor(Color baseColor, float intensity) {
        double ambientR = baseColor.getRed() * ambientIntensity;
        double ambientG = baseColor.getGreen() * ambientIntensity;
        double ambientB = baseColor.getBlue() * ambientIntensity;
        double diffuseR = baseColor.getRed() * diffuseIntensity * intensity;
        double diffuseG = baseColor.getGreen() * diffuseIntensity * intensity;
        double diffuseB = baseColor.getBlue() * diffuseIntensity * intensity;
        double r = ambientR + diffuseR;
        double g = ambientG + diffuseG;
        double b = ambientB + diffuseB;
        r = Math.max(0.0, Math.min(1.0, r));
        g = Math.max(0.0, Math.min(1.0, g));
        b = Math.max(0.0, Math.min(1.0, b));

        return new Color(r, g, b, baseColor.getOpacity());
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }
}
