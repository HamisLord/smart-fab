package dev.smartfab.model;

// порядок точек ABC имеет значение, так как если смотреть на треугольник снаружи, то вершины
// идут против часовой стрелки
public record Triangle(Point3d pointA, Point3d pointB, Point3d pointC, Point3d normal) {
}
