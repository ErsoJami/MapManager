package com.example.mapmanager.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.models.Chat;
import com.example.mapmanager.R;
import com.example.mapmanager.models.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
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
                holder.myTextView.setText(message.getMessage());
                holder.otherTextView.setText(message.getMessage());
                holder.otherUsernameTextView.setText(message.getMessage());
                holder.myTextView.setVisibility(View.VISIBLE);
                holder.myMessageContainer.setVisibility(View.VISIBLE);
                holder.myTextView.setOnLongClickListener(v -> {
                    chatAdapterListener.onMyMessageClick(position);
                    return true;
                });
                holder.otherTextView.setVisibility(View.GONE);
                holder.otherUsernameTextView.setVisibility(View.GONE);
                holder.otherMessageContainer.setVisibility(View.GONE);
            } else {
                holder.myTextView.setText(message.getMessage());
                holder.otherTextView.setText(message.getMessage());
                holder.otherUsernameTextView.setText(message.getNick());
                holder.otherTextView.setOnLongClickListener(v -> {
                    chatAdapterListener.onOtherMessageClick(position);
                    return true;
                });
                holder.otherMessageContainer.setVisibility(View.VISIBLE);
                holder.otherTextView.setVisibility(View.VISIBLE);
                holder.otherUsernameTextView.setVisibility(View.VISIBLE);
                holder.myMessageContainer.setVisibility(View.VISIBLE);
                holder.myTextView.setVisibility(View.GONE);
                holder.myMessageContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout otherMessageContainer, myMessageContainer;
        final TextView myTextView, otherTextView, otherUsernameTextView;
        final DatabaseReference databaseReference;
        ViewHolder(View view){
            super(view);
            otherMessageContainer = view.findViewById(R.id.otherMessageContainer);
            myMessageContainer = view.findViewById(R.id.myMessageContainer);
            myTextView = view.findViewById(R.id.myTextView);
            otherTextView = view.findViewById(R.id.otherTextView);
            otherUsernameTextView = view.findViewById(R.id.otherUsernameTextView);
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
    }
}