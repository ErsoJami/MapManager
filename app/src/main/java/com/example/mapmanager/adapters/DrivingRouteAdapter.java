package com.example.mapmanager.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.R;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.transport.masstransit.Route;

import java.util.ArrayList;

public class DrivingRouteAdapter extends RecyclerView.Adapter<DrivingRouteAdapter.ViewHolder> {

    private int selectPosition = 0;
    private final LayoutInflater inflater;
    private ArrayList<DrivingRoute> routeArrayList;
    private DrivingRouteAdapterListener drivingRouteAdapterListener;
    public interface DrivingRouteAdapterListener {
        void onClickDrivingItem(int position);
    }

    public DrivingRouteAdapter(Context context, ArrayList<DrivingRoute> routeArrayList, DrivingRouteAdapterListener drivingRouteAdapterListener) {
        this.inflater = LayoutInflater.from(context);
        this.routeArrayList = routeArrayList;
        this.drivingRouteAdapterListener = drivingRouteAdapterListener;
    }
    @NonNull
    @Override
    public DrivingRouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.select_route_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrivingRouteAdapter.ViewHolder holder, int position) {
        DrivingRoute route = routeArrayList.get(position);
        if (route != null) {
            if (position == selectPosition) {
                holder.timeText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                holder.distanceText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_grey));
                holder.itemView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_blue)));
            } else {
                holder.timeText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
                holder.distanceText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.dark_grey));
                holder.itemView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_grey)));
            }
            holder.timeText.setText(route.getMetadata().getWeight().getTime().getText());
            holder.distanceText.setText(route.getMetadata().getWeight().getDistance().getText());
            holder.itemView.setOnClickListener(v -> {
                setCurrentButton(position);
            });
        }
    }
    private void setCurrentButton(int position) {
        selectPosition = position;
        notifyDataSetChanged();
        drivingRouteAdapterListener.onClickDrivingItem(position);
    }
    @Override
    public int getItemCount() {
        return routeArrayList.size();
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView timeText, distanceText;
        ViewHolder(View view){
            super(view);
            timeText = view.findViewById(R.id.timeText);
            distanceText = view.findViewById(R.id.distanceText);
        }
    }
}
