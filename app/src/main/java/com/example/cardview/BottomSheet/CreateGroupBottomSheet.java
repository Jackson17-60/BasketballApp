package com.example.cardview.BottomSheet;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.cardview.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import com.example.cardview.Model_Class.GroupChat;

public class CreateGroupBottomSheet  extends BottomSheetDialogFragment {

        private ImageView groupImage;
        private EditText groupNameET, groupDescriptionET;
        private Button createGrpBtn;
        private ProgressBar loadingIndicator;
        private String imageUrl;
        private Uri selectedImageUri;
        private String buttonText;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    public CreateGroupBottomSheet() {

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_groupchat_layout, container, false);
        groupImage = rootView.findViewById(R.id.groupImage);

        groupNameET = rootView.findViewById(R.id.groupNameET);
        groupDescriptionET = rootView.findViewById(R.id.groupDescriptionET);
        createGrpBtn = rootView.findViewById(R.id.createGrpBtn);
        loadingIndicator = rootView.findViewById(R.id.groupChatloading_indicator);
        createGrpBtn.setOnClickListener(v -> createGroup());
        buttonText = createGrpBtn.getText().toString();

        setupClickListeners(rootView);
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            selectedImageUri = null;

            if (uri != null) {
                selectedImageUri = uri;
                if (isAdded()) {
                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.upload)
                            .into(groupImage);
                }
            }else{
                Toast.makeText(getContext(), "No Image Uploaded", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void createGroup() {
        String groupName = groupNameET.getText().toString().trim();
        String groupDescription = groupDescriptionET.getText().toString().trim();

        if (groupName.isEmpty()) {
            groupNameET.setError("Group name cannot be empty");
            return;
        }

        if (groupDescription.isEmpty()) {
            groupDescriptionET.setError("Group description cannot be empty");
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "No Group Image Selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CreateGroup", "Selected Image URI: " + selectedImageUri);

        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);
        createGrpBtn.setEnabled(false);
        createGrpBtn.setText("");
        // Create a new group chat object
        // Save the new group chat to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference groupImagesRef = storageRef.child("group_images/" + UUID.randomUUID().toString());

        groupImagesRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    groupImagesRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        imageUrl = downloadUri.toString();
                        loadingIndicator.setVisibility(View.GONE);  // Hide the progress bar here

                        long timestamp = System.currentTimeMillis();
                        GroupChat newGroupChat = new GroupChat();
                        newGroupChat.setName(groupName);
                        newGroupChat.setDescription(groupDescription);
                        newGroupChat.setGroupImage(imageUrl);
                        newGroupChat.setTimestamp(timestamp);

                        db.collection("groups")
                                .add(newGroupChat)
                                .addOnSuccessListener(documentReference -> {
                                    String groupId = documentReference.getId();
                                    newGroupChat.setId(groupId);
                                    db.collection("groups").document(groupId).set(newGroupChat);
                                    if (isAdded()) {
                                        Toast.makeText(getContext(), "Group created successfully", Toast.LENGTH_SHORT).show();

                                    }
                                    loadingIndicator.setVisibility(View.GONE);  // Hide the progress bar here
                                    createGrpBtn.setText(buttonText);
                                    createGrpBtn.setEnabled(true);
                                    dismiss();

                                })
                                .addOnFailureListener(e -> {
                                    // Hide loading indicator
                                    createGrpBtn.setText(buttonText);
                                    createGrpBtn.setEnabled(true);
                                    loadingIndicator.setVisibility(View.GONE);
                                    if (isAdded()) {
                                        Toast.makeText(getContext(), "Failed to create group", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }).addOnFailureListener(e -> {
                        loadingIndicator.setVisibility(View.GONE);  // Hide the progress bar here
                        Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("failure msg", "Upload failure: ", e);
                    });
                })
                .addOnFailureListener(e -> {
                    loadingIndicator.setVisibility(View.GONE);  // Hide the progress bar here
                    Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("failure msg", "Upload failure: ", e);
                });
    }



    private void setupClickListeners(View view) {
        groupImage.setOnClickListener(v ->
                pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );


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
