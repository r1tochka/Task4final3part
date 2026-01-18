package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObjReaderTest {

    @Test
    public void testParseVertex01() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.03f);
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testParseVertex02() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.10f);
        Assertions.assertNotEquals(expectedResult, result);
    }

    @Test
    public void testParseVertex03() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("ab", "o", "ba"));
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.parseVertex(wordsInLineWithoutToken, 10));
        assertTrue(ex.getMessage().contains("Error parsing OBJ file on line: 10."));
        assertTrue(ex.getMessage().contains("Invalid vertex coordinate format"));
    }

    @Test
    public void testParseVertex04() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0"));
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.parseVertex(wordsInLineWithoutToken, 10));
        assertTrue(ex.getMessage().contains("Error parsing OBJ file on line: 10."));
        assertTrue(ex.getMessage().contains("Vertex requires at least 3 coordinates"));
    }

    @Test
    public void testParseVertex05() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "1.0"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 1);
        assertEquals(new Vector3f(1.0f, 2.0f, 3.0f), result);
    }

    @Test
    public void testParseTextureVertexWithWIsIgnored() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1.0", "2.0", "2.0"));
        Vector2f result = ObjReader.parseTextureVertex(words, 1);
        assertEquals(new Vector2f(1.0f, 2.0f), result);
    }

    @Test
    public void testParseTextureVertexWithSingleCoordinateDefaultsVToZero() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("0.25"));
        Vector2f result = ObjReader.parseTextureVertex(words, 1);
        assertEquals(new Vector2f(0.25f, 0.0f), result);
    }

    @Test
    public void testParseTextureVertexWithTwoCoordinates() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("0.25", "0.75"));
        Vector2f result = ObjReader.parseTextureVertex(words, 1);
        assertEquals(new Vector2f(0.25f, 0.75f), result);
    }

    @Test
    public void testParseTextureVertexThrowsOnInvalidNumbers() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("u", "v"));
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.parseTextureVertex(words, 7));
        assertTrue(ex.getMessage().contains("Error parsing OBJ file on line: 7."));
        assertTrue(ex.getMessage().contains("Invalid texture vertex coordinate format"));
    }

    @Test
    public void testParseTextureVertexAllowsZeroW() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("0.1", "0.2", "0.0"));
        Vector2f result = ObjReader.parseTextureVertex(words, 1);
        assertEquals(new Vector2f(0.1f, 0.2f), result);
    }

    @Test
    public void testParseFaceVertexTextureNormal() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1/1/1", "2/2/2", "3/3/3"));
        Polygon p = ObjReader.parseFace(words, 1);
        assertEquals(Arrays.asList(0, 1, 2), p.getVertexIndices());
        assertEquals(Arrays.asList(0, 1, 2), p.getTextureVertexIndices());
        assertEquals(Arrays.asList(0, 1, 2), p.getNormalIndices());
    }

    @Test
    public void testParseFaceVertexTexture() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1/1", "2/2", "3/3"));
        Polygon p = ObjReader.parseFace(words, 1);
        assertEquals(Arrays.asList(0, 1, 2), p.getVertexIndices());
        assertEquals(Arrays.asList(0, 1, 2), p.getTextureVertexIndices());
        assertTrue(p.getNormalIndices().isEmpty());
    }

    @Test
    public void testParseFaceVertexNormalWithDoubleSlash() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1//1", "2//2", "3//3"));
        Polygon p = ObjReader.parseFace(words, 1);
        assertEquals(Arrays.asList(0, 1, 2), p.getVertexIndices());
        assertTrue(p.getTextureVertexIndices().isEmpty());
        assertEquals(Arrays.asList(0, 1, 2), p.getNormalIndices());
    }

    @Test
    public void testParseFaceThrowsOnZeroIndex() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("0/1/1", "2/2/2", "3/3/3"));
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.parseFace(words, 3));
        assertTrue(ex.getMessage().contains("Negative vertex index not supported"));
    }

    @Test
    public void testParseFaceThrowsOnInconsistentTextureIndices() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1/1", "2", "3/3"));
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.parseFace(words, 3));
        assertTrue(ex.getMessage().contains("Inconsistent texture coordinate indices"));
    }

    @Test
    public void testParseFaceThrowsOnTooManyVertices() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.parseFace(words, 3));
        assertTrue(ex.getMessage().contains("Face has too many vertices"));
    }

    @Test
    public void testParseFaceThrowsOnInvalidFaceTokenFormat() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1/2/3/4", "2/2/2", "3/3/3"));
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.parseFace(words, 3));
        assertTrue(ex.getMessage().contains("Invalid face vertex format"));
    }

    @Test
    public void testParseNormalWithTooManyArguments() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "4.0"));
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.parseNormal(words, 1));
        assertTrue(ex.getMessage().contains("Normal requires exactly 3 coordinates"));
    }
}
