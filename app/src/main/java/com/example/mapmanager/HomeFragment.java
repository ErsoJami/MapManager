package com.example.mapmanager;

import static com.example.mapmanager.MainActivity.user;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import com.google.firebase.database.ChildEventListener;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment implements RouteSelectAdapter.PostListener, RouteDisplayAdapter.RouteDisplayListener  {
    private RecyclerView routeListView, selectingRouteListView;
    private RouteDisplayAdapter adapter;
    private RouteSelectAdapter selectAdapter;
    private List<RouteCard> routeList;
    private List<Route> localRouteList;
    private ImageView routeCardBuildingCloseImage;
    private TextView addRouteCardView, routeCardBuildingDateEditText, routeCardBuildingStartTimeEditText, routeCardBuildingEndTimeEditText;
    private EditText routeCardBuildingNameEnter, routeCardBuildingDescriptionEnter;
    private View plusView;
    private AlertDialog dialog;
    private FirebaseAuth mAuth;
    private Calendar startTime, endTime;
    private HomeFragmentListener homeFragmentListener;

    @Override
    public void acceptRouteButton(int position) {

    }

    @Override
    public void showInMapButton(int position) {
        FirebaseDatabase.getInstance().getReference().child("routes").child(routeList.get(position).getRoute()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    homeFragmentListener.showInMap(dataSnapshot.getValue(Route.class));
                }
            }
        });

    }

    interface HomeFragmentListener {
        void showInMap(Route route);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        routeList = new ArrayList<RouteCard>();
        localRouteList = new ArrayList<Route>();
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("routeCards");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    routeList.add(snapshot.getValue(RouteCard.class));
                    adapter.notifyItemInserted(adapter.getItemCount() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    RouteCard routeCard = snapshot.getValue(RouteCard.class);
                    int position = Collections.binarySearch(routeList, routeCard, new Comparator<RouteCard>() {
                        @Override
                        public int compare(RouteCard routeCard, RouteCard routeCard1) {
                            return routeCard.getId().compareTo(routeCard1.getId());
                        }
                    });
                    if (position != -1) {
                        routeList.set(position, routeCard);
                    }
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                RouteCard routeCard = snapshot.getValue(RouteCard.class);
                int position = Collections.binarySearch(routeList, routeCard, new Comparator<RouteCard>() {
                    @Override
                    public int compare(RouteCard routeCard, RouteCard routeCard1) {
                        return routeCard.getId().compareTo(routeCard1.getId());
                    }
                });
                if (position != -1) {
                    routeList.remove(position);
                }
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference routeReference = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("routeList");
        routeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                localRouteList.clear();
                GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                ArrayList<String> routeId = snapshot.getValue(t);
                if (routeId != null) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("routes");
                    for (String id : routeId) {
                        DatabaseReference routesRef = databaseReference.child(id);
                        if (routesRef != null) {
                            databaseReference.child(id).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot dataSnapshot) {
                                    Route route = dataSnapshot.getValue(Route.class);
                                    if (route != null) {
                                        localRouteList.add(route);
                                        selectAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        homeFragmentListener = (HomeFragmentListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        routeListView = view.findViewById(R.id.routeListView);
        plusView = view.findViewById(R.id.imageView3);
        adapter = new RouteDisplayAdapter(getContext(), routeList, this);
        selectAdapter = new RouteSelectAdapter(localRouteList, this);
        routeListView.setLayoutManager(new LinearLayoutManager(getContext()));
        routeListView.setAdapter(adapter);
        routeListView.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        plusView.setOnClickListener(v->{
            LayoutInflater inflater = getLayoutInflater();
            View changeDialogView = inflater.inflate(R.layout.select_route_dialog, null);
            dialog = new AlertDialog.Builder(requireActivity()).setView(changeDialogView).create();
            selectingRouteListView = changeDialogView.findViewById(R.id.selectingRouteListView);
            selectingRouteListView.setLayoutManager(new LinearLayoutManager(getContext()));
            selectingRouteListView.setAdapter(selectAdapter);
            selectingRouteListView.setVisibility(View.VISIBLE);
            dialog.show();
        });
    }
    @Override
    public void postRouteCard(Route curRoute) {
        dialog.dismiss();
        LayoutInflater inflater = getLayoutInflater();
        View postRouteCardDialogView = inflater.inflate(R.layout.route_card_build_dialog, null);
        AlertDialog buildDialog = new AlertDialog.Builder(requireActivity()).setView(postRouteCardDialogView).setCancelable(false).create();
        buildDialogDeclaration(postRouteCardDialogView);
        routeCardBuildingCloseImage.setOnClickListener(v->{
            buildDialog.dismiss();
        });
        addRouteCardView.setOnClickListener(v->{
            final long initialStartTimeMillis = startTime.getTimeInMillis();
            final long initialEndTimeMillis = endTime.getTimeInMillis();
            startTime = Calendar.getInstance();
            endTime = Calendar.getInstance();
            startTime.add(Calendar.SECOND, 1);
            endTime.add(Calendar.SECOND, 1);
            if (!routeCardBuildingNameEnter.getText().toString().isEmpty() &&
                    initialStartTimeMillis > startTime.getTimeInMillis() && initialEndTimeMillis > endTime.getTimeInMillis()) {
                RouteCard routeCard = new RouteCard(curRoute.getId(), routeCardBuildingNameEnter.getText().toString(), routeCardBuildingDescriptionEnter.getText().toString(),
                        new RouteCardSettings(), startTime.getTimeInMillis(), endTime.getTimeInMillis());
                routeCard.createRoutCard();
                buildDialog.dismiss();
                startTime = Calendar.getInstance();
                endTime = Calendar.getInstance();
            } else if (routeCardBuildingNameEnter.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Маршрут должен иметь название", Toast.LENGTH_SHORT).show();
            } else if (initialStartTimeMillis <= startTime.getTimeInMillis() && initialEndTimeMillis <= endTime.getTimeInMillis()) {
                Toast.makeText(requireContext(), "Недопустимое время начала и конца", Toast.LENGTH_SHORT).show();
            }
        });
        routeCardBuildingDateEditText.setOnClickListener(v->{
            new DatePickerDialog(requireActivity(), d, startTime.get(Calendar.YEAR), startTime.get(Calendar.MONTH), startTime.get(Calendar.DAY_OF_MONTH)).show();
        });
        routeCardBuildingStartTimeEditText.setOnClickListener(v->{
            new TimePickerDialog(requireActivity(), st, startTime.get(Calendar.HOUR), startTime.get(Calendar.MINUTE), true).show();
        });
        routeCardBuildingEndTimeEditText.setOnClickListener(v->{
            new TimePickerDialog(requireActivity(), et, endTime.get(Calendar.HOUR), endTime.get(Calendar.MINUTE), true).show();
        });
        buildDialog.show();
    }

    public void buildDialogDeclaration(View view) {
        routeCardBuildingCloseImage = view.findViewById(R.id.routeCardBuildingCloseImage);
        addRouteCardView = view.findViewById(R.id.addRouteCardView);
        routeCardBuildingNameEnter = view.findViewById(R.id.routeCardBuildingNameEnter);
        routeCardBuildingDescriptionEnter = view.findViewById(R.id.routeCardBuildingDescriptionEnter);
        routeCardBuildingDateEditText = view.findViewById(R.id.routeCardBuildingDateEditText);
        routeCardBuildingStartTimeEditText = view.findViewById(R.id.routeCardBuildingStartTimeEditText);
        routeCardBuildingEndTimeEditText = view.findViewById(R.id.routeCardBuildingEndTimeEditText);
    }
    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            startTime.set(Calendar.YEAR, year);
            startTime.set(Calendar.MONTH, monthOfYear);
            startTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            endTime.set(Calendar.YEAR, year);
            endTime.set(Calendar.MONTH, monthOfYear);
            endTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (startTime.getTimeInMillis() >= endTime.getTimeInMillis()) {
                endTime.add(Calendar.DAY_OF_MONTH, 1);
            }
            setInitialDate();
        }
    };

    TimePickerDialog.OnTimeSetListener st = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            startTime.set(Calendar.HOUR_OF_DAY, hour);
            startTime.set(Calendar.MINUTE, minute);
            setInitialTime(true);
            setInitialDate();
        }
    };

    TimePickerDialog.OnTimeSetListener et = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            endTime.set(Calendar.HOUR_OF_DAY, hour);
            endTime.set(Calendar.MINUTE, minute);
            setInitialTime(false);
            setInitialDate();
        }
    };

    void setInitialTime(boolean isStart) {
        if (isStart) {
            routeCardBuildingStartTimeEditText.setText(DateUtils.formatDateTime(requireContext(),
                    startTime.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_TIME));
        } else {
            routeCardBuildingEndTimeEditText.setText(DateUtils.formatDateTime(requireContext(),
                    endTime.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_TIME));
        }
    }
    void setInitialDate() {
        routeCardBuildingDateEditText.setText(DateUtils.formatDateTime(requireContext(),
                startTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

}