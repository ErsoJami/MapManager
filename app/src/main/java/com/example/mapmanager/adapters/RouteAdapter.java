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
    private RouteAdapterListener routeAdapterListener;
    public interface RouteAdapterListener {
        void onRouteNameChanged(String text, int position);
        void onRouteDescriptionChanged(String text, int position);
        void onDeleteRoute(int position);
        void onFocusToRoute(int position);
    }
    public RouteAdapter(Context context, ArrayList<Route> routeArrayList, RouteAdapterListener routeAdapterListener) {
        this.routeArrayList = routeArrayList;
        this.inflater = LayoutInflater.from(context);
        this.routeAdapterListener = routeAdapterListener;
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
            holder.checkUp.setOnClickListener(v -> {
                String text = holder.text1.getText().toString();
                routeAdapterListener.onRouteNameChanged(text, position);
            });
            holder.checkDown.setOnClickListener(v -> {
                String text = holder.text2.getText().toString();
                routeAdapterListener.onRouteDescriptionChanged(text, position);
            });
            holder.deleteRoute.setOnClickListener(v -> {
                routeAdapterListener.onDeleteRoute(position);
            });
            holder.focusToRoute.setOnClickListener(v -> {
                routeAdapterListener.onFocusToRoute(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return routeArrayList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final EditText text1, text2;
        final ImageView checkUp, checkDown, focusToRoute, deleteRoute;
        ViewHolder(View view){
            super(view);
            text1 = view.findViewById(R.id.editNameText);
            text2 = view.findViewById(R.id.editDescriptionText);
            checkUp = view.findViewById(R.id.checkUp);
            checkDown = view.findViewById(R.id.checkDown);
            focusToRoute = view.findViewById(R.id.focusToRoute);
            deleteRoute = view.findViewById(R.id.deleteRoute);
        }
    }
}
