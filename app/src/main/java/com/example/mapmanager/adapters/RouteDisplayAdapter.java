package com.example.mapmanager.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.R;
import com.example.mapmanager.models.RouteCard;

import java.util.List;

public class RouteDisplayAdapter extends RecyclerView.Adapter<RouteDisplayAdapter.ViewHolder> {
    private List<RouteCard> routeList;
    private Context context;
    private RouteDisplayListener routeDisplayListener;
    public RouteDisplayAdapter(Context context, List<RouteCard> routeList, RouteDisplayListener routeDisplayListener) {
        this.routeList = routeList;
        this.context = context;
        this.routeDisplayListener = routeDisplayListener;
    }
    public interface RouteDisplayListener {
        void acceptRouteButton(int position);
        void showInMapButton(int position);
    }
    @NonNull
    @Override
    public RouteDisplayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RouteCard curRoute = routeList.get(position);
        holder.RouteNameText.setText(curRoute.getName());
        holder.DescriptionTextView.setText(curRoute.getDescription());
        holder.RouteTimeIntervalText.setText(timeIntervalConversion(curRoute.getStartTime(), curRoute.getEndTime()));
        holder.AcceptRouteButton.setOnClickListener(v->{
            routeDisplayListener.acceptRouteButton(position);
        });
        holder.ShowInMapButton.setOnClickListener(v->{
            routeDisplayListener.showInMapButton(position);
        });
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView RouteTimeIntervalText, RouteNameText, DescriptionTextView;
        private View AcceptRouteButton, ShowInMapButton;
        ViewHolder(View view){
            super(view);
            RouteNameText = view.findViewById(R.id.RouteNameText);
            RouteTimeIntervalText = view.findViewById(R.id.RouteTimeIntervalText);
            DescriptionTextView = view.findViewById(R.id.DescriptionTextView);
            AcceptRouteButton = view.findViewById(R.id.AcceptRouteButton);
            ShowInMapButton = view.findViewById(R.id.ShowInMapButton);
        }
    }
    private String timeIntervalConversion(long sTime, long eTime) {
        String sText = DateUtils.formatDateTime(context, sTime, DateUtils.FORMAT_SHOW_TIME);
        String eText = DateUtils.formatDateTime(context, eTime, DateUtils.FORMAT_SHOW_TIME);
        return sText + "-" + eText;
    }
}
