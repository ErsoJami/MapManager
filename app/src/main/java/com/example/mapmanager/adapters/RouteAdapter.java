package com.example.mapmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.R;
import com.example.mapmanager.models.Route;
import com.yandex.mapkit.map.PlacemarkMapObject;

import java.util.ArrayList;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final ArrayList<Route> routeArrayList;
    public RouteAdapter(Context context, ArrayList<Route> routeArrayList) {
        this.routeArrayList = routeArrayList;
        this.inflater = LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public RouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.route_item_layout, parent, false);
        return new RouteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteAdapter.ViewHolder holder, int position) {
        Route route = routeArrayList.get(position);
        if (route != null) {
            holder.text1.setText(route.getName());
            holder.text2.setText(route.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return routeArrayList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final EditText text1, text2;
        final ImageView checkUp, checkDown, focusToWaypoint, deleteWaypoint;
        ViewHolder(View view){
            super(view);
            text1 = view.findViewById(R.id.editNameText);
            text2 = view.findViewById(R.id.editDescriptionText);
            checkUp = view.findViewById(R.id.checkUp);
            checkDown = view.findViewById(R.id.checkDown);
            focusToWaypoint = view.findViewById(R.id.focusToRoute);
            deleteWaypoint = view.findViewById(R.id.deleteRoute);
        }
    }
}
