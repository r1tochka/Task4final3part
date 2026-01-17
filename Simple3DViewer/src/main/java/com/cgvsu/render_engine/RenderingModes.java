package com.cgvsu.render_engine;

//  флаги режимов рендера (каркас/текстура/освещение).

public class RenderingModes {
    private boolean drawWireframe;
    private boolean useTexture;
    private boolean useLighting;

    public RenderingModes() {
        this.drawWireframe = false;
        this.useTexture = false;
        this.useLighting = false;
    }

    public RenderingModes(boolean drawWireframe, boolean useTexture, boolean useLighting) {
        this.drawWireframe = drawWireframe;
        this.useTexture = useTexture;
        this.useLighting = useLighting;
    }

    public boolean isDrawWireframe() {
        return drawWireframe;
    }

    public void setDrawWireframe(boolean drawWireframe) {
        this.drawWireframe = drawWireframe;
    }

    public boolean isUseTexture() {
        return useTexture;
    }

    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    public boolean isUseLighting() {
        return useLighting;
    }

    public void setUseLighting(boolean useLighting) {
        this.useLighting = useLighting;
    }

    public boolean hasAnyModeEnabled() {
        return useTexture || useLighting;
    }
}

