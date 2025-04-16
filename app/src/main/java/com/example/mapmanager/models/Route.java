package com.example.mapmanager.models;

import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.PlacemarkMapObject;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private ArrayList<PlacemarkMapObject> placemarkMapObjects;
    private String id;
    private String name;
    private String description;

    public Route() {}

    public Route(ArrayList<PlacemarkMapObject> placemarkMapObjects, String id, String name, String description) {
        this.placemarkMapObjects = placemarkMapObjects;
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
