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
        Button joinGame = view.findViewById(R.id.join_gameBtn);
        DatabaseReference databaseRef;
        databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(game.getHost());
        FirebaseDatabase databaseRef2 = FirebaseDatabase.getInstance();
        DatabaseReference gamesRef = databaseRef2.getReference("games");
        FirebaseAuth database = FirebaseAuth.getInstance();
        FirebaseUser user = database.getCurrentUser();;
        DatabaseReference userParticipationRef = gamesRef.child(game.getGameID()).child("participants").child(user.getUid());
        userParticipationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    joinGame.setText("Leave Game");
//                    joinGame.setBackgroundColor(getResources().getColor(R.color.red));
                } else {
                    joinGame.setText("Join Game");
//                    joinGame.setBackgroundColor(getResources().getColor(R.color.light_blue));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error here
            }
        });
        joinGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String gameID = game.getGameID();
                String userID = user.getUid();

                gamesRef.child(gameID).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Game details = snapshot.getValue(Game.class);
                        if(details != null) {
                            String hostID = details.getHost();

                            gamesRef.child(gameID).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(userID.equals(hostID)) {
                                        Toast.makeText(getContext(), "As the host, you cannot leave the game", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if(dataSnapshot.hasChild(userID)) {
                                        // User is already a participant, allow them to leave the game
                                        gamesRef.child(gameID).child("participants").child(userID).removeValue().addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Decrement the participantCount
                                                gamesRef.child(gameID).child("details").child("participantCount").runTransaction(new Transaction.Handler() {
                                                    @NonNull
                                                    @Override
                                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                                        Integer count = mutableData.getValue(Integer.class);
                                                        if (count != null && count > 0) {
                                                            mutableData.setValue(count - 1);
                                                        }
                                                        return Transaction.success(mutableData);
                                                    }

                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                                        // Handle completion
                                                        Toast.makeText(getContext(), "You have left the game", Toast.LENGTH_SHORT).show();
                                                        joinGame.setText("Join Game"); // Change button text to "Join Game"
                                                    }
                                                });
                                            } else {
                                                // Handle failure
                                                Toast.makeText(getContext(), "Failed to leave the game", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        // User is not a participant, allow them to join the game
                                        gamesRef.child(gameID).child("participants").child(userID).setValue(true).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Increment the participantCount
                                                gamesRef.child(gameID).child("details").child("participantCount").runTransaction(new Transaction.Handler() {
                                                    @NonNull
                                                    @Override
                                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                                        Integer count = mutableData.getValue(Integer.class);
                                                        if (count != null) {
                                                            mutableData.setValue(count + 1);
                                                        }
                                                        return Transaction.success(mutableData);
                                                    }

                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                                        // Handle completion
                                                        Toast.makeText(getContext(), "You have joined the game", Toast.LENGTH_SHORT).show();
                                                        joinGame.setText("Leave Game"); // Change button text to "Leave Game"
                                                    }
                                                });
                                            } else {
                                                // Handle failure
                                                Toast.makeText(getContext(), "Failed to join the game", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle database error here
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle potential errors
                    }
                });
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
}