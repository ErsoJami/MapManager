package com.example.mapmanager.models;

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

    private String getDeclension(int num, String one, String two, String three) {
        int ten = num % 10;
        int hundred = num % 100;
        if (hundred >= 11 && hundred <= 19) {
            return num + " " + three;
        }
        if (ten == 1) {
            return num + " " + one;
        }
        if (ten >= 2 && ten <= 4) {
            return num + " " + two;
        }
        return num + " " + three;
    }
    public String getStep() {
        return getDeclension(step, "шаг", "шага", "шагов");
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getCalories() {
        return getDeclension(calories, "калория", "калории", "калорий");
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
}
