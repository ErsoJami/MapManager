package com.example.mapmanager;

import static com.example.mapmanager.MainActivity.user;
import static com.google.common.primitives.Ints.max;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.GnssAntennaInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.adapters.DrivingRouteAdapter;
import com.example.mapmanager.adapters.MasstransitRouteAdapter;
import com.example.mapmanager.adapters.RouteAdapter;
import com.example.mapmanager.adapters.RouteAdapter.RouteAdapterListener;
import com.example.mapmanager.adapters.SegmentInfoAdapter;
import com.example.mapmanager.adapters.WaypointsAdapter;
import com.example.mapmanager.models.MapManager;
import com.example.mapmanager.models.MapManager.MapManagerSearchListener;
import com.example.mapmanager.models.MapManager.MapManagerSuggestListener;
import com.example.mapmanager.models.MapManager.MapLongTapListener;
import com.example.mapmanager.adapters.WaypointsAdapter.WaypointsAdapterListener;
import com.example.mapmanager.models.Route;
import com.example.mapmanager.models.SegmentInfo;
import com.example.mapmanager.models.Waypoint;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.geometry.PolylinePosition;
import com.yandex.mapkit.geometry.SubpolylineHelper;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.BusinessObjectMetadata;
import com.yandex.mapkit.search.ToponymObjectMetadata;
import com.yandex.mapkit.transport.masstransit.Section;
import com.yandex.mapkit.transport.masstransit.SectionMetadata;
import com.yandex.mapkit.transport.masstransit.Session;
import com.yandex.mapkit.transport.masstransit.Transport;
import com.yandex.runtime.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements MapManagerSearchListener, MapManagerSuggestListener, MapLongTapListener, WaypointsAdapterListener, RouteAdapterListener, MasstransitRouteAdapter.MasstransitRouteAdapterListener, DrivingRouteAdapter.DrivingRouteAdapterListener {
    private MapView mapView;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<String[]> getLocationPermission;
    private ListView listView;
    private TextView routeText, saveRouteText, deleteRouteText, focusRouteText, hintText, continueRouteText;
    private View pointsView, menuView;
    private ImageView userLocationImageView, newRouteImageView, menuImageView;
    private TextView carTextView, busTextView, walkingTextView, bicycleTextView;
    private TextView backButton, listPointText;
    private Drawable carDrawable, busDrawable, walkingDrawable, bicycleDrawable;
    private ImageView carImageView, busImageView, walkingImageView, bicycleImageView;
    private SearchView searchView;
    private ArrayAdapter<Map<String, String>> adapter;
    private WaypointsAdapter waypointAdapter;
    private RouteAdapter routeAdapter;
    private ArrayList<PlacemarkMapObject> placemarkMapObjectArrayList;
    private ArrayList<Route> routeArrayList;
    private MasstransitRouteAdapter busRouteAdapter, walkingRouteAdapter, bicycleRouteAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MapManager mapManager;
    private RecyclerView recyclerView, routeRecyclerView, masstransitRouteRecyclerView;
    private Boolean isClosePointsView = true, isCloseMenuView = true;
    private ConstraintLayout routeBuilderLayout;
    private Route routeToShowOnReady = null;
    private boolean needToFocusOnPolyline = false;
    private InputMethodManager imm;
    private ArrayList<com.yandex.mapkit.transport.masstransit.Route> busRoutes;
    private ArrayList<com.yandex.mapkit.transport.masstransit.Route> bicycleRoutes;
    private ArrayList<com.yandex.mapkit.transport.masstransit.Route> walkingRoutes;
    private ArrayList<DrivingRoute> carRoutes;
    private ArrayList<SegmentInfo> segmentInfos;
    private SegmentInfoAdapter segmentInfoAdapter;
    private RecyclerView segmentInfoRecyclerView;
    private Session.RouteListener walkingListener, busListener, bicycleListener;
    private DrivingSession.DrivingRouteListener carListener;
    private DrivingRouteAdapter carRouteAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLocationPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            Boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted!= null && coarseLocationGranted) {
                mapManager.createUserLayer();
            } else {
                // TODO доделать сообщение об исключении
            }
        });
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            routeToShowOnReady = (Route) getArguments().getSerializable("ROUTE_TO_SHOW");
        }
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapview);
        menuView = view.findViewById(R.id.menuView);
        routeText = view.findViewById(R.id.routeText);
        hintText = view.findViewById(R.id.hintText);
        carTextView = view.findViewById(R.id.carTextView);
        busTextView = view.findViewById(R.id.busTextView);
        walkingTextView = view.findViewById(R.id.walkingTextView);
        bicycleTextView = view.findViewById(R.id.bicycleTextView);
        carImageView = view.findViewById(R.id.carImageView);
        busImageView = view.findViewById(R.id.busImageView);
        walkingImageView = view.findViewById(R.id.walkingImageView);
        bicycleImageView = view.findViewById(R.id.bicycleImageView);
        routeBuilderLayout = view.findViewById(R.id.routeBuilderLayout);
        backButton = view.findViewById(R.id.backButton);
        listPointText = view.findViewById(R.id.textView14);

        carDrawable = carImageView.getDrawable();
        busDrawable = busImageView.getDrawable();
        walkingDrawable = walkingImageView.getDrawable();
        bicycleDrawable = bicycleImageView.getDrawable();

        busRoutes = new ArrayList<>();
        walkingRoutes = new ArrayList<>();
        carRoutes = new ArrayList<>();
        bicycleRoutes = new ArrayList<>();
        segmentInfos = new ArrayList<>();

        segmentInfoAdapter = new SegmentInfoAdapter(requireContext(), segmentInfos);
        segmentInfoRecyclerView = view.findViewById(R.id.segmentInfoRecyclerView);
        segmentInfoRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        segmentInfoRecyclerView.setAdapter(segmentInfoAdapter);

        walkingRouteAdapter = new MasstransitRouteAdapter(requireContext(), walkingRoutes, this);
        carRouteAdapter = new DrivingRouteAdapter(requireContext(), carRoutes, this);
        busRouteAdapter = new MasstransitRouteAdapter(requireContext(), busRoutes, this);
        bicycleRouteAdapter = new MasstransitRouteAdapter(requireContext(), bicycleRoutes, this);
        masstransitRouteRecyclerView = view.findViewById(R.id.masstransitRouteRecyclerView);
        masstransitRouteRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);

        saveRouteText = view.findViewById(R.id.saveRoute);
        continueRouteText = view.findViewById(R.id.continueRoute);
        deleteRouteText = view.findViewById(R.id.deleteRoute);
        focusRouteText = view.findViewById(R.id.focusRoute);
        routeRecyclerView = view.findViewById(R.id.routeRecyclerView);
        routeArrayList = new ArrayList<>();
        routeAdapter = new RouteAdapter(requireContext(), routeArrayList, this);
        routeRecyclerView.setAdapter(routeAdapter);
        routeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dataLoad();
        menuImageView = view.findViewById(R.id.menuButton);
        mapManager = new MapManager(requireContext(), mapView, this, this, this);
        getLocationPermission();
        placemarkMapObjectArrayList = new ArrayList<>();
        waypointAdapter = new WaypointsAdapter(requireContext(), placemarkMapObjectArrayList, this);
        listView = view.findViewById(R.id.listView);
        recyclerView = view.findViewById(R.id.recyclerView1);
        recyclerView.setAdapter(waypointAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        pointsView = view.findViewById(R.id.pointsView);
        searchView = view.findViewById(R.id.searchView);
        userLocationImageView = view.findViewById(R.id.userPosition);
        newRouteImageView = view.findViewById(R.id.createNewRoute);
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                Map<String, String> item = getItem(position);
                if (item != null) {
                    text1.setText(item.getOrDefault("name", ""));
                    text2.setText(item.getOrDefault("info", ""));
                }
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setVisibility(View.GONE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> adress = adapter.getItem(position);
                String responce = adress.getOrDefault("name", "") + " " + adress.getOrDefault("info", "").trim();
                listView.setVisibility(View.GONE);
                searchView.setQuery(responce, true);
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mapManager.search(query);
                searchView.clearFocus();
                listView.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacksAndMessages(null);
                if (newText.isEmpty()) {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    mapManager.suggest("");
                    return true;
                }
                listView.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> {
                    mapManager.suggest(newText);
                }, 500);
                return true;
            }
        });

        busListener = new Session.RouteListener() {
            @Override
            public void onMasstransitRoutes(@NonNull List<com.yandex.mapkit.transport.masstransit.Route> list) {
                busRoutes.clear();
                busRoutes.addAll(list);
                if (busRoutes != null && !busRoutes.isEmpty()) {
                    busTextView.setText(busRoutes.get(0).getMetadata().getWeight().getTime().getText());
                }
                busRouteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onMasstransitRoutesError(@NonNull Error error) {
                // TODO доделать сообщение об исключении
            }
        };
        bicycleListener = new Session.RouteListener() {
                    @Override
                    public void onMasstransitRoutes(@NonNull List<com.yandex.mapkit.transport.masstransit.Route> list) {
                        bicycleRoutes.clear();
                        bicycleRoutes.addAll(list);
                        if (bicycleRoutes != null && !bicycleRoutes.isEmpty()) {
                            bicycleTextView.setText(bicycleRoutes.get(0).getMetadata().getWeight().getTime().getText());
                        }
                        bicycleRouteAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onMasstransitRoutesError(@NonNull Error error) {
                        // TODO доделать сообщение об исключении
                    }
                };

        walkingListener = new Session.RouteListener() {
            @Override
            public void onMasstransitRoutes(@NonNull List<com.yandex.mapkit.transport.masstransit.Route> list) {
                walkingRoutes.clear();
                walkingRoutes.addAll(list);
                walkingRouteAdapter.notifyDataSetChanged();
                if (walkingRoutes != null && !walkingRoutes.isEmpty()) {
                    walkingTextView.setText(walkingRoutes.get(0).getMetadata().getWeight().getTime().getText());
                    onClickMasstransitItem(0);
                }
            }

            @Override
            public void onMasstransitRoutesError(@NonNull Error error) {
                // TODO доделать сообщение об исключении
            }
        };

        carListener = new DrivingSession.DrivingRouteListener() {
            @Override
            public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
                carRoutes.clear();
                carRoutes.addAll(list);
                carRouteAdapter.notifyDataSetChanged();
                if (carRoutes != null && !carRoutes.isEmpty()) {
                    carTextView.setText(carRoutes.get(0).getMetadata().getWeight().getTime().getText());
                }
            }

            @Override
            public void onDrivingRoutesError(@NonNull Error error) {
                // TODO доделать сообщение об исключении
            }
        };

        carTextView.setOnClickListener(v -> {
            setCurrentButton(carTextView);
            if (busRoutes != null && !busRoutes.isEmpty()) {
                onClickDrivingItem(0);
            }
            carRouteAdapter.setSelectPosition(0);
        });
        busTextView.setOnClickListener(v -> {
            setCurrentButton(busTextView);
            if (busRoutes != null && !busRoutes.isEmpty()) {
                onClickMasstransitItem(0);
            }
            busRouteAdapter.setSelectPosition(0);
        });
        walkingTextView.setOnClickListener(v -> {
            setCurrentButton(walkingTextView);
            if (walkingRoutes != null && !walkingRoutes.isEmpty()) {
                onClickMasstransitItem(0);
            }
            walkingRouteAdapter.setSelectPosition(0);
        });
        bicycleTextView.setOnClickListener(v -> {
            setCurrentButton(bicycleTextView);
            if (bicycleRoutes != null && !bicycleRoutes.isEmpty()) {
                onClickMasstransitItem(0);
            }
            bicycleRouteAdapter.setSelectPosition(0);
        });

        userLocationImageView.setOnClickListener(v -> {
            if (mapManager != null && mapManager.getCurrentUserLocation() != null) {
                mapManager.moveCamera(mapManager.getCurrentUserLocation(), 17.0f);
            }
        });
        newRouteImageView.setOnClickListener(v -> {
            if (!mapManager.isUserInCreateMode()) {
                mapManager.setUserInCreateMode(true);
                newRouteImageView.setImageResource(R.drawable.arrow_up);
            }
            if (isClosePointsView) {
                openRouteView();
            } else {
                closeRouteView();
            }

        });
        continueRouteText.setOnClickListener(v -> {
            getAllRoutes();
        });
        saveRouteText.setOnClickListener(v -> {
            LayoutInflater inflater2 = getLayoutInflater();
            View dialogView = inflater2.inflate(R.layout.save_route_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            View saveView = dialogView.findViewById(R.id.applySaveRoute);
            View cancelView = dialogView.findViewById(R.id.cancelView);
            EditText text1, text2;
            text1 = dialogView.findViewById(R.id.editNameText);
            text2 = dialogView.findViewById(R.id.editDescriptionText);
            saveView.setOnClickListener(v1 -> {
                ArrayList<Waypoint> waypointArrayList = new ArrayList<>();
                for (PlacemarkMapObject placemarkMapObject : placemarkMapObjectArrayList) {
                    Waypoint data = (Waypoint) placemarkMapObject.getUserData();
                    waypointArrayList.add(new Waypoint(data.getName(), data.getDescription(), placemarkMapObject.getGeometry()));
                }
                Route route = new Route(waypointArrayList, "", text1.getText().toString(), text2.getText().toString());
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference routeRef = databaseReference.child("routes").push();
                route.setId(routeRef.getKey());
                routeRef.setValue(route);
                ArrayList<String> routeList = user.getRouteList();
                routeList.add(route.getId());
                user.setRouteList(routeList);
                user.changeData(databaseReference.child("users").child(mAuth.getUid()));
                dialog.dismiss();
            });
            cancelView.setOnClickListener(v1 -> {
                dialog.dismiss();
            });
            dialog.show();
        });
        backButton.setOnClickListener(v -> {
            closeAllRoutes();
        });
        deleteRouteText.setOnClickListener(v -> {
            closeRouteView();
            mapManager.mapObjectCollection.clear();
            placemarkMapObjectArrayList.clear();
            mapManager.mapWaypointObjectCollection.clear();
            mapManager.mapRoutesObjectCollection.clear();
            waypointAdapter.notifyDataSetChanged();
            newRouteImageView.setImageResource(R.drawable.plus);
            mapManager.setUserInCreateMode(false);
        });
        menuImageView.setOnClickListener(v ->{
            if (isCloseMenuView) {
                openMenuView();
            } else {
                closeMenuView();
            }
        });
        focusRouteText.setOnClickListener(v -> {
            mapManager.focusOnPolyline();
            closeRouteView();
        });

        return view;
    }
    private void setCurrentButton(TextView textView) {
        resetButtonsColor();
        if (textView == carTextView) {
            carDrawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            carTextView.setTextColor(getResources().getColor(R.color.white));
            carTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
            masstransitRouteRecyclerView.setAdapter(carRouteAdapter);
        } else if (textView == busTextView) {
            busDrawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            busTextView.setTextColor(getResources().getColor(R.color.white));
            busTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
            masstransitRouteRecyclerView.setAdapter(busRouteAdapter);
        } else if (textView == walkingTextView) {
            walkingDrawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            walkingTextView.setTextColor(getResources().getColor(R.color.white));
            walkingTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
            masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);
        } else if (textView == bicycleTextView) {
            bicycleDrawable.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            bicycleTextView.setTextColor(getResources().getColor(R.color.white));
            bicycleTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_blue)));
            masstransitRouteRecyclerView.setAdapter(bicycleRouteAdapter);
        }
    }
    private void resetButtonsColor() {
        carDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        carTextView.setTextColor(getResources().getColor(R.color.black));
        carTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        busDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        busTextView.setTextColor(getResources().getColor(R.color.black));
        busTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        walkingDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        walkingTextView.setTextColor(getResources().getColor(R.color.black));
        walkingTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        bicycleDrawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        bicycleTextView.setTextColor(getResources().getColor(R.color.black));
        bicycleTextView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
    }
    @Override
    public void onClickMasstransitItem(int position) {
        MasstransitRouteAdapter masstransitRouteAdapter = (MasstransitRouteAdapter) masstransitRouteRecyclerView.getAdapter();
        com.yandex.mapkit.transport.masstransit.Route route = null;
        mapManager.mapRoutesObjectCollection.clear();
        segmentInfos.clear();
        if (masstransitRouteAdapter == walkingRouteAdapter) {
            route = walkingRoutes.get(position);
        } else if (masstransitRouteAdapter == busRouteAdapter) {
            route = busRoutes.get(position);
        } else if (masstransitRouteAdapter == bicycleRouteAdapter) {
            route = bicycleRoutes.get(position);
        }
        if (route != null) {
            List<Section> sections = route.getSections();
            int number = 1;
            for (Section section : sections) {
                Polyline sectionGeometry = SubpolylineHelper.subpolyline(route.getGeometry(), section.getGeometry());
                PolylineMapObject sectionPolyline = mapManager.mapRoutesObjectCollection.addPolyline(sectionGeometry);
                SectionMetadata data = section.getMetadata();
                if (data.getWeight().getWalkingDistance() != null && (data.getData().getTransports() == null || data.getData().getTransports().isEmpty())) {
                    sectionPolyline.setStrokeColor(getResources().getColor(R.color.dark_grey));
                    sectionPolyline.setStrokeWidth(5.0f);
                    sectionPolyline.setDashLength(10.0f);
                    sectionPolyline.setGapLength(8.0f);
                    sectionPolyline.setZIndex(10.0f);
                    if (data.getWeight().getWalkingDistance().getValue() == 0) {
                        segmentInfos.add(new SegmentInfo(1, getResources().getString(R.string.wait) + " ", "0", data.getWeight().getTime().getText(),
                                (int) (data.getWeight().getWalkingDistance().getValue() / 0.762), (int) (data.getWeight().getWalkingDistance().getValue() / 24), ""));
                        number--;
                    } else {
                        segmentInfos.add(new SegmentInfo(1, getResources().getString(R.string.section) + " " + number, data.getWeight().getWalkingDistance().getText(), data.getWeight().getTime().getText(),
                                (int) (data.getWeight().getWalkingDistance().getValue() / 0.762), (int) (data.getWeight().getWalkingDistance().getValue() / 24), ""));
                    }
                }
                else if (data.getData().getTransports() != null && !data.getData().getTransports().isEmpty()) {
                    Transport transport = data.getData().getTransports().get(0);
                    Integer apiColor = null;
                    if (transport.getLine().getStyle() != null && transport.getLine().getStyle().getColor() != null) {
                        apiColor = transport.getLine().getStyle().getColor();
                    }
                    if (apiColor != null) {
                        sectionPolyline.setStrokeColor(getResources().getColor(R.color.dark_grey));
                    }
                    sectionPolyline.setStrokeWidth(7.0f);
                    sectionPolyline.setOutlineColor(getResources().getColor(R.color.white));
                    sectionPolyline.setOutlineWidth(1.0f);
                    sectionPolyline.setZIndex(20.0f);
                    String allTranport = "";
                    for (Transport t : data.getData().getTransports()) {
                        allTranport += ", " + t.getLine().getName();
                    }
                    allTranport = getResources().getString(R.string.tranport) + " " + allTranport.replaceFirst(", ", "");
                    segmentInfos.add(new SegmentInfo(2, getResources().getString(R.string.section) + " " + number, data.getWeight().getWalkingDistance().getText(), data.getWeight().getTime().getText(),
                            0, 0, allTranport));
                }
                else if (data.getData().getWait() != null || data.getData().getTransfer() != null) {
                    mapManager.mapRoutesObjectCollection.remove(sectionPolyline);
                }
                else {
                    sectionPolyline.setStrokeColor(getResources().getColor(R.color.black));
                    sectionPolyline.setStrokeWidth(3.0f);
                    sectionPolyline.setZIndex(1.0f);
                }
                number++;
            }
            if (route.getGeometry() != null && !route.getGeometry().getPoints().isEmpty()) {
                mapManager.focusOnPolyline(route.getGeometry());
            }
        } else {
            mapManager.mapRoutesObjectCollection.clear();
        }
        segmentInfoAdapter.notifyDataSetChanged();
    }
    @Override
    public void onClickDrivingItem(int position) {
        segmentInfos.clear();
        DrivingRoute route = carRoutes.get(position);
        Polyline routeGeometry = route.getGeometry();
        mapManager.mapRoutesObjectCollection.clear();
        if (routeGeometry != null && !routeGeometry.getPoints().isEmpty()) {
            PolylineMapObject routePolyline = mapManager.mapRoutesObjectCollection.addPolyline(routeGeometry);
            routePolyline.setStrokeColor(getResources().getColor(R.color.dark_grey));
            routePolyline.setStrokeWidth(5.0f);
            routePolyline.setZIndex(100f);
            mapManager.focusOnPolyline(routeGeometry);
            segmentInfos.add(new SegmentInfo(3, getResources().getString(R.string.section) + " 1", route.getMetadata().getWeight().getDistance().getText(), route.getMetadata().getWeight().getTime().getText(),
                    0, 0, ""));
        }
        segmentInfoAdapter.notifyDataSetChanged();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (routeToShowOnReady != null) {
            routeOnMap(routeToShowOnReady);
        }
    }
    private void getAllRoutes() {
        mapManager.getRoute(placemarkMapObjectArrayList, busListener, walkingListener, carListener, bicycleListener);
        masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);
        recyclerView.setVisibility(View.GONE);
        listPointText.setVisibility(View.GONE);
        hintText.setVisibility(View.GONE);
        continueRouteText.setVisibility(View.GONE);
        deleteRouteText.setVisibility(View.GONE);
        focusRouteText.setVisibility(View.GONE);
        setCurrentButton(walkingTextView);
        routeBuilderLayout.setVisibility(View.VISIBLE);
    }
    private void closeAllRoutes() {
        if (placemarkMapObjectArrayList.isEmpty()) {
            hintText.setVisibility(View.VISIBLE);
        }
        listPointText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        continueRouteText.setVisibility(View.VISIBLE);
        deleteRouteText.setVisibility(View.VISIBLE);
        focusRouteText.setVisibility(View.VISIBLE);
        routeBuilderLayout.setVisibility(View.GONE);
        busRouteAdapter.setSelectPosition(0);
        walkingRouteAdapter.setSelectPosition(0);
        bicycleRouteAdapter.setSelectPosition(0);
        carRouteAdapter.setSelectPosition(0);
    }
    private void openMenuView() {
        ValueAnimator valueAnimator;
        ViewGroup.LayoutParams params = menuView.getLayoutParams();
        int screenWidth = getScreenWidth(requireContext());
        if (screenWidth == -1) {
            screenWidth = (int) DpToPx(400f, requireContext());
        }
        if (isCloseMenuView) {
            menuView.setVisibility(View.VISIBLE);
            routeText.setVisibility(View.VISIBLE);
            routeRecyclerView.setVisibility(View.VISIBLE);
            valueAnimator = ValueAnimator.ofInt( 1, Math.min((int) DpToPx(400f, requireContext()), screenWidth));
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                    params.width = (int) valueAnimator.getAnimatedValue();
                    menuView.setLayoutParams(params);
                }
            });
            valueAnimator.setDuration(500);
            valueAnimator.start();
            isCloseMenuView = false;
        }
    }
    private void closeMenuView() {
        ValueAnimator valueAnimator;
        ViewGroup.LayoutParams params = menuView.getLayoutParams();
        int screenWidth = getScreenWidth(requireContext());
        if (screenWidth == -1) {
            screenWidth = (int) DpToPx(400f, requireContext());
        }
        if (!isCloseMenuView) {
            valueAnimator = ValueAnimator.ofInt(Math.min((int) DpToPx(400f, requireContext()), screenWidth), 1);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                    params.width = (int) valueAnimator.getAnimatedValue();
                    menuView.setLayoutParams(params);
                }
            });
            valueAnimator.setDuration(500);
            valueAnimator.start();
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                    super.onAnimationEnd(animation, isReverse);
                    menuView.setVisibility(View.GONE);
                    routeText.setVisibility(View.GONE);
                    routeRecyclerView.setVisibility(View.GONE);
                }
            });
            isCloseMenuView = true;
        }
    }
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        } else {
            return -1;
        }
    }
    public static int PxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (px / displayMetrics.density);
    }
    private void openRouteView() {
        if (isClosePointsView) {
            ValueAnimator valueAnimator, valueAnimator1;
            ViewGroup.LayoutParams params = pointsView.getLayoutParams();
            pointsView.setVisibility(View.VISIBLE);
            if (placemarkMapObjectArrayList.isEmpty()) {
                hintText.setVisibility(View.VISIBLE);
            }
            listPointText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            continueRouteText.setVisibility(View.VISIBLE);
            deleteRouteText.setVisibility(View.VISIBLE);
            focusRouteText.setVisibility(View.VISIBLE);
            valueAnimator = ValueAnimator.ofInt( 1, (int) DpToPx(300f, requireContext()));
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                    params.height = (int) valueAnimator.getAnimatedValue();
                    pointsView.setLayoutParams(params);
                }
            });
            valueAnimator.setDuration(500);
            valueAnimator1 = ValueAnimator.ofFloat(180f, 360f);
            valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                    newRouteImageView.setRotation((Float) valueAnimator1.getAnimatedValue());
                }
            });
            valueAnimator1.setDuration(500);
            valueAnimator.start();
            valueAnimator1.start();
            isClosePointsView = false;
        }
    }
    private void closeRouteView() {
        if (!isClosePointsView) {
            ValueAnimator valueAnimator, valueAnimator1;
            ViewGroup.LayoutParams params = pointsView.getLayoutParams();
            valueAnimator = ValueAnimator.ofInt((int) DpToPx(300f, requireContext()), 1);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                    params.height = (int) valueAnimator.getAnimatedValue();
                    pointsView.setLayoutParams(params);
                }
            });
            valueAnimator.setDuration(500);
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                    super.onAnimationEnd(animation, isReverse);
                    pointsView.setVisibility(View.GONE);
                    hintText.setVisibility(View.GONE);
                    listPointText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    continueRouteText.setVisibility(View.GONE);
                    deleteRouteText.setVisibility(View.GONE);
                    focusRouteText.setVisibility(View.GONE);
                    routeBuilderLayout.setVisibility(View.GONE);
                }
            });
            valueAnimator1 = ValueAnimator.ofFloat(360f, 180f);
            valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                    newRouteImageView.setRotation((Float) valueAnimator1.getAnimatedValue());
                }
            });
            valueAnimator1.setDuration(500);
            valueAnimator.start();
            valueAnimator1.start();
            isClosePointsView = true;
        }
    }
    public void dataLoad() {
        routeArrayList.clear();
        routeAdapter.notifyDataSetChanged();
        ArrayList<String> routeId = user.getRouteList();
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
                                routeArrayList.add(route);
                                routeAdapter.notifyDataSetChanged();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // TODO доделать сообщение об исключении
                        }
                    });
                }
            }
        }
    }
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapManager.createUserLayer();
        } else {
            getLocationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        mapManager.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapManager.onStop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView = null;
        getLocationPermission = null;
        listView = null;
        searchView = null;
        mapManager = null;
    }

    @Override
    public void onSearchSuccess() {
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSearchError(String error) {
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSuggestResults(List<Map<String, String>> suggestions) {
        adapter.clear();
        if (!suggestions.isEmpty()) {
            adapter.addAll(suggestions);
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSuggestError(String error) {
        listView.setVisibility(View.GONE);
        adapter.clear();
        adapter.notifyDataSetChanged();
    }
    public static float DpToPx(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    @Override
    public void onMapLongTap(PlacemarkMapObject placemarkMapObject) {
        placemarkMapObject.setText(String.valueOf(placemarkMapObjectArrayList.size() + 1));
        placemarkMapObject.setUserData(new Waypoint(String.valueOf(placemarkMapObjectArrayList.size() + 1), ""));
        placemarkMapObjectArrayList.add(placemarkMapObject);
        mapManager.getRoute(placemarkMapObjectArrayList, busListener, walkingListener, carListener, bicycleListener);
        masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);
        hintText.setVisibility(View.GONE);
        waypointAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFocusToPolyline() {
        if (needToFocusOnPolyline) {
            mapManager.focusOnPolyline();
            needToFocusOnPolyline = false;
        }
    }

    @Override
    public void onDragWaypoint() {
//        mapManager.getRoute(placemarkMapObjectArrayList);
        mapManager.getRoute(placemarkMapObjectArrayList, busListener, walkingListener, carListener, bicycleListener);
        masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);
    }

    @Override
    public void arrowUpClickListener(int position) {
        if (position != 0) {
            swap(position - 1, position);
        }
    }

    @Override
    public void arrowDownClickListener(int position) {
        if (position != placemarkMapObjectArrayList.size() - 1) {
            swap(position, position + 1);
        }
    }

    @Override
    public void onNameChanged(String text, int position) {
        PlacemarkMapObject placemarkMapObject = placemarkMapObjectArrayList.get(position);
        placemarkMapObject.setText(text);
        Waypoint waypoint = (Waypoint) placemarkMapObject.getUserData();
        waypoint.setName(text);
        placemarkMapObject.setUserData(waypoint);
        placemarkMapObjectArrayList.set(position, placemarkMapObject);
    }

    @Override
    public void onDescriptionChanged(String text, int position) {
        PlacemarkMapObject placemarkMapObject = placemarkMapObjectArrayList.get(position);
        Waypoint waypoint = (Waypoint) placemarkMapObject.getUserData();
        waypoint.setDescription(text);
        placemarkMapObject.setUserData(waypoint);
        placemarkMapObjectArrayList.set(position, placemarkMapObject);
    }

    @Override
    public void onRouteNameChanged(String text, int position) {
        Route route = routeArrayList.get(position);
        route.setName(text);
        routeArrayList.set(position, route);
        FirebaseDatabase.getInstance().getReference().child("routes").child(route.getId()).child("name").setValue(text).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO доделать сообщение об исключении
            }
        });
    }

    @Override
    public void onRouteDescriptionChanged(String text, int position) {
        Route route = routeArrayList.get(position);
        route.setDescription(text);
        routeArrayList.set(position, route);
        FirebaseDatabase.getInstance().getReference().child("routes").child(route.getId()).child("description").setValue(text).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO доделать сообщение об исключении
            }
        });
    }

    @Override
    public void onDeleteRoute(int position) {
        Route route = routeArrayList.get(position);
        ArrayList<String> routeList = MainActivity.user.getRouteList();
        routeList.remove(route.getId());
        MainActivity.user.setRouteList(routeList);
        routeArrayList.remove(position);
        routeAdapter.notifyItemRemoved(position);
        FirebaseDatabase.getInstance().getReference().child("routes").child(route.getId()).removeValue().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO доделать сообщение об исключении
            }
        });
