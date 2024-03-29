package com.example.cardview.Model_Class;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.util.Date;

public class Game {



    private String gameID;
    private String date;
    private String time;
    private String location;
    private Double Lat;
    private Double Long;
    private String level;
    private String numOfPlayer;

    private String host;


    private long participantCount;
    public Game() {
        // Default constructor required by Firestore
    }


    public Game(String gameID,String date, String time, String location,Double Lat, Double Long, String level, String numOfPlayer,long participantCount, String host) {
        this.gameID = gameID;
        this.date = date;
        this.time = time;
        this.location = location;
        this.Long = Long;
        this.Lat = Lat;
        this.level = level;
        this.numOfPlayer = numOfPlayer;
        this.participantCount = participantCount;
        this.host = host;
    }
    public Double getLat(){ return Lat;}
    public void setLat(Double Lat){this.Lat = Lat;}

    public Double getLong(){ return Long;}
    public void setLong(Double Long){this.Long = Long;}
    public long getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(long participantCount) {
        this.participantCount = participantCount;
    }
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getNumOfPlayer() {
        return numOfPlayer;
    }

    public void setNumOfPlayer(String numOfPlayer) {
        this.numOfPlayer = numOfPlayer;
    }



}
