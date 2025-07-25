package com.example.mapmanager.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Chat> chats;
    private final OnChatClickListener onChatClickListener;
    public interface OnChatClickListener{
        void onChatClick(Chat chat, int position);
    }
    public ChatListAdapter(Context context, List<Chat> chats, OnChatClickListener onChatClickListener) {
        this.chats = chats;
        this.inflater = LayoutInflater.from(context);
        this.onChatClickListener = onChatClickListener;
    }
    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.chat_item_layout, parent, false);
        return new ViewHolder(view);
    }


        @Override
    public void onBindViewHolder(ChatListAdapter.ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        if (chat != null) {
            holder.text1.setText(chat.getGroupName());
            Glide.with(holder.itemView.getContext())
                    .load(chat.getGroupAvatarUrl())
                    .placeholder(R.drawable.account_icon)
                    .into(holder.imageView);
            if (chat.getLastMessage() != null) {
                holder.text2.setText(chat.getLastMessage());
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChatClickListener.onChatClick(chat, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text1, text2;
        final ShapeableImageView imageView;
        ViewHolder(View view){
            super(view);
            text1 = view.findViewById(R.id.textView);
            text2 = view.findViewById(R.id.textView2);
            imageView = view.findViewById(R.id.imageView);
        }
    }
}