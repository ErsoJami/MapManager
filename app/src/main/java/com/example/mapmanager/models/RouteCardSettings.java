package com.example.mapmanager.models;

import com.google.firebase.Timestamp;

public class RouteCardSettings {
    private int minAge;
    private int maxAge;
    private Timestamp timeStart;

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

    public Timestamp getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Timestamp timeStart) {
        this.timeStart = timeStart;
    }

    public RouteCardSettings(int minAge, int maxAge, Timestamp timeStart) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.timeStart = timeStart;
    }

}
