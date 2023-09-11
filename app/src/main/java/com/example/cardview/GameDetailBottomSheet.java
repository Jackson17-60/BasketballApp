package com.example.cardview;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class GameDetailBottomSheet  extends BottomSheetDialogFragment {
    String name;
    private Game game;
    TextView playerTextView;

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
        playerTextView = view.findViewById(R.id.game_detail_player);
        TextView levelTextView = view.findViewById(R.id.game_detail_level);
        TextView locationTextView = view.findViewById(R.id.game_detail_location);
        TextView hostTextView = view.findViewById(R.id.game_detail_host);
        Button joinGame = view.findViewById(R.id.join_gameBtn);

        DatabaseReference databaseRef;
        databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(game.getHost());
        FirebaseDatabase databaseRef2 = FirebaseDatabase.getInstance();
        DatabaseReference gamesRef = databaseRef2.getReference("games");
        FirebaseAuth database = FirebaseAuth.getInstance();
        FirebaseUser user = database.getCurrentUser();;
        DatabaseReference userParticipationRef = gamesRef.child(game.getGameID()).child("participants").child(user.getUid());

        gamesRef.child(game.getGameID()).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Game details = snapshot.getValue(Game.class);
                if (details != null) {
                    String hostID = details.getHost();

                    userParticipationRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (isAdded()) {
                                    joinGame.setText("Leave Game");
                                    joinGame.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
                                }
                                joinGame.setOnClickListener(view -> {
                                    // Add code to leave the game here
                                    gamesRef.child(game.getGameID()).child("participants").child(user.getUid()).removeValue().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "You have left the game", Toast.LENGTH_SHORT).show();
                                            updateParticipantCount(false); // Update participant count
                                            dismiss();
                                        } else {
                                            Toast.makeText(getContext(), "Failed to leave the game", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                            } else {
                                // ...
                                joinGame.setOnClickListener(view -> {
                                    if (isAdded()) {
                                        joinGame.setText("Join Game");
                                        joinGame.setBackgroundColor(getResources().getColor(R.color.light_blue));
                                    }
                                    gamesRef.child(game.getGameID()).child("participants").child(user.getUid()).setValue(true).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            if (isAdded()) {
                                                Toast.makeText(getContext(), "You have joined the game", Toast.LENGTH_SHORT).show();
                                                updateParticipantCount(true); // Update participant count
                                                dismiss();
                                            }
                                        } else {
                                            if (isAdded()) {
                                                Toast.makeText(getContext(), "Failed to join the game", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
            }
        });


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
    private void updateParticipantCount(boolean isJoining) {
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("games").child(game.getGameID()).child("details");

        gameRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Game game = mutableData.getValue(Game.class);
                if (game != null) {
                    long participantCount = game.getParticipantCount();
                    if (isJoining) {
                        game.setParticipantCount(participantCount + 1);
                    } else {
                        game.setParticipantCount(Math.max(0, participantCount - 1));
                    }
                    mutableData.setValue(game);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Toast.makeText(getContext(), "Failed to update participant count", Toast.LENGTH_SHORT).show();
                } else {
                    // Update the UI to reflect the new participant count
                    String par = game.getParticipantCount() + "/" + game.getNumOfPlayer();
                    playerTextView.setText(par);
                }
            }
        });
    }

}