package com.example.mapmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.R;
import com.example.mapmanager.models.Route;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.*;

public class RouteSelectAdapter extends RecyclerView.Adapter<RouteSelectAdapter.ViewHolder> {
    public interface PostListener{
        void postRouteCard(Route curRoute);
    }
    PostListener postListener;
    private List<Route> routeList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    public RouteSelectAdapter(List<Route> routeList, PostListener postListener) {
        this.postListener = postListener;
        this.routeList = routeList;
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_select_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseUser user = mAuth.getCurrentUser();
        Route curRoute = routeList.get(position);
        holder.RouteNameText.setText(curRoute.getName());
        holder.DescriptionTextView.setText(curRoute.getDescription());
        holder.postButtonView.setOnClickListener(v->{
            postListener.postRouteCard(curRoute);
        });
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView RouteNameText, DescriptionTextView;
        private View postButtonView;
        ViewHolder(View itemView) {
            super(itemView);
            RouteNameText = itemView.findViewById(R.id.RouteNameText);
            DescriptionTextView = itemView.findViewById(R.id.DescriptionTextView);
            postButtonView = itemView.findViewById(R.id.postButtonView);
        }
    }
}
