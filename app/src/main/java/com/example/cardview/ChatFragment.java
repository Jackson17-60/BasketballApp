//package com.example.cardview;
//
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageButton;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class ChatFragment extends Fragment {
//    private RecyclerView recyclerView;
//    private ChatAdapter chatAdapter;
//    private List<Message> messageList;
//    private String currentUserId,senderID;
//    FirebaseFirestore db;
//
//    public ChatFragment() {
//        // Required empty public constructor
//    }
//
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_chat, container, false);
//        senderID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        recyclerView = view.findViewById(R.id.message_recycler_view);
//        messageList = new ArrayList<>();
//
//        // Setup RecyclerView
//        db = FirebaseFirestore.getInstance();
//        chatAdapter = new ChatAdapter(messageList, senderID);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(chatAdapter);
//        ImageButton sendButton = view.findViewById(R.id.send_button);
//        EditText messageInput = view.findViewById(R.id.message_edit_text);
//
//        sendButton.setOnClickListener(v -> {
//            String messageText = messageInput.getText().toString();
//            if (!messageText.isEmpty()) {
//                sendMessage(messageText);
//                messageInput.setText("");  // Clear the input field
//            }
//        });
//        db.collection("chat")
//                .orderBy("timestamp") // Ensure messages are ordered by timestamp
//                .addSnapshotListener((value, error) -> {
//                    if (error != null) {
//                        Log.w("Firestore", "Listen failed.", error);
//                        return;
//                    }
//
//                    // Clear the existing messages
//                    messageList.clear();
//
//                    for (DocumentSnapshot doc : value) {
//                        Message message = doc.toObject(Message.class);
//                        messageList.add(message);
//                    }
//
//                    // Notify the adapter that the data set has changed
//                    chatAdapter.notifyDataSetChanged();
//                    recyclerView.scrollToPosition(messageList.size()-1);
//                });
//        return view;
//    }
//    private void sendMessage(String messageText) {
//        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        long timestamp = System.currentTimeMillis();
//
//        // Get a reference to the Firebase Realtime Database
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//
//        // Query the database to get the sender details using senderId
//        databaseReference.child("users").child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                // Get sender details from the database
//                String senderName = dataSnapshot.child("name").getValue(String.class);
//                String senderProfileImg = dataSnapshot.child("profileImg").getValue(String.class);
//
//                // Create a new Message object with sender details
//                Message message = new Message(senderName,senderProfileImg,messageText, senderId, timestamp);
//
//
//                // Add the message to Firestore (assuming you are using Firestore for storing messages)
//                db.collection("chat").add(message)
//                        .addOnSuccessListener(documentReference -> {
//                            Log.d("Test", "DocumentSnapshot added with ID: " + documentReference.getId());
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.w("error", "Error adding document", e);
//                        });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w("error", "Database error: " + error.getMessage());
//
//            }
//
//        });
//    }
//
//
//}