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
import com.yandex.mapkit.transport.masstransit.Route;

import java.util.ArrayList;

public class MasstransitRouteAdapter extends RecyclerView.Adapter<MasstransitRouteAdapter.ViewHolder> {

    private int selectPosition = 0;
    private final LayoutInflater inflater;
    private ArrayList<Route> routeArrayList;
    private MasstransitRouteAdapterListener masstransitRouteAdapterListener;
    public interface MasstransitRouteAdapterListener {
        void onClickMasstransitItem(int position);
    }

    public MasstransitRouteAdapter(Context context, ArrayList<Route> routeArrayList, MasstransitRouteAdapterListener masstransitRouteAdapterListener) {
        this.inflater = LayoutInflater.from(context);
        this.routeArrayList = routeArrayList;
        this.masstransitRouteAdapterListener = masstransitRouteAdapterListener;
    }
    @NonNull
    @Override
    public MasstransitRouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.select_route_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MasstransitRouteAdapter.ViewHolder holder, int position) {
        Route route = routeArrayList.get(position);
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
            holder.distanceText.setText(route.getMetadata().getWeight().getWalkingDistance().getText());
            holder.itemView.setOnClickListener(v -> {
                setCurrentButton(position);
            });
        }
    }
    private void setCurrentButton(int position) {
        selectPosition = position;
        notifyDataSetChanged();
        masstransitRouteAdapterListener.onClickMasstransitItem(position);
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
