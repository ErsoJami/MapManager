package com.example.mapmanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.adapters.ChatAdapter;
import com.example.mapmanager.adapters.RouteDisplayAdapter;
import com.example.mapmanager.adapters.RouteSelectAdapter;
import com.example.mapmanager.models.Chat;
import com.example.mapmanager.models.Message;
import com.example.mapmanager.models.Route;
import com.example.mapmanager.models.RouteCard;
import com.example.mapmanager.models.RouteCardSettings;
import com.example.mapmanager.models.Waypoint;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment {
    private RecyclerView routeListView, selectingRouteListView;
    private RouteDisplayAdapter adapter;
    private RouteSelectAdapter selectAdapter;
    private List<RouteCard> routeList;
    private List<Route> localRouteList;
    private View plusView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        routeList = new ArrayList<RouteCard>();
        localRouteList = new ArrayList<Route>();
        routeListView = view.findViewById(R.id.routeListView);
        plusView = view.findViewById(R.id.plusView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        routeList.add(new RouteCard(new Route(), "dsfsdfsdfsdf", "Hijab", "The way of Hitler", new RouteCardSettings(), 1488, 5252));
        for (int i = 0; i < 12; i++) localRouteList.add(new Route(new ArrayList<Waypoint>(), "luberGay", "RooK", "BlablaBlaBla"));
        adapter = new RouteDisplayAdapter(getContext(), routeList);
        selectAdapter = new RouteSelectAdapter(localRouteList);
        routeListView.setLayoutManager(new LinearLayoutManager(getContext()));
        routeListView.setAdapter(adapter);
        routeListView.setVisibility(View.VISIBLE);
        plusView.setOnClickListener(v->{
            LayoutInflater inflater = getLayoutInflater();
            View changeDialogView = inflater.inflate(R.layout.select_route_dialog, null);
            AlertDialog dialog = new AlertDialog.Builder(requireActivity()).setView(changeDialogView).create();
            selectingRouteListView = changeDialogView.findViewById(R.id.selectingRouteListView);
            selectingRouteListView.setLayoutManager(new LinearLayoutManager(getContext()));
            selectingRouteListView.setAdapter(selectAdapter);
            selectingRouteListView.setVisibility(View.VISIBLE);
            dialog.show();
        });
    }
}