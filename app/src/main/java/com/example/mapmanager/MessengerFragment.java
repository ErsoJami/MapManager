package com.example.mapmanager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.adapters.ChatListAdapter;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.models.ChatsData;
import com.example.mapmanager.models.Message;
import com.example.mapmanager.models.Route;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessengerFragment extends Fragment {

    public interface OnChatSelectChat {
        void onSelectChat(String id, String lastReadMessageId);
    }
    private OnChatSelectChat selectChatListner;
    private FirebaseAuth mAuth;
    private ArrayList<Chat> chats;
    private ChatListAdapter adapter;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private DatabaseReference chatListReference;
    private DatabaseReference chatsReference;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        selectChatListner = (OnChatSelectChat) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        chats = new ArrayList<>();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messenger, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChatListAdapter.OnChatClickListener chatClickListener = new ChatListAdapter.OnChatClickListener() {
            @Override
            public void onChatClick(Chat chat, int position) {
                if (selectChatListner != null) {
                    selectChatListner.onSelectChat(chat.getId(), chat.getLastReadMessageId());
                }
            }
        };
        adapter = new ChatListAdapter(requireContext(), chats, chatClickListener);
        FirebaseUser user = mAuth.getCurrentUser();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
        dataLoad();
    }
    public void dataLoad() {
        chats.clear();
        adapter.notifyDataSetChanged();
        if (MainActivity.user.getUserChatsData() != null) {
            for (HashMap.Entry<String, ChatsData> item : MainActivity.user.getUserChatsData().entrySet()) {
                String chatId = item.getKey();
                ChatsData chatsData = item.getValue();
                if (chatId != null && chatsData != null) {
                    chatsReference = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId);
                    chatsReference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Chat chat = dataSnapshot.getValue(Chat.class);
                                chat.setLastReadMessageId(chatsData.getLastMessageId());
                                chat.setId(chatId);
                                DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages");
                                Query lastMessageQuery = messagesRef.orderByKey().limitToLast(1);
                                lastMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                                String messageId = messageSnapshot.getKey();
                                                Message lastMessage = messageSnapshot.getValue(Message.class);
                                                chat.setLastMessage(lastMessage.getMessage());
                                                chats.add(chat);
                                                adapter.notifyDataSetChanged();
                                            }
                                        } else {
                                            chats.add(chat);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        chats.add(chat);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        selectChatListner = null;
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
