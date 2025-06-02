package com.example.mapmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.R;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.models.SegmentInfo;

import java.util.ArrayList;
import java.util.List;

public class SegmentInfoAdapter extends RecyclerView.Adapter<SegmentInfoAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final ArrayList<SegmentInfo> segmentInfos;
    public SegmentInfoAdapter(Context context, ArrayList<SegmentInfo> segmentInfos) {
        this.segmentInfos = segmentInfos;
        this.inflater = LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public SegmentInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.route_info_item_layout, parent, false);
        return new SegmentInfoAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(SegmentInfoAdapter.ViewHolder holder, int position) {
        SegmentInfo segmentInfo = segmentInfos.get(position);
        if (segmentInfo != null) {
            if (segmentInfo.getType() == 2 || segmentInfo.getType() == 3) {
                holder.busLayout.setVisibility(View.VISIBLE);
                holder.walkingLayout.setVisibility(View.GONE);
                if (segmentInfo.getType() == 2) {
                    holder.busImageView.setImageResource(R.drawable.bus);
                    holder.busList.setText(segmentInfo.getBusList());
                    holder.busList.setVisibility(View.VISIBLE);
                    holder.dataBusLine.setText(segmentInfo.getTime());
                } else {
                    holder.busImageView.setImageResource(R.drawable.car);
                    holder.busList.setVisibility(View.GONE);
                    holder.dataBusLine.setText(segmentInfo.getDistance() + " • " + segmentInfo.getTime());
                }
                holder.nameBusLine.setText(segmentInfo.getName());
            } else {
                holder.busLayout.setVisibility(View.GONE);
                holder.walkingLayout.setVisibility(View.VISIBLE);
                holder.fitnessWalkingLine.setVisibility(View.VISIBLE);
                if (segmentInfo.getType() == 1) {
                    holder.walkingImageView.setImageResource(R.drawable.walking);
                    holder.fitnessWalkingLine.setText(segmentInfo.getStep() + " • " + segmentInfo.getCalories());
                } else {
                    holder.walkingImageView.setImageResource(R.drawable.bicycle);
                    holder.fitnessWalkingLine.setText(segmentInfo.getCalories());
                }
                holder.dataWalkingLine.setVisibility(View.VISIBLE);
                holder.dataWalkingLine.setText(segmentInfo.getDistance() + " • " + segmentInfo.getTime());
                if (segmentInfo.getDistance().equals("0")) {
                    holder.nameWalkingLine.setText(segmentInfo.getName());
                    holder.dataWalkingLine.setText(segmentInfo.getTime());
                    holder.fitnessWalkingLine.setVisibility(View.GONE);
                } else {
                    holder.nameWalkingLine.setText(segmentInfo.getName());
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return segmentInfos.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout walkingLayout, busLayout;
        final View walkingLine, busLine;
        final TextView nameWalkingLine, dataWalkingLine, fitnessWalkingLine;
        final TextView nameBusLine, dataBusLine, busList;
        final ImageView walkingImageView, busImageView;
        ViewHolder(View view){
            super(view);
            walkingLayout = view.findViewById(R.id.walkingLayout);
            busLayout = view.findViewById(R.id.busLayout);
            walkingLine = view.findViewById(R.id.walkingLine);
            busLine = view.findViewById(R.id.busLine);
            nameWalkingLine = view.findViewById(R.id.nameWalkingLine);
            dataWalkingLine = view.findViewById(R.id.dataWalkingLine);
            fitnessWalkingLine = view.findViewById(R.id.fitnessWalkingLine);
            nameBusLine = view.findViewById(R.id.nameBusLine);
            dataBusLine = view.findViewById(R.id.dataBusLine);
            busList = view.findViewById(R.id.busList);
            walkingImageView = view.findViewById(R.id.walkingImageView);
            busImageView = view.findViewById(R.id.busImageView);
        }
    }
}