package com.example.cardview;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameDetailBottomSheet  extends BottomSheetDialogFragment {
    String name;
    private Game game;
    public GameDetailBottomSheet(Game game){
        this.game = game;
    }
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_details, container, false);

        // Set the game data to your views
        TextView dateTextView = view.findViewById(R.id.game_date_detail_text);
        TextView timeTextView = view.findViewById(R.id.game_time_detail_text);
        TextView playerTextView = view.findViewById(R.id.game_detail_player);
        TextView levelTextView = view.findViewById(R.id.game_detail_level);
        TextView locationTextView = view.findViewById(R.id.game_detail_location);
        TextView hostTextView = view.findViewById(R.id.game_detail_host);
        DatabaseReference databaseRef;
        databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(game.getHost());

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the user's data
                    String par = game.getParticipantCount() + "/" + game.getNumOfPlayer();
                    name = dataSnapshot.child("name").getValue(String.class);
                    hostTextView.setText(name);
                    playerTextView.setText(par);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        dateTextView.setText(game.getDate());
        timeTextView.setText(game.getTime());
        levelTextView.setText(game.getLevel());
        locationTextView.setText(game.getLocation());
        return view;
    }
}