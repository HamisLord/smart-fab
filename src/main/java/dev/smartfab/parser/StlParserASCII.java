package dev.smartfab.parser;

import dev.smartfab.model.Point3d;
import dev.smartfab.model.Triangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class StlParserASCII {

    private static final Logger log = LoggerFactory.getLogger(StlParserASCII.class);

    public List<Triangle> parseFile(File file) {

        List<Triangle> triangles = new ArrayList<>();

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            List<Point3d> currentVertices = new ArrayList<>();
            Point3d normal = null;
            int lineCounter = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCounter++;
                String[] parts = line.strip().split("\\s+");

                switch (parts[0].toLowerCase(Locale.ROOT)) {
                    case "facet" -> {
                        if (!currentVertices.isEmpty()) {
                            throw new IllegalArgumentException("предыдущая вершина не закрыта!, строка ошибки: " + lineCounter);

                        }
                        if (parts.length >= 5) {
                            normal = new Point3d(
                                    parseToDouble(parts[2], lineCounter),
                                    parseToDouble(parts[3], lineCounter),
                                    parseToDouble(parts[4], lineCounter));
                        } else {
                            throw new IllegalArgumentException("В facet normal не хватает координат. " +
                                    "Номер строки в файле: " +
                                    lineCounter);
                        }

                    }
                    case "vertex" -> {
                        if (parts.length == 4) {
                            currentVertices.add( new Point3d(
                                    parseToDouble(parts[1], lineCounter),
                                    parseToDouble(parts[2], lineCounter),
                                    parseToDouble(parts[3], lineCounter)));
                        } if (parts.length < 4){

                            throw new IllegalArgumentException("В vertex не хватает координат. " +
                                    "номер строки в файле: " +
                                    lineCounter);
                        } if (parts.length > 4) {
                            throw new IllegalArgumentException("в vertex слишком много координат!" +
                                    "номер строки в файле: " +
                                    lineCounter);
                        }

                    }
                    case "endfacet" -> {
                        if (currentVertices.size() > 3) {
                            throw new IllegalArgumentException("Вершин больше 3! Номер строки в файле: " + lineCounter);
                        } if (currentVertices.size() < 3 ) {
                            throw new IllegalArgumentException("Вершин меньше 3! Номер строки в файле: " + lineCounter);
                        } if (normal == null) {
                            throw new IllegalArgumentException("Нормаль отсутствует! Номер строки в файле: " + lineCounter);
                        }else {
                            triangles.add(new Triangle(
                                    currentVertices.get(0),
                                    currentVertices.get(1),
                                    currentVertices.get(2),
                                    normal));
                        }
                        currentVertices.clear();
                        normal = null;
                    }
                }
            }
            if (!(currentVertices.isEmpty()) || normal != null) {
                if (currentVertices.size() == 3 && normal != null) {
                    log.warn("В накопителе остались неиспользованные вершины! возможно файл незавершён или поврежден!" +
                            " Но вершины были записаны, так как соответствуют нормам для создания треугольника место пойманной ошибки строка № {}", lineCounter);
                    triangles.add(new Triangle(
                            currentVertices.get(0),
                            currentVertices.get(1),
                            currentVertices.get(2),
                            normal));
                } else {
                    throw new IllegalArgumentException("в файле остались никуда не записанные вершины/нормаль! номер строки: " + lineCounter + ", количество вершин: " + currentVertices.size());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("не удалось прочитать файл", e);
        }

        return triangles;
    }

    private static double parseToDouble(String value, int lineNumber) {
        try {
            double parsedDouble= Double.parseDouble(value);
            if (Double.isFinite(parsedDouble)) {
                return Double.parseDouble(value);
            } else {
                throw new NumberFormatException("число: " + value + " является бесконечным или NaN");
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("неправильный формат числа, дано: " + value + ", в строке " + lineNumber, e);
        }
    }
}
