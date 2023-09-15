package com.example.cardview;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;


import java.util.ArrayList;
import java.util.List;

public class GroupChatFragment extends Fragment{


    private RecyclerView groupChatRecyclerView;
    private GroupChatAdapter groupChatAdapter;
    private List<GroupChat> groupChatList = new ArrayList<>();
    private static final String TAG = "GroupChatFragment";

    private ListenerRegistration firestoreListener;
    private FloatingActionButton fab;

    public GroupChatFragment() {
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
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);
        groupChatRecyclerView = view.findViewById(R.id.groupChatRecyclerView);
        groupChatAdapter = new GroupChatAdapter(groupChatList, (groupId, groupName)-> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("group_id", groupId);
            intent.putExtra("group_name", groupName);
            startActivity(intent);
        });
        groupChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupChatRecyclerView.setAdapter(groupChatAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(groupChatRecyclerView.getContext(), LinearLayoutManager.VERTICAL);
        Context context = getContext();
        if (context != null) {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.custom_divider);
            if (drawable != null) {
                dividerItemDecoration.setDrawable(drawable);
            } else {
                Log.e("GroupChatFragment", "Drawable not found");
            }
        } else {
            Log.e("GroupChatFragment", "Context is null");
        }
        groupChatRecyclerView.addItemDecoration(dividerItemDecoration);

        setupFab(view);
        fetchGroupChats();
        return view;
    }

    private void fetchGroupChats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        firestoreListener = db.collection("groups")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            GroupChat groupChat = dc.getDocument().toObject(GroupChat.class);
                            groupChat.setId(dc.getDocument().getId());
                            int index = dc.getNewIndex();

                            switch (dc.getType()) {
                                case ADDED:
                                    groupChatList.add(index, groupChat);
                                    groupChatAdapter.notifyItemInserted(index);
                                    break;

                                case MODIFIED:
                                    groupChatList.set(index, groupChat);
                                    groupChatAdapter.notifyItemChanged(index);
                                    break;

                                case REMOVED:
                                    int oldIndex = dc.getOldIndex();
                                    if (oldIndex >= 0 && oldIndex < groupChatList.size()) {
                                        groupChatList.remove(oldIndex);
                                        groupChatAdapter.notifyItemRemoved(oldIndex);
                                    }
                                    break;
                            }
                        }
                    } else {
                        Log.w(TAG, "Snapshot is null");
                    }
                });
    }


    private void setupFab(View view) {
        fab = view.findViewById(R.id.addGroupChat);
        fab.setOnClickListener(view1 -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                CreateGroupBottomSheet bottomSheet = new CreateGroupBottomSheet();
                bottomSheet.show(activity.getSupportFragmentManager(), "CreateGroupBottomSheet");
            } else {
                Log.e("HomeFragment", "Activity is null");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();  // Remove the snapshot listener when the view is destroyed
        }
    }
}