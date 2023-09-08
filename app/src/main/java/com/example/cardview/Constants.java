package com.example.cardview;

import java.util.ArrayList;

public class Constants {

    public static ArrayList<CardData> getCardData()
    {

        ArrayList<CardData> cardData = new ArrayList<CardData>();

        cardData.add(new CardData(R.drawable.mikeedgar, "Card Title 1", "Secondary Text 1", "Supporting Text 1"));
        cardData.add(new CardData(R.drawable.icecream, "Card Title 2", "Secondary Text 2", "Supporting Text 2"));



        return cardData;
    }
}
