package com.example.myapplication;

public class CardData {
    private int imageResource;
    private String title;
    private String secondaryText;
    private String supportingText;
//    private String actionText;

    public CardData(int imageResource, String title, String secondaryText, String supportingText) {
        this.imageResource = imageResource;
        this.title = title;
        this.secondaryText = secondaryText;
        this.supportingText = supportingText;
//        this.actionText = actionText;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public void setSecondaryText(String secondaryText) {
        this.secondaryText = secondaryText;
    }

    public String getSupportingText() {
        return supportingText;
    }

    public void setSupportingText(String supportingText) {
        this.supportingText = supportingText;
    }

//    public String getActionText() {
//        return actionText;
//    }
//
//    public void setActionText(String actionText) {
//        this.actionText = actionText;
//    }
}
