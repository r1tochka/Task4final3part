package com.cgvsu.objreader;

import com.cgvsu.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjReaderReadContentTest {

    @Test
    void readContentParsesTransformedFlagAndCollectsWarnings() {
        String obj = "# TRANSFORMED: true\n" +
                "v 0 0 0\n" +
                "v 1 0 0\n" +
                "v 0 1 0\n" +
                "f 1 2 3\n" +
                "unknown_token 123\n";

        ObjReader.ReadResult rr = ObjReader.readContent(obj);
        Model m = rr.getModel();

        assertTrue(m.isTransformed());
        assertEquals(3, m.getVertices().size());
        assertEquals(1, m.getPolygons().size());
        assertEquals(1, rr.getWarnings().size());
        assertTrue(rr.getWarnings().get(0).contains("Unknown token"));
    }

    @Test
    void readContentThrowsOnEmpty() {
        assertThrows(ObjReaderException.class, () -> ObjReader.readContent("\n  \n"));
    }

    @Test
    void readContentThrowsWhenNoVerticesPresent() {
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.readContent("f 1 2 3\nvt 0.1 0.2\n"));
        assertTrue(ex.getMessage().contains("OBJ has no vertex coordinates"));
    }

    @Test
    void readContentThrowsWhenOnlyGarbageTextPresent() {
        ObjReaderException ex = assertThrows(ObjReaderException.class, () -> ObjReader.readContent("hello world\njust words\n"));
        assertTrue(ex.getMessage().contains("OBJ has no vertex coordinates"));
    }
}
