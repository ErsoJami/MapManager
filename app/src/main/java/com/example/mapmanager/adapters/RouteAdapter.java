package com.example.mapmanager.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
    private InputMethodManager imm;
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
        imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
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
            holder.text1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    String text = holder.text1.getText().toString();
                    holder.text1.clearFocus();
                    imm.hideSoftInputFromWindow(holder.text1.getWindowToken(), 0);
                    routeAdapterListener.onRouteNameChanged(text, position);
                    return true;
                }
            });
            holder.text2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    String text = holder.text2.getText().toString();
                    holder.text2.clearFocus();
                    imm.hideSoftInputFromWindow(holder.text2.getWindowToken(), 0);
                    routeAdapterListener.onRouteDescriptionChanged(text, position);
                    return true;
                }
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
        final ImageView focusToRoute, deleteRoute;
        ViewHolder(View view){
            super(view);
            text1 = view.findViewById(R.id.editNameText);
            text2 = view.findViewById(R.id.editDescriptionText);
            focusToRoute = view.findViewById(R.id.focusToRoute);
            deleteRoute = view.findViewById(R.id.deleteRoute);
        }
    }
}
