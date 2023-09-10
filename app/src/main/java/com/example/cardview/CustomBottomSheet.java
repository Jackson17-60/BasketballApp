package com.example.cardview;

import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomBottomSheet extends BottomSheetDialogFragment {
    private static final String SUCCESS_MESSAGE = "Successfully created the game";
    private static final String FAILURE_MESSAGE = "Failed to create the new game";
    private int layoutResId;
    private String selectedLevel, addDate, addTime, addGameLoc, addPlayers;
    private RadioGroup levelRadioGroup;
    private Button addGameBtn;
    private TextView dateTv, timeTv;
    private EditText gameLocationET, playersNeededET;
    private DatabaseReference gamesReference;
    private FirebaseUser user;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    public CustomBottomSheet(int layoutResId){
        this.layoutResId = layoutResId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        gamesReference = database.getReference().child("games");
    }
    private void initializeViews(View rootView) {
        levelRadioGroup = rootView.findViewById(R.id.gamelevelRadioGroup);
        playersNeededET = rootView.findViewById(R.id.playersNeededET);
        gameLocationET = rootView.findViewById(R.id.gameLocationET);
        dateTv = rootView.findViewById(R.id.gameDateText);
        timeTv = rootView.findViewById(R.id.gameTimeText);
        addGameBtn = rootView.findViewById(R.id.addgame_btn);
    }
    private void setupClickListeners(View rootView) {
        LinearLayout datePickerLayout = rootView.findViewById(R.id.datePickerLayout);
        LinearLayout timePickerLayout = rootView.findViewById(R.id.timePickerLayout);
        datePickerLayout.setOnClickListener(this::handleDatePickerClick);
        timePickerLayout.setOnClickListener(this::handleTimePickerClick);
        addGameBtn.setOnClickListener(this::handleAddGameClick);
    }
    private void setupLevelRadioGroupListener() {
        levelRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.game_beginner_radiobtn) {
                    selectedLevel = "Beginner";
                } else if (checkedId == R.id.game_ama_radiobtn) {
                    selectedLevel = "Amateur";
                }
                else if(checkedId == R.id.game_pro_radiobtn) {
                    selectedLevel = "Professional";
                }
            }
        });
    }

    private void handleAddGameClick(View view) {
        addDate = dateTv.getText().toString().trim();
        addTime = timeTv.getText().toString().trim();
        addGameLoc = gameLocationET.getText().toString().trim();
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
        newGameRef.setValue(gameData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), SUCCESS_MESSAGE, Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("AddGame", "Failed to create the game", e);
                    Toast.makeText(requireContext(), FAILURE_MESSAGE, Toast.LENGTH_SHORT).show();
                });
    }

    private void handleDatePickerClick(View view) {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        CalendarConstraints.DateValidator pastDatesValidator = DateValidatorPointForward.now();
        constraintsBuilder.setValidator(pastDatesValidator);
        CalendarConstraints constraints = constraintsBuilder.build();
        MaterialDatePicker<Long> datepicker = MaterialDatePicker.Builder.datePicker().setTitleText(R.string.select_date).setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .setCalendarConstraints(constraints)
                        .build();
                datepicker.show(getParentFragmentManager(),"datepicker");
        datepicker.addOnPositiveButtonClickListener(selection -> dateTv.setText(datepicker.getHeaderText()));

    }

    private void handleTimePickerClick(View view) {
        final Calendar calendar = Calendar.getInstance();
            final int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
            final int currentMinute = calendar.get(Calendar.MINUTE);


                MaterialTimePicker timepicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(currentHour)
                        .setMinute(currentMinute)
                        .setTitleText(R.string.select_time)
                        .setInputMode(INPUT_MODE_KEYBOARD)
                        .build();
                timepicker.show(getParentFragmentManager(),"timepicker");
                timepicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int hour = timepicker.getHour(); // 24-hour format
                        int minute = timepicker.getMinute();
                        String format;
                        if (hour == 0) {
                            hour = 12; // 12:00 AM
                            format = "AM";
                        } else if (hour == 12) {
                            format = "PM";
                        } else if (hour > 12) {
                            hour -= 12;
                            format = "PM";
                        } else {
                            format = "AM";
                        }
                        String formattedTime = String.format(Locale.US, "%02d:%02d %s", hour, minute, format);
                        timeTv.setText(formattedTime);
                    }
                });

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(layoutResId, container, false);
        initializeViews(rootView);
        setupClickListeners(rootView);
        setupLevelRadioGroupListener();
        return rootView;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
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