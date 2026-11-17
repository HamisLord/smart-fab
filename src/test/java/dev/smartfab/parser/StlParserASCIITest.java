package dev.smartfab.parser;

import dev.smartfab.model.Point3d;
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
    void parsesSingleTriangleFromFileTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.5 0.25 -1.5
                outer loop
                vertex 7.0 3.0 5.0
                vertex 6.0 2.0 9.0
                vertex 11.0 10.0 3.0
                endloop
                endfacet
                """;
        File file = tempDirUtil(tempDir, content);

        List<Triangle> result = parser.parseFile(file);
        Point3d pointA = new Point3d(7.0, 3.0, 5.0);
        Point3d pointB = new Point3d(6.0, 2.0, 9.0);
        Point3d pointC = new Point3d(11.0, 10.0, 3.0);
        Point3d normal = new Point3d(0.5, 0.25, -1.5);


        assertEquals(1, result.size());
        assertEquals(pointA, result.getFirst().pointA());
        assertEquals(pointB, result.getFirst().pointB());
        assertEquals(pointC, result.getFirst().pointC());
        assertEquals(normal, result.getFirst().normal());
    }

    @Test
    @DisplayName("пустой файл на приём")
    void emptyInputFileTest(@TempDir Path tempDir) throws IOException {
        String content = """
                
                """;
        File file = tempDirUtil(tempDir, content);

        List<Triangle> result = parser.parseFile(file);

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("проверка добавления нескольких треугольников")
    void manyTrianglesParseTest() {

        File file = new File("src/test/resources/SquareFile");

        List<Triangle> result = parser.parseFile(file);

        assertEquals(12, result.size());

    }

    @Test
    @DisplayName("проверка ошибки при отсутствии координат")
    void tooLowCoordinatesReadingTest(@TempDir Path tempDir) throws IOException {
        File file = tempDirUtil(tempDir, "vertex 0.0 0.0");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("В vertex не хватает координат"), () -> "сообщение об ошибке не указывет на проблему с вершиной:" + e.getMessage());
    }

    @Test
    @DisplayName("проверка ошибки о большем количестве вершин")
    void toMuchCoordinatesReadingTest(@TempDir Path tempDir) throws IOException {
        String content = """
                vertex 0.1 0.0 1.0
                vertex 0.0 0.0 0.0 6.3
                vertex 1.1 0.1 0.0
                """;
        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));
        assertTrue(e.getMessage().contains("в vertex слишком много координат!"), () -> "сообщение не содержит информации о лишней вершине: " + e.getMessage());
    }

    @Test
    @DisplayName("проверка ввода не численных значений в вершину")
    void nanInputInVertexTest(@TempDir Path tempDir) throws IOException {
        File file = tempDirUtil(tempDir, "vertex 5.0 3.0 abc");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("неправильный формат числа"), () -> "сообщение не содержит ошибку о неправильном числовом вводе:" + e.getMessage());

    }

    @Test
    @DisplayName("проверка отсутствия нормали")
    void emptyNormalTest(@TempDir Path tempDir) throws IOException {
        String content = """
                outer loop
                vertex 5.0 -15.0 8.0
                vertex 3.0 2.0 1.0
                vertex 8.0 9.0 1.0
                endloop
                endfacet
                """;

        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("Нормаль отсутствует!"), () -> "ошибка не связана с нормалью:" + e.getMessage());
    }

    @Test
    @DisplayName("грань с 4-я вершинами")
    void tooMuchVerticesTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.5 0.25 -1.5
                outer loop
                vertex 7.0 3.0 5.0
                vertex 9.0 -4.0 2.0
                vertex 6.0 2.0 9.0
                vertex 11.0 10.0 3.0
                endloop
                endfacet
                """;
        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("Вершин больше 3!"), () -> "ошибка не связана с лишней вершиной! " + e.getMessage());
    }

    @Test
    @DisplayName("в грани 2 вершины")
    void lessThanNeededVerticesTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.5 0.25 -1.5
                outer loop
                vertex 7.0 3.0 5.0
                vertex 9.0 -4.0 2.0
                endloop
                endfacet
                """;

        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("Вершин меньше 3!"), () -> "Ошибка не связана с меньшим количеством вершин! " + e.getMessage());
    }

    @Test
    @DisplayName("отсутствие endfacet, но с правильной структурой")
    void verticesWithoutEndfacetButThreeVerticesTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.5 0.25 -1.5
                outer loop
                vertex 7.0 3.0 5.0
                vertex 9.0 -4.0 2.0
                vertex 6.0 2.0 9.0
                endloop
                """;

        File file = tempDirUtil(tempDir, content);
        List<Triangle> result = parser.parseFile(file);

        assertEquals(1, result.size());
        assertEquals(new Point3d(7.0, 3.0, 5.0), result.getFirst().pointA());
        assertEquals(new Point3d(9.0, -4.0, 2.0), result.getFirst().pointB());
        assertEquals(new Point3d(6.0, 2.0, 9.0), result.getFirst().pointC());
        assertEquals(new Point3d(0.5, 0.25, -1.5), result.getFirst().normal());
    }

    @Test
    @DisplayName("отсутствие endfacet и неправильная структура")
    void verticesWithoutEndfacetAndWrongStructureTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.5 0.25 1.0
                outer loop
                vertex 7.0 3.0 5.0
                vertex 6.0 2.0 9.0
                endloop
                """;

        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("в файле остались никуда не записанные вершины/нормаль!"), () -> "ошибка не связана с неправильной структурой и отсутствием endfacet! " + e.getMessage());

    }

    @Test
    @DisplayName("отображается правильная строка с ошибкой")
    void rightExceptionLineShowTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.0 0.0 1.0
                outer loop
                vertex 0.0 0.0 0.0
                vertex 0.0 0.0 1.0
                vertex 0.0 1.0 1.0
                endloop
                endfacet
                
                facet normal 0.0 0.0 1.0
                outer loop
                vertex 0.0 0.0
                vertex  0.0 0.0 1.0
                vertex  0.0 1.0 1.0
                endloop
                endfacet
                """;
        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("В vertex не хватает координат."), () -> "ошибка не связана с нехваткой вершин: " + e.getMessage());
        assertTrue(e.getMessage().contains("номер строки в файле: 11"), () -> "ошибка не связана с номером строки: " + e.getMessage());
    }

    @Test
    @DisplayName("отсутствует endfacet, а за ним вторая вершина")
    void verticesWithoutEndfacetAndNextVerticesAfterTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.5 0.25 -1.5
                outer loop
                vertex 7.0 3.0 5.0
                vertex 9.0 -4.0 2.0
                vertex 6.0 2.0 9.0
                endloop
                
                facet normal 0.5 0.25 -1.5
                outer loop
                vertex -7.0 -97.0 8.0
                vertex 44.0 -4.0 25.0
                vertex 8.0 0.0 9.0
                endloop
                endfacet
                """;

        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("строка ошибки: 8"));
        assertTrue(e.getMessage().contains("предыдущая вершина не закрыта!"), () -> "ошибка не связана с отсутствующей endfacet: " + e.getMessage());
    }

    @Test
    @DisplayName("проверка infinity числа")
    void isFinityTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.5 0.25 -1.5
                outer loop
                vertex -7.0 -97.0 8.0
                vertex 7.0 -4.0 25.0
                vertex 8.0 1e999 9.0
                endloop
                endfacet
                """;

        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("в строке 5"), () -> "ошибка не связана с infinity");
        assertTrue(e.getMessage().contains("неправильный формат числа"), () -> "ошибка не связана с infinity: " + e.getMessage());
    }

    @Test
    @DisplayName("проверка на NaN")
    void isNanTest(@TempDir Path tempDir) throws IOException {
        String content = """
                facet normal 0.5 0.25 -1.5
                outer loop
                vertex -7.0 -97.0 8.0
                vertex 7.0 -4.0 NaN
                vertex 8.0 8.3 9.0
                endloop
                endfacet
                """;

        File file = tempDirUtil(tempDir, content);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parser.parseFile(file));

        assertTrue(e.getMessage().contains("в строке 4"), () -> "ошибка не связана с Nan");
        assertTrue(e.getMessage().contains("неправильный формат числа"), () -> "ошибка не связана с NaN: " + e.getMessage());
    }
}
