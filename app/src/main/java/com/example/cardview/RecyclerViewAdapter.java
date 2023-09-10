package com.example.cardview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final List<Game> GameList;
    private OnItemClickListener onItemClickListener;

    public RecyclerViewAdapter(List<Game> GameList, OnItemClickListener onItemClickListener) {
        this.GameList = GameList;
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onItemClick(Game game);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_games_layout, parent, false);
        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Game gameData = GameList.get(position);

//        holder.imageView.setImageResource(gameData.get());
        holder.GameDate.setText(gameData.getDate());
        holder.GameTime.setText(gameData.getTime());
        holder.GamePlayer.setText(gameData.getParticipantCount()+"/"+gameData.getNumOfPlayer());
        holder.GameLevel.setText(gameData.getLevel());
        holder.GameLocation.setText(gameData.getLocation());
    }

    @Override
    public int getItemCount() {
        return GameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView imageView;
        TextView GameDate, GameTime, GameLocation,GamePlayer,GameLevel;


        public ViewHolder(@NonNull View itemView,final OnItemClickListener listener) {
            super(itemView);
//            imageView = itemView.findViewById(R.id.card_image);
            GameDate = itemView.findViewById(R.id.game_date);
            GameTime = itemView.findViewById(R.id.game_time);
            GameLocation = itemView.findViewById(R.id.game_location);
            GameLevel = itemView.findViewById(R.id.game_level);
            GamePlayer = itemView.findViewById(R.id.numOfPlayer);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(GameList.get(position));
                }
            });
        }
    }

}
