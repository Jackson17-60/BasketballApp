package com.example.cardview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements Filterable {
    private List<Game> gameList;
    private List<Game> gameListFull; // List to hold all the items
    private OnItemClickListener onItemClickListener;
    private OnDataChangeListener onDataChangeListener;

    public interface OnDataChangeListener{
        void onDataChanged(int size);
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
        this.onDataChangeListener = onDataChangeListener;
    }
    public RecyclerViewAdapter(List<Game> gameList, OnItemClickListener onItemClickListener) {
        this.gameList = gameList;
        this.onItemClickListener = onItemClickListener;
        this.gameListFull = new ArrayList<>(gameList); // Initialize with all the items
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
        Game gameData = gameList.get(position);

//        holder.imageView.setImageResource(gameData.get());
        holder.GameDate.setText(gameData.getDate());
        holder.GameTime.setText(gameData.getTime());
        holder.GamePlayer.setText(gameData.getParticipantCount()+"/"+gameData.getNumOfPlayer());
        holder.GameLevel.setText(gameData.getLevel());
        holder.GameLocation.setText(gameData.getLocation());
    }


    @Override
    public int getItemCount() {
        return gameList.size();
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
                    listener.onItemClick(gameList.get(position));
                }
            });
        }
    }
    public void updateData(List<Game> newGameList) {
        this.gameList.clear();
        this.gameList.addAll(newGameList);
        notifyDataSetChanged();
        Log.d("RecyclerViewAdapter", "Data updated. gameList size: " + gameList.size() + ", gameListFull size: " + gameListFull.size());
    }
    public void updateFullDataList(List<Game> newFullDataList) {
        this.gameListFull.clear();
        this.gameListFull.addAll(newFullDataList);
    }


    @Override
    public Filter getFilter() {
        return gameFilter;
    }

    private Filter gameFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Game> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(gameListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Game game : gameListFull) {
                    if (game.getLevel().toLowerCase().contains(filterPattern)) {
                        filteredList.add(game);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            gameList.clear();
            gameList.addAll((List) results.values);
            notifyDataSetChanged();
            if (onDataChangeListener != null) {
                onDataChangeListener.onDataChanged(gameList.size());
            }
        }
    };


}
