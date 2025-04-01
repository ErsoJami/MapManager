package com.example.mapmanager.models;

import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private List<RequestPoint> requestPointList;

    public Route() {
        requestPointList = new ArrayList<>();
    }
    public Route(List<RequestPoint> requestPointList) {
        this.requestPointList = requestPointList;
    }

    public List<RequestPoint> getRequestPointList() {
        return requestPointList;
    }

    public void setRequestPointList(List<RequestPoint> requestPointList) {
        this.requestPointList = requestPointList;
    }
}
