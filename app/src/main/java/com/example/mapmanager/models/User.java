package com.example.mapmanager.models;

import android.util.Pair;

import com.example.mapmanager.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String email;
    private String name;
    private String userId;
    private String nick;
    private String phoneNumber;
    private String country;
    private String city;
    private String avatarUrl;
    private ArrayList<String> routeList;
    private HashMap<String, ChatsData> userChatsData;
    private long birthDayTime;
    public User() {
        this.routeList = new ArrayList<>();
    }
    public HashMap<String, ChatsData> getUserChatsData() {
        return userChatsData;
    }
    public void setUserChatsData(HashMap<String, ChatsData> userChatsData) {
        this.userChatsData = userChatsData;
    }
    public ArrayList<String> getRouteList() {
        return routeList;
    }

    public void setRouteList(ArrayList<String> routeList) {
        this.routeList = routeList;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setBirthDayTime(long birthDayTime) {
        this.birthDayTime = birthDayTime;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getNick() {
        return nick;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public long getBirthDayTime() {
        return birthDayTime;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void changeData(DatabaseReference databaseReference) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("nick", nick);
        data.put("chatList", userChatsData);
        data.put("email", email);
        data.put("phoneNumber", phoneNumber);
        data.put("country", country);
        data.put("city", city);
        data.put("birthDate", birthDayTime);
        data.put("routeList", routeList);
        data.put("avatarUrl", avatarUrl);
        databaseReference.updateChildren(data);
    }
    public void loadData(DataSnapshot ashot) {
        if (ashot.exists()) userId = ashot.getKey();
        if (ashot.child("name").exists()) name = ashot.child("name").getValue(String.class);
        if (ashot.child("nick").exists()) nick = ashot.child("nick").getValue(String.class);
        if (ashot.child("email").exists()) email = ashot.child("email").getValue(String.class);
        if (ashot.child("phoneNumber").exists()) phoneNumber = ashot.child("phoneNumber").getValue(String.class);
        if (ashot.child("country").exists()) country = ashot.child("country").getValue(String.class);
        if (ashot.child("city").exists()) city = ashot.child("city").getValue(String.class);
        if (ashot.child("birthDate").exists()) birthDayTime = ashot.child("birthDate").getValue(long.class);
        if (ashot.child("avatarUrl").exists()) avatarUrl = ashot.child("avatarUrl").getValue(String.class);
        GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
        GenericTypeIndicator<HashMap<String, ChatsData>> t1 = new GenericTypeIndicator<>() {};
        if (ashot.child("routeList").exists()) routeList = ashot.child("routeList").getValue(t);
        else routeList = new ArrayList<>();
        if (ashot.child("chatList").exists()) userChatsData = ashot.child("chatList").getValue(t1);
        else userChatsData = new HashMap<>();
    }
}
