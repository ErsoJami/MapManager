package com.example.mapmanager.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.models.Chat;
import com.example.mapmanager.R;
import com.example.mapmanager.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Message> messageList;
    private FirebaseAuth mAuth;
    public interface ChatAdapterListener {
        void onMyMessageClick(int position);
        void onOtherMessageClick(int position);
    }
    private ChatAdapterListener chatAdapterListener;
    public ChatAdapter(Context context, List<Message> messageList, ChatAdapterListener chatAdapterListener) {
        this.messageList = messageList;
        this.inflater = LayoutInflater.from(context);
        this.mAuth = FirebaseAuth.getInstance();
        this.chatAdapterListener = chatAdapterListener;
    }
    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.message_item_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message != null) {
            if (message.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                holder.text2.setText(message.getMessage());
                holder.text1.setText(message.getMessage());
                holder.text2.setVisibility(View.VISIBLE);
                holder.text2.setOnLongClickListener(v -> {
                    chatAdapterListener.onMyMessageClick(position);
                    return true;
                });
                holder.text1.setVisibility(View.GONE);
            } else {
                holder.text1.setText(message.getMessage());
                holder.text2.setText(message.getMessage());
                holder.text1.setOnLongClickListener(v -> {
                    chatAdapterListener.onOtherMessageClick(position);
                    return true;
                });
                holder.text1.setVisibility(View.VISIBLE);
                holder.text2.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView text1, text2;
        ViewHolder(View view){
            super(view);
            text1 = view.findViewById(R.id.textView13);
            text2 = view.findViewById(R.id.textView14);
        }
    }
}