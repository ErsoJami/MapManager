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

import java.util.*;

public class RouteSelectAdapter extends RecyclerView.Adapter<RouteSelectAdapter.ViewHolder> {
    private List<Route> routeList;
    public RouteSelectAdapter(List<Route> routeList) {
        this.routeList = routeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_select_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Route curRoute = routeList.get(position);
        holder.RouteNameText.setText(curRoute.getName());
        holder.DescriptionTextView.setText(curRoute.getDescription());
        holder.postButtonView.setOnClickListener(v->{

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
