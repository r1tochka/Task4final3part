package com.cgvsu.controller;

//применение аффинных преобразований к Model через удобные методы.
//перенос/поворот/масштаб и сброс трансформаций.

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;

public class AffineTransformController implements ModelTransformer {

    @Override
    public void translate(Model model, float dx, float dy, float dz) {
        model.translate(new Vector3f(dx, dy, dz));
    }

    @Override
    public void rotate(Model model, float rx, float ry, float rz) {
        model.rotate(new Vector3f(rx, ry, rz));
    }

    @Override
    public void scale(Model model, float sx, float sy, float sz) {
        model.scale(new Vector3f(sx, sy, sz));
    }

    @Override
    public void resetTransformations(Model model) {
        model.resetTransformations();
    }

    @Override
    public void translateX(Model model, float delta) {
        model.translateX(delta);
    }

    @Override
    public void translateY(Model model, float delta) {
        model.translateY(delta);
    }

    @Override
    public void translateZ(Model model, float delta) {
        model.translateZ(delta);
    }

    @Override
    public void rotateX(Model model, float delta) {
        model.rotateX(delta);
    }

    @Override
    public void rotateY(Model model, float delta) {
        model.rotateY(delta);
    }

    @Override
    public void rotateZ(Model model, float delta) {
        model.rotateZ(delta);
    }

    @Override
    public void scaleX(Model model, float factor) {
        model.scaleX(factor);
    }

    @Override
    public void scaleY(Model model, float factor) {
        model.scaleY(factor);
    }

    @Override
    public void scaleZ(Model model, float factor) {
        model.scaleZ(factor);
    }
}
