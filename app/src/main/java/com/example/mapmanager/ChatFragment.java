package com.example.mapmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.adapters.ChatAdapter;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.models.Message;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.Timestamp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private String chatId;
    private RecyclerView messageListView;
    private ChatAdapter adapter;
    private List<Message> messageList;
    private DatabaseReference databaseReference;
    private DatabaseReference messageListReference;
    private DatabaseReference messageReference;
    public static ChatFragment updateChat(String chatId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("chat_id", chatId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chatId = getArguments().getString("chat_id");
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        messageListView = view.findViewById(R.id.messageListView);
        messageList = new ArrayList<>();
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ChatAdapter(requireContext(), messageList);
        messageListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        messageListView.setAdapter(adapter);
        messageListView.setVisibility(View.VISIBLE);
        if (chatId != null) {
            messageListReference = databaseReference.child("chats").child(chatId).child("messages");
            messageListReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
                        String id = snapshot.getKey();
                        int typeMessage = snapshot.child("typeMessage").getValue(Integer.class);
                        String userId = snapshot.child("userId").getValue(String.class);
                        Long seconds = snapshot.child("time").child("seconds").getValue(Long.class);
                        Integer nanoseconds = snapshot.child("time").child("nanoseconds").getValue(Integer.class);
                        Timestamp time = new Timestamp(seconds, nanoseconds);
                        String message = snapshot.child("message").getValue(String.class);
                        messageList.add(new Message(typeMessage, userId, id, time, message));
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatId = null;
    }
}
