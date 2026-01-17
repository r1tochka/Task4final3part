package com.cgvsu.controller;

import com.cgvsu.model.Model;

public interface ModelTransformer {
    void translate(Model model, float dx, float dy, float dz);
    void rotate(Model model, float rx, float ry, float rz);
    void scale(Model model, float sx, float sy, float sz);
    void resetTransformations(Model model);

    void translateX(Model model, float delta);
    void translateY(Model model, float delta);
    void translateZ(Model model, float delta);

    void rotateX(Model model, float delta);
    void rotateY(Model model, float delta);
    void rotateZ(Model model, float delta);

    void scaleX(Model model, float factor);
    void scaleY(Model model, float factor);
    void scaleZ(Model model, float factor);
}
