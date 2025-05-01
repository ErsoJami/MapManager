package com.example.mapmanager.models;

import com.google.firebase.Timestamp;

public class RouteCardSettings {
    private int minAge;
    private int maxAge;

    public RouteCardSettings() {}

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

}