//        Log.d("debug", "onDeleteRoute: " + routeArrayList.size() + " " + routeAdapter.getItemCount() + " " + routeRecyclerView.getVisibility());
        MainActivity.user.changeData(FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()));
    }

    @Override
    public void onFocusToRoute(int position) {
        hintText.setVisibility(View.GONE);
        placemarkMapObjectArrayList.clear();
        mapManager.mapObjectCollection.clear();
        Route route = routeArrayList.get(position);
        ArrayList<Waypoint> waypointArrayList = route.getWaypointArrayList();
        for (Waypoint waypoint : waypointArrayList) {
            PlacemarkMapObject mark = mapManager.addPlacemarkMapObject(waypoint.getPoint());
            mark.setUserData(new Waypoint(waypoint.getName(), waypoint.getDescription()));
            mark.setText(waypoint.getName());
            placemarkMapObjectArrayList.add(mark);
        }
        needToFocusOnPolyline = true;
        mapManager.getRoute(placemarkMapObjectArrayList, busListener, walkingListener, carListener, bicycleListener);
        masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);
        mapManager.setUserInCreateMode(true);
        closeMenuView();
        newRouteImageView.setImageResource(R.drawable.arrow_up);
        openRouteView();
        waypointAdapter.notifyDataSetChanged();
    }
    public void routeOnMap(Route route) {
        hintText.setVisibility(View.GONE);
        placemarkMapObjectArrayList.clear();
        mapManager.mapObjectCollection.clear();
        ArrayList<Waypoint> waypointArrayList = route.getWaypointArrayList();
        for (Waypoint waypoint : waypointArrayList) {
            PlacemarkMapObject mark = mapManager.addPlacemarkMapObject(waypoint.getPoint());
            mark.setUserData(new Waypoint(waypoint.getName(), waypoint.getDescription()));
            mark.setText(waypoint.getName());
            placemarkMapObjectArrayList.add(mark);
        }
        needToFocusOnPolyline = true;
        mapManager.getRoute(placemarkMapObjectArrayList, busListener, walkingListener, carListener, bicycleListener);
        masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);
        mapManager.setUserInCreateMode(true);
        closeMenuView();
        newRouteImageView.setImageResource(R.drawable.arrow_up);
        openRouteView();
        waypointAdapter.notifyDataSetChanged();
    }
    @Override
    public void onDeleteWaypoint(int position) {
        PlacemarkMapObject placemarkMapObject = placemarkMapObjectArrayList.get(position);
        mapManager.mapObjectCollection.remove(placemarkMapObject);
        placemarkMapObjectArrayList.remove(position);
        if (placemarkMapObjectArrayList.isEmpty()) {
            hintText.setVisibility(View.VISIBLE);
            mapManager.mapRoutesObjectCollection.clear();
        }
        mapManager.getRoute(placemarkMapObjectArrayList, busListener, walkingListener, carListener, bicycleListener);
        masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);
        waypointAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFocusToWaypoint(int position) {
        Point point = placemarkMapObjectArrayList.get(position).getGeometry();
        mapManager.moveCamera(point, 17.0f);
    }

    void swap(int position1, int position2) {
        PlacemarkMapObject placemarkMapObject = placemarkMapObjectArrayList.get(position1);
        placemarkMapObjectArrayList.set(position1, placemarkMapObjectArrayList.get(position2));
        placemarkMapObjectArrayList.set(position2, placemarkMapObject);
        mapManager.getRoute(placemarkMapObjectArrayList, busListener, walkingListener, carListener, bicycleListener);
        masstransitRouteRecyclerView.setAdapter(walkingRouteAdapter);

        waypointAdapter.notifyDataSetChanged();
    }

}
