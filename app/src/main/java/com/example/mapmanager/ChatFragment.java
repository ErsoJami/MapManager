package com.example.mapmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.adapters.ChatListAdapter;
import com.example.mapmanager.models.Chat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private FirebaseAuth mAuth;
    private List<Chat> chats;
    private ChatListAdapter adapter;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private DatabaseReference chatListReference;
    private DatabaseReference chatsReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        chats = new ArrayList<>();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChatListAdapter.OnChatClickListener chatClickListener = new ChatListAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(Chat chat, int position) {
                Toast.makeText(requireContext(), chat.getId(), Toast.LENGTH_SHORT).show();
            }
        };
        adapter = new ChatListAdapter(requireContext(), chats, chatClickListener);
        FirebaseUser user = mAuth.getCurrentUser();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
        if (user != null) {
            chatListReference = databaseReference.child("users").child(user.getUid()).child("chatsList");
            chatListReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        chats.clear();
                        for (DataSnapshot chatId : snapshot.getChildren()) {
                            String id = chatId.getValue(String.class);
                            chatsReference = FirebaseDatabase.getInstance().getReference().child("chats").child(id);
                            chatsReference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        List<String> usersChatList = dataSnapshot.child("membersList").getValue(new GenericTypeIndicator<List<String>>() {});
                                        String chatName = dataSnapshot.child("groupName").getValue(String.class);
                                        String chatAvatarUrl = dataSnapshot.child("groupAvatarUrl").getValue(String.class);
                                        chats.add(new Chat(id, Timestamp.now(), usersChatList, chatName, chatAvatarUrl));
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
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
        mAuth = null;
        chats = null;
        adapter = null;
        databaseReference = null;
        recyclerView = null;
        chatListReference = null;
        chatsReference = null;
    }
}
