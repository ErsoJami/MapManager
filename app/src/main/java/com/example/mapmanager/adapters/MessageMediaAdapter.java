package com.example.mapmanager.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapmanager.MapFragment;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.R;
import com.example.mapmanager.models.LoadMedia;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class MessageMediaAdapter extends RecyclerView.Adapter<MessageMediaAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final ArrayList<Uri> mediaList;
    private final ArrayList<LoadMedia> loadMediaArrayList;
    private int modeType; // 0 - только просмотр, 1 - удаление с просмотром, 2 - загрузка
    public interface OnMediaListener {
        void onDeleteMedia(int position);
    }
    private OnMediaListener onMediaListener;
    private ChatAdapter.OnSelectMedia onSelectMedia;
    public MessageMediaAdapter(Context context, ArrayList<Uri> mediaList, OnMediaListener onMediaListener, int modeType) {
        this.mediaList = mediaList;
        this.loadMediaArrayList = null;
        this.inflater = LayoutInflater.from(context);
        this.onSelectMedia = (ChatAdapter.OnSelectMedia) context;
        this.onMediaListener = onMediaListener;
        this.modeType = modeType;
    }
    public MessageMediaAdapter(Context context, ArrayList<LoadMedia> loadMediaArrayList, OnMediaListener onMediaListener, int modeType, boolean isLoadMedia) {
        this.loadMediaArrayList = loadMediaArrayList;
        this.mediaList = null;
        this.inflater = LayoutInflater.from(context);
        this.onSelectMedia = (ChatAdapter.OnSelectMedia) context;
        this.onMediaListener = onMediaListener;
        this.modeType = modeType;
    }
    @NonNull
    @Override
    public MessageMediaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.media_item_layout, parent, false);
        return new ViewHolder(view);
    }


    public int getModeType() {
        return modeType;
    }

    public void setModeType(int modeType) {
        this.modeType = modeType;
    }

    @Override
    public void onBindViewHolder(MessageMediaAdapter.ViewHolder holder, int position) {
        if (modeType == 1) {
            holder.deleteImage.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.image.getLayoutParams();
            params.height = (int) MapFragment.DpToPx(100, holder.itemView.getContext());
            params.width = (int) MapFragment.DpToPx(100, holder.itemView.getContext());
            holder.image.setLayoutParams(params);
        } else if (modeType == 0) {
            holder.deleteImage.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.image.getLayoutParams();
            params.width = (int) (holder.widthPixels);
            params.height = params.width;
            holder.image.setLayoutParams(params);
            Uri uri = Uri.parse(String.valueOf(mediaList.get(position)));
            Glide.with(holder.itemView.getContext()).load(uri).into(holder.image);
            holder.itemView.setOnClickListener(v -> {
                onSelectMedia.onSelectMedia(uri);
            });
            holder.deleteImage.setOnClickListener(v -> {
                onMediaListener.onDeleteMedia(position);
            });
            return;
        } else if (modeType == 2) {
            holder.deleteImage.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.image.getLayoutParams();
            params.height = (int) MapFragment.DpToPx(100, holder.itemView.getContext());
            params.width = (int) MapFragment.DpToPx(100, holder.itemView.getContext());
            holder.image.setLayoutParams(params);
        }
        LoadMedia loadMedia = loadMediaArrayList.get(position);
        Uri uri = Uri.parse(String.valueOf(loadMedia.getUri()));
        if (loadMedia.getProgress() != 100 && loadMedia.getProgress() != 0) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(loadMedia.getProgress(), true);
            Log.d("Chat", position + " " + loadMedia.getProgress());
        } else {
            holder.progressBar.setVisibility(View.GONE);
        }
        Glide.with(holder.itemView.getContext()).load(uri).into(holder.image);
        holder.itemView.setOnClickListener(v -> {
            onSelectMedia.onSelectMedia(uri);
        });
        holder.deleteImage.setOnClickListener(v -> {
            onMediaListener.onDeleteMedia(position);
        });
    }

    @Override
    public int getItemCount() {
        if (modeType == 0) {
            return mediaList.size();
        } else {
            return loadMediaArrayList.size();
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image, deleteImage;
        final CircularProgressIndicator progressBar;
        final int widthPixels;

        ViewHolder(View view){
            super(view);
            WindowManager windowManager = (WindowManager) itemView.getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
            widthPixels = displayMetrics.widthPixels;
            image = view.findViewById(R.id.imageView);
            deleteImage = view.findViewById(R.id.imageView2);
            progressBar = view.findViewById(R.id.progressBar);
        }
    }

}