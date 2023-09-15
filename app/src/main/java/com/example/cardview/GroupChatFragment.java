package com.example.cardview;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupChatFragment extends Fragment{


    private RecyclerView groupChatRecyclerView;
    private GroupChatAdapter groupChatAdapter;
    private List<GroupChat> groupChatList = new ArrayList<>();

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
        db.collection("groups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        groupChatList.clear(); // Clear the existing list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GroupChat groupChat = document.toObject(GroupChat.class);
                            groupChat.setId(document.getId());
                            groupChatList.add(groupChat);
                            groupChatAdapter.notifyItemInserted(groupChatList.size() - 1); // Notify about the newly inserted item
                        }
                    } else {
                        Log.w("Firebase", "Error getting documents.", task.getException());
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


}