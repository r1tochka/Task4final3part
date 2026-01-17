package com.cgvsu.objreader;


import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    public static Model read(final String fileContent) {
        Model result = new Model();
        boolean isTransformed = false;

        int lineInd = 0;
        Scanner scanner = new Scanner(fileContent);
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            ++lineInd;

            if (line.trim().isEmpty()) {
                continue;
            }

            ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
            if (wordsInLine.isEmpty()) {
                continue;
            }

            final String token = wordsInLine.get(0);
            wordsInLine.remove(0);

            try {
                if (token.equals("#") && wordsInLine.size() > 0 &&
                        wordsInLine.get(0).equals("TRANSFORMED:")) {
                    isTransformed = Boolean.parseBoolean(wordsInLine.get(1));
                    continue;
                }

                switch (token) {
                    case OBJ_VERTEX_TOKEN -> result.addVertex(parseVertex(wordsInLine, lineInd));
                    case OBJ_TEXTURE_TOKEN -> result.addTextureVertex(parseTextureVertex(wordsInLine, lineInd));
                    case OBJ_NORMAL_TOKEN -> result.addNormal(parseNormal(wordsInLine, lineInd));
                    case OBJ_FACE_TOKEN -> result.addPolygon(parseFace(wordsInLine, lineInd));
                    default -> {
                    }
                }
            } catch (ObjReaderException e) {
                throw e;
            } catch (Exception e) {
                throw new ObjReaderException(e.getMessage() != null ? e.getMessage() : "Unexpected error", lineInd);
            }
        }

        result.setTransformed(isTransformed);
        return result;
    }

    public static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, final int lineInd) {
        if (wordsInLineWithoutToken.size() < 3) {
            throw new ObjReaderException("Too few vertex arguments.", lineInd);
        }

        try {
            float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
            float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
            float z = Float.parseFloat(wordsInLineWithoutToken.get(2));
            return new Vector3f(x, y, z);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);
        }
    }

    public static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, final int lineInd) {
        if (wordsInLineWithoutToken.size() < 1) {
            throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
        }

        try {
            float u = Float.parseFloat(wordsInLineWithoutToken.get(0));
            float v = 0.0f;
            if (wordsInLineWithoutToken.size() >= 2) {
                v = Float.parseFloat(wordsInLineWithoutToken.get(1));
            }
            return new Vector2f(u, v);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);
        }
    }

    public static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, final int lineInd) {
        if (wordsInLineWithoutToken.size() < 3) {
            throw new ObjReaderException("Too few normal arguments.", lineInd);
        }

        try {
            float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
            float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
            float z = Float.parseFloat(wordsInLineWithoutToken.get(2));

            return new Vector3f(x, y, z);
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);
        }
    }

    public static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, final int lineInd) {
        ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
        ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
        ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

        for (String s : wordsInLineWithoutToken) {
            parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
        }

        Polygon result = new Polygon();
        result.setVertexIndices(onePolygonVertexIndices);
        result.setTextureVertexIndices(onePolygonTextureVertexIndices);
        result.setNormalIndices(onePolygonNormalIndices);
        return result;
    }

    protected static void parseFaceWord(
            String wordInLine,
            ArrayList<Integer> onePolygonVertexIndices,
            ArrayList<Integer> onePolygonTextureVertexIndices,
            ArrayList<Integer> onePolygonNormalIndices,
            int lineInd) {
        try {
            String[] wordIndices = wordInLine.split("/");
            switch (wordIndices.length) {
                case 1 -> {
                    onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                }
                case 2 -> {
                    onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                    onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
                }
                case 3 -> {
                    onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
                    onePolygonNormalIndices.add(Integer.parseInt(wordIndices[2]) - 1);
                    if (!wordIndices[1].equals("")) {
                        onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
                    }
                }

                default -> {
                    throw new ObjReaderException("Invalid element size.", lineInd);
                }
            }

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse int value.", lineInd);
        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few arguments.", lineInd);
        }
    }
}
