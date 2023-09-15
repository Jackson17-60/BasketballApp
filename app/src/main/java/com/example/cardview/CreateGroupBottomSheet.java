package com.example.cardview;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class CreateGroupBottomSheet  extends BottomSheetDialogFragment {

        private ImageView groupImage;
        private EditText groupNameET, groupDescriptionET;
        private Button createGrpBtn;
        private ProgressBar loadingIndicator;
        private String imageUrl;
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
//        groupImage.setOnClickListener(view1 ->
//                pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
//                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
//                        .build())
//        );

        setupClickListeners(rootView);
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference groupImagesRef = storageRef.child("group_images/" + UUID.randomUUID().toString());
            if (uri != null) {
                loadingIndicator.setVisibility(View.VISIBLE);
                groupImagesRef.putFile(uri)
                        .addOnSuccessListener(taskSnapshot -> {
                            groupImagesRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                imageUrl = downloadUri.toString();
                                if (isAdded()) {
                                    Glide.with(this)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.upload)
                                            .into(groupImage);
                                }
                                loadingIndicator.setVisibility(View.GONE);  // Hide the progress bar here
                            });
                        })
                        .addOnFailureListener(e -> {
                            loadingIndicator.setVisibility(View.GONE);  // Hide the progress bar here
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
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

        // Show loading indicator


        // Create a new group chat object
        long timestamp = System.currentTimeMillis();
        GroupChat newGroupChat = new GroupChat();
        newGroupChat.setName(groupName);
        newGroupChat.setDescription(groupDescription);
        newGroupChat.setGroupImage(imageUrl);
        newGroupChat.setTimestamp(timestamp);

        // Save the new group chat to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        loadingIndicator.setVisibility(View.VISIBLE);
        db.collection("groups")
                .add(newGroupChat)
                .addOnSuccessListener(documentReference -> {
                    String groupId = documentReference.getId();
                    newGroupChat.setId(groupId);
                    db.collection("groups").document(groupId).set(newGroupChat);
                    if(isAdded()){
                        Toast.makeText(getContext(), "Group created successfully", Toast.LENGTH_SHORT).show();

                    }
                    loadingIndicator.setVisibility(View.GONE);  // Hide the progress bar here
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    loadingIndicator.setVisibility(View.GONE);
                    if(isAdded()){
                        Toast.makeText(getContext(), "Failed to create group", Toast.LENGTH_SHORT).show();
                    }
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
