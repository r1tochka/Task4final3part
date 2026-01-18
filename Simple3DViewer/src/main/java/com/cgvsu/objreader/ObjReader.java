package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ObjReader {

    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";
    private static final String OBJ_COMMENT_TOKEN = "#";
    private static final String OBJ_OBJECT_TOKEN = "o";
    private static final String OBJ_GROUP_TOKEN = "g";
    private static final String OBJ_USEMTL_TOKEN = "usemtl";
    private static final String OBJ_MTLLIB_TOKEN = "mtllib";

    public static class ReadResult {
        private final Model model;
        private final List<String> warnings;

        public ReadResult(Model model, List<String> warnings) {
            this.model = model;
            this.warnings = warnings;
        }

        public Model getModel() { return model; }
        public List<String> getWarnings() { return warnings; }
    }

    // Основной метод чтения (для обратной совместимости с GuiController)
    public static Model read(final String fileContent) {
        try {
            ReadResult result = readContent(fileContent);
            return result.getModel();
        } catch (ObjReaderException e) {
            throw e;
        } catch (Exception e) {
            throw new ObjReaderException(e.getMessage() != null ? e.getMessage() : "Unexpected error", 0);
        }
    }

    public static ReadResult readContent(String fileContent) throws ObjReaderException {
        Model model = new Model();
        ArrayList<String> warnings = new ArrayList<>();

        if (fileContent == null || fileContent.trim().isEmpty()) {
            throw new ObjReaderException("File content is empty", 0);
        }

        String[] lines = fileContent.split("\r?\n");

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex].trim();
            if (line.isEmpty()) {
                continue;
            }
            ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));

            if (wordsInLine.isEmpty()) {
                continue;
            }

            final String token = wordsInLine.get(0);
            wordsInLine.remove(0);

            try {
                switch (token) {
                    case OBJ_COMMENT_TOKEN:
                        handleComment(wordsInLine, lineIndex + 1, model, warnings);
                        break;
                    case OBJ_VERTEX_TOKEN:
                        model.addVertex(parseVertex(wordsInLine, lineIndex + 1));
                        break;
                    case OBJ_TEXTURE_TOKEN:
                        model.addTextureVertex(parseTextureVertex(wordsInLine, lineIndex + 1));
                        break;
                    case OBJ_NORMAL_TOKEN:
                        model.addNormal(parseNormal(wordsInLine, lineIndex + 1));
                        break;
                    case OBJ_FACE_TOKEN:
                        model.addPolygon(parseFace(wordsInLine, lineIndex + 1));
                        break;
                    case OBJ_OBJECT_TOKEN:
                    case OBJ_GROUP_TOKEN:
                    case OBJ_USEMTL_TOKEN:
                    case OBJ_MTLLIB_TOKEN:
                        break;
                    default:
                        warnings.add("Line " + (lineIndex + 1) + ": Unknown token '" + token + "' - skipping");
                        break;
                }
            } catch (ObjReaderException e) {
                throw e;
            } catch (Exception e) {
                throw new ObjReaderException("Unexpected error: " + e.getMessage(), lineIndex + 1);
            }
        }

        if (model.getVertices().isEmpty()) {
            throw new ObjReaderException("OBJ has no vertex coordinates", 0);
        }

        validateModel(model, warnings);

        return new ReadResult(model, warnings);
    }

    public static ReadResult readFile(Path filePath) throws IOException, ObjReaderException {
        if (!Files.exists(filePath)) {
            throw new IOException("File does not exist: " + filePath);
        }

        if (!Files.isReadable(filePath)) {
            throw new IOException("File is not readable: " + filePath);
        }

        String fileContent;
        try {
            fileContent = Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Failed to read file with UTF-8 encoding: " + e.getMessage(), e);
        }

        return readContent(fileContent);
    }

    private static void handleComment(ArrayList<String> wordsInLine, int lineInd, Model model, ArrayList<String> warnings) {
        if (wordsInLine.size() >= 2 && wordsInLine.get(0).equals("TRANSFORMED:")) {
            try {
                boolean isTransformed = Boolean.parseBoolean(wordsInLine.get(1));
                model.setTransformed(isTransformed);
            } catch (Exception e) {
                warnings.add("Line " + lineInd + ": Invalid TRANSFORMED flag format - ignoring");
            }
        }
    }

    protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) throws ObjReaderException {
        if (wordsInLineWithoutToken.size() < 3) {
            throw new ObjReaderException("Vertex requires at least 3 coordinates (x, y, z)", lineInd);
        }

        if (wordsInLineWithoutToken.size() > 4) {
            throw new ObjReaderException("Vertex has too many coordinates (max 4: x, y, z, [w])", lineInd);
        }

        try {
            float x = parseFloatSafe(wordsInLineWithoutToken.get(0), lineInd);
            float y = parseFloatSafe(wordsInLineWithoutToken.get(1), lineInd);
            float z = parseFloatSafe(wordsInLineWithoutToken.get(2), lineInd);
            if (wordsInLineWithoutToken.size() == 4) {
                float w = parseFloatSafe(wordsInLineWithoutToken.get(3), lineInd);
                if (Math.abs(w) < 1e-6) {
                    throw new ObjReaderException("Vertex w coordinate cannot be zero", lineInd);
                }
                x /= w;
                y /= w;
                z /= w;
            }

            return new Vector3f(x, y, z);

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid vertex coordinate format: " + e.getMessage(), lineInd);
        }
    }

    protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) throws ObjReaderException {
        if (wordsInLineWithoutToken.size() < 1) {
            throw new ObjReaderException("Texture vertex requires at least 1 coordinate (u)", lineInd);
        }

        if (wordsInLineWithoutToken.size() > 3) {
            throw new ObjReaderException("Texture vertex has too many coordinates (max 3: u, [v], [w])", lineInd);
        }

        try {
            float u = parseFloatSafe(wordsInLineWithoutToken.get(0), lineInd);
            float v = 0.0f;

            if (wordsInLineWithoutToken.size() >= 2) {
                v = parseFloatSafe(wordsInLineWithoutToken.get(1), lineInd);
            }
            if (wordsInLineWithoutToken.size() == 3) {
                parseFloatSafe(wordsInLineWithoutToken.get(2), lineInd);
            }

            return new Vector2f(u, v);

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid texture vertex coordinate format: " + e.getMessage(), lineInd);
        }
    }

    protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) throws ObjReaderException {
        if (wordsInLineWithoutToken.size() != 3) {
            throw new ObjReaderException("Normal requires exactly 3 coordinates (x, y, z)", lineInd);
        }

        try {
            float x = parseFloatSafe(wordsInLineWithoutToken.get(0), lineInd);
            float y = parseFloatSafe(wordsInLineWithoutToken.get(1), lineInd);
            float z = parseFloatSafe(wordsInLineWithoutToken.get(2), lineInd);

            Vector3f normal = new Vector3f(x, y, z);
            float length = (float) Math.sqrt(x*x + y*y + z*z);
            if (length > 1e-6) {
                if (Math.abs(length - 1.0f) > 1e-3) {
                    normal = new Vector3f(x/length, y/length, z/length);
                }
            } else {
                throw new ObjReaderException("Normal vector cannot be zero length", lineInd);
            }

            return normal;

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid normal coordinate format: " + e.getMessage(), lineInd);
        }
    }

    protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) throws ObjReaderException {
        if (wordsInLineWithoutToken.size() < 3) {
            throw new ObjReaderException("Face requires at least 3 vertices", lineInd);
        }

        if (wordsInLineWithoutToken.size() > 4) {
            throw new ObjReaderException("Face has too many vertices (max 4 for triangle/quad)", lineInd);
        }

        ArrayList<Integer> vertexIndices = new ArrayList<>();
        ArrayList<Integer> textureVertexIndices = new ArrayList<>();
        ArrayList<Integer> normalIndices = new ArrayList<>();

        for (String faceWord : wordsInLineWithoutToken) {
            parseFaceWord(faceWord, vertexIndices, textureVertexIndices, normalIndices, lineInd);
        }
        validateFaceIndices(vertexIndices, textureVertexIndices, normalIndices, lineInd);

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(vertexIndices);
        polygon.setTextureVertexIndices(textureVertexIndices);
        polygon.setNormalIndices(normalIndices);

        return polygon;
    }

    protected static void parseFaceWord(
            String wordInLine,
            ArrayList<Integer> vertexIndices,
            ArrayList<Integer> textureVertexIndices,
            ArrayList<Integer> normalIndices,
            int lineInd) throws ObjReaderException {

        if (wordInLine.isEmpty()) {
            throw new ObjReaderException("Empty face vertex specification", lineInd);
        }

        String[] wordIndices = wordInLine.split("/");

        try {
            // Поддерживаемые форматы: v | v/vt | v//vn | v/vt/vn
            switch (wordIndices.length) {
                case 1: // v
                    vertexIndices.add(parseIntSafe(wordIndices[0], lineInd) - 1);
                    break;

                case 2: // v/vt or v//
                    vertexIndices.add(parseIntSafe(wordIndices[0], lineInd) - 1);
                    if (!wordIndices[1].isEmpty()) {
                        textureVertexIndices.add(parseIntSafe(wordIndices[1], lineInd) - 1);
                    }
                    break;

                case 3: // v/vt/vn or v//vn
                    vertexIndices.add(parseIntSafe(wordIndices[0], lineInd) - 1);
                    if (!wordIndices[1].isEmpty()) {
                        textureVertexIndices.add(parseIntSafe(wordIndices[1], lineInd) - 1);
                    }
                    normalIndices.add(parseIntSafe(wordIndices[2], lineInd) - 1);
                    break;

                default:
                    throw new ObjReaderException("Invalid face vertex format: " + wordInLine, lineInd);
            }
        } catch (NumberFormatException e) {
            throw new ObjReaderException("Invalid face index format: " + e.getMessage(), lineInd);
        }
    }

    private static void validateFaceIndices(
            ArrayList<Integer> vertexIndices,
            ArrayList<Integer> textureVertexIndices,
            ArrayList<Integer> normalIndices,
            int lineInd) throws ObjReaderException {
        // Проверка индексов: поддержка только неотрицательных индексов
        for (int i = 0; i < vertexIndices.size(); i++) {
            if (vertexIndices.get(i) < 0) {
                throw new ObjReaderException("Negative vertex index not supported", lineInd);
            }
        }
        // Инвариант: если UV/нормали присутствуют, размер списка должен совпадать с числом вершин грани
        if (!textureVertexIndices.isEmpty() && textureVertexIndices.size() != vertexIndices.size()) {
            throw new ObjReaderException("Inconsistent texture coordinate indices", lineInd);
        }
        if (!normalIndices.isEmpty() && normalIndices.size() != vertexIndices.size()) {
            throw new ObjReaderException("Inconsistent normal indices", lineInd);
        }
    }

    private static void validateModel(Model model, ArrayList<String> warnings) {
        // Валидация модели: наличие используемых UV/нормалей и проверка индексов граней
        if (!model.getTextureVertices().isEmpty()) {
            boolean hasTextureFaces = model.getPolygons().stream()
                    .anyMatch(p -> !p.getTextureVertexIndices().isEmpty());
            if (!hasTextureFaces) {
                warnings.add("Model has texture coordinates but no faces use them");
            }
        }
        if (!model.getNormals().isEmpty()) {
            boolean hasNormalFaces = model.getPolygons().stream()
                    .anyMatch(p -> !p.getNormalIndices().isEmpty());
            if (!hasNormalFaces) {
                warnings.add("Model has normals but no faces use them");
            }
        }
        for (int i = 0; i < model.getPolygons().size(); i++) {
            Polygon polygon = model.getPolygons().get(i);
            for (int vertexIndex : polygon.getVertexIndices()) {
                if (vertexIndex < 0 || vertexIndex >= model.getVertices().size()) {
                    warnings.add("Face " + (i + 1) + " references vertex " + (vertexIndex + 1) +
                            " but model only has " + model.getVertices().size() + " vertices");
                }
            }
        }
    }

    private static float parseFloatSafe(String value, int lineInd) throws NumberFormatException {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            if (value.equalsIgnoreCase("inf") || value.equalsIgnoreCase("+inf")) {
                return Float.POSITIVE_INFINITY;
            }
            if (value.equalsIgnoreCase("-inf")) {
                return Float.NEGATIVE_INFINITY;
            }
            if (value.equalsIgnoreCase("nan")) {
                return Float.NaN;
            }
            throw e;
        }
    }

    private static int parseIntSafe(String value, int lineInd) throws NumberFormatException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw e;
        }
    }
}
