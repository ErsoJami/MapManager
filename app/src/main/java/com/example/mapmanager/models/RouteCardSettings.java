package com.example.mapmanager.models;

import java.util.regex.Pattern;
import com.google.firebase.Timestamp;
import static java.lang.Math.*;

public class RouteCardSettings {
    private int averageAge;
    private String city;
    private double length;
    public RouteCardSettings() {}

    public RouteCardSettings(int averageAge, String city, double length) {
        this.averageAge = averageAge;
        this.city = city;
        this.length = length;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getAverageAge() {
        return averageAge;
    }

    public void setAverageAge(int averageAge) {
        this.averageAge = averageAge;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[^\\p{L}\\p{N}\\s-]+");
    private static final Pattern MULTIPLE_HYPHENS_PATTERN = Pattern.compile("-+");
    private static final Pattern MULTIPLE_WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern HYPHEN_WITH_SPACES_PATTERN = Pattern.compile("\\s+-\\s+");

    public void normalize() {
        averageAge = max(0, min(100, averageAge));
        city = FORBIDDEN_CHARS_PATTERN.matcher(city).replaceAll("");
        city = MULTIPLE_HYPHENS_PATTERN.matcher(city).replaceAll("-");
        city = MULTIPLE_WHITESPACE_PATTERN.matcher(city).replaceAll(" ");
        city = HYPHEN_WITH_SPACES_PATTERN.matcher(city).replaceAll("-");
        city.trim();
    }
}
