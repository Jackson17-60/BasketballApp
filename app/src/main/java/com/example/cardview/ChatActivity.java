package com.example.cardview;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cardview.RecyclerView.ChatRecylerViewAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import com.example.cardview.Model_Class.Message;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private ChatRecylerViewAdapter chatRecylerViewAdapter;
    private TextView chat_title;

    private ImageView chatImg;
    private List<Message> messageList = new ArrayList<>();
    private String senderID, groupId,groupName,groupChatImage;

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
        groupChatImage = getIntent().getStringExtra("group_image");
        if (groupId == null || groupName == null ||  groupChatImage == null) {
            Log.e(TAG, "Group ID, group name or group chat image is null");
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
        chatImg = findViewById(R.id.chatImage);
        recyclerView = findViewById(R.id.message_recycler_view);
        ImageButton sendButton = findViewById(R.id.send_button);
        EditText messageInput = findViewById(R.id.message_edit_text);
        ImageButton backBtn = findViewById(R.id.chatback_button);
        chat_title.setText(groupName);

            Glide.with(this)
                    .load(groupChatImage)
                    .placeholder(R.drawable.upload)
                    .circleCrop()
                    .into(chatImg);

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
        chatRecylerViewAdapter = new ChatRecylerViewAdapter(messageList, senderID);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatRecylerViewAdapter);

        chatRecylerViewAdapter.setOnMessageLongClickListener((position, message) -> {
            if (message.getSenderUid().equals(senderID)) {
                showDeleteConfirmationDialog(position, message.getDocumentId());
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatRecylerViewAdapter);

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
                            message.setDocumentId(dc.getDocument().getId());
                            int index = dc.getNewIndex();

                            switch (dc.getType()) {
                                case ADDED:
                                    messageList.add(index, message);
                                    chatRecylerViewAdapter.notifyItemInserted(index);
                                    break;

                                case MODIFIED:
                                    messageList.set(index, message);
                                    chatRecylerViewAdapter.notifyItemChanged(index);
                                    break;

                                case REMOVED:
                                    int oldIndex = dc.getOldIndex();
                                    if (oldIndex >= 0 && oldIndex < messageList.size()) {
                                        messageList.remove(oldIndex);
                                        chatRecylerViewAdapter.notifyItemRemoved(oldIndex);
                                    }
                                    break;
                            }
                        }

                        // Scrolling to the last position to show the latest message
                        if (!messageList.isEmpty()) {
                            recyclerView.post(() -> recyclerView.scrollToPosition(messageList.size() - 1));
                        }
                    } else {
                        Log.w(TAG, "Snapshot is null");
                    }
                });
    }

    private void showDeleteConfirmationDialog(int position, String documentId) {

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(ChatActivity.this, R.style.MaterialAlertDialog_rounded)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.confirm), (dialogInterface, which) -> {
                    deleteMessageFromFirestore(position, documentId);
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        dialog.show();

    }

    private void deleteMessageFromFirestore(int position, String documentId) {
        if (position >= 0 && position < messageList.size()) {
            db.collection("chat")
                    .document(groupId)
                    .collection("messages")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Log success
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error deleting document", e);
                        Toast.makeText(ChatActivity.this, "Error deleting message.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.w(TAG, "Invalid position: " + position);
        }
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
