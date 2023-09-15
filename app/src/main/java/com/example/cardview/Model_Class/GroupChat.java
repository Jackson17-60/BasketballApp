package com.example.cardview.Model_Class;

import java.io.Serializable;

public class GroupChat implements Serializable {
    private String id;
    private String name;
    private String description;
    private String groupImage;


    private long timestamp;
    public GroupChat() {
        // Empty constructor needed for Firestore
    }

    public GroupChat(String id, String name, String description, String groupImage,long timestamp) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupImage = groupImage;
        this.timestamp = timestamp;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }
}
