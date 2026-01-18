package com.cgvsu.objreader;

import com.cgvsu.model.Model;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ObjReaderReadFileTest {

    @Test
    void readFileLoadsModelFromDisk() throws IOException {
        Path tmp = Files.createTempFile("cgvsu-test-", ".obj");
        tmp.toFile().deleteOnExit();

        String obj = "# TRANSFORMED: true\n" +
                "v 0 0 0\n" +
                "v 1 0 0\n" +
                "v 0 1 0\n" +
                "f 1 2 3\n";

        Files.writeString(tmp, obj, StandardCharsets.UTF_8);

        ObjReader.ReadResult rr = ObjReader.readFile(tmp);
        Model m = rr.getModel();

        assertNotNull(m);
        assertEquals(3, m.getVertices().size());
        assertEquals(1, m.getPolygons().size());
        assertTrue(m.isTransformed());
    }

    @Test
    void readFileThrowsWhenFileDoesNotExist() {
        Path p = Path.of("Z:/definitely-not-existing-path/cgvsu.obj");
        assertThrows(IOException.class, () -> ObjReader.readFile(p));
    }
}
