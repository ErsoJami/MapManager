package com.example.mapmanager.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;

public class RouteCard {
    private String route;
    private String id;
    private String name;
    private String description;
    private long startTime, endTime;
    private RouteCardSettings routeCardSettings;
    public RouteCard() {}

    public RouteCard(String route, String id, String name, String description, RouteCardSettings routeCardSettings, long startTime, long endTime) {
        this.route = route;
        this.id = id;
        this.name = name;
        this.description = description;
        this.routeCardSettings = routeCardSettings;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public RouteCard(String route, String name, String description, RouteCardSettings routeCardSettings, long startTime, long endTime) {
        this.route = route;
        this.name = name;
        this.description = description;
        this.routeCardSettings = routeCardSettings;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public void createRoutCard() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("routeCards");
        HashMap<String, Object> data = new HashMap<>();
        data.put("route", route);
        data.put("name", name);
        data.put("description", description);
        data.put("startTime", startTime);
        data.put("endTime", endTime);
        data.put("routeCardSettings", routeCardSettings);
        DatabaseReference newRouteCard = databaseReference.push();
        data.put("id", newRouteCard.getKey());
        newRouteCard.setValue(data);
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public RouteCardSettings getRouteCardSettings() {
        return routeCardSettings;
    }

    public void setRouteCardSettings(RouteCardSettings routeCardSettings) {
        this.routeCardSettings = routeCardSettings;
    }


}
