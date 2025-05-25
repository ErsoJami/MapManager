package com.example.mapmanager.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.R;
import com.example.mapmanager.models.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{


    private final LayoutInflater inflater;
    private final List<Message> messageList;
//    private RecyclerView recyclerViewMessageMedia;
//    private MessageMediaAdapter mediaAdapter;
    private FirebaseAuth mAuth;

    public interface ChatAdapterListener {
        void onMyMessageClick(int position);
        void onOtherMessageClick(int position);
    }
    public interface OnSelectMedia {
        void onSelectMedia(Object url);
        void onSelectViewAllMedia(ArrayList<String> uri);
    }
    private OnSelectMedia onSelectMedia;
    private ChatAdapterListener chatAdapterListener;
    public ChatAdapter(Context context, List<Message> messageList, ChatAdapterListener chatAdapterListener) {
        this.messageList = messageList;
        this.inflater = LayoutInflater.from(context);
        this.mAuth = FirebaseAuth.getInstance();
        this.chatAdapterListener = chatAdapterListener;
        onSelectMedia = (OnSelectMedia) context;
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
        holder.gridLayout.removeAllViews();
        if (message != null) {
            ArrayList<String> mediaUris = message.getMediaList();
            holder.gridLayout.removeAllViews();
            if (mediaUris != null && !mediaUris.isEmpty()) {
                holder.gridLayout.setVisibility(VISIBLE);
                ViewGroup.LayoutParams params1 = holder.gridLayout.getLayoutParams();
                WindowManager windowManager = (WindowManager) holder.itemView.getContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
                params1.width = (int) (displayMetrics.widthPixels * 0.7);
                params1.height = params1.width;
                holder.gridLayout.setLayoutParams(params1);
                if (mediaUris.size() == 2) {
                    holder.gridLayout.setColumnCount(1);
                    holder.gridLayout.setRowCount(2);
                    for (int i = 0; i < 2; i++) {
                        ShapeableImageView imageView = createImageView(holder.itemView.getContext());
                        String url = mediaUris.get(i);
                        Glide.with(holder.itemView.getContext())
                                .load(url)
                                .into(imageView);
                        holder.gridLayout.addView(imageView);
                        imageView.setOnClickListener(v -> {
                            if (onSelectMedia != null) {
                                onSelectMedia.onSelectMedia(url);
                            }
                        });
                    }
                } else if (mediaUris.size() == 3) {
                    holder.gridLayout.setColumnCount(2);
                    holder.gridLayout.setRowCount(2);
                    for (int i = 0; i < 3; i++) {
                        ShapeableImageView imageView = createImageView(holder.itemView.getContext());
                        String url = mediaUris.get(i);
                        if (i == 2) {
                            GridLayout.LayoutParams params = (GridLayout.LayoutParams) imageView.getLayoutParams();
                            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 2, 1f);
                            imageView.setLayoutParams(params);
                        }
                        Glide.with(holder.itemView.getContext())
                                .load(url)
                                .into(imageView);
                        holder.gridLayout.addView(imageView);
                        imageView.setOnClickListener(v -> {
                            if (onSelectMedia != null) {
                                onSelectMedia.onSelectMedia(url);
                            }
                        });
                    }
                } else if (mediaUris.size() <= 4) {
                    holder.gridLayout.setColumnCount(mediaUris.size() == 1 ? 1 : 2);
                    holder.gridLayout.setRowCount(mediaUris.size() == 1 || mediaUris.size() == 2 ? 1 : 2);
                    for (String url : mediaUris) {
                        ShapeableImageView imageView = createImageView(holder.itemView.getContext());
                        Glide.with(holder.itemView.getContext())
                                .load(url)
                                .into(imageView);
                        imageView.setOnClickListener(v -> {
                            if (onSelectMedia != null) {
                                onSelectMedia.onSelectMedia(url);
                            }
                        });
                        holder.gridLayout.addView(imageView);
                    }
                } else {
                    holder.gridLayout.setColumnCount(2);
                    holder.gridLayout.setRowCount(2);
                    for (int i = 0; i < 3; i++) {
                        ShapeableImageView imageView = createImageView(holder.itemView.getContext());
                        String url = mediaUris.get(i);
                        Glide.with(holder.itemView.getContext())
                                .load(url)
                                .into(imageView);
                        holder.gridLayout.addView(imageView);
                        imageView.setOnClickListener(v -> {
                            if (onSelectMedia != null) {
                                onSelectMedia.onSelectMedia(url);
                            }
                        });
                    }
                    String url = mediaUris.get(3);
                    FrameLayout imageView = createImageViewWithText(holder.itemView.getContext(), url, "+ " + (mediaUris.size() - 4));
                    imageView.setOnClickListener(v -> {
                        if (onSelectMedia != null) {
                            onSelectMedia.onSelectViewAllMedia(mediaUris);
                        }
                    });
                    holder.gridLayout.addView(imageView);
                }
            } else {
                holder.gridLayout.setVisibility(GONE);
            }
            if (message.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                holder.myTextView.setText(message.getMessage());
                holder.otherTextView.setText(message.getMessage());
                holder.otherUsernameTextView.setText(message.getMessage());
                if (!message.getMessage().isEmpty()) {
                    holder.myTextView.setVisibility(VISIBLE);
                } else {
                    holder.myTextView.setVisibility(GONE);
                }
                holder.myMessageContainer.setVisibility(VISIBLE);
                holder.myMessageContainer.setOnLongClickListener(v -> {
                    chatAdapterListener.onMyMessageClick(position);
                    return true;
                });
                holder.otherTextView.setVisibility(GONE);
                holder.otherUsernameTextView.setVisibility(GONE);
                holder.otherMessageContainer.setVisibility(GONE);
            } else {
                holder.myTextView.setText(message.getMessage());
                holder.otherTextView.setText(message.getMessage());
                holder.otherUsernameTextView.setText(message.getNick());
                holder.otherMessageContainer.setOnLongClickListener(v -> {
                    chatAdapterListener.onOtherMessageClick(position);
                    return true;
                });
                holder.otherMessageContainer.setVisibility(VISIBLE);
                if (!message.getMessage().isEmpty()) {
                    holder.otherTextView.setVisibility(VISIBLE);
                } else {
                    holder.otherTextView.setVisibility(GONE);
                }
                holder.otherUsernameTextView.setVisibility(VISIBLE);
                holder.myMessageContainer.setVisibility(VISIBLE);
                holder.myTextView.setVisibility(GONE);
                holder.myMessageContainer.setVisibility(GONE);
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
//        final RecyclerView recyclerViewMessageMedia;
//        MessageMediaAdapter mediaAdapter;
        final GridLayout gridLayout;
        ViewHolder(View view){
            super(view);
            otherMessageContainer = view.findViewById(R.id.otherMessageContainer);
            myMessageContainer = view.findViewById(R.id.myMessageContainer);
            myTextView = view.findViewById(R.id.myTextView);
            otherTextView = view.findViewById(R.id.otherTextView);
            otherUsernameTextView = view.findViewById(R.id.otherUsernameTextView);
//            recyclerViewMessageMedia = view.findViewById(R.id.otherMediaView);
//            recyclerViewMessageMedia.setVisibility(VISIBLE);
            gridLayout = view.findViewById(R.id.myMediaGridLayout);
            ViewGroup.LayoutParams params = gridLayout.getLayoutParams();
            WindowManager windowManager = (WindowManager) itemView.getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            params.width = (int) (displayMetrics.widthPixels * 0.7);
            params.height = params.width;
            gridLayout.setLayoutParams(params);
//            recyclerViewMessageMedia.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
    }
    private ShapeableImageView createImageView(Context context) {
        ShapeableImageView shapeableImageView = new ShapeableImageView(context);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        int marginInDp = 2;
        int marginInPx = (int) (marginInDp * context.getResources().getDisplayMetrics().density);
        params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
        shapeableImageView.setLayoutParams(params);
        shapeableImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        float cornerRadiusDp = 12f;
        float cornerRadiusPx = cornerRadiusDp * context.getResources().getDisplayMetrics().density;
        shapeableImageView.setShapeAppearanceModel(
                ShapeAppearanceModel.builder()
                        .setAllCornerSizes(cornerRadiusPx)
                        .build()
        );
        return shapeableImageView;
    }
    private FrameLayout createImageViewWithText(Context context, String imageUri, String text) {
        FrameLayout frameLayout = new FrameLayout(context);
        GridLayout.LayoutParams frameLayoutParams = new GridLayout.LayoutParams();
        frameLayoutParams.width = 0;
        frameLayoutParams.height = 0;
        frameLayoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        frameLayoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        int marginInDp = 2;
        int marginInPx = (int) (marginInDp * context.getResources().getDisplayMetrics().density);
        frameLayoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
        frameLayout.setLayoutParams(frameLayoutParams);
        ShapeableImageView shapeableImageView = new ShapeableImageView(context);
        FrameLayout.LayoutParams imageLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        float cornerRadiusDp = 12f;
        float cornerRadiusPx = cornerRadiusDp * context.getResources().getDisplayMetrics().density;
        shapeableImageView.setLayoutParams(imageLayoutParams);
        shapeableImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        shapeableImageView.setShapeAppearanceModel(
                ShapeAppearanceModel.builder()
                        .setAllCornerSizes(cornerRadiusPx)
                        .build()
        );
        TextView textView = new TextView(context);
        FrameLayout.LayoutParams textLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        textLayoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(textLayoutParams);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(24);
        textView.setShadowLayer(5, 2, 2, Color.BLACK);
        frameLayout.addView(shapeableImageView);
        frameLayout.addView(textView);
        Glide.with(context)
                .load(imageUri)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(10, 2)))
                .into(shapeableImageView);
        return frameLayout;
    }
}