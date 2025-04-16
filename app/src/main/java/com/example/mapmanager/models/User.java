package com.example.mapmanager.models;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.HashMap;

public class User {
    private String email;
    private String name;
    private String nick;
    private String phoneNumber;
    private String country;
    private String city;
    private long birthDayTime;
    public User() {}
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

    public void changeData(DatabaseReference databaseReference) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("nick", nick);
        data.put("email", email);
        data.put("phoneNumber", phoneNumber);
        data.put("country", country);
        data.put("city", city);
        data.put("birthDate", birthDayTime);
        databaseReference.updateChildren(data);
    }
    public void loadData(DataSnapshot ashot) {
        if (ashot.child("name").exists()) name = ashot.child("name").getValue(String.class);
        if (ashot.child("nick").exists()) nick = ashot.child("nick").getValue(String.class);
        if (ashot.child("email").exists()) email = ashot.child("email").getValue(String.class);
        if (ashot.child("phoneNumber").exists()) phoneNumber = ashot.child("phoneNumber").getValue(String.class);
        if (ashot.child("country").exists()) country = ashot.child("country").getValue(String.class);
        if (ashot.child("city").exists()) city = ashot.child("city").getValue(String.class);
        if (ashot.child("birthDate").exists()) birthDayTime = ashot.child("birthDate").getValue(long.class);
    }
}
