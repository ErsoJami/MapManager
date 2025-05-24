package com.example.mapmanager.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
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

import java.util.ArrayList;
import java.util.List;

public class MessageMediaAdapter extends RecyclerView.Adapter<MessageMediaAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final ArrayList<Uri> mediaList;
    private final boolean modeType;
    public interface OnMediaListener {
        void onDeleteMedia(int position);
    }
    private OnMediaListener onMediaListener;
    private ChatAdapter.OnSelectMedia onSelectMedia;
    public MessageMediaAdapter(Context context, ArrayList<Uri> mediaList, OnMediaListener onMediaListener, boolean modeType) {
        this.mediaList = mediaList;
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


    @Override
    public void onBindViewHolder(MessageMediaAdapter.ViewHolder holder, int position) {
        if (!modeType) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.image.getLayoutParams();
            params.height = (int) MapFragment.DpToPx(100, holder.itemView.getContext());
            params.width = (int) MapFragment.DpToPx(100, holder.itemView.getContext());
            holder.image.setLayoutParams(params);
        } else {
            holder.deleteImage.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.image.getLayoutParams();
            params.width = (int) (holder.widthPixels);
            params.height = params.width;
            holder.image.setLayoutParams(params);
        }
        Uri uri = mediaList.get(position);
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
        return mediaList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image, deleteImage;
        final ProgressBar progressBar;
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