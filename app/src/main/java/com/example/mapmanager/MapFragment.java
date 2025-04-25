package com.example.mapmanager;

import static com.example.mapmanager.MainActivity.user;
import static com.google.common.primitives.Ints.max;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapmanager.adapters.RouteAdapter;
import com.example.mapmanager.adapters.RouteAdapter.RouteAdapterListener;
import com.example.mapmanager.adapters.WaypointsAdapter;
import com.example.mapmanager.models.MapManager;
import com.example.mapmanager.models.MapManager.MapManagerSearchListener;
import com.example.mapmanager.models.MapManager.MapManagerSuggestListener;
import com.example.mapmanager.models.MapManager.MapLongTapListener;
import com.example.mapmanager.adapters.WaypointsAdapter.WaypointsAdapterListener;
import com.example.mapmanager.models.Route;
import com.example.mapmanager.models.Waypoint;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.BusinessObjectMetadata;
import com.yandex.mapkit.search.ToponymObjectMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements MapManagerSearchListener, MapManagerSuggestListener, MapLongTapListener, WaypointsAdapterListener, RouteAdapterListener {
    private MapView mapView;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<String[]> getLocationPermission;
    private ListView listView;
    private TextView routeText, saveRouteText;
    private View pointsView, saveRouteView, menuView, myRoutesView;
    private ImageView userLocationImageView, newRouteImageView, menuImageView;
    private SearchView searchView;
    private ArrayAdapter<Map<String, String>> adapter;
    private WaypointsAdapter waypointAdapter;
    private RouteAdapter routeAdapter;
    private ArrayList<PlacemarkMapObject> placemarkMapObjectArrayList;
    private ArrayList<Route> routeArrayList;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MapManager mapManager;
    private RecyclerView recyclerView, routeRecyclerView;
    private Boolean isClosePointsView = true, isCloseMenuView = true;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLocationPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            Boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted!= null && coarseLocationGranted) {
                mapManager.createUserLayer();
            } else {
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        });
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapview);
        menuView = view.findViewById(R.id.menuView);
        routeText = view.findViewById(R.id.routeText);
        saveRouteText = view.findViewById(R.id.textView10);
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
        saveRouteView = view.findViewById(R.id.saveRoute);
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

        userLocationImageView.setOnClickListener(v -> {
            if (mapManager != null) {
                mapManager.moveCamera(mapManager.getCurrentUserLocation(), 17.0f);
            }
        });
        newRouteImageView.setOnClickListener(v -> {
            ValueAnimator valueAnimator, valueAnimator1;
            ViewGroup.LayoutParams params = pointsView.getLayoutParams();
            if (isClosePointsView) {
                pointsView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                saveRouteView.setVisibility(View.VISIBLE);
                saveRouteText.setVisibility(View.VISIBLE);
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
            } else {
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
                        saveRouteView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        saveRouteText.setVisibility(View.GONE);
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

        });
        saveRouteView.setOnClickListener(v -> {
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
        menuImageView.setOnClickListener(v ->{
            ValueAnimator valueAnimator;
            ViewGroup.LayoutParams params = menuView.getLayoutParams();
            if (isCloseMenuView) {
                menuView.setVisibility(View.VISIBLE);
                routeText.setVisibility(View.VISIBLE);
                routeRecyclerView.setVisibility(View.VISIBLE);
                valueAnimator = ValueAnimator.ofInt( 1, (int) DpToPx(400f, requireContext()));
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
            } else {
                valueAnimator = ValueAnimator.ofInt((int) DpToPx(400f, requireContext()), 1);
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
        });

        return view;
    }

    public void dataLoad() {
        routeArrayList.clear();
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
                            }
                        }
                    });
                }
            }
        }
        routeAdapter.notifyDataSetChanged();
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
        placemarkMapObjectArrayList.add(placemarkMapObject);
        mapManager.getRoute(placemarkMapObjectArrayList);
        waypointAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnDragWaypoint() {
        mapManager.getRoute(placemarkMapObjectArrayList);
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
        FirebaseDatabase.getInstance().getReference().child("routes").child(route.getId()).child("name").setValue(text);
    }

    @Override
    public void onRouteDescriptionChanged(String text, int position) {
        Route route = routeArrayList.get(position);
        route.setDescription(text);
        routeArrayList.set(position, route);
        FirebaseDatabase.getInstance().getReference().child("routes").child(route.getId()).child("description").setValue(text);
    }

    @Override
    public void onDeleteRoute(int position) {
        Route route = routeArrayList.get(position);
        ArrayList<String> routeList = MainActivity.user.getRouteList();
        routeList.remove(route.getId());
        MainActivity.user.setRouteList(routeList);
        routeArrayList.remove(position);
        routeAdapter.notifyDataSetChanged();
        MainActivity.user.changeData(FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()));
    }

    @Override
    public void onFocusToRoute(int position) {
        placemarkMapObjectArrayList.clear();
        mapManager.mapObjectCollection.clear();
        Route route = routeArrayList.get(position);
        ArrayList<Waypoint> waypointArrayList = route.getWaypointArrayList();
        for (Waypoint waypoint : waypointArrayList) {
            PlacemarkMapObject mark = mapManager.addPlacemarkMapObject(waypoint.getPoint());
            mark.setUserData(new Waypoint(waypoint.getName(), waypoint.getDescription()));
            placemarkMapObjectArrayList.add(mark);
        }
        mapManager.getRoute(placemarkMapObjectArrayList);
        waypointAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteWaypoint(int position) {
        PlacemarkMapObject placemarkMapObject = placemarkMapObjectArrayList.get(position);
        mapManager.mapObjectCollection.remove(placemarkMapObject);
        placemarkMapObjectArrayList.remove(position);
        mapManager.getRoute(placemarkMapObjectArrayList);
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
        mapManager.getRoute(placemarkMapObjectArrayList);
        waypointAdapter.notifyDataSetChanged();
    }
}
