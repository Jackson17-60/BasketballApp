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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
    private List<Game> gameListFull = new ArrayList<>();
    private String currentFilter = null;
    private ChildEventListener childEventListener;
    private FirebaseAuth firebaseAuth ;
    private FirebaseUser currentUser ;
    private RecyclerViewAdapter adapter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
    DatabaseReference gamesReference;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Date currentDate = new Date();
            for (int i = gameListFull.size() - 1; i >= 0; i--) {
                Game game = gameListFull.get(i);
                try {
                    Date gameDate = sdf.parse(game.getDate() + " " + game.getTime());
                    if (gameDate != null && gameDate.before(currentDate)) {
                        gameListFull.remove(i);
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
        setupLevelFilterSpinner(view);

        return view;
    }
    private void setupRecyclerViewAndAdapter(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        adapter = new RecyclerViewAdapter(gameListFull, getOnItemClickListener());
        adapter.setOnDataChangeListener(this);
        recyclerView.setAdapter(adapter);
    }


private void setupLevelFilterSpinner(View view) {

    String[] levelArray = getResources().getStringArray(R.array.level_array);
    ArrayAdapter<String> spinnerAdapter= new ArrayAdapter<>(requireContext(), R.layout.spinner_dropdown_item, levelArray);
    AutoCompleteTextView spinner = view.findViewById(R.id.levelFilterSpinner);
    spinner.setAdapter(spinnerAdapter);
    spinner.setText(spinnerAdapter.getItem(0), false);
    spinner.setOnItemClickListener((parent, view1, position, id) -> {
        currentFilter = (String) parent.getItemAtPosition(position);
        if ("All Levels".equals(currentFilter)) {
            currentFilter = null;
        }

        adapter.getFilter().filter(currentFilter);
    });
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
                if (currentUser != null && !game.getHost().equals(currentUser.getUid())) {
                    addGameToTheList(game);
                } else {
                    Log.d("Firebase", "No Game Found");
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
            for (int i = 0; i < gameListFull.size(); i++) {
                if (gameListFull.get(i).getGameID().equals(gameId)) {
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

        for (int i = 0; i < gameListFull.size(); i++) {
            if (gameListFull.get(i).getGameID().equals(gameId)) {
                gameListFull.remove(i); // Remove from gameListFull here too
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
                    GameDetailBottomSheet bottomSheetFragment = new GameDetailBottomSheet(game);
                    bottomSheetFragment.show(activity.getSupportFragmentManager(), bottomSheetFragment.getTag());
            } else {
                Log.e("HomeFragment", "Current user is null or activity is null");
            }
        };
    }

    private void updateGamesCount(int count) {
        TextView gamesCountTextView = getView().findViewById(R.id.games_count_text_view);
        gamesCountTextView.setText(String.format(Locale.US, "Games : %d", count));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gamesReference != null && childEventListener != null) {
            gamesReference.removeEventListener(childEventListener);
        }
        adapter = null;
        recyclerView = null;
        handler.removeCallbacks(runnable); // Ensure to remove any lingering callbacks
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