package com.example.cardview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardview.databinding.ItemMessageMeBinding;

public class SentMessageViewHolder extends RecyclerView.ViewHolder {
    private final ItemMessageMeBinding binding;

    public SentMessageViewHolder(@NonNull ItemMessageMeBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void resetView() {
        binding.sendMsgTV.setText(null);
        binding.sendTimeStamp.setText(null);
    }

    public void bind(String messageText, String timeStamp) {
        binding.sendMsgTV.setText(messageText);
        binding.sendTimeStamp.setText(timeStamp);
    }
}
