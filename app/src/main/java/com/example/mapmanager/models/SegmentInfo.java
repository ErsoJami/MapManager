package com.example.mapmanager.models;

import com.example.mapmanager.R;

public class SegmentInfo {
    private int type; // 0 - велосипед, 1 - пешком, 2 - транспорт, 3 - машина
    private String name;
    private String distance;
    private String time;
    private int step;
    private int calories;
    private String busList;

    public SegmentInfo(int type, String name, String distance, String time, int step, int calories, String busList) {
        this.type = type;
        this.name = name;
        this.distance = distance;
        this.time = time;
        this.step = step;
        this.calories = calories;
        this.busList = busList;
    }

    public String getBusList() {
        return busList;
    }

    public void setBusList(String busList) {
        this.busList = busList;
    }

    public int getStep() {
        return step;
    }

    public int getCalories() {
        return calories;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
}
