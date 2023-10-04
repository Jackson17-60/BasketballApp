package com.example.cardview;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardview.BottomSheet.CreateEditGameBottomSheet;
import com.example.cardview.RecyclerView.GameRecyclerViewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.cardview.Model_Class.Game;


public class MyGameFragment extends Fragment implements GameRecyclerViewAdapter.OnDataChangeListener {
    private GameRecyclerViewAdapter adapter;
    private DatabaseReference gamesReference;
    private ChildEventListener childEventListener;
    private RecyclerView recyclerView;
    private List<Game> gameListFull = new ArrayList<>();
    private FirebaseAuth firebaseAuth ;
    private FirebaseUser currentUser ;
    private FloatingActionButton fab;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);

    public MyGameFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mygame, container, false);
        setupFirebaseDatabase();
        setupRecyclerViewAndAdapter(view);
        setupFab(view);
        return view;
    }
    private void setupFab(View view) {
        fab = view.findViewById(R.id.add_games);
        fab.setOnClickListener(view1 -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                CreateEditGameBottomSheet bottomSheet = new CreateEditGameBottomSheet(null);
                bottomSheet.show(activity.getSupportFragmentManager(), "CreateEditGameBottomSheet");
            } else {
                Log.e("HomeFragment", "Activity is null");
            }
        });
    }
    private void setupRecyclerViewAndAdapter(View view) {
        recyclerView = view.findViewById(R.id.myGamerecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        adapter = new GameRecyclerViewAdapter(gameListFull, getOnItemClickListener());
        adapter.setOnDataChangeListener(this);
        recyclerView.setAdapter(adapter);
    }
    private void setupFirebaseDatabase() {
        gamesReference = FirebaseDatabase.getInstance().getReference().child("games");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                handleChildAdded(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                handleChildChanged(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                handleChildRemoved(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle child moved if necessary
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error retrieving data", databaseError.toException());
            }
        };
        gamesReference.addChildEventListener(childEventListener);
    }
    private void handleChildAdded(DataSnapshot dataSnapshot) {
        DataSnapshot detailsSnapshot = dataSnapshot.child("details");
        Game game = detailsSnapshot.getValue(Game.class);

        if (game != null) {
            try {
                if (currentUser != null && game.getHost().equals(currentUser.getUid())) {
                    addGameToTheList(game);
                } else {
                    Log.d("Firebase", "The game is not hosted by the current user");
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("Firebase", "Error parsing date", e);
            }
        } else {
            Log.d("Firebase", "Game is null");
        }
    }
    private void addGameToTheList(Game game) throws ParseException {
        Date gameDate = sdf.parse(game.getDate() + " " + game.getTime());
        Date currentDate = new Date();

        if (gameDate != null && gameDate.after(currentDate)) {
            gameListFull.add(0, game);
            adapter.updateFullDataList(gameListFull);
            adapter.notifyItemInserted(0);
            recyclerView.scrollToPosition(0);
            adapter.getFilter().filter(null);
        } else {
            Log.d("Firebase", "Game date is null or not after the current date");
        }
    }
    private void handleChildChanged(@NonNull DataSnapshot dataSnapshot) {
        String gameId = dataSnapshot.getKey();
        DataSnapshot detailsSnapshot = dataSnapshot.child("details");
        Game game = detailsSnapshot.getValue(Game.class);
        DataSnapshot participantsSnapshot = dataSnapshot.child("participants");
        long participantCount = participantsSnapshot.getChildrenCount();

        if (game != null && gameId != null) {
            for (int i = 0; i < gameListFull.size(); i++) {
                if (gameListFull.get(i).getGameID().equals(gameId)) {
                    game.setParticipantCount(participantCount);
                    gameListFull.set(i, game); // Update gameListFull first
                    adapter.updateFullDataList(gameListFull); // Update full data list in adapter
                    adapter.getFilter().filter(null); // This will update gameList and refresh the RecyclerView
                    break;
                }
            }
        }
        else{
            Log.e("Firebase", "gameId is null in onChildChanged");
        }
    }

    private void handleChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        String gameId = dataSnapshot.getKey();
        if (gameId == null) {
            Log.e("Firebase", "gameId is null in onChildRemoved");
            return;
        }

        for (int i = 0; i < gameListFull.size(); i++) {
            if (gameListFull.get(i).getGameID().equals(gameId)) {
                gameListFull.remove(i); // Remove from gameListFull here too
                adapter.updateFullDataList(gameListFull); // Update full data list in adapter
                adapter.notifyItemRemoved(i);
                adapter.getFilter().filter(null);
                break;
            }
        }
    }

    private GameRecyclerViewAdapter.OnItemClickListener getOnItemClickListener() {
        return game -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            FragmentActivity activity = getActivity();
            if (currentUser != null && activity != null) {
                    CreateEditGameBottomSheet bottomSheet = new CreateEditGameBottomSheet(game);
                    bottomSheet.show(activity.getSupportFragmentManager(), bottomSheet.getTag());
            } else {
                Log.e("My Game Fragment", "Current user is null or activity is null");
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (childEventListener != null && gamesReference != null) {
            gamesReference.removeEventListener(childEventListener);
        }
        adapter = null;
        recyclerView = null;
        fab = null;

    }
    private void updateGamesCount(int count) {
        TextView myGameCounter = getView().findViewById(R.id.myGameCounter);
        myGameCounter.setText(String.format(Locale.US, "My Games : %d", count));
    }
    @Override
    public void onDataChanged(int size) {
        updateGamesCount(size);
    }
}