package com.example.cardview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cardview.databinding.ItemMessageMeBinding;
import com.example.cardview.databinding.ItemMessageReceiveBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private DatabaseReference databaseReference;
    private List<Message> messages;
    private String currentUserId;

    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ME) {
            ItemMessageMeBinding binding = ItemMessageMeBinding.inflate(inflater, parent, false);
            return new SentMessageViewHolder(binding);
        } else {
            ItemMessageReceiveBinding binding = ItemMessageReceiveBinding.inflate(inflater, parent, false);
            return new ReceivedMessageViewHolder(binding);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        String formattedTime = formatTimestamp(message.getTimestamp());

        if (holder.getItemViewType() == VIEW_TYPE_ME) {
            SentMessageViewHolder sendMsgViewHolder = (SentMessageViewHolder) holder;
            sendMsgViewHolder.bind(message.getMessageText(), formattedTime);
        } else {
            ReceivedMessageViewHolder receiveViewHolder = (ReceivedMessageViewHolder) holder;

            databaseReference.child(message.getSenderUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        receiveViewHolder.bind(message.getMessageText(), user.getName(), formattedTime, user.getprofileImg());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("ChatAdapter", "Database read failed", error.toException());
                }
            });
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).cleanup();
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return currentUserId.equals(message.getSenderUid()) ? VIEW_TYPE_ME : VIEW_TYPE_RECEIVED;
    }

    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
