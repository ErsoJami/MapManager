package com.example.mapmanager.models;

import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;

public class Waypoint {
    private String name;
    private String description;
    private Point point;

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Waypoint() {}
    public Waypoint(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public Waypoint(String name, String description, Point point) {
        this.name = name;
        this.description = description;
        this.point = point;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
