package com.example.cardview;

import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class CustomBottomSheet extends BottomSheetDialogFragment {
    private static final String SUCCESS_MESSAGE = "Successfully created the game";
    private static final String FAILURE_MESSAGE = "Failed to create the new game";
    private int layoutResId;
    private String selectedLevel, addDate, addTime, addGameLoc, addPlayers,selectedLocation;
    private RadioGroup levelRadioGroup;
    private Button addGameBtn,deleteGameBtn;
    private TextView dateTv, timeTv,gameStatus,gameLocationTv;
    private EditText playersNeededET;
    private DatabaseReference gamesReference;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ActivityResultLauncher<String> launcher;

    private ProgressBar loadingIndicator;
    private FirebaseDatabase database;
    private Game existingGame;
    public CustomBottomSheet() {

    }
    public CustomBottomSheet(@Nullable Game existingGame) {

        this.existingGame = existingGame;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        gamesReference = database.getReference().child("games");
        launcher = registerForActivityResult(new ActivityResultContract<String, String>() {   @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String input) {
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("selected_location", input);
            return intent;
        }
            @Override
            public String parseResult(int resultCode, @Nullable Intent intent) {
                if (intent == null || resultCode != Activity.RESULT_OK) {
                    return null; // or you might want to throw an exception
                }
                return intent.getStringExtra("selected_location");
            }
        }, result -> {
            if (result != null) {
                gameLocationTv.setText(result);
                selectedLocation = result;
            }
        });
    }
    private void initializeViews(View rootView) {
        levelRadioGroup = rootView.findViewById(R.id.gamelevelRadioGroup);
        playersNeededET = rootView.findViewById(R.id.playersNeededET);
        gameLocationTv = rootView.findViewById(R.id.gameLocationTextView);
        dateTv = rootView.findViewById(R.id.gameDateText);
        timeTv = rootView.findViewById(R.id.gameTimeText);
        addGameBtn = rootView.findViewById(R.id.addgame_btn);
        deleteGameBtn = rootView.findViewById(R.id.delete_game_btn);
        gameStatus = rootView.findViewById(R.id.gameStatus);
        loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        if (existingGame != null) {
            populateFieldsForEditing(existingGame);
        }
    }
    private void showLoadingIndicator() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingIndicator.setVisibility(View.GONE);
    }
    private void setupClickListeners(View rootView) {
        LinearLayout datePickerLayout = rootView.findViewById(R.id.datePickerLayout);
        LinearLayout timePickerLayout = rootView.findViewById(R.id.timePickerLayout);
        LinearLayout gameLocationLayout = rootView.findViewById(R.id.gamesLocationLayout);
        datePickerLayout.setOnClickListener(this::handleDatePickerClick);
        timePickerLayout.setOnClickListener(this::handleTimePickerClick);
        gameLocationLayout.setOnClickListener(v -> launcher.launch(selectedLocation));
        addGameBtn.setOnClickListener(this::handleAddGameClick);
        deleteGameBtn.setOnClickListener(this::handleDeleteGameClick);
    }
    private void setupLevelRadioGroupListener() {
        levelRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.game_beginner_radiobtn) {
                selectedLevel = "Beginner";
            } else if (checkedId == R.id.game_ama_radiobtn) {
                selectedLevel = "Amateur";
            }
            else if(checkedId == R.id.game_pro_radiobtn) {
                selectedLevel = "Professional";
            }
        });
    }
    private void addNewGameToDatabase() {
        addDate = dateTv.getText().toString().trim();
        addTime = timeTv.getText().toString().trim();
        addGameLoc = gameLocationTv.getText().toString().trim();
        addPlayers = playersNeededET.getText().toString().trim();
        int selectedRadioButtonId = levelRadioGroup.getCheckedRadioButtonId();

        if(addDate.isEmpty() || addTime.isEmpty() || addGameLoc.isEmpty() || addPlayers.isEmpty() || selectedRadioButtonId == -1){
            Toast.makeText(requireContext(), "Please Fill In All The Field", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference newGameRef = gamesReference.push();
        Map<String, Object> gameData = new HashMap<>();
        Game newGame = new Game(newGameRef.getKey(), addDate, addTime, addGameLoc, selectedLevel, addPlayers,1L, user != null ? user.getUid() : null);
        gameData.put("details", newGame);

        if (user != null) {
            Map<String, Object> participants = new HashMap<>();
            participants.put(user.getUid(), true);
            gameData.put("participants", participants);
        }

        if (newGameRef.getKey() != null) {
            newGame.setGameID(newGameRef.getKey());
        }
        showLoadingIndicator();
        newGameRef.setValue(gameData)
                .addOnSuccessListener(aVoid -> {
                    hideLoadingIndicator();
                    Toast.makeText(requireContext(), SUCCESS_MESSAGE, Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    Log.e("AddGame", "Failed to create the game", e);
                    Toast.makeText(requireContext(), FAILURE_MESSAGE, Toast.LENGTH_SHORT).show();
                });
    }
    private void handleAddGameClick(View view) {
        if (validateInput()) {
            if (existingGame != null) {
                updateExistingGameInDatabase();
            } else {
                addNewGameToDatabase();
            }
        } else {
            Toast.makeText(requireContext(), "Please Fill In All The Fields", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        addDate = dateTv.getText().toString().trim();
        addTime = timeTv.getText().toString().trim();
        addGameLoc = gameLocationTv.getText().toString().trim();
        addPlayers = playersNeededET.getText().toString().trim();
        int selectedRadioButtonId = levelRadioGroup.getCheckedRadioButtonId();
        return !addDate.isEmpty() && !addTime.isEmpty() && !addGameLoc.isEmpty() && !addPlayers.isEmpty() && selectedRadioButtonId != -1;
    }
    private void updateExistingGameInDatabase() {

        existingGame.setDate(addDate);
        existingGame.setTime(addTime);
        existingGame.setLocation(addGameLoc);
        existingGame.setNumOfPlayer(addPlayers);
        existingGame.setLevel(selectedLevel);

        // Update the existing game details in the database
        DatabaseReference existingGameRef = gamesReference.child(existingGame.getGameID());
        existingGameRef.child("details").setValue(existingGame)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Successfully updated the game", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("EditGame", "Failed to update the game", e);
                    Toast.makeText(requireContext(), "Failed to update the game", Toast.LENGTH_SHORT).show();
                });
    }
    private Calendar parseDate(String date) {
        try {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0].trim());
            int month = Integer.parseInt(parts[1].trim()) - 1; // months are zero-based in Calendar
            int day = Integer.parseInt(parts[2].trim());

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            return calendar;
        } catch (Exception e) {
            return Calendar.getInstance(); // return current date in case of any exception

        }
    }


    private void handleDatePickerClick(View view) {
        Calendar calendar;
        if (existingGame != null) {
            // Editing a game, get the stored date
            calendar = parseDate(existingGame.getDate());
        } else {
            // Creating a new game, get the current date
            calendar = Calendar.getInstance();
        }
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        CalendarConstraints.DateValidator pastDatesValidator = DateValidatorPointForward.now();
        constraintsBuilder.setValidator(pastDatesValidator);
        CalendarConstraints constraints = constraintsBuilder.build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_date)
                .setSelection(calendar.getTimeInMillis())
                .setCalendarConstraints(constraints)
                .build();

        datePicker.show(getParentFragmentManager(), "datepicker");
        datePicker.addOnPositiveButtonClickListener(selection -> dateTv.setText(datePicker.getHeaderText()));
    }

    private int[] parseTime(String time) {
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0].trim());
            String[] minuteParts = parts[1].split(" ");
            int minute = Integer.parseInt(minuteParts[0].trim());
            String amPm = minuteParts[1].trim();
            if ("PM".equals(amPm) && hour != 12) {
                hour += 12;
            } else if ("AM".equals(amPm) && hour == 12) {
                hour = 0;
            }
            return new int[]{hour, minute};
        } catch (Exception e) {
            return new int[]{0, 0};
        }
    }
    private void handleTimePickerClick(View view) {
        int hour, minute;
        final Calendar calendar = Calendar.getInstance();
        if (existingGame != null) {
            // Editing a game, get the stored time
            int[] timeParts = parseTime(existingGame.getTime());
            hour = timeParts[0];
            minute = timeParts[1];
        } else {
            // Creating a new game, get the current time
            hour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
            minute = calendar.get(Calendar.MINUTE);
        }

        MaterialTimePicker timepicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(R.string.select_time)
                .setInputMode(INPUT_MODE_KEYBOARD)
                .build();

        timepicker.show(getParentFragmentManager(), "timepicker");
        timepicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedHour = timepicker.getHour(); // 24-hour format
                int selectedMinute = timepicker.getMinute();

                // Get the selected date from the date picker
                String selectedDateString = dateTv.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                try {
                    Date selectedDate = sdf.parse(selectedDateString);
                    if (selectedDate != null) {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.setTime(selectedDate);
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        selectedCalendar.set(Calendar.MINUTE, selectedMinute);

                        // Check if the selected date and time is before the current date and time
                        if (selectedCalendar.getTime().before(calendar.getTime())) {
                            // Show a message to the user and don't proceed
                            Toast.makeText(getContext(), "You cannot select a past time", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String format;
                if (selectedHour == 0) {
                    selectedHour += 12; // 12:00 AM
                    format = "AM";
                } else if (selectedHour == 12) {
                    format = "PM";
                } else if (selectedHour > 12) {
                    selectedHour -= 12;
                    format = "PM";
                } else {
                    format = "AM";
                }

                String formattedTime = String.format(Locale.US, "%02d:%02d %s", selectedHour, selectedMinute, format);
                timeTv.setText(formattedTime);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_games_layout, container, false);
        initializeViews(rootView);
        setupClickListeners(rootView);
        setupLevelRadioGroupListener();


        if (existingGame != null) {
            gameStatus.setText(R.string.edit_game);
            deleteGameBtn.setVisibility(View.VISIBLE);
            populateFieldsForEditing(existingGame);
        }
        return rootView;
    }
    private void populateFieldsForEditing(Game game) {

        dateTv.setText(game.getDate());
        timeTv.setText(game.getTime());
        gameLocationTv.setText(game.getLocation());
        selectedLocation = game.getLocation();
        playersNeededET.setText(game.getNumOfPlayer() != null ? String.valueOf(game.getNumOfPlayer()) : "");


        String gameLevel = game.getLevel();
        if (gameLevel != null) {
            switch (gameLevel) {
                case "Beginner":
                    levelRadioGroup.check(R.id.game_beginner_radiobtn);
                    selectedLevel = "Beginner";
                    break;
                case "Amateur":
                    levelRadioGroup.check(R.id.game_ama_radiobtn);
                    selectedLevel = "Amateur";
                    break;
                case "Professional":
                    levelRadioGroup.check(R.id.game_pro_radiobtn);
                    selectedLevel = "Professional";
                    break;
                default:
                    // Handle the case where game level does not match any known values
                    break;
            }
        } else {
            // Handle the case where game level is null
        }
    }
    private void handleDeleteGameClick(View view) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setTitle("Delete Game")
                .setMessage("Are you sure you want to delete this game?")
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.confirm), (dialogInterface, which) -> {
                    deleteGameFromDatabase();
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        dialog.show();
    }

    private void deleteGameFromDatabase() {
        if (existingGame != null) {
            showLoadingIndicator();
            gamesReference.child(existingGame.getGameID()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        hideLoadingIndicator();
                        Toast.makeText(requireContext(), "Game deleted successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingIndicator();
                        Toast.makeText(requireContext(), "Failed to delete the game", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        launcher.unregister();
        auth =null;
        user = null;
        gamesReference = null;
        database = null;


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View view = getView();
        if (view != null) {
            LinearLayout datePickerLayout = view.findViewById(R.id.datePickerLayout);
            LinearLayout timePickerLayout = view.findViewById(R.id.timePickerLayout);
            LinearLayout gameLocationLayout = view.findViewById(R.id.gamesLocationLayout);

            if (datePickerLayout != null) {
                datePickerLayout.setOnClickListener(null);
            }

            if (timePickerLayout != null) {
                timePickerLayout.setOnClickListener(null);
            }

            if (gameLocationLayout != null) {
                gameLocationLayout.setOnClickListener(null);
            }
        }

        if (levelRadioGroup != null) {
            levelRadioGroup.setOnCheckedChangeListener(null);
            levelRadioGroup = null;
        }

        if (addGameBtn != null) {
            addGameBtn.setOnClickListener(null);
            addGameBtn = null;
        }

        if (deleteGameBtn != null) {
            deleteGameBtn.setOnClickListener(null);
            deleteGameBtn = null;
        }

        playersNeededET = null;
        gameLocationTv = null;
        dateTv = null;
        timeTv = null;
        gameStatus = null;
        loadingIndicator = null;
    }
    @Override
    public void onPause() {
        super.onPause();
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