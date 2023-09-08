package com.example.cardview;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


    private TextView locationTv, heightTv, genderTv, levelTv, nameTv,amaTv,beginnerTV,proTv;
    private ImageView imageViewProfilePicture;
    private LinearLayout nameLayout,genderLayout,heightLayout,levelLayout,locationLayout;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    FirebaseAuth database;
    FirebaseUser user;
    DatabaseReference db;
    DatabaseReference databaseRef;
    String height,gender, location, level, name,profileImg,selectedSex,selectedLevel;
    EditText locationET,change_name;
    NumberPicker heightCm,heightDecimal;
    AlertDialog dialog;

    RadioGroup genderRadioGroup;
    RadioButton maleRadioButton,femaleRadioButton;
    public ProfileFragment(String name , String gender, String height, String level , String location ,String profileImg) {
        this.name = name;
        this.gender = gender;
        this.height= height;
        this.level = level;
        this.location = location;
        this.profileImg = profileImg;
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseAuth.getInstance();
        user = database.getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        databaseRef = FirebaseDatabase.getInstance().getReference().child(user.getUid());

        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                storageRef.child(user.getUid()).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.child(user.getUid()).getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUri) {
                                        String imageUrl = downloadUri.toString();
                                        Toast.makeText(getContext(), "Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                                        db.child(user.getUid()).child("profileImg").setValue(imageUrl);

                                        Glide.with(ProfileFragment.this).load(downloadUri).into(imageViewProfilePicture);
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
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
        setProfileData(name , gender,  height,  level ,  location,profileImg);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the user's data
                    gender = dataSnapshot.child("gender").getValue(String.class);
                    height = dataSnapshot.child("height").getValue(String.class);
                    level = dataSnapshot.child("level").getValue(String.class);
                    location = dataSnapshot.child("location").getValue(String.class);
                    name = dataSnapshot.child("name").getValue(String.class);
                    profileImg = dataSnapshot.child("profileImg").getValue(String.class);
                }
                setProfileData(gender, height, level, location, name, profileImg);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
            }
        });
        imageViewProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        nameLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showNameDialog();
            }
        });
        genderLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showGenderDialog();
            }
        });
        heightLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showHeightDialog();
            }
        });
        levelLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showLevelDialog();
            }
        });
        locationLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                showLocationDialog();
            }
        });
        return view;
    }

    private void showNameDialog() {
        dialog =  createCustomAlertDialog(R.layout.name_layout);
        change_name.setHint(name);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = change_name.getText().toString();
                db.child(user.getUid()).child("name").setValue(newName);
                Toast.makeText(getContext(), "Name Changed", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
    private void showGenderDialog() {
        dialog =  createCustomAlertDialog(R.layout.gender_layout);
        if(gender=="Male"){
            maleRadioButton.setChecked(true);
        }
        else{
            femaleRadioButton.setChecked(true);
        }
        selectedSex = gender;
        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.male_radiobtn) {
                    // Male radio button is selected
                    selectedSex = "Male";
                    // Do something with the selectedGender value
                } else if (checkedId == R.id.female_radiobtn) {
                    selectedSex = "Female";
                }
            }
        });

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("Sex","sex"+selectedSex);
                db.child(user.getUid()).child("gender").setValue(selectedSex);
            }
        });

        dialog.show();
    }
    private void showHeightDialog() {
        dialog =  createCustomAlertDialog(R.layout.height_picker);
        heightCm.setMaxValue(240);
        heightCm.setMinValue(80);
        heightCm.setValue(170);
        String[] decimalValues = new String[] {".0", "0.1", "0.2", "0.3", "0.4","0.5","0.6","0.7","0.8","0.9" };
        heightDecimal.setMinValue(0);  // Set the minimum value
        heightDecimal.setMaxValue(9); // Set the maximum value
        heightDecimal.setDisplayedValues(decimalValues);
        if(height.contains(".")){
            String[] parts = height.split("\\.");
            heightCm.setValue(Integer.parseInt(parts[0]));
            heightDecimal.setValue(Integer.parseInt(parts[1]));
        }
        else{
            heightCm.setValue(Integer.parseInt(height));
            heightDecimal.setValue(0);
        }

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newHeight = Integer.toString(heightCm.getValue()) + "." + heightDecimal.getValue();
                db.child(user.getUid()).child("height").setValue(newHeight);
            }
        });

        dialog.show();
    }

    private void showLevelDialog() {
        dialog =  createCustomAlertDialog(R.layout.level_layout);
        amaTv.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         selectedLevel = "Amateur";
                                     }
                                 });
        beginnerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedLevel = "Beginner";
            }
        });
        proTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedLevel = "Professional";
            }
        });

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.child(user.getUid()).child("level").setValue(selectedLevel);
            }
        });

        dialog.show();
    }

    private void showLocationDialog() {

        AlertDialog dialog =  createCustomAlertDialog(R.layout.location_layout);
        locationET.setHint(location);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newLoc = locationET.getText().toString();
                db.child(user.getUid()).child("location").setValue(newLoc);
                Toast.makeText(getContext(), "Location Updated", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }


    private AlertDialog createCustomAlertDialog(int layoutResId) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(layoutResId, null);
        locationET = dialogView.findViewById(R.id.locationET);
        amaTv = dialogView.findViewById(R.id.amaTv);
        beginnerTV = dialogView.findViewById(R.id.beginnerTV);
        proTv = dialogView.findViewById(R.id.proTv);
        heightCm = dialogView.findViewById(R.id.heightCm);
        heightDecimal = dialogView.findViewById(R.id.heightDecimal);
         genderRadioGroup = dialogView.findViewById(R.id.genderRadioGroup);
         maleRadioButton = dialogView.findViewById(R.id.male_radiobtn);
         femaleRadioButton = dialogView.findViewById(R.id.female_radiobtn);
         change_name = dialogView.findViewById(R.id.change_name);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
                .setView(dialogView)
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        return dialog;
    }
    public void setProfileData(String gender, String height, String level, String location, String name, String profileImg) {
        genderTv.setText(gender);
        heightTv.setText(height + " cm");
        levelTv.setText(level);
        locationTv.setText(location);
        nameTv.setText(name);
        Glide.with(ProfileFragment.this).load(profileImg).into(imageViewProfilePicture);
    }

}