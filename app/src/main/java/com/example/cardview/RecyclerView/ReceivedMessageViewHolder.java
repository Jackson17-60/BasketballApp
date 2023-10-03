package com.example.cardview.RecyclerView;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cardview.R;
import com.example.cardview.databinding.ItemMessageReceiveBinding;

public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
    private final ItemMessageReceiveBinding binding;

    public ReceivedMessageViewHolder(@NonNull ItemMessageReceiveBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void resetView() {
        binding.receivedTV.setText(null);
        binding.senderName.setText(null);
        binding.receivedTimeStamp.setText(null);
        binding.profileImage.setImageDrawable(null);
    }

    public void bind(String messageText, String senderNameText, String timeStamp, String imageUrl) {
        binding.receivedTV.setText(messageText);
        binding.senderName.setText(senderNameText);
        binding.receivedTimeStamp.setText(timeStamp);
        Log.d("binding","binding"+imageUrl);
        Glide.with(binding.getRoot().getContext())
                .load(imageUrl)
                .placeholder(R.drawable.upload)
                .circleCrop()
                .into(binding.profileImage);
    }

    public void cleanup() {
        Glide.with(binding.getRoot().getContext()).clear(binding.profileImage);
    }
}
