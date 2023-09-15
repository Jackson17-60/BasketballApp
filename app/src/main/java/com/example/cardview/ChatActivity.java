package com.example.cardview;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private TextView chat_title;
    private List<Message> messageList = new ArrayList<>();
    private String senderID, groupId,groupName;

    private FirebaseFirestore db;
    private ListenerRegistration firestoreListenerRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase services
        senderID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Get group ID from intent
        groupId = getIntent().getStringExtra("group_id");
        groupName = getIntent().getStringExtra("group_name");
        if (groupId == null || groupName == null) {
            Log.e(TAG, "Group ID or group name is null");
            finish();
            return;
        }


        // Setup views
        setupViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup Firestore listener
        setupFirestoreListener();
    }
    private void setupViews() {
        chat_title = findViewById(R.id.chat_title);
        recyclerView = findViewById(R.id.message_recycler_view);
        ImageButton sendButton = findViewById(R.id.send_button);
        EditText messageInput = findViewById(R.id.message_edit_text);
        ImageButton backBtn = findViewById(R.id.chatback_button);
        chat_title.setText(groupName);
        backBtn.setOnClickListener(v -> onBackPressed());
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                messageInput.setText("");
            }
        });
    }
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList, senderID);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }
    private void setupFirestoreListener() {
        if (groupId == null) {
            Log.e(TAG, "Cannot set up Firestore listener; group ID is null");
            return;
        }

        firestoreListenerRegistration = db.collection("chat")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener(this, (snapshots, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        Toast.makeText(ChatActivity.this, "Error loading messages.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Message message = dc.getDocument().toObject(Message.class);
                            int index = dc.getNewIndex();

                            switch (dc.getType()) {
                                case ADDED:
                                    messageList.add(index, message);
                                    chatAdapter.notifyItemInserted(index);
                                    break;

                                case MODIFIED:
                                    messageList.set(index, message);
                                    chatAdapter.notifyItemChanged(index);
                                    break;

                                case REMOVED:
                                    int oldIndex = dc.getOldIndex();
                                    if (oldIndex >= 0 && oldIndex < messageList.size()) {
                                        messageList.remove(oldIndex);
                                        chatAdapter.notifyItemRemoved(oldIndex);
                                    }
                                    break;
                            }
                        }
                        if (!messageList.isEmpty()) {
                            recyclerView.post(() -> recyclerView.scrollToPosition(messageList.size() - 1));
                        }
                    } else {
                        Log.w(TAG, "Snapshot is null");
                    }



                    // Scrolling to the last position to show the latest message
                    if (!messageList.isEmpty()) {
                        recyclerView.post(() -> recyclerView.scrollToPosition(messageList.size() - 1));
                    }
                });
    }


    private void sendMessage(String messageText) {
        long timestamp = System.currentTimeMillis();
        Message message = new Message(senderID, messageText, timestamp);

        db.collection("chat")
                .document(groupId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(ChatActivity.this, "Error sending message.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListenerRegistration != null) {
            firestoreListenerRegistration.remove();
        }
    }
}
