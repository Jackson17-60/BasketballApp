package com.example.cardview;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Game> GameData;
    FloatingActionButton fab ;
    private LinearLayout datePickerLayout,timePickerLayout,gamesLevelLayout,playerLayout,gamesLocationLayout;
    public HomeFragment() {
        // Required empty public constructor
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
                showBottomSheet();
            }
        });


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference gamesReference = databaseReference.child("games");
        List<Game> gameList = new ArrayList<>();

        RecyclerViewAdapter.OnItemClickListener onItemClickListener = new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game game) {
                // Create a new instance of the bottom sheet fragment
                GameDetailBottomSheet bottomSheetFragment = new GameDetailBottomSheet(game);
                bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
            }
        };
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(gameList,onItemClickListener); // Initialize the adapter once

        gamesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gameList.clear();

                for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot detailsSnapshot = gameSnapshot.child("details");
                    Game game = detailsSnapshot.getValue(Game.class);
                    if (game != null) {
                        gameList.add(game);
                    }
                }
                Collections.reverse(gameList);

                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
            }
        });
        gamesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String gameId = dataSnapshot.getKey();

                DataSnapshot participantsSnapshot = dataSnapshot.child("participants");
                long participantCount = participantsSnapshot.getChildrenCount();

                if (gameId != null) {
                    gamesReference.child(gameId).child("details").child("participantCount").setValue(participantCount).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Find the game in your list and update the participant count
                            for (Game game : gameList) {
                                if (game.getGameID().equals(gameId)) {
                                    game.setParticipantCount(participantCount);

                                    adapter.notifyItemChanged(gameList.indexOf(game));
                                    break;
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


        recyclerView.setAdapter(adapter);



        return view;
    }
    public void showBottomSheet() {
        CustomBottomSheet bottomSheet = new CustomBottomSheet(R.layout.games_layout);
        bottomSheet.show(getActivity().getSupportFragmentManager(), bottomSheet.getTag());
    }


}