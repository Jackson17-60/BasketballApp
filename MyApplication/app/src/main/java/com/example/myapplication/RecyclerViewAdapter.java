package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<CardData> data;
    private LayoutInflater inflater;
     public RecyclerViewAdapter(Context context, List<CardData> data) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mat_cardview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardData item = data.get(position);

        holder.imageView.setImageResource(item.getImageResource());
        holder.titleTextView.setText(item.getTitle());
        holder.secondaryTextView.setText(item.getSecondaryText());
        holder.supportingTextView.setText(item.getSupportingText());
//        holder.actionButton.setText(item.getActionText());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView secondaryTextView;
        TextView supportingTextView;
//        Button actionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.card_image);
            titleTextView = itemView.findViewById(R.id.card_title);
            secondaryTextView = itemView.findViewById(R.id.secondary_text);
            supportingTextView = itemView.findViewById(R.id.supporting_text);
//            actionButton = itemView.findViewById(R.id.card_button);
        }
    }
}
