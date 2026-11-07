package dev.smartfab;

import dev.smartfab.model.Triangle;
import dev.smartfab.parser.StlParserASCII;

import java.io.File;
import java.util.List;

public class App {
    public static void main(String[] args) {
        StlParserASCII parserASCII = new StlParserASCII();
        File path = new File("src/main/resources/file.stl");

        List<Triangle> list = parserASCII.parseFile(path);
        for (Triangle triangle : list) {
            System.out.println(triangle);
        }
    }
}
