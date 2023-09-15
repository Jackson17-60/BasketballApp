package com.example.cardview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment extends Fragment {


    private TextView locationTv, heightTv, genderTv, levelTv, nameTv;
    private ImageView imageViewProfilePicture,logOutBtn;
    private LinearLayout nameLayout,genderLayout,heightLayout,levelLayout,locationLayout;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    FirebaseAuth database;
    FirebaseUser user;
    DatabaseReference db;
    DatabaseReference databaseRef;
    String height,gender, location, level, name,profileImg;
    Activity activity;
    private DatabaseReference userDatabaseRef;
    private ValueEventListener databaseRefListener;
    private ActivityResultLauncher<String> launcher;
    public ProfileFragment() {
    }


    public ProfileFragment(String name , String gender, String height, String level , String location ,String profileImg) {
        this.name = name;
        this.gender = gender;
        this.height= height;
        this.level = level;
        this.location = location;
        this.profileImg = profileImg;
    }
    public static ProfileFragment newInstance(String selectedLocation) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("selected_location", selectedLocation);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            activity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth and Database reference
        database = FirebaseAuth.getInstance();
        user = database.getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();
        userDatabaseRef = db.child("users").child(user.getUid());

        // Initialize Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Ensure user is not null to avoid NullPointerException in subsequent code
        if(user != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        }


        launcher = registerForActivityResult(new ActivityResultContract<String, String>() {
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, String input) {
                // Create an intent for an ACTIVITY, not a fragment
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("selected_location", input);
                return intent;
            }

            @Override
            public String parseResult(int resultCode, @Nullable Intent intent) {
                if (intent == null || resultCode != Activity.RESULT_OK) {
                    return null;
                }
                return intent.getStringExtra("selected_location");
            }
        }, result -> {
            if (result != null) {
                Log.d("res","res" + result);
                    db.child("users").child(user.getUid()).child("location").setValue(result)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(requireContext(), getString(R.string.location_updated), Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), getString(R.string.location_update_failed), Toast.LENGTH_SHORT).show());
                locationTv.setText(result);
            }
        });




        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                storageRef.child(user.getUid()).putFile(uri).addOnSuccessListener(taskSnapshot ->
                        storageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String imageUrl = downloadUri.toString();
                            if (activity != null) {
                                Toast.makeText(activity, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                            }
                            db.child("users").child(user.getUid()).child("profileImg").setValue(imageUrl);
                            if (isAdded()) { // Ensure the fragment is attached to an activity
                                Glide.with(ProfileFragment.this)
                                        .load(downloadUri)
                                        .placeholder(R.drawable.upload)
                                        .into(imageViewProfilePicture);


                            }
                        })
                ).addOnFailureListener(e -> {
                    if (activity != null) {
                        Toast.makeText(activity, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isAdded()) {
            // Load image with Glide
            Glide.with(this)
                    .load(profileImg)
                    .placeholder(R.drawable.upload)
                    .into(imageViewProfilePicture);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        locationTv = view.findViewById(R.id.locationTextView);
        nameTv = view.findViewById(R.id.nameTextView);
        levelTv = view.findViewById(R.id.levelTextView);
        heightTv = view.findViewById(R.id.heightTextView);
        genderTv = view.findViewById(R.id.genderTextView);
        imageViewProfilePicture = view.findViewById(R.id.profilePic);
        nameLayout = view.findViewById(R.id.nameLayout);
        genderLayout = view.findViewById(R.id.genderLayout);
        heightLayout = view.findViewById(R.id.heightLayout);
        levelLayout = view.findViewById(R.id.levelLayout);
        locationLayout = view.findViewById(R.id.locationLayout);
        logOutBtn = view.findViewById(R.id.logoutButton);

        setProfileData(name, gender, height, level, location, profileImg);
        // Fetch and set profile data from Firebase
        databaseRefListener = new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("gender")) {
                        gender = dataSnapshot.child("gender").getValue(String.class);
                        genderTv.setText(gender);
                    }
                    if (dataSnapshot.hasChild("height")) {
                        height = dataSnapshot.child("height").getValue(String.class);
                        heightTv.setText(getString(R.string.height_format, height));
                    }
                    if (dataSnapshot.hasChild("level")) {
                        level = dataSnapshot.child("level").getValue(String.class);
                        levelTv.setText(level);
                    }
                    if (dataSnapshot.hasChild("location")) {
                        location = dataSnapshot.child("location").getValue(String.class);
                        locationTv.setText(location);
                    }
                    if (dataSnapshot.hasChild("name")) {
                        name = dataSnapshot.child("name").getValue(String.class);
                        nameTv.setText(name);
                    }
                    if (dataSnapshot.hasChild("profileImg")) {
                        profileImg = dataSnapshot.child("profileImg").getValue(String.class);
                        Glide.with(ProfileFragment.this).load(profileImg).placeholder(R.drawable.upload).into(imageViewProfilePicture);
                    }
                }}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
            }
        };
        databaseRef.addValueEventListener(databaseRefListener);


