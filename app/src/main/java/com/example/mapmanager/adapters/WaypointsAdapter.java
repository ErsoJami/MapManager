package com.example.mapmanager.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.models.Chat;
import com.example.mapmanager.R;
import com.example.mapmanager.models.Message;
import com.example.mapmanager.models.Waypoint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.Image;
import com.yandex.mapkit.map.PlacemarkMapObject;

import java.util.ArrayList;
import java.util.List;

public class WaypointsAdapter extends RecyclerView.Adapter<WaypointsAdapter.ViewHolder>{

    public interface WaypointsAdapterListener {
        void arrowUpClickListener(int position);
        void arrowDownClickListener(int position);
        void onNameChanged(String text, int position);
        void onDescriptionChanged(String text, int position);
        void onDeleteWaypoint(int position);
        void onFocusToWaypoint(int position);
    }
    private final LayoutInflater inflater;
    private final WaypointsAdapterListener waypointsAdapterListener;
    private final ArrayList<PlacemarkMapObject> placemarkMapObjectArrayList;
    public WaypointsAdapter(Context context, ArrayList<PlacemarkMapObject> placemarkMapObjectArrayList, WaypointsAdapterListener waypointsAdapterListener) {
        this.placemarkMapObjectArrayList = placemarkMapObjectArrayList;
        this.inflater = LayoutInflater.from(context);
        this.waypointsAdapterListener = waypointsAdapterListener;
    }
    @NonNull
    @Override
    public WaypointsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.waypoint_item_layout, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(WaypointsAdapter.ViewHolder holder, int position) {
        PlacemarkMapObject mark = placemarkMapObjectArrayList.get(position);
        if (mark != null) {
            Waypoint waypoint = (Waypoint) mark.getUserData();
            holder.text1.setText(waypoint.getName());
//            holder.text2.setText(waypoint.getDescription());
            holder.text2.setText(mark.getGeometry().getLatitude() + " " + mark.getGeometry().getLongitude());
            if (position == 0) {
                holder.arrowUp.setVisibility(View.GONE);
            } else {
                holder.arrowUp.setVisibility(View.VISIBLE);
            }
            if (position == placemarkMapObjectArrayList.size() - 1) {
                holder.arrowDown.setVisibility(View.GONE);
            } else {
                holder.arrowDown.setVisibility(View.VISIBLE);
            }
            holder.checkUp.setOnClickListener(v -> {
                String text = holder.text1.getText().toString();
                waypointsAdapterListener.onNameChanged(text, position);
            });
            holder.checkDown.setOnClickListener(v -> {
                String text = holder.text2.getText().toString();
                waypointsAdapterListener.onDescriptionChanged(text, position);
            });
            holder.arrowDown.setOnClickListener(v -> {
                waypointsAdapterListener.arrowDownClickListener(position);
                Log.d("test", "arrowDown " + position);
            });
            holder.arrowUp.setOnClickListener(v -> {
                waypointsAdapterListener.arrowUpClickListener(position);
                Log.d("test", "arrowUp " + position);
            });
            holder.deleteWaypoint.setOnClickListener(v -> {
                waypointsAdapterListener.onDeleteWaypoint(position);
            });
            holder.focusToWaypoint.setOnClickListener(v -> {
                waypointsAdapterListener.onFocusToWaypoint(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return placemarkMapObjectArrayList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final EditText text1, text2;
        final ImageView arrowUp, arrowDown, checkUp, checkDown, focusToWaypoint, deleteWaypoint;
        ViewHolder(View view){
            super(view);
            text1 = view.findViewById(R.id.editTextText2);
            text2 = view.findViewById(R.id.editTextText);
            arrowUp = view.findViewById(R.id.arrowUp);
            arrowDown = view.findViewById(R.id.arrowDown);
            checkUp = view.findViewById(R.id.checkUp);
            checkDown = view.findViewById(R.id.checkDown);
            focusToWaypoint = view.findViewById(R.id.focusToWaypoint);
            deleteWaypoint = view.findViewById(R.id.deleteWaypoint);
        }
    }
}