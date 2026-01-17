package com.cgvsu.objreader;

// Назначение: чтение OBJ из строки и сборка Model.
// Поддержка токенов: v/vt/vn/f, игнорирование прочих строк.
// Разбор граней: v/vt/vn и варианты v//vn.

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class FlexibleObjReader {

    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    public static Model read(String fileContent) {
        return ObjReader.read(fileContent);
    }

    protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        return ObjReader.parseVertex(wordsInLineWithoutToken, lineInd);
    }

    protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        return ObjReader.parseTextureVertex(wordsInLineWithoutToken, lineInd);
    }

    protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        return ObjReader.parseNormal(wordsInLineWithoutToken, lineInd);
    }

    protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        return ObjReader.parseFace(wordsInLineWithoutToken, lineInd);
    }

    protected static void parseFaceWord(
            String wordInLine,
            ArrayList<Integer> onePolygonVertexIndices,
            ArrayList<Integer> onePolygonTextureVertexIndices,
            ArrayList<Integer> onePolygonNormalIndices,
            int lineInd) {
        ObjReader.parseFaceWord(wordInLine, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
    }
}