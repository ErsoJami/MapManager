package com.example.mapmanager;

import static android.app.Activity.RESULT_OK;

import static com.example.mapmanager.MapFragment.DpToPx;
import static com.example.mapmanager.MapFragment.PxToDp;
import static com.example.mapmanager.MapFragment.getScreenWidth;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapmanager.adapters.ChatAdapter;
import com.example.mapmanager.adapters.MessageMediaAdapter;
import com.example.mapmanager.adapters.UserAdapter;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.models.ChatsData;
import com.example.mapmanager.models.LoadMedia;
import com.example.mapmanager.models.Message;

import com.example.mapmanager.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatFragment extends Fragment implements ChatAdapter.ChatAdapterListener, MessageMediaAdapter.OnMediaListener, UserAdapter.OnDeleteUserListener {

    private String chatId;
    private String groupAvatarUrl;
    private RecyclerView messageListView;
    private ChatAdapter adapter;
    private ArrayList<Message> messageList;
    private DatabaseReference databaseReference;
    private DatabaseReference messageListReference;
    private EditText sendTextEdit;
    private TextView chatNameText;
    private TextView saveData;
    private ImageView leaveChatButton;
    private ImageView sendView;
    private ImageView settingsChatButton;
    private FirebaseAuth mAuth;
    private InputMethodManager imm;
    private String lastReadMessageId;
    private DatabaseReference messageReference;
    private String lastMessageId;
    private ImageView mediaButton;
    private ImageView leaveChatSettingsButton;
    private ArrayList<LoadMedia> mediaList;
    private ActivityResultLauncher<String[]> getMediaPermissions;
    private ActivityResultLauncher<String[]> mediaPicker;
    private MessageMediaAdapter mediaLoaderAdapter;
    private RecyclerView mediaLoaderView;
    private StorageReference storageReference;
    private ConstraintLayout settingsLayout;
    private ShapeableImageView shapeableImageView;
    private RecyclerView peopleRecyclerView;
    private UserAdapter userAdapter;
    private ArrayList<User> userArrayList;
    private String ownerId;
    private ActivityResultLauncher<String[]> mediaSinglePicker;
    private ActivityResultLauncher<String[]> getSingleMediaPermissions;
    private boolean bim = true;
    private Uri uri;
    private View.OnClickListener saveDataListener;
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
        mediaList = new ArrayList<>();
        storageReference = FirebaseStorage.getInstance().getReference().child(chatId);
        mediaPicker = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), new ActivityResultCallback<List<Uri>>() {
            @Override
            public void onActivityResult(List<Uri> uris) {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        mediaList.add(new LoadMedia(uri, 0));
                    }
                    mediaLoaderAdapter.notifyDataSetChanged();
                    mediaLoaderView.setVisibility(View.VISIBLE);
                } else if (mediaList.isEmpty()) {
                    mediaLoaderView.setVisibility(View.GONE);
                }
            }
        });
        mediaSinglePicker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri o) {
                uri = o;
                Glide.with(requireContext())
                        .load(uri)
                        .placeholder(R.drawable.user_icon)
                        .into(shapeableImageView);
            }
        });
        getMediaPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> o) {
                mediaPicker.launch(new String[]{"image/*", "video/*"});
            }
        });
        getSingleMediaPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> o) {
                mediaSinglePicker.launch(new String[]{"image/*"});
            }
        });
        userArrayList = new ArrayList<>();
        saveDataListener = v -> {
            saveData.setOnClickListener(null);
            if (uri != null) {
                StorageReference fileReference = FirebaseStorage.getInstance().getReference().child("chatAvatars").child(chatId);
                UploadTask uploadTask = fileReference.putFile(uri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, com.google.android.gms.tasks.Task<Uri>>() {
                    @Override
                    public com.google.android.gms.tasks.Task<Uri> then(@NonNull com.google.android.gms.tasks.Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                } ).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (downloadUri != null) {
                                uri = null;
                                saveData.setOnClickListener(saveDataListener);
                                FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("groupAvatarUrl").setValue(downloadUri.toString());
                                Glide.with(requireContext())
                                        .load(downloadUri)
                                        .placeholder(R.drawable.account_icon)
                                        .into(shapeableImageView);
                                Toast.makeText(getContext(), getResources().getString(R.string.successful_save), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), getResources().getString(R.string.error_file_load), Toast.LENGTH_SHORT).show();
                            saveData.setOnClickListener(saveDataListener);
                        }
                    }
                });
            } else {
                MainActivity.user.changeData(databaseReference);
                Toast.makeText(getContext(), getResources().getString(R.string.successful_save), Toast.LENGTH_SHORT).show();
            }
        };
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
        FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("ownerId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ownerId = snapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        messageListView = view.findViewById(R.id.messageListView);
        leaveChatButton = view.findViewById(R.id.leaveChatButton);
        sendTextEdit = view.findViewById(R.id.sendTextEdit);
        sendView = view.findViewById(R.id.sendButton);
        mediaButton = view.findViewById(R.id.mediaButton);
        saveData = view.findViewById(R.id.saveData);
        leaveChatSettingsButton = view.findViewById(R.id.leaveChatSettingsButton);
        settingsChatButton = view.findViewById(R.id.settingsChatButton);
        settingsLayout = view.findViewById(R.id.settingsLayout);
        shapeableImageView = view.findViewById(R.id.shapeableImageView);

        peopleRecyclerView = view.findViewById(R.id.peopleRecyclerView);
        userAdapter = new UserAdapter(requireContext(), userArrayList, false, this);
        peopleRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        peopleRecyclerView.setAdapter(userAdapter);
        FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("membersList")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userArrayList.clear();
                        if (snapshot.exists()) {
                            GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                            ArrayList<String> memberIds = snapshot.getValue(t);

                            if (memberIds != null && !memberIds.isEmpty()) {
                                for (String currentUserId : memberIds) {
                                    FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                    if (userSnapshot.exists()) {
                                                        User user = userSnapshot.getValue(User.class);
                                                        if (user != null) {
                                                            user.setUserId(currentUserId);
                                                            userArrayList.add(user);
                                                            userAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                }
                                            });
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("groupAvatarUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    groupAvatarUrl = snapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mAuth = FirebaseAuth.getInstance();
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(requireContext(), messageList, this);
        messageListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        messageListView.setAdapter(adapter);
        messageListView.setVisibility(View.VISIBLE);

        mediaLoaderView = view.findViewById(R.id.mediaPreviewView);
        mediaLoaderAdapter = new MessageMediaAdapter(requireContext(), mediaList, this, 1, true);
        mediaLoaderView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mediaLoaderView.setAdapter(mediaLoaderAdapter);
        if (mediaList == null || mediaList.isEmpty()) {
            mediaLoaderView.setVisibility(View.GONE);
        }
        String key1;
        if (lastReadMessageId != null) {
            key1 = new String(lastReadMessageId);
        } else {
            key1 = "";
        }
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
                    HashMap<String, ChatsData> map = MainActivity.user.getUserChatsData();
                    map.replace(chatId, new ChatsData(map.get(chatId).getMessageList(), lastMessageId));
                    MainActivity.user.setUserChatsData(map);
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
                        if (previousChildName == null || !previousChildName.equals(snapshot.getKey())) {
                            Message message1 = snapshot.getValue(Message.class);
                            message1.setMessageId(snapshot.getKey());
                            int position = Collections.binarySearch(messageList, message1, new Comparator<Message>() {
                                @Override
                                public int compare(Message message, Message t1) {
                                    return message.getMessageId().compareTo(t1.getMessageId());
                                }
                            });
                            if (position >= 0 && position < messageList.size()) {
                                messageList.set(position, message1);
                            } else {
                                messageList.add(message1);
                            }
                            adapter.notifyItemInserted(adapter.getItemCount() - 1);
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
                            } else if (!isFocus[0] && key[0].equals(message1.getMessageId())) {
                                focusOnMessage(messageList.size() - 1);
                                isFocus[0] = true;
                            }
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
                    if (position >= 0 && position < messageList.size()) {
                        messageList.set(position, message);
                    }
                    adapter.notifyItemChanged(position);
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
                        if (position >= 0 && position < messageList.size()) {
                            messageList.remove(position);
                        }
                        adapter.notifyItemRemoved(position);
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
            ArrayList<Uri> mediaUris = new ArrayList<>();
            for (LoadMedia loadMedia : mediaList) {
                mediaUris.add(loadMedia.getUri());
            }
            if (!text.isEmpty() || !mediaUris.isEmpty()) {
                sendTextEdit.setText("");
                sendTextEdit.clearFocus();
                imm.hideSoftInputFromWindow(sendTextEdit.getWindowToken(), 0);
                if (!mediaUris.isEmpty()) {
                    uploadMediaAndSendMessage(mediaUris, text);
                } else {
                    mediaList.clear();
                    mediaLoaderView.setVisibility(View.GONE);
                    mediaLoaderAdapter.notifyDataSetChanged();
                    Message message = new Message(0, mAuth.getUid(), Timestamp.now().getSeconds(), text, new ArrayList<>());
                    message.createNewMessage(chatId);
                }
            }
        });
        leaveChatButton.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        mediaButton.setOnClickListener(v -> {
            mediaLoaderView.setVisibility(View.VISIBLE);
            openMediaPicker();
        });
        settingsChatButton.setOnClickListener(v -> {
            if (ownerId.equals(mAuth.getUid())) {
                userAdapter.setType(true);
            } else {
                userAdapter.setType(false);
            }
            settingsLayout.setVisibility(View.VISIBLE);
            Glide.with(requireContext())
                    .load(groupAvatarUrl)
                    .placeholder(R.drawable.account_icon)
                    .into(shapeableImageView);

        });
        leaveChatSettingsButton.setOnClickListener(v -> {
            settingsLayout.setVisibility(View.GONE);
        });
        saveData.setOnClickListener(saveDataListener);
        shapeableImageView.setOnClickListener(v -> {
            openSingleMediaPicker();
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
                Message message = messageList.get(position);
                if (message.getUserId().equals(mAuth.getUid())) {
                    TextView textView = itemView.findViewById(R.id.myTextView);
                    textView.setText(message.getMessage());
                } else {
                    TextView textView = itemView.findViewById(R.id.otherTextView);
                    TextView usernameTextView = itemView.findViewById(R.id.otherUsernameTextView);
                    textView.setText(message.getMessage());
                    usernameTextView.setText(message.getNick());
                }
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
        Message message = messageList.get(position);
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
                    ArrayList<Uri> mediaUris = new ArrayList<>();
                    for (LoadMedia loadMedia : mediaList) {
                        mediaUris.add(loadMedia.getUri());
                    }
                    if (!text2.isEmpty() || !mediaUris.isEmpty()) {
                        sendTextEdit.setText("");
                        sendTextEdit.clearFocus();
                        imm.hideSoftInputFromWindow(sendTextEdit.getWindowToken(), 0);
                        if (!mediaUris.isEmpty()) {
                            uploadMediaAndSendMessage(mediaUris, text2);
                        } else {
                            mediaList.clear();
                            mediaLoaderView.setVisibility(View.GONE);
                            mediaLoaderAdapter.notifyDataSetChanged();
                            Message message2 = new Message(0, mAuth.getUid(), Timestamp.now().getSeconds(), text2, new ArrayList<>());
                            message2.createNewMessage(chatId);
                        }
                    }
                });
            });
        });
        deleteMessageText.setOnClickListener(v -> {
            message.deleteMessage(chatId);
            HashMap<String, ChatsData> map = MainActivity.user.getUserChatsData();
            ChatsData chatsData = map.get(chatId);
            ArrayList<String> messageList = chatsData.getMessageList();
            messageList.remove(message.getMessageId());
            chatsData.setMessageList(messageList);
            map.replace(chatId, chatsData);
            MainActivity.user.setUserChatsData(map);
            MainActivity.user.changeData(FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()));
            popupWindow.dismiss();
        });
        copyMessageText.setOnClickListener(v -> {
            copyMessage(message.getMessage());
            popupWindow.dismiss();
        });
        popupWindow.setOutsideTouchable(true);
        int xoff = (int) (getScreenWidth(requireContext()) - DpToPx(273, requireContext()) - DpToPx(22, requireContext()));
        popupWindow.showAsDropDown(view, xoff, -(int) DpToPx(15, requireContext()), Gravity.START);
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
        view2 = itemView.findViewById(R.id.divider1);
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
        popupWindow.showAsDropDown(view, (int) DpToPx(22, requireContext()), -(int) DpToPx(15, requireContext()), Gravity.START);
    }
    private void copyMessage(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getResources().getString(R.string.copy_message), text);
        clipboard.setPrimaryClip(clip);
    }
    private void openMediaPicker() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            mediaPicker.launch(new String[]{"image/*", "video/*"});
        } else {
            getMediaPermissions.launch(permissionsToRequest.toArray(new String[0]));
        }
    }
    private void openSingleMediaPicker() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            mediaSinglePicker.launch(new String[]{"image/*"});
        } else {
            getSingleMediaPermissions.launch(permissionsToRequest.toArray(new String[0]));
        }
    }

    @Override
    public void onDeleteMedia(int position) {
        mediaList.remove(position);
        mediaLoaderAdapter.notifyDataSetChanged();
        if (mediaList.isEmpty()) {
            mediaLoaderView.setVisibility(View.GONE);
        }
    }
    private void uploadMediaAndSendMessage(ArrayList<Uri> localUris, String messageText) {
        sendView.setOnClickListener(null);
        ArrayList<Task<Uri>> uploadTasks = new ArrayList<>();
        ArrayList<String> uploadedMediaUrls = new ArrayList<>();
        mediaLoaderAdapter.setModeType(2);
        for (int i = 0; i < localUris.size(); i++) {
            Uri uri = localUris.get(i);
            if (uri == null) continue;
            final String fileName = UUID.randomUUID().toString() + "." + getFileExtension(uri);
            StorageReference fileReference = storageReference.child(fileName);

            UploadTask uploadTask = fileReference.putFile(uri);

            int finalI = i;
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    mediaList.set(finalI, new LoadMedia(uri, progress));
                    mediaLoaderAdapter.notifyItemChanged(finalI, null);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            } ).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            uploadedMediaUrls.add(downloadUri.toString());
                        }
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.error_file_load), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            uploadTasks.add(urlTask);
        }
        Tasks.whenAllSuccess(uploadTasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {
                sendView.setOnClickListener(v -> {
                    String text = sendTextEdit.getText().toString();
                    ArrayList<Uri> mediaUris = new ArrayList<>();
                    for (LoadMedia loadMedia : mediaList) {
                        mediaUris.add(loadMedia.getUri());
                    }
                    if (!text.isEmpty() || !mediaUris.isEmpty()) {
                        sendTextEdit.setText("");
                        sendTextEdit.clearFocus();
                        imm.hideSoftInputFromWindow(sendTextEdit.getWindowToken(), 0);
                        if (!mediaUris.isEmpty()) {
                            uploadMediaAndSendMessage(mediaUris, text);
                        } else {
                            mediaList.clear();
                            mediaLoaderView.setVisibility(View.GONE);
                            mediaLoaderAdapter.notifyDataSetChanged();
                            Message message = new Message(0, mAuth.getUid(), Timestamp.now().getSeconds(), text, new ArrayList<>());
                            message.createNewMessage(chatId);
                        }
                    }
                });
                mediaList.clear();
                mediaLoaderAdapter.notifyDataSetChanged();
                mediaLoaderView.setVisibility(View.GONE);
                Message message = new Message(0, mAuth.getUid(), Timestamp.now().getSeconds(), messageText, uploadedMediaUrls);
                message.createNewMessage(chatId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sendView.setOnClickListener(v -> {
                    String text = sendTextEdit.getText().toString();
                    ArrayList<Uri> mediaUris = new ArrayList<>();
                    for (LoadMedia loadMedia : mediaList) {
                        mediaUris.add(loadMedia.getUri());
                    }
                    if (!text.isEmpty() || !mediaUris.isEmpty()) {
                        sendTextEdit.setText("");
                        sendTextEdit.clearFocus();
                        imm.hideSoftInputFromWindow(sendTextEdit.getWindowToken(), 0);
//                mediaList.clear();
//                adapter.notifyDataSetChanged();
//                mediaLoaderView.setVisibility(View.GONE);
                        if (!mediaUris.isEmpty()) {
                            uploadMediaAndSendMessage(mediaUris, text);
                        } else {
                            mediaList.clear();
                            mediaLoaderView.setVisibility(View.GONE);
                            mediaLoaderAdapter.notifyDataSetChanged();
                            Message message = new Message(0, mAuth.getUid(), Timestamp.now().getSeconds(), text, new ArrayList<>());
                            message.createNewMessage(chatId);
                        }
                    }
                });
                mediaList.clear();
                mediaLoaderView.setVisibility(View.GONE);
                mediaLoaderAdapter.notifyDataSetChanged();
                mediaLoaderAdapter.setModeType(1);
                Toast.makeText(getContext(), getResources().getString(R.string.media_error) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        if (uri == null) return null;
        ContentResolver cR = requireContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = mime.getExtensionFromMimeType(cR.getType(uri));
        if (extension == null) {
            String path = uri.getPath();
            if (path != null) {
                int lastDot = path.lastIndexOf(".");
                if (lastDot != -1 && lastDot < path.length() - 1) {
                    extension = path.substring(lastDot + 1);
                }
            }
        }
        return extension != null ? extension : "tmp";
    }

    @Override
    public void onDeleteUser(int position) {
        User user = userArrayList.get(position);
        FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("membersList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                    ArrayList<String> userId = snapshot.getValue(t);
                    userId.remove(user.getUserId());
                    FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("membersList").setValue(userId);
                    FirebaseDatabase.getInstance().getReference().child("users").child(user.getUserId()).child("chatList").child(chatId).removeValue();
                    userArrayList.remove(position);
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void dataLoad() {
        if (MainActivity.user.getUserChatsData() != null) {
            if (!MainActivity.user.getUserChatsData().containsKey(chatId)) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        }
    }
}
