package com.example.cardview.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardview.RecyclerView.ReceivedMessageViewHolder;
import com.example.cardview.RecyclerView.SentMessageViewHolder;
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

import com.example.cardview.Model_Class.Message;
import com.example.cardview.Model_Class.User;

public class ChatRecylerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private DatabaseReference databaseReference;
    private List<Message> messages;
    private String currentUserId;

    public ChatRecylerViewAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    }


    public interface OnMessageLongClickListener {
        void onMessageLongClick(int position, Message message);
    }

    private OnMessageLongClickListener onMessageLongClickListener;

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener) {
        this.onMessageLongClickListener = listener;
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
        holder.itemView.setOnLongClickListener(v -> {
            if (onMessageLongClickListener != null) {
                onMessageLongClickListener.onMessageLongClick(position, message);
            }
            return true;
        });
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
                        Log.d("user profile img","pro img"+user.getprofileImg());
                        receiveViewHolder.bind(message.getMessageText(), user.getName(), formattedTime, user.getprofileImg());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("ChatRecylerViewAdapter", "Database read failed", error.toException());
                }
            });
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ReceivedMessageViewHolder) {
//            ((ReceivedMessageViewHolder) holder).cleanup();
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
