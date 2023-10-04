package com.example.cardview.BottomSheet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.cardview.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import com.example.cardview.Model_Class.Game;

public class GameDetailBottomSheet  extends BottomSheetDialogFragment {
    private String name,selectedaddress;
    private Game game;
    private TextView playerTextView, dateTextView, timeTextView, levelTextView, locationTextView, hostTextView;

    ImageView mapPreviewImageView ;
    private Button joinGame,open_googlemapBtn;
    private ValueEventListener userParticipationListener;
    private DatabaseReference userParticipationRef;

    public GameDetailBottomSheet(Game game){
        this.game = game;
    }
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_details, container, false);
        initializeViews(view);
        setUpUserParticipationListener(view);
        return view;
    }

    private void initializeViews(View view) {
        // Set the game data to your views
         dateTextView = view.findViewById(R.id.game_date_detail_text);
         timeTextView = view.findViewById(R.id.game_time_detail_text);
        playerTextView = view.findViewById(R.id.game_detail_player);
         levelTextView = view.findViewById(R.id.game_detail_level);
         locationTextView = view.findViewById(R.id.game_detail_location);
         hostTextView = view.findViewById(R.id.game_detail_host);
        mapPreviewImageView = view.findViewById(R.id.mapImageView);
        open_googlemapBtn = view.findViewById(R.id.open_googlemapBtn);

        selectedaddress = game.getLocation();
        Double selectedlat = game.getLat();
        Double selectedlong = game.getLong();
        String apiKey = "AIzaSyBZVfsU9RWTh04vxBS5gHX5XQd2jvBSQtY";


        String urlString = String.format(
                Locale.US,
                "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=20&size=500x200&markers=color:blue%%7Clabel:S%%7C%f,%f&key=%s",
                selectedlat,
                selectedlong,
                selectedlat,
                selectedlong,
                apiKey
        );

        Glide.with(this)
                .load(urlString)
                .into(mapPreviewImageView);

        open_googlemapBtn.setOnClickListener(view1 -> openInGoogleMaps(selectedlat, selectedlong));



        dateTextView.setText(game.getDate());
        timeTextView.setText(game.getTime());
        levelTextView.setText(game.getLevel());
        locationTextView.setText(game.getLocation());

        // Set host name and participants
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(game.getHost());
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    name = dataSnapshot.child("name").getValue(String.class);
                    hostTextView.setText(name);
                    updatePlayerTextView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void setUpUserParticipationListener(View view) {
        joinGame = view.findViewById(R.id.join_gameBtn);

        FirebaseAuth database = FirebaseAuth.getInstance();
        FirebaseUser user = database.getCurrentUser();
        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");
        userParticipationRef = gamesRef.child(game.getGameID()).child("participants").child(user.getUid());

        userParticipationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    if (dataSnapshot.exists()) {
                        setUpLeaveGameButton(joinGame, gamesRef, user);
                    } else {
                        DatabaseReference  usersRef = FirebaseDatabase.getInstance().getReference("users");
                        setUpJoinGameButton(joinGame, gamesRef, usersRef,user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        userParticipationRef.addValueEventListener(userParticipationListener);
    }

    private void setUpLeaveGameButton(Button joinGame, DatabaseReference gamesRef, FirebaseUser user) {
        joinGame.setText("Leave Game");
        joinGame.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red));
        joinGame.setOnClickListener(view -> {
            gamesRef.child(game.getGameID()).child("participants").child(user.getUid()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "You have left the game", Toast.LENGTH_SHORT).show();
                    updateParticipantCount(false);
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to leave the game", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setUpJoinGameButton(Button joinGame, DatabaseReference gamesRef, DatabaseReference usersRef, FirebaseUser user) {
        joinGame.setText("Join Game");
        joinGame.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue));
        joinGame.setOnClickListener(view -> {
            usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String userLevel = dataSnapshot.child("level").getValue(String.class);
                    String gameLevel = game.getLevel(); // Assuming 'game' is an object that holds the game's details.

                     if (userLevel != null && userLevel.equals(gameLevel)) {
                        if (game.getParticipantCount() < Long.parseLong(game.getNumOfPlayer())) {
                            gamesRef.child(game.getGameID()).child("participants").child(user.getUid()).setValue(true).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "You have joined the game", Toast.LENGTH_SHORT).show();
                                    updateParticipantCount(true);
                                    dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Failed to join the game", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            if (isAdded()) {
                                joinGame.setText("Game is Full");
                                Toast.makeText(getContext(), "Game is full, you cannot join", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "You cannot join this game as your level does not match the game level", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Toast.makeText(getContext(), "An error occurred: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
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
                if (committed) {
                    updatePlayerTextView();
                } else {
                    Toast.makeText(getContext(), "Failed to update participant count", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePlayerTextView() {
        String par = game.getParticipantCount() + "/" + game.getNumOfPlayer();
        playerTextView.setText(par);
    }


    private void openInGoogleMaps(double latitude, double longitude) {
        String strUri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + selectedaddress + ")";
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strUri));
        mapIntent.setPackage("com.google.android.apps.maps");
        Activity activity = getActivity();
        if (activity != null && mapIntent.resolveActivity(activity.getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Google Maps is not installed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getView() != null) {
            ViewGroup parent = (ViewGroup) getView().getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
        }
        if (joinGame != null) {
            joinGame.setOnClickListener(null);
            joinGame = null;
        }
        open_googlemapBtn.setOnClickListener(null);
        dateTextView = null;
        timeTextView = null;
        levelTextView = null;
        locationTextView = null;
        hostTextView = null;
        if (userParticipationRef != null && userParticipationListener != null) {
            userParticipationRef.removeEventListener(userParticipationListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            View parent = (View) view.getParent();
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

}
