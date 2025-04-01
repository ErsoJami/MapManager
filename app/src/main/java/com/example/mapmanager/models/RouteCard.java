package com.example.mapmanager.models;

public class RouteCard {
    private Route route;
    private String id;
    private String name;
    private String description;
    private RouteCardSettings routeCardSettings;
    public RouteCard() {}

    public RouteCard(Route route, String id, String name, String description, RouteCardSettings routeCardSettings) {
        this.route = route;
        this.id = id;
        this.name = name;
        this.description = description;
        this.routeCardSettings = routeCardSettings;
    }
    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
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