//        imageViewProfilePicture.setOnClickListener(view1 ->
//                pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
//                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
//                        .build())
//        );
        setupClickListeners(view);

        return view;
    }

    private void showNameDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.name_layout, null);
        EditText change_name = dialogView.findViewById(R.id.change_name);
        change_name.setHint(name);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setView(dialogView)
                .setNeutralButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton("Confirm", (dialogInterface, which) -> {
                    String newName = change_name.getText().toString();
                    if (!newName.isEmpty()) {
                        userDatabaseRef.child("name").setValue(newName);
                            Toast.makeText(requireContext(), getString(R.string.name_changed), Toast.LENGTH_SHORT).show();
                    } else {
                            Toast.makeText(requireContext(), getString(R.string.name_empty_error), Toast.LENGTH_SHORT).show();
                    }
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        dialog.show();
    }
    private void showGenderDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.gender_layout, null);
        RadioGroup genderRadioGroup = dialogView.findViewById(R.id.genderRadioGroup);
        RadioButton maleRadioButton = dialogView.findViewById(R.id.male_radiobtn);
        RadioButton femaleRadioButton = dialogView.findViewById(R.id.female_radiobtn);

        if (gender.equals("Male")) {
            maleRadioButton.setChecked(true);
        } else {
            femaleRadioButton.setChecked(true);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setView(dialogView)
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.confirm), (dialogInterface, which) -> {
                    int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                    String selectedGender = ((RadioButton) dialogView.findViewById(selectedId)).getText().toString();
                    db.child("users").child(user.getUid()).child("gender").setValue(selectedGender);

                        Toast.makeText(requireContext(), getString(R.string.gender_updated), Toast.LENGTH_SHORT).show();

                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        dialog.show();
    }

    private void showHeightDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.height_picker, null);
        NumberPicker heightCm = dialogView.findViewById(R.id.heightCm);
        NumberPicker heightDecimal = dialogView.findViewById(R.id.heightDecimal);

        final int MAX_HEIGHT = 240;
        final int MIN_HEIGHT = 80;
        final int DEFAULT_HEIGHT = 170;

        heightCm.setMaxValue(MAX_HEIGHT);
        heightCm.setMinValue(MIN_HEIGHT);
        heightCm.setValue(DEFAULT_HEIGHT);

        String[] decimalValues = new String[]{".0", ".1", ".2", ".3", ".4", ".5", ".6", ".7", ".8", ".9"};
        heightDecimal.setMinValue(0);
        heightDecimal.setMaxValue(9);
        heightDecimal.setDisplayedValues(decimalValues);

        try {
            if (height.contains(".")) {
                String[] parts = height.split("\\.");
                heightCm.setValue(Integer.parseInt(parts[0]));
                heightDecimal.setValue(Integer.parseInt(parts[1]));
            } else {
                heightCm.setValue(Integer.parseInt(height));
                heightDecimal.setValue(0);
            }
        } catch (NumberFormatException e) {
            Log.e("ProfileFragment", "Invalid height format: " + height, e);
            // Set to default values in case of an error
            heightCm.setValue(DEFAULT_HEIGHT);
            heightDecimal.setValue(0);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setView(dialogView)
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.confirm), (dialogInterface, which) -> {
                    String newHeight = Integer.toString(heightCm.getValue()) + decimalValues[heightDecimal.getValue()];
                    db.child("users").child(user.getUid()).child("height").setValue(newHeight);
                        Toast.makeText(requireContext(), getString(R.string.height_updated), Toast.LENGTH_SHORT).show();

                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        dialog.show();
    }
    private void showLogOut() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out ?")
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.confirm), (dialogInterface, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    databaseRef.child("fcmToken").setValue(null);
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        dialog.show();
    }
    private void showLevelDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.level_layout, null);

        RadioGroup levelRadioGroup = dialogView.findViewById(R.id.levelRadioGroup);
        RadioButton beginnerBtn = dialogView.findViewById(R.id.beginner_radiobtn);
        RadioButton amaBtn = dialogView.findViewById(R.id.ama_radiobtn);
        RadioButton proBtn = dialogView.findViewById(R.id.pro_radiobtn);

        // Set the current level on the radio group
        setInitialLevelSelection(level, beginnerBtn, amaBtn, proBtn);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setView(dialogView)
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.confirm), (dialogInterface, which) -> {
                    int selectedId = levelRadioGroup.getCheckedRadioButtonId();
                    String selectedLevel = getSelectedLevel(selectedId, beginnerBtn, amaBtn, proBtn);
                    db.child("users").child(user.getUid()).child("level").setValue(selectedLevel);
                        Toast.makeText(requireContext(), getString(R.string.level_updated), Toast.LENGTH_SHORT).show();
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        dialog.show();
    }

    private void setInitialLevelSelection(String currentLevel, RadioButton beginnerBtn, RadioButton amaBtn, RadioButton proBtn) {
        switch (currentLevel) {
            case "Beginner":
                beginnerBtn.setChecked(true);
                break;
            case "Amateur":
                amaBtn.setChecked(true);
                break;
            case "Professional":
                proBtn.setChecked(true);
                break;
            default:
                beginnerBtn.setChecked(false);
                amaBtn.setChecked(false);
                proBtn.setChecked(false);
                break;
        }
    }

    private String getSelectedLevel(int selectedId, RadioButton beginnerBtn, RadioButton amaBtn, RadioButton proBtn) {
        if (selectedId == beginnerBtn.getId()) {
            return "Beginner";
        } else if (selectedId == amaBtn.getId()) {
            return "Amateur";
        } else if (selectedId == proBtn.getId()) {
            return "Professional";
        }
        return "Beginner";  // Default value
    }


    private void setupClickListeners(View view) {
        imageViewProfilePicture.setOnClickListener(v ->
                pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        nameLayout.setOnClickListener(v -> showNameDialog());
        genderLayout.setOnClickListener(v -> showGenderDialog());
        heightLayout.setOnClickListener(v -> showHeightDialog());
        levelLayout.setOnClickListener(v -> showLevelDialog());
        logOutBtn.setOnClickListener(v -> showLogOut());

        locationLayout.setOnClickListener(v -> launcher.launch(location));
    }



    public void setProfileData(String gender, String height, String level, String location, String name, String profileImg) {
        if (gender != null && !gender.isEmpty()) {
            genderTv.setText(gender);
        }

        if (height != null && !height.isEmpty()) {
            String heightText = getString(R.string.height_format, height);
            heightTv.setText(heightText);
        }

        if (level != null && !level.isEmpty()) {
            levelTv.setText(level);
        }

        if (location != null && !location.isEmpty()) {
            locationTv.setText(location);
        }

        if (name != null && !name.isEmpty()) {
            nameTv.setText(name);
        }

        if (profileImg != null && !profileImg.isEmpty()) {
            Glide.with(ProfileFragment.this).load(profileImg).placeholder(R.drawable.upload).into(imageViewProfilePicture);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseRefListener != null && databaseRef != null) {
            databaseRef.removeEventListener(databaseRefListener);
        }
        locationTv = null;
        nameTv = null;
        levelTv = null;
        heightTv = null;
        genderTv = null;
        imageViewProfilePicture = null;
        nameLayout.setOnClickListener(null);
        genderLayout.setOnClickListener(null);
        heightLayout.setOnClickListener(null);
        levelLayout.setOnClickListener(null);
        locationLayout.setOnClickListener(null);
        launcher.unregister();
    }
}