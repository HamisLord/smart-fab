package dev.smartfab.parser;

import dev.smartfab.model.Triangle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StlParserASCIITest {

    private static File tempDirUtil(Path tempDir, String description) throws IOException {
        Path path = tempDir.resolve("test.txt");
        Files.writeString(path, description);
        return path.toFile();
    }

     private final StlParserASCII parser = new StlParserASCII();

    @Test
    @DisplayName("проверка добавления треугольника")
    void parsesSingleTriangleFromFileTest() {
        File file = new File("src/test/resources/SingleTriangle.stl");

        List<Triangle> result = parser.parseFile(file);

        assertEquals(1, result.size());
    }
    @Test
    @DisplayName("проверка добавления нескольких треугольников")
    void manyTrianglesParseTest() {
        File file = new File("src/test/resources/ManyTriangles");

        List<Triangle> result = parser.parseFile(file);

        assertEquals(6, result.size());

    }

    @Test
    @DisplayName("проверка ошибки при отсутствии вершины")
    void tooLowVerticesReadingTest(@TempDir Path tempDir) throws IOException {
        File file = tempDirUtil(tempDir, "vertex 0.0 0.0");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("В vertex не хватает координат"), () -> "сообщение об ошибке не указывет на проблему с вершиной:" + e.getMessage());
    }

    @Test
    @DisplayName("проверка ошибки о большем количестве вершин")
    void toMuchVerticesReadingTest(@TempDir Path tempDir) throws IOException {
        File file = tempDirUtil(tempDir, "vertex 0.0 0.0 0.0 0.0");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));
        assertTrue(e.getMessage().contains("никуда не записанные вершины"), () -> "сообщение не содержит информации о лишней вершине: " + e.getMessage());
    }



}
