package com.example.cardview;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

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


public class HomeFragment extends Fragment implements RecyclerViewAdapter.OnDataChangeListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private List<Game> gameList = new ArrayList<>();
    private List<Game> gameListFull = new ArrayList<>();
    private String currentFilter = null;


    private RecyclerViewAdapter adapter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
    DatabaseReference gamesReference;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Date currentDate = new Date();
            for (int i = gameList.size() - 1; i >= 0; i--) {
                Game game = gameList.get(i);
                try {
                    Date gameDate = sdf.parse(game.getDate() + " " + game.getTime());
                    if (gameDate != null && gameDate.before(currentDate)) {
                        gameList.remove(i);
                        adapter.notifyItemRemoved(i);
                    }
                } catch (ParseException e) {
                   e.printStackTrace();
                }
            }
            handler.postDelayed(this, 60000); // Check every minute
        }
    };


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
        setupFirebaseDatabase();
        setupRecyclerViewAndAdapter(view);
        setupFab(view);
        setupLevelFilterSpinner(view);

        return view;
    }
    private void setupRecyclerViewAndAdapter(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        adapter = new RecyclerViewAdapter(gameList, getOnItemClickListener());
        adapter.setOnDataChangeListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupFab(View view) {
        fab = view.findViewById(R.id.add_games);
        fab.setOnClickListener(view1 -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                CustomBottomSheet bottomSheet = new CustomBottomSheet(R.layout.add_games_layout, null);
                bottomSheet.show(activity.getSupportFragmentManager(), "CustomBottomSheet");
            } else {
                Log.e("HomeFragment", "Activity is null");
            }
        });
    }
    private void setupLevelFilterSpinner(View view) {
        Spinner levelFilterSpinner = view.findViewById(R.id.levelFilterSpinner);
        levelFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = (String) parent.getItemAtPosition(position);
                if ("All Levels".equals(currentFilter)) {
                    currentFilter = null;
                }
                adapter.getFilter().filter(currentFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupFirebaseDatabase() {
        gamesReference = FirebaseDatabase.getInstance().getReference().child("games");
        gamesReference.addChildEventListener(new ChildEventListener() {
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
        });
    }
    private void handleChildAdded(DataSnapshot dataSnapshot) {
        DataSnapshot detailsSnapshot = dataSnapshot.child("details");
        Game game = detailsSnapshot.getValue(Game.class);

        if (game != null) {
            try {
                addGameToTheList(game);
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
            gameList.add(0, game);
            gameListFull.add(0, game);
            adapter.updateData(gameListFull);
            adapter.updateFullDataList(gameListFull);
            adapter.notifyItemInserted(0);
            recyclerView.scrollToPosition(0);
            adapter.getFilter().filter(currentFilter);
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
            for (int i = 0; i < gameList.size(); i++) {
                if (gameList.get(i).getGameID().equals(gameId)) {
                    game.setParticipantCount(participantCount);
                    gameListFull.set(i, game); // Update gameListFull first
                    adapter.updateFullDataList(gameListFull); // Update full data list in adapter
                    adapter.getFilter().filter(currentFilter); // This will update gameList and refresh the RecyclerView
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

        for (int i = 0; i < gameList.size(); i++) {
            if (gameList.get(i).getGameID().equals(gameId)) {
                gameList.remove(i);
                gameListFull.remove(i); // Remove from gameListFull here too
                adapter.updateData(gameList);
                adapter.updateFullDataList(gameListFull); // Update full data list in adapter
                adapter.notifyItemRemoved(i);
                adapter.getFilter().filter(currentFilter);
                break;
            }
        }
    }

    private RecyclerViewAdapter.OnItemClickListener getOnItemClickListener() {
        return game -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            FragmentActivity activity = getActivity();
            if (currentUser != null && activity != null) {
                String currentUserId = currentUser.getUid();

                if (currentUserId.equals(game.getHost())) {
                    // Current user is the host
                    CustomBottomSheet bottomSheet = new CustomBottomSheet(R.layout.add_games_layout, game);
                    bottomSheet.show(activity.getSupportFragmentManager(), bottomSheet.getTag());
                } else {
                    // Current user is not the host
                    GameDetailBottomSheet bottomSheetFragment = new GameDetailBottomSheet(game);
                    bottomSheetFragment.show(activity.getSupportFragmentManager(), bottomSheetFragment.getTag());
                }
            } else {
                Log.e("HomeFragment", "Current user is null or activity is null");
            }
        };
    }

    private void updateGamesCount(int count) {
        TextView gamesCountTextView = getView().findViewById(R.id.games_count_text_view);
        gamesCountTextView.setText(String.format(Locale.US, "Upcoming Games: %d", count));
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnable); // Start the periodic task when the fragment is resumed
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // Stop the periodic task when the fragment is paused
    }


    @Override
    public void onDataChanged(int size) {
        updateGamesCount(size);
    }
}