package com.example.mapmanager;

import static com.google.android.material.internal.ViewUtils.dpToPx;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

public class ChatFragment extends Fragment implements ChatAdapter.ChatAdapterListener {

    private String chatId;
    private RecyclerView messageListView;
    private ChatAdapter adapter;
    private ArrayList<Message> messageList;
    private DatabaseReference databaseReference;
    private DatabaseReference messageListReference;
    private EditText sendTextEdit;
    private TextView chatNameText;
    private ImageView sendView;
    private FirebaseAuth mAuth;
    private InputMethodManager imm;
    private String lastReadMessageId;
    private DatabaseReference messageReference;
    private String lastMessageId;
    static ChatFragment updateChat(String chatId, String lastReadMessageId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("chat_id", chatId);
        args.putString("last_read_message_id", lastReadMessageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chatId = getArguments().getString("chat_id");
            lastReadMessageId = getArguments().getString("last_read_message_id");
        }
        imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        messageReference = databaseReference.child("chats").child(chatId).child("messages");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatNameText = view.findViewById(R.id.textView4);
        FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("groupName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    chatNameText.setText(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        messageListView = view.findViewById(R.id.messageListView);
        sendTextEdit = view.findViewById(R.id.sendTextEdit);
        sendView = view.findViewById(R.id.sendButton);
        mAuth = FirebaseAuth.getInstance();
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(requireContext(), messageList, this);
        messageListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        messageListView.setAdapter(adapter);
        messageListView.setVisibility(View.VISIBLE);

        String key1 = new String(lastReadMessageId);
        final String[] key = {new String()};
        final Boolean[] isFocus = {new Boolean(false)};
        Query item = messageReference.orderByKey().limitToLast(1);
        item.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot lastItemSnap = snapshot.getChildren().iterator().next();
                    key[0] = lastItemSnap.getKey();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        messageListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (messageList.size() != 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) messageListView.getLayoutManager();
                    int position = layoutManager.findLastVisibleItemPosition();
                    lastMessageId = messageList.get(position).getMessageId();
                    HashMap<String, String> map = MainActivity.user.getChatList();
                    map.replace(chatId, lastMessageId);
                    MainActivity.user.setChatList(map);
                    MainActivity.user.changeData(FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()));
                }

            }
        });
        if (chatId != null) {
            messageListReference = databaseReference.child("chats").child(chatId).child("messages");
            messageListReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
                        Message message1 = snapshot.getValue(Message.class);
                        message1.setMessageId(snapshot.getKey());
                        messageList.add(message1);
                        adapter.notifyDataSetChanged();
                        if (!isFocus[0] && message1.getMessageId().compareTo(key1) > 0) {
                            if (messageList.size() > 1) {
                                focusOnMessage(messageList.size() - 2);
                            } else {
                                focusOnMessage(messageList.size() - 1);
                            }
                            isFocus[0] = true;
                        } else if (!isFocus[0] && message1.getMessageId().compareTo(key1) == 0) {
                            focusOnMessage(messageList.size() - 1);
                            isFocus[0] = true;
                        } else if (!isFocus[0] && key[0] == message1.getMessageId()) {
                            focusOnMessage(messageList.size() - 1);
                            isFocus[0] = true;
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Message message = snapshot.getValue(Message.class);
                    message.setMessageId(snapshot.getKey());
                    int position = Collections.binarySearch(messageList, message, new Comparator<Message>() {
                        @Override
                        public int compare(Message message, Message t1) {
                            return message.getMessageId().compareTo(t1.getMessageId());
                        }
                    });
                    if (position != -1) {
                        messageList.set(position, message);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Message message = snapshot.getValue(Message.class);
                        message.setMessageId(snapshot.getKey());
                        int position = Collections.binarySearch(messageList, message, new Comparator<Message>() {
                            @Override
                            public int compare(Message message, Message t1) {
                                return message.getMessageId().compareTo(t1.getMessageId());
                            }
                        });
                        if (position != -1) {
                            messageList.remove(position);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendView.setOnClickListener(v -> {
            String text = sendTextEdit.getText().toString();
            if (!text.isEmpty()) {
                Message message = new Message(0, mAuth.getUid(), Timestamp.now().getSeconds(), text);
                message.createNewMessage(chatId);
                sendTextEdit.setText("");
                sendTextEdit.clearFocus();
                imm.hideSoftInputFromWindow(sendTextEdit.getWindowToken(), 0);
                focusOnMessage(adapter.getItemCount() - 1);
            }
        });

    }
    private void focusOnMessage(int position) {
        if (position < 0 || position >= messageList.size()) {
            return;
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) messageListView.getLayoutManager();
        messageListView.post(new Runnable() {
            @Override
            public void run() {
                int viewHeight = messageListView.getHeight();
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                View itemView = inflater.inflate(R.layout.message_item_layout, messageListView, false);
                TextView textView = itemView.findViewById(R.id.textView14);
                textView.setText(messageList.get(position).getMessage());
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int parentWidth = messageListView.getWidth();
                int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentWidth - messageListView.getPaddingLeft() - messageListView.getPaddingRight(), View.MeasureSpec.EXACTLY);
                itemView.measure(widthMeasureSpec, heightMeasureSpec);
                int measuredItemHeight = itemView.getMeasuredHeight();
                try {
                    layoutManager.scrollToPositionWithOffset(position, viewHeight - measuredItemHeight);
                } catch (Exception e) {

                }
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        chatId = null;
    }

    @Override
    public void onMyMessageClick(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) messageListView.getLayoutManager();
        View view = layoutManager.findViewByPosition(position);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View itemView = inflater.inflate(R.layout.popup_menu_message, messageListView, false);
        TextView deleteMessageText, copyMessageText, changeMessageText;
        deleteMessageText = itemView.findViewById(R.id.deleteMessageText);
        copyMessageText = itemView.findViewById(R.id.copyMessageText);
        changeMessageText = itemView.findViewById(R.id.changeMessageText);
        int weight = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        PopupWindow popupWindow = new PopupWindow(itemView, weight, height, false);
        changeMessageText.setOnClickListener(v -> {
            Message message = messageList.get(position);
            String text = sendTextEdit.getText().toString();
            sendTextEdit.setText(message.getMessage());
            sendView.setImageResource(R.drawable.check);
            popupWindow.dismiss();
            sendView.setOnClickListener(v1 -> {
                String text1 = sendTextEdit.getText().toString();
                message.setMessage(text1);
                sendView.setImageResource(R.drawable.send_icon);
                sendTextEdit.setText(text);
                message.updateMessage(chatId);
                if (text.isEmpty()) {
                    sendTextEdit.clearFocus();
                    imm.hideSoftInputFromWindow(sendTextEdit.getWindowToken(), 0);
                    focusOnMessage(position);
                }
                sendView.setOnClickListener(v2 -> {
                    String text2 = sendTextEdit.getText().toString();
                    if (!text2.isEmpty()) {
                        Message message1 = new Message(0, mAuth.getUid(), Timestamp.now().getSeconds(), text2);
                        message1.createNewMessage(chatId);
                        sendTextEdit.setText("");
                        sendTextEdit.clearFocus();
                        imm.hideSoftInputFromWindow(sendTextEdit.getWindowToken(), 0);
                        focusOnMessage(adapter.getItemCount() - 1);
                    }
                });
            });
        });
        deleteMessageText.setOnClickListener(v -> {
            Message message = messageList.get(position);
            message.deleteMessage(chatId);
            popupWindow.dismiss();
        });
        copyMessageText.setOnClickListener(v -> {
            Message message = messageList.get(position);
            copyMessage(message.getMessage());
            popupWindow.dismiss();
        });
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(view, 300, 0, Gravity.START);
    }

    @Override
    public void onOtherMessageClick(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) messageListView.getLayoutManager();
        View view = layoutManager.findViewByPosition(position);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View itemView = inflater.inflate(R.layout.popup_menu_message, messageListView, false);
        TextView deleteMessageText, copyMessageText, changeMessageText;
        deleteMessageText = itemView.findViewById(R.id.deleteMessageText);
        copyMessageText = itemView.findViewById(R.id.copyMessageText);
        deleteMessageText.setVisibility(View.GONE);
        deleteMessageText.setVisibility(View.GONE);
        View view1, view2;
        view1 = itemView.findViewById(R.id.view2);
        view2 = itemView.findViewById(R.id.view5);
        view1.setVisibility(View.GONE);
        view2.setVisibility(View.GONE);
        ImageView imageView = itemView.findViewById(R.id.view3), imageView1 = itemView.findViewById(R.id.view17);
        imageView.setVisibility(View.GONE);
        imageView1.setVisibility(View.GONE);
        changeMessageText = itemView.findViewById(R.id.changeMessageText);
        changeMessageText.setVisibility(View.GONE);
        int weight = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        PopupWindow popupWindow = new PopupWindow(itemView, weight, height, false);
        copyMessageText.setOnClickListener(v -> {
            Message message = messageList.get(position);
            copyMessage(message.getMessage());
            popupWindow.dismiss();
        });
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(view, 0, 0, Gravity.START);
    }
    private void copyMessage(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Скопированное сообщение", text);
        clipboard.setPrimaryClip(clip);
    }
}
