package com.example.cardview;

import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD;

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
import java.util.Map;

public class CustomBottomSheet extends BottomSheetDialogFragment {
    int layoutResId;
    String selectedLevel,addDate,addTime,addGameLoc,addPlayers;
    RadioGroup levelRadioGroup;
    Button addgame_btn;
    TextView dateTv, timeTv;
    EditText gameLocationET,playersNeededET;
    public CustomBottomSheet(int layoutResId){
        this.layoutResId = layoutResId;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate your bottom sheet layout here
        View rootView = inflater.inflate(layoutResId, container, false);
        LinearLayout datePickerLayout = rootView.findViewById(R.id.datePickerLayout);
        LinearLayout timePickerLayout = rootView.findViewById(R.id.timePickerLayout);
        dateTv = rootView.findViewById(R.id.gameDateText);
        timeTv = rootView.findViewById(R.id.gameTimeText);
        addgame_btn = rootView.findViewById(R.id.addgame_btn);
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        levelRadioGroup = rootView.findViewById(R.id.gamelevelRadioGroup);
        playersNeededET = rootView.findViewById(R.id.playersNeededET);
        gameLocationET = rootView.findViewById(R.id.gameLocationET);
        CalendarConstraints.DateValidator pastDatesValidator = DateValidatorPointForward.now();
        constraintsBuilder.setValidator(pastDatesValidator);
        CalendarConstraints constraints = constraintsBuilder.build();

        addgame_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDate = dateTv.getText().toString().trim();
                addTime = timeTv.getText().toString().trim();
                addGameLoc = gameLocationET.getText().toString().trim();
                addPlayers = playersNeededET.getText().toString().trim();
                int selectedRadioButtonId = levelRadioGroup.getCheckedRadioButtonId();
                if(addDate.isEmpty() || addTime.isEmpty() ||addGameLoc.isEmpty() || addPlayers.isEmpty() || selectedRadioButtonId == -1){
                    Toast.makeText(requireContext(), "Please Fill In All The Field", Toast.LENGTH_SHORT).show();
                }

                else{
                    FirebaseUser user;
                    FirebaseAuth database ;
                    database = FirebaseAuth.getInstance();
                    user = database.getCurrentUser();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference gamesReference = databaseReference.child("games");

                    DatabaseReference newGameRef = gamesReference.push();
                    Map<String, Object> gameData = new HashMap<>();
                    Game newGame = new Game(newGameRef.getKey(), addDate,addTime,addGameLoc,selectedLevel,addPlayers,1L,user.getUid());
                    gameData.put("details", newGame);
                    Map<String, Object> participants = new HashMap<>();
                    participants.put(user.getUid(), true);
                    gameData.put("participants", participants);
                    if (newGameRef.getKey() != null) {
                        newGame.setGameID(newGameRef.getKey());
                    }
                    newGameRef.setValue(gameData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Successfully created the new game
//                                    Toast.makeText(requireContext(), "Successfully created the new game", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Failed to create the new game
//                                    Toast.makeText(requireContext(), "Failed to create the new game: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    dismiss();
                }
            }
        });
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
        datePickerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDatePicker<Long> datepicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select Date").setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                        .setCalendarConstraints(constraints)
                        .build();
                datepicker.show(getParentFragmentManager(),"datepicker");
                datepicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                            dateTv.setText(datepicker.getHeaderText());
                    }
                });
            }
        });

        timePickerLayout.setOnClickListener(new View.OnClickListener() {
            final Calendar calendar = Calendar.getInstance();
            final int currentHour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
            final int currentMinute = calendar.get(Calendar.MINUTE);
            @Override
            public void onClick(View view) {

                MaterialTimePicker timepicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(currentHour)
                        .setMinute(currentMinute)
                        .setTitleText("Select Time")
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
                        String formattedTime = String.format("%02d:%02d %s", hour, minute, format);
                        timeTv.setText(formattedTime);
                    }
                });
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View)view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}