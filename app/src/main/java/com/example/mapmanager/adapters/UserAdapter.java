package com.example.mapmanager.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapmanager.R;
import com.example.mapmanager.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private boolean type;
    private final LayoutInflater inflater;
    private final ArrayList<User> userArrayList;
    private final FirebaseAuth mAuth;
    private OnDeleteUserListener onDeleteUserListener;
    public interface OnDeleteUserListener {
        void onDeleteUser(int position);
    }
    public UserAdapter(Context context, ArrayList<User> userArrayList, boolean type, OnDeleteUserListener onDeleteUserListener) {
        this.inflater = LayoutInflater.from(context);
        this.userArrayList = userArrayList;
        this.type = type;
        this.onDeleteUserListener = onDeleteUserListener;
        mAuth = FirebaseAuth.getInstance();
    }
    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.user_item_layout, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        User user = userArrayList.get(position);
        if (user != null) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.account_icon)
                    .into(holder.userIconImageView);
            holder.nameTextView.setText(user.getName());
            holder.nickTextView.setText(user.getNick());
            if (type) {
                if (user.getUserId().equals(mAuth.getUid())) {
                    holder.deleteUserView.setVisibility(View.GONE);
                } else {
                    holder.deleteUserView.setVisibility(View.VISIBLE);
                }
                holder.deleteUserView.setOnClickListener(v -> {
                    onDeleteUserListener.onDeleteUser(position);
                });
            } else if (user.getUserId().equals(mAuth.getUid())) {
                holder.deleteUserView.setVisibility(View.VISIBLE);
                holder.deleteUserView.setOnClickListener(v -> {
                    onDeleteUserListener.onDeleteUser(position);
                });
            } else {
                holder.deleteUserView.setVisibility(View.GONE);
            }
        }
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView userIconImageView;
        final TextView nameTextView, nickTextView;
        final ImageView deleteUserView;
        ViewHolder(View view){
            super(view);
            userIconImageView = view.findViewById(R.id.userIconImageView);
            nameTextView = view.findViewById(R.id.nameTextView);
            nickTextView = view.findViewById(R.id.nickTextView);
            deleteUserView = view.findViewById(R.id.deleteUserView);
        }
    }
}
