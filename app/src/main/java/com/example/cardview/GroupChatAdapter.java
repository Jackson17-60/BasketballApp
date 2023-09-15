package com.example.cardview;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.cardview.databinding.GroupchatLayoutBinding;
import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupChatViewHolder> {

    private List<GroupChat> groupChatList;
    private OnGroupChatClickListener onGroupChatClickListener;

    public GroupChatAdapter(List<GroupChat> groupChatList, OnGroupChatClickListener onGroupChatClickListener) {
        this.groupChatList = groupChatList;
        this.onGroupChatClickListener = onGroupChatClickListener;
    }

    public interface OnGroupChatClickListener {
        void onGroupChatClick(String groupId,String groupName);
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GroupchatLayoutBinding binding = GroupchatLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        GroupChat groupChat = groupChatList.get(position);
        holder.binding.groupChatTitle.setText(groupChat.getName());
        holder.binding.groupChatDesciptionTV.setText(groupChat.getDescription());

        Glide.with(holder.binding.groupChatImg.getContext())
                .load(groupChat.getGroupImage())
                .circleCrop()
                .placeholder(R.drawable.baseline_person_24)
                .into(holder.binding.groupChatImg);
    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }

    class GroupChatViewHolder extends RecyclerView.ViewHolder {
        GroupchatLayoutBinding binding;

        public GroupChatViewHolder(@NonNull GroupchatLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                if (onGroupChatClickListener != null) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onGroupChatClickListener.onGroupChatClick(groupChatList.get(position).getId(),groupChatList.get(position).getName());
                    }
                }
            });
        }
    }
}
