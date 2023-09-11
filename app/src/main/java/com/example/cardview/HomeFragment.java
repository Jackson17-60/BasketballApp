package com.example.cardview;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    FloatingActionButton fab ;
    public HomeFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager lm = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(lm);
        fab = view.findViewById(R.id.add_games);



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomBottomSheet bottomSheet = new CustomBottomSheet(R.layout.add_games_layout, null);
                bottomSheet.show(getActivity().getSupportFragmentManager(), "CustomBottomSheet");
            }
        });



        DatabaseReference gamesReference = FirebaseDatabase.getInstance().getReference().child("games");
        List<Game> gameList = new ArrayList<>();

        RecyclerViewAdapter.OnItemClickListener onItemClickListener = new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String currentUserId = currentUser.getUid();

                    if (currentUserId.equals(game.getHost())) {
                        // Current user is the host
                        CustomBottomSheet bottomSheet = new CustomBottomSheet(R.layout.add_games_layout, game);
                        bottomSheet.show(getActivity().getSupportFragmentManager(), bottomSheet.getTag());
                    } else {
                        // Current user is not the host
                        GameDetailBottomSheet bottomSheetFragment = new GameDetailBottomSheet(game);
                        bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
                    }
                } else {
                    // Handle the case where the current user is null (not authenticated)
                }
            }
        };
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(gameList,onItemClickListener); // Initialize the adapter once

        gamesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                DataSnapshot detailsSnapshot = dataSnapshot.child("details");
                Game game = detailsSnapshot.getValue(Game.class);
                if (game != null) {
                    gameList.add(0, game); // Adding game at the start of the list
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0); // Automatically scroll to the top to show the new item
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String gameId = dataSnapshot.getKey();
                DataSnapshot detailsSnapshot = dataSnapshot.child("details");
                Game game = detailsSnapshot.getValue(Game.class);
                DataSnapshot participantsSnapshot = dataSnapshot.child("participants");
                long participantCount = participantsSnapshot.getChildrenCount();

                if (game != null && gameId != null) {
                    for (int i = 0; i < gameList.size(); i++) {
                        if (gameList.get(i).getGameID().equals(gameId)) {
                            game.setParticipantCount(participantCount);
                            gameList.set(i, game);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String gameId = dataSnapshot.getKey();
                for (int i = 0; i < gameList.size(); i++) {
                    if (gameList.get(i).getGameID().equals(gameId)) {
                        gameList.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle child moved if necessary
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error retrieving data", databaseError.toException());
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
//    public void showBottomSheet(Game game) {
//        CustomBottomSheet bottomSheet = new CustomBottomSheet(R.layout.add_games_layout,game);
//        bottomSheet.show(getActivity().getSupportFragmentManager(), bottomSheet.getTag());
//    }


}