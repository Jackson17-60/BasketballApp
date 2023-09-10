package com.example.cardview;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User {
    private String email;
    private String password;
    private String name;
    private String gender;
    private String profileImg;
    private String height; // You can use int or double depending on your requirements
    private String level;
    private String location;

    // Constructors (default and parameterized)

    public User() {
        // Default constructor required for Firebase
    }


    public User(String profileImg, String name, String gender,
                String height, String level, String location) {
        this.profileImg = profileImg;
        this.name = name;
        this.gender = gender;
        this.height = height;
        this.level = level;
        this.location = location;
    }
    // Getters and Setters


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getprofileImg() {
        return profileImg;
    }

    public void setprofileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
