package com.example.cardview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final List<CardData> cardDataList;
    private final Context context;

    public RecyclerViewAdapter(List<CardData> cardDataList, Context context) {
        this.cardDataList = cardDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardData cardData = cardDataList.get(position);

        holder.imageView.setImageResource(cardData.getImageResource());
        holder.titleTextView.setText(cardData.getTitle());
        holder.secondaryTextView.setText(cardData.getSecondaryText());
        holder.supportingTextView.setText(cardData.getSupportingText());
    }

    @Override
    public int getItemCount() {
        return cardDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView secondaryTextView;
        TextView supportingTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.card_image);
            titleTextView = itemView.findViewById(R.id.card_title);
            secondaryTextView = itemView.findViewById(R.id.secondary_text);
            supportingTextView = itemView.findViewById(R.id.supporting_text);
        }
    }

}
