package com.example.mapmanager.models;

public class FilterSettings {
    public final static double DOWN_BORDER_LEN = -1, UP_BORDER_LEN = 1e18;
    private short minAge, maxAge;
    private String city;
    private double maxLen, minLen;

    public short getMinAge() {
        return minAge;
    }

    public void setMinAge(short minAge) {
        this.minAge = minAge;
    }

    public short getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(short maxAge) {
        this.maxAge = maxAge;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(double maxLen) {
        this.maxLen = maxLen;
    }

    public double getMinLen() {
        return minLen;
    }

    public void setMinLen(double minLen) {
        this.minLen = minLen;
    }

    public FilterSettings() {
        minAge = 0;
        maxAge = 100;
        minLen = DOWN_BORDER_LEN;
        maxLen = UP_BORDER_LEN;
        city = "";
    }

    public FilterSettings(short minAge, short maxAge, String city, double maxLen, double minLen) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.city = city;
        this.maxLen = maxLen;
        this.minLen = minLen;
    }
    public boolean passFilter (RouteCardSettings settings) {
        return !(minAge > settings.getAverageAge() || maxAge < settings.getAverageAge() ||
                !city.isEmpty() && !settings.getCity().equals(city) ||
                minLen > settings.getLength() || maxLen < settings.getLength());
    }
}
