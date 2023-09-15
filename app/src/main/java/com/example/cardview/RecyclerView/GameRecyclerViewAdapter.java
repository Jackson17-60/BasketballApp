package com.example.cardview.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardview.R;
import com.example.cardview.databinding.UpcomingGamesLayoutBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.cardview.Model_Class.Game;

public class GameRecyclerViewAdapter extends RecyclerView.Adapter<GameRecyclerViewAdapter.ViewHolder> implements Filterable {
    private List<Game> gameList;
    private List<Game> gameListFull;
    private OnItemClickListener onItemClickListener;
    private OnDataChangeListener onDataChangeListener;
    private int[] colors;
    private final Random rnd = new Random();

    public interface OnDataChangeListener{
        void onDataChanged(int size);
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
        this.onDataChangeListener = onDataChangeListener;
    }
    public GameRecyclerViewAdapter(List<Game> gameList, OnItemClickListener onItemClickListener) {
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UpcomingGamesLayoutBinding binding = UpcomingGamesLayoutBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding, onItemClickListener);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Game gameData = gameList.get(position);
        Context context = holder.binding.getRoot().getContext();
        colors = context.getResources().getIntArray(R.array.color_array);
        int color = colors[rnd.nextInt(colors.length)];
        holder.binding.bkbIcon.setColorFilter(color);

        holder.binding.gameDateTime.setText(gameData.getDate() + " @ "  +gameData.getTime());
        holder.binding.numOfPlayer.setText(gameData.getParticipantCount()+"/"+gameData.getNumOfPlayer());
        holder.binding.gameLocation.setText(gameData.getLocation());
    }


    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        UpcomingGamesLayoutBinding binding;

        public ViewHolder(@NonNull UpcomingGamesLayoutBinding binding, final OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(gameList.get(position));
                }
            });
        }
    }
//    public void updateData(List<Game> newGameList) {
//        this.gameList.clear();
//        this.gameList.addAll(newGameList);
//        notifyDataSetChanged();
//    }
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
